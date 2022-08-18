package com.better.community.controller;

import com.better.community.annotation.LoginRequired;
import com.better.community.dao.DiscussPostMapper;
import com.better.community.entity.*;
import com.better.community.event.EventProducer;
import com.better.community.service.CommentService;
import com.better.community.service.DiscussPostService;
import com.better.community.service.LikeService;
import com.better.community.service.UserService;
import com.better.community.util.CommunityConstant;
import com.better.community.util.CommunityUtil;
import com.better.community.util.HostHolder;
import com.better.community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 处理帖子相关的请求（发布、删除）
 * @Date 2022/7/12
 */
@Controller
@RequestMapping("/discuss")
public class DiscussPostController implements CommunityConstant {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private CommentService commentService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 处理发布帖子请求
     * @param title 帖子标题
     * @param content 帖子内容
     * @return 发布结果，以JSON格式返回 code msg
     */
    @ResponseBody   //返回JSON格式字符串
    @LoginRequired  //需要登录才能发布请求
    @PostMapping("/add")
    public String addDiscussPost(String title, String content) {
        if (StringUtils.isBlank(title) || StringUtils.isBlank(content)) {
            return CommunityUtil.getJSONString(400, "参数不能为空！");
        }

        //获取用户
        User user = hostHolder.getUser();

        DiscussPost post = new DiscussPost();
        post.setTitle(title);
        post.setContent(content);
        post.setCreateTime(new Date());
        post.setUserId(user.getId());

        discussPostService.addPost(post);

        // 触发发帖事件，（消费者会把帖子也存在es中一份）
        // 为什么使用消息队列，不直接存到es中，因为访问频繁且存到es中速度较慢，使用消息队列提高并发能力(扔给消息队列之后，直接执行后续逻辑了)
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(user.getId())
                .setEntityId(ENTITY_TYPE_POST)
                .setEntityId(post.getId());
        eventProducer.fireEvent(event);

        // 计算帖子分数
        String redisKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(redisKey, post.getId());

        // 报错的异常以后统一处理
        return CommunityUtil.getJSONString(0, "发布成功！");
    }


    @GetMapping("/detail/{discussPostId}")
    public String getDiscussDetail(@PathVariable("discussPostId") int discussPostId, Model model, Page page) {

        // 1.查询discussPostId对应的帖子
        DiscussPost discussPost = discussPostService.findDiscussPostById(discussPostId);
        // 2.查询发帖人信息
        User user = userService.findUserById(discussPost.getUserId());

        // 3.封装评论VO列表（需要做分页查询）
        // 分页信息
        page.setLimit(5);   //一页显示多少条数据
        page.setRows(discussPost.getCommentCount());    //总条数
        page.setPath("/discuss/detail/" + discussPostId);   //路径

        // （一条评论对应一个map，map中包括：评论相关信息、评论人相关信息、评论的回复列表、评论回复数量、回复的目标）
        List<Map<String, Object>> commentVOList = new ArrayList<>();
        // 查询所有评论信息，以评论信息为准，查询其附加信息
        List<Comment> commentList = commentService.findCommentByEntity(
                ENTITY_TYPE_POST, discussPost.getId(), page.getOffset(), page.getLimit());
        if (commentList != null) {
            for(Comment comment : commentList) {
                //用户封装一条commentVO信息
                Map<String, Object> commentVO = new HashMap<>();
                // a.把评论基本信息封装到commentVO中
                commentVO.put("comment", comment);
                // b.把评论的作者封装到commentVO中
                commentVO.put("user", userService.findUserById(comment.getUserId()));
                // c.把给评论点赞的数量和点赞的状态封装到commentVO中
                commentVO.put("likeCount", likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, comment.getId()));
                if (hostHolder.getUser() != null)
                    commentVO.put("likeStatus", likeService.findLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, comment.getId()));
                else
                    commentVO.put("likeStatus", 0);
                // d.把评论的回复数量封装到commentVO中
                commentVO.put("replyCount", commentService.findReplyCountByEntityId(comment.getId()));
                // e.把评论对应的回复封装到commentVO中
                List<Map<String, Object>> replyVOList = new ArrayList<>();
                List<Comment> replies = commentService.findCommentByEntity(
                        ENTITY_TYPE_COMMENT, comment.getId(), 0, Integer.MAX_VALUE);
                // 一个回复中要包含评论基本信息、作者 和 回复目标
                if (replies != null) {
                    for (Comment replay : replies) {
                        // 封装一条replyVO信息
                        Map<String, Object> replayVO = new HashMap<>();
                        //  回复基本信息
                        replayVO.put("reply", replay);
                        //  回复作者
                        replayVO.put("user", userService.findUserById(replay.getUserId()));
                        //  回复目标
                        replayVO.put("target", userService.findUserById(replay.getTargetId()));
                        // 回复赞数 和 赞的状态
                        replayVO.put("likeCount", likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, replay.getId()));
                        if (hostHolder.getUser() != null)
                            replayVO.put("likeStatus", likeService.findLikeStatus(hostHolder.getUser().getId(),ENTITY_TYPE_COMMENT, replay.getId()));
                        else
                            replayVO.put("likeStatus", 0);
                        // 把一条replayVO信息添加到replyVOList中
                        replyVOList.add(replayVO);
                    }
                }
                commentVO.put("replies", replyVOList);

                //把一条commentVO信息添加到commentVOList中
                commentVOList.add(commentVO);
            }
        }


        // 4.封装数据到model
        // post：帖子信息
        // user：发帖人
        // likeCount：点赞数量
        // likeStatus：点赞状态
        // comments：评论列表（一条评论中包括：评论相关信息、评论人相关信息、评论的回复列表、评论回复数量、回复的目标）
        // page：分页相关信息，当传入的是一个实体类，在执行完controller之后，springmvc会自动把该实体类封装进model中
        model.addAttribute("post", discussPost);
        model.addAttribute("user", user);
        model.addAttribute("comments", commentVOList);
        model.addAttribute("likeCount", likeService.findEntityLikeCount(ENTITY_TYPE_POST, discussPostId));
        if (hostHolder.getUser() != null)
            model.addAttribute("likeStatus", likeService.findLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_POST, discussPostId));
        else
            model.addAttribute("likeStatus", 0);
        return "/site/discuss-detail";
    }

    //置顶
    //异步请求
    @PostMapping("/top")
    @ResponseBody
    public String setTop(int id) {
        discussPostService.updateType(id, 1);
        //同步更新到es中

        // 触发发帖事件，（消费者会把帖子也存在es中一份）
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(hostHolder.getUser().getId())
                .setEntityId(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0);
    }

    //加精
    //异步请求
    @PostMapping("/wonderful")
    @ResponseBody
    public String setWonderful(int id) {
        discussPostService.updateStatus(id, 1);
        //同步更新到es中

        // 触发发帖事件，（消费者会把帖子也存在es中一份）
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(hostHolder.getUser().getId())
                .setEntityId(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);

        // 计算帖子分数
        String redisKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(redisKey, id);

        return CommunityUtil.getJSONString(0);
    }

    //删除
    //异步请求
    @PostMapping("/delete")
    @ResponseBody
    public String setDelete(int id) {
        discussPostService.updateStatus(id, 2);
        //同步更新到es中

        // 触发删帖事件，（消费者会把帖子也存在es中一份）
        Event event = new Event()
                .setTopic(TOPIC_DELETE)
                .setUserId(hostHolder.getUser().getId())
                .setEntityId(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);

        //return "{code:0}";
        return CommunityUtil.getJSONString(0);
    }
}
