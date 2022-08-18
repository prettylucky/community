package com.better.community.controller;

import com.better.community.annotation.LoginRequired;
import com.better.community.entity.Comment;
import com.better.community.entity.DiscussPost;
import com.better.community.entity.Event;
import com.better.community.entity.User;
import com.better.community.event.EventProducer;
import com.better.community.service.CommentService;
import com.better.community.service.DiscussPostService;
import com.better.community.util.CommunityConstant;
import com.better.community.util.HostHolder;
import com.better.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Date;

/**
 * 处理评论相关请求
 * @Date 2022/7/13
 */
@Controller
@RequestMapping("/comment")
public class CommentController implements CommunityConstant {

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private CommentService commentService;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private EventProducer eventProducer;    //消息生产者

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 处理增加评论请求
     * @param comment 前端传入帖子部分参数（content entityType entityId）
     * @return 重定向到详情页面
     */
    @PostMapping("/add/{discussPostId}")
    @LoginRequired
    public String addComment(@PathVariable("discussPostId") String discussPostId, Comment comment) {
        //获取当前用户
        User user = hostHolder.getUser();

        //补全comment剩余参数之后，再传给service
        comment.setCreateTime(new Date());
        comment.setStatus(0);
        comment.setUserId(user.getId());

        commentService.addComment(comment);

        //触发评论事件 构建event对象，然后调用生产者把消息抛给消息队列
        //这里评论的对象可能是帖子，也可能对评论评论（回复）
        Event event = new Event();
        event.setTopic(TOPIC_COMMENT)
                .setUserId(user.getId())
                .setEntityType(comment.getEntityType())
                .setEntityId(comment.getEntityId())
                .setData("postId", discussPostId);      //系统给用户发送消息的时候，可能需要把评论的链接附上
        //根据评论对象是帖子还是其他评论，需要查不同的表来获取EntityUserId
        if (comment.getEntityType() == ENTITY_TYPE_POST) {      //如果是帖子，则查discuss_post表
            DiscussPost discusspost = discussPostService.findDiscussPostById(comment.getEntityId());
            event.setEntityUserId(discusspost.getUserId());
        } else if (comment.getEntityType() == ENTITY_TYPE_COMMENT) {    //如果是其他评论，则查comment表
//            Comment targetComment = commentService.findCommentById(comment.getEntityId());
//            if (targetComment == null) {
//
//            }
            if (comment.getTargetId() != 0)     //表示回复的某个人的评论
                event.setEntityUserId(comment.getTargetId());
            else {      //表示回复的某个评论
                int userId = commentService.findCommentById(comment.getEntityId()).getUserId();
                event.setEntityUserId(userId);
            }
        }
        eventProducer.fireEvent(event);     //触发事件

        // 如果是给帖子评论，那会更新discusspost表中的commentCount字段，所以需要更新es中帖子数据
        // 此处触发发帖事件，丢给消息队列去处理
        if (comment.getEntityType() == ENTITY_TYPE_POST) {
            event = new Event()
                    .setTopic(TOPIC_PUBLISH)
                    .setUserId(user.getId())
                    .setEntityId(comment.getEntityId())
                    .setEntityType(comment.getEntityType());

            eventProducer.fireEvent(event);


            // 如果给帖子评论，则计算帖子分数
            String redisKey = RedisKeyUtil.getPostScoreKey();
            redisTemplate.opsForSet().add(redisKey, Integer.parseInt(discussPostId));
        }


        return "redirect:/discuss/detail/" + discussPostId;
    }
}
