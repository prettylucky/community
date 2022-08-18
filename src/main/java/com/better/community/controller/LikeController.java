package com.better.community.controller;

import com.better.community.annotation.LoginRequired;
import com.better.community.entity.Event;
import com.better.community.event.EventProducer;
import com.better.community.service.CommentService;
import com.better.community.service.DiscussPostService;
import com.better.community.service.LikeService;
import com.better.community.util.CommunityConstant;
import com.better.community.util.CommunityUtil;
import com.better.community.util.HostHolder;
import com.better.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.HashMap;

/**
 *
 * @Date 2022/7/16
 */
@Controller
public class LikeController implements CommunityConstant {

    @Autowired
    private LikeService likeService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private CommentService commentService;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private RedisTemplate redisTemplate;

    //点赞是一个异步请求，实现在页面不刷新的情况下完成点赞
    //将结果以JSON格式返回
    @PostMapping("/like")
    @ResponseBody
    @LoginRequired
    public String like(int entityType, int entityId, int entityUserId, int postId) {
        //用于封装返回的结果
        HashMap<String, Object> map = new HashMap<>();
        //点赞/取消点赞
        likeService.like(hostHolder.getUser().getId(), entityType, entityId, entityUserId);
        //将点赞/取消点赞之后：赞的数量、赞的状态封装到map中
        map.put("likeCount", likeService.findEntityLikeCount(entityType, entityId));
        map.put("likeStatus", likeService.findLikeStatus(hostHolder.getUser().getId(), entityType, entityId));

        //如果是点赞，则触发事件 （取消赞就算了，恶心人）
        if ((int) map.get("likeStatus") == 1) {
            //封装event对象
            Event event = new Event();
            event.setTopic(TOPIC_LIKE)
                    .setUserId(hostHolder.getUser().getId())
                    .setEntityType(entityType)
                    .setEntityId(entityId)
                    .setEntityUserId(entityUserId)
                    .setData("postId", postId);     //这里重构这个方法，直接让页面传过来这个postId
//            //查询当前实体（评论或者回复）所在帖子链接，方便给用户附加一个链接
//            int discussPostId = 0;
//            if (entityType == ENTITY_TYPE_POST) {
//                discussPostId = entityId;
//            } else {
//                discussPostId = commentService.findCommentById(entityId).getEntityId();
//            }
//            event.setData("postId", discussPostId);
            eventProducer.fireEvent(event);

        }

        //如果给帖子点赞则重新计算分数
        if (entityType == ENTITY_TYPE_POST) {
            String redisKey = RedisKeyUtil.getPostScoreKey();
            redisTemplate.opsForSet().add(redisKey, postId);
        }

        return CommunityUtil.getJSONString(0, "点赞成功！", map);
    }
}
