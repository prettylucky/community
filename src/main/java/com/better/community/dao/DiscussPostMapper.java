package com.better.community.dao;

import com.better.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 讨论贴相关DAO操作
 * @Date 2022/5/4
 */
@Mapper
public interface DiscussPostMapper {
    /**
     * 分页查询
     * @param userId 方便以后查询某指定用户的帖子，传入0为全部用户的帖子
     * @param offset 分页查询所需的起始行
     * @param limit 分页查询的条数
     * @param orderMode 0:按照时间排序 1:按照score排序
     * @return 指定查询的讨论贴集合
     */
    List<DiscussPost> selectPosts(@Param("userId") int userId,@Param("offset") int offset,@Param("limit") int limit, int orderMode);

    /**
     * 查询所有帖子的条数
     * @return 所有帖子条数
     */
    int selectRows(@Param("userId") int userId);

    int insertPost(DiscussPost post);

    DiscussPost selectDiscussPostById(int id);

    /**
     * 更新帖子的评论数量
     */
    int updateCommentCount(int id, int commentCount);

    int updateType(int id, int type);

    int updateStatus(int id, int status);

    int updateScore(int id, double score);
}
