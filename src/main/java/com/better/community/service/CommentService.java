package com.better.community.service;

import com.better.community.dao.CommentMapper;
import com.better.community.dao.DiscussPostMapper;
import com.better.community.entity.Comment;
import com.better.community.entity.DiscussPost;
import com.better.community.util.CommunityConstant;
import com.better.community.util.SensitiveWordsFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

/**
 * @param
 * @author Bubble
 * @description
 * @return
 * @Date 2022/7/13
 */
@Service
public class CommentService implements CommunityConstant {
    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private SensitiveWordsFilter sensitiveWordsFilter;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    //根据id查询comment
    public Comment findCommentById(int id) {
        return commentMapper.selectCommentById(id);
    }

    //根据entity查询出所有评论，需要做分页查询
    public List<Comment> findCommentByEntity(int entityType,int entityId, int offset, int limit) {
        return commentMapper.selectCommentByEntity(entityType, entityId, offset, limit);
    }

    //根据评论id查询该评论总共有多少条回复
    public int findReplyCountByEntityId(int entityId) {
        return commentMapper.selectReplyCountByEntityId(entityId);
    }

    //添加帖子业务
    //此业务不仅需要插入comment表中一条数据，而且还要更新discuss_post表中评论的数量 comment_count
    //故一定要声明事务，此处采用声明式事务
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public int addComment(Comment comment) {
        if (comment == null) {
            throw new IllegalArgumentException("参数不能为空！");
        }

        // 1.添加帖子
        // 预处理：转义HTML字符+敏感词过滤
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        comment.setContent(sensitiveWordsFilter.filter(comment.getContent()));
        // 插入表中
        int rows = commentMapper.insertComment(comment);

        // 2.当entityType = ENTITY_TYPE_POST时 更新discuss_post表中的评论数量
        if (comment.getEntityType() == ENTITY_TYPE_POST) {
            // 获取原来评论数量
            DiscussPost discussPost = discussPostMapper.selectDiscussPostById(comment.getEntityId());
            int commentCount = discussPost.getCommentCount();
            // 更新数据
            discussPostMapper.updateCommentCount(comment.getEntityId(), commentCount + 1);
        }


        return rows;
    }
}
