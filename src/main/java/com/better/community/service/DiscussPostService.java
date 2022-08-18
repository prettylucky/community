package com.better.community.service;

import com.better.community.dao.DiscussPostMapper;
import com.better.community.entity.DiscussPost;
import com.better.community.entity.User;
import com.better.community.util.CommunityUtil;
import com.better.community.util.SensitiveWordsFilter;
import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @param
 * @author Bubble
 * @description
 * @return
 * @Date 2022/5/4
 */
@Service
public class DiscussPostService {
    public static final Logger logger = LoggerFactory.getLogger(DiscussPostService.class);

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private SensitiveWordsFilter sensitiveWordsFilter;

    @Value("${caffeine.posts.max-size}")
    private int maxSize;

    @Value("${caffeine.posts.expire-seconds}")
    private int expireSeconds;

    // Caffeine核心接口： Cache, (LoadingCache, AsyncLoadingCache)
    // 帖子列表的缓存
    private LoadingCache<String, List<DiscussPost>> postListCache;
    // 帖子总数的缓存
    private LoadingCache<Integer, Integer> postRowsCache;

    @PostConstruct
    public void init() {
        // 初始化帖子列表缓存
        postListCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .build(new CacheLoader<String, List<DiscussPost>>() {
                    @Override
                    public @Nullable List<DiscussPost> load(@NonNull String key) throws Exception {
                        // 定义缓存的来源的逻辑（基本就是查数据库）

                        // key就是查缓存的时候传入的key，这里规定为 [offset]:[limit]

                        if (key == null || key.length() == 0) {
                            throw new IllegalArgumentException("参数错误！");
                        }

                        String[] params = key.split(":");
                        if (params == null || params.length != 2) {
                            throw new IllegalArgumentException("参数错误！");
                        }

                        int offset = Integer.parseInt(params[0]);
                        int limit = Integer.parseInt(params[1]);

                        // 在访问数据库之前，这里可以加个二级缓存，如：redis
                        logger.debug("load post list from DB.");
                        return discussPostMapper.selectPosts(0, offset, limit, 1);
                    }
                });
        // 初始化帖子总数缓存

        postRowsCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .build(new CacheLoader<Integer, Integer>() {
                    @Override
                    public @Nullable Integer load(@NonNull Integer key) throws Exception {
                        logger.debug("load post rows from DB.");
                        return discussPostMapper.selectRows(0);
                    }
                });
    }

    public List<DiscussPost> findPosts(int userId, int offset, int limit, int orderMode){
        //只有查热门贴子的时候才启用缓存
        if (userId == 0 && orderMode == 1) {
            return postListCache.get(offset + ":" + limit);
        }
        logger.debug("load post list from DB.");

        return discussPostMapper.selectPosts(userId, offset, limit, orderMode);
    }

    public int findRows(int userId){
        //在首页查询的时候查缓存，查个人帖子总数不用缓存了
        if (userId == 0) {
            return postRowsCache.get(userId);
        }
        logger.debug("load post rows from DB.");
        return discussPostMapper.selectRows(userId);
    }

    public int addPost(DiscussPost post) {
        //空值处理
        if (post == null) {
            throw new IllegalArgumentException("参数不能为空！");
        }

        //敏感词过滤
        post.setTitle(sensitiveWordsFilter.filter(post.getTitle()));
        post.setContent(sensitiveWordsFilter.filter(post.getContent()));

        //转义HTML标签，防止用户恶意提交有危害性的 js、html 代码
        post.setTitle(HtmlUtils.htmlEscape(post.getTitle()));
        post.setContent(HtmlUtils.htmlEscape(post.getContent()));

        return discussPostMapper.insertPost(post);
    }

    public DiscussPost findDiscussPostById(int discussPostId) {
        return discussPostMapper.selectDiscussPostById(discussPostId);
    }

    public int updateType(int id, int type) {
        return discussPostMapper.updateType(id, type);
    }

    public int updateStatus(int id, int status) {
        return discussPostMapper.updateStatus(id, status);
    }

    public int updateScore(int id, double socre) {
        return discussPostMapper.updateScore(id, socre);
    }

}
