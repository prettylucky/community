package com.better.community.service;

import com.better.community.entity.User;
import com.better.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 关注相关业务
 * @Date 2022/7/17
 */
@Service
public class FollowService {
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private UserService userService;

    //关注或取关
    //关注：把实体id存入当前用户的followee，且把当前用户的id存入对应实体的follower中
    //取关：逻辑与关注相反
    //因为涉及多次DML操作，故使用编程式事务
    public void follow(int userId, int entityType, int entityId) {
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String userFolloweeKey = RedisKeyUtil.getUserFolloweeKey(userId, entityType);
                String entityFollowerKey = RedisKeyUtil.getEntityFollowerKey(entityType, entityId);

                //判断是否已经关注了该用户，如果没关注则关注，如果关注了则取关
                boolean isFollowed = findFollowStatus(userId, entityType, entityId);

                //开启事务
                operations.multi();

                //没有关注 则关注
                if (!isFollowed) {
                    operations.opsForZSet().add(userFolloweeKey, entityId, System.currentTimeMillis());
                    operations.opsForZSet().add(entityFollowerKey, userId, System.currentTimeMillis());
                } else {
                //已经关注了，取关
                    operations.opsForZSet().remove(userFolloweeKey, entityId);
                    operations.opsForZSet().remove(entityFollowerKey, userId);
                }

                //提交事务
                return operations.exec();
            }
        });
    }

    //查询当前用户是否关注了某个实体/关注状态
    public boolean findFollowStatus(int userId, int entityType, int entityId) {
        String userFolloweeKey = RedisKeyUtil.getUserFolloweeKey(userId, entityType);
        //zset中没有提供判断某个成员是否存在方法
        //此时直接查询对应key的排名，如果不存在排名则不存在该key
        return redisTemplate.opsForZSet().rank(userFolloweeKey, entityId) != null;
    }

    //查询某个实体的粉丝follower有多少
    public long findEntityFollowerCount(int entityType, int entityId) {
        String entityFollowerKey = RedisKeyUtil.getEntityFollowerKey(entityType, entityId);
        return redisTemplate.opsForZSet().zCard(entityFollowerKey);
    }

    //查询某个用户关注了多少某种类型的实体，用户的 entityType是3
    public long findUserFolloweeCount(int userId, int entityType) {
        String userFolloweeKey = RedisKeyUtil.getUserFolloweeKey(userId, entityType);
        return redisTemplate.opsForZSet().zCard(userFolloweeKey);
    }

    //查询某个用户关注的所有实体
    public List<Map<String, Object>> findUserFollowees(int userId,int loginUserId, int entityType, int offset, int limit) {
        String userFolloweeKey = RedisKeyUtil.getUserFolloweeKey(userId, entityType);
        //封装到List中
        List<Map<String, Object>> list = new ArrayList<>();
        Set<Integer> followeeIds = redisTemplate.opsForZSet().range(userFolloweeKey, offset, offset + limit - 1);
        if (followeeIds == null) {
            return null;
        }
        for (int followeeId : followeeIds) {
            HashMap<String, Object> map = new HashMap<>();
            User user = userService.findUserById(followeeId);
            map.put("user", user);
            Double score = redisTemplate.opsForZSet().score(userFolloweeKey, followeeId);
            map.put("followTime", new Date(score.longValue()));
            //查询当前登录对该用户的关注状态
            boolean followStatus = findFollowStatus(loginUserId, entityType, followeeId);
            map.put("followStatus", followStatus);
            list.add(map);
        }

        return list;
    }

    //查询某个实体的关注者
    public List<Map<String, Object>> findEntityFollowers(int userId ,int entityType, int entityId, int offset, int limit) {
        String entityFollowerKey = RedisKeyUtil.getEntityFollowerKey(entityType, entityId);
        //封装到List中
        List<Map<String, Object>> list = new ArrayList<>();
        Set<Integer> followerIds = redisTemplate.opsForZSet().range(entityFollowerKey, offset, offset + limit - 1);
        if (followerIds == null) {
            return null;
        }
        for (int followerId : followerIds) {
            HashMap<String, Object> map = new HashMap<>();
            User user = userService.findUserById(followerId);
            map.put("user", user);
            Double score = redisTemplate.opsForZSet().score(entityFollowerKey, followerId);
            map.put("followTime", new Date(score.longValue()));
            //查询当前登录对该用户的关注状态
            boolean followStatus = findFollowStatus(userId, entityType, followerId);
            map.put("followStatus", followStatus);
            list.add(map);
        }

        return list;
    }
}
