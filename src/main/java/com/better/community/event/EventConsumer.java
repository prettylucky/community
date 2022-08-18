package com.better.community.event;

import com.alibaba.fastjson.JSONObject;
import com.better.community.dao.MessageMapper;
import com.better.community.dao.elasticsearch.DiscussPostRepository;
import com.better.community.entity.DiscussPost;
import com.better.community.entity.Event;
import com.better.community.entity.Message;
import com.better.community.service.DiscussPostService;
import com.better.community.service.ElasticsearchService;
import com.better.community.util.CommunityConstant;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 事件消费者：使用@KafkaListener()注解监听阻塞队列中的消息
 * 使用ConsumerRecorde获取消息，然后存入数据库（message表中）
 *
 * @Date 7/19/2022
 */
@Component
public class EventConsumer implements CommunityConstant {
    //日志组件
    private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);

    @Autowired
    private MessageMapper messageMapper;
    @Autowired
    private DiscussPostRepository discussRepository;
    @Autowired
    private DiscussPostService discussService;
    @Autowired
    private ElasticsearchService elasticsearchService;
    @Value("${wk.image.storage}")
    private String wkImageStorage;
    @Value("${wk.image.command}")
    private String wkImageCommand;

    //对于三种事件，对应存储的message的模板是相同的，此处直接使用一个方法监听这三个事件
    @KafkaListener(topics = {TOPIC_COMMENT, TOPIC_FOLLOW, TOPIC_LIKE})
    public void handleCommentMessage(ConsumerRecord record) {

        // 1.从消息队列中获取消息，同时把JSON格式的消息重新解析为Event对象
        if (record == null || record.value() == null) {     //如果没有获取到消息，记录日志，直接返回
            logger.error("消息的内容为空！");
            return;
        }
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);

        if (event == null) {    //如果没有解析出结果，表示JSON格式不正确
            logger.error("消息格式错误！");
            return;
        }

        // 2.把event中的数据，存放到message实体中，然后存入数据库
        Message message = new Message();
        message.setFromId(SYSTEM_ID);
        message.setToId(event.getEntityUserId());
        message.setCreateTime(new Date());
        message.setConversationId(event.getTopic());
        //使用map封装content信息
        Map<String, Object> content = new HashMap<>();
        content.put("userId", event.getUserId());
        content.put("entityType", event.getEntityType());
        content.put("entityId", event.getEntityId());
        if (!event.getData().isEmpty()) {   //如果data中携带附加信息，则打散添加进来
            for (Map.Entry<String, Object> entry : event.getData().entrySet()) {
                content.put(entry.getKey(), entry.getValue());
            }
        }
        message.setContent(JSONObject.toJSONString(content));

        //把message存入数据库
        messageMapper.insertMessage(message);


    }

    //处理发帖事件，把对应帖子存到es中
    @KafkaListener(topics = {TOPIC_PUBLISH})
    public void handlePublicMessage(ConsumerRecord record) {
        // 判空，出错记日志
        if (record == null || record.value() ==null) {
            logger.error("消息内容为空！");
            return;
        }
        //1. 获取消息内容（JSON），并解析回event对象
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            logger.error("消息格式不正确！");
            return;
        }

        //2.根据event查询数据库查出对应的帖子，然后添加到es中一份
        DiscussPost post = discussService.findDiscussPostById(event.getEntityId());
        if (post != null) {
            elasticsearchService.saveDiscussPost(post);
        }

    }

    //消费删帖事件
    @KafkaListener(topics = {TOPIC_DELETE})
    public void handleDeleteMessage(ConsumerRecord record) {
        // 判空，出错记日志
        if (record == null || record.value() ==null) {
            logger.error("消息内容为空！");
            return;
        }
        //1. 获取消息内容（JSON），并解析回event对象
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            logger.error("消息格式不正确！");
            return;
        }

        elasticsearchService.deleteDiscussPost(event.getEntityId());
    }

    //消费分享事件
    @KafkaListener(topics = {TOPIC_SHARE})
    public void handleShareMessage(ConsumerRecord record) {
        // 判空，出错记日志
        if (record == null || record.value() ==null) {
            logger.error("消息内容为空！");
            return;
        }
        //1. 获取消息内容（JSON），并解析回event对象
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            logger.error("消息格式不正确！");
            return;
        }

        String htmlUrl = (String) event.getData().get("htmlUrl");
        String fileName = (String) event.getData().get("fileName");
        String suffix = (String) event.getData().get("suffix");

        String cmd = wkImageCommand + " --quality 75 " + htmlUrl + " " + wkImageStorage + "/" + fileName + suffix;
        try {
            Runtime.getRuntime().exec(cmd);
            logger.info("生成长图成功：" + cmd);
        } catch (IOException e) {
            logger.error("生成长图失败：" + e.getMessage());
        }

    }


}
