package com.better.community.service;

import com.better.community.util.CommunityUtil;
import com.better.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

/**
 * 与各种实体（帖子、评论、回复）的赞有关的业务
 * @Date 2022/7/16
 */
@Service
public class LikeService {

    @Autowired
    private RedisTemplate redisTemplate;

    //点赞业务，如果已经点过了赞，则取消
    //赞 以set的形式存储，key为 like:entity:entityType:entityId value为 userId的set
    //便于查询谁点了赞（如果只是用一个整数来存储，那么就不能查出来是谁点的赞）
    //---------------------------
    //功能重构：在点赞的同时维护对应用户的总赞数
    //一个业务涉及多个DML操作，使用编程式事务
    public void like(int userId, int entityType, int entityId, int entityUserId) {
        //功能重构
        //生成key
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        String userLikeKey = RedisKeyUtil.getUserLikeKey(entityUserId);

        //编程式事务
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                //判断登录用户是否对该实体点过赞了(查询操作在事务之外)
                Boolean isMember = operations.opsForSet().isMember(entityLikeKey, userId);

                //开启事务
                operations.multi();
                if (isMember) {
                    //取消点赞，实体赞把登录用户去除，实体对应作者赞减少
                    operations.opsForSet().remove(entityLikeKey, userId);
                    operations.opsForValue().decrement(userLikeKey);
                } else {
                    //点赞，与取消点赞逻辑相反
                    operations.opsForSet().add(entityLikeKey, userId);
                    operations.opsForValue().increment(userLikeKey);
                }
                //提交事务
                return operations.exec();
            }
        });



//        //生成key
//        String redisKey = RedisKeyUtil.getEntityLikeKey(entityType,entityId);
//        //判断该用户是否已经点过了赞
//        boolean isMember = redisTemplate.opsForSet().isMember(redisKey, userId);
//        //如果点过赞了，则取消赞
//        if (isMember) {
//            redisTemplate.opsForSet().remove(redisKey, userId);
//        } else {
//        //如果没有点过赞，则点赞
//            redisTemplate.opsForSet().add(redisKey, userId);
//        }

    }

    //查询某个实体点赞数量
    public long findEntityLikeCount(int entityType, int entityId) {
        //生成key
        String redisKey = RedisKeyUtil.getEntityLikeKey(entityType,entityId);
        return redisTemplate.opsForSet().size(redisKey);
    }

    //查询某人对某个实体的点赞状态
    //为什么返回int而不是boolean
    //因为int方便扩展，如果后面想加一个点踩的功能，还能返回第三种状态，而Boolean不方便扩展。
    public int findLikeStatus(int userId, int entityType, int entityId) {
        //生成key
        String redisKey = RedisKeyUtil.getEntityLikeKey(entityType,entityId);
        return redisTemplate.opsForSet().isMember(redisKey, userId) ? 1 : 0;
    }

    //查询某个用户获得赞的数量
    public int findUserLikeCount(int userId) {
        String userLikeKey = RedisKeyUtil.getUserLikeKey(userId);
        Object count = redisTemplate.opsForValue().get(userLikeKey);
        return count == null ? 0 : (int) count;
    }
}
