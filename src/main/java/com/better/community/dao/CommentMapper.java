package com.better.community.dao;

import com.better.community.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @param
 * @author Bubble
 * @description
 * @return
 * @Date 2022/7/13
 */
@Mapper
public interface CommentMapper {
    //根据entity查询出所有评论，需要做分页查询
    List<Comment> selectCommentByEntity(int entityType,int entityId, int offset, int limit);

    //根据评论id查询该评论总共有多少条回复
    int selectReplyCountByEntityId(int entityId);

    //插入一条评论
    int insertComment(Comment comment);

    //根据id查询出一条comment
    Comment selectCommentById(int id);
}
