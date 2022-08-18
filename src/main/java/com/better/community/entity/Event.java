package com.better.community.entity;

import java.util.HashMap;
import java.util.Map;

/**
 * 使用kafka做消息队列，用于发布系统信息。
 * 什么时候会触发系统发送通知：评论后、点赞后、关注后，我们称这些为一个事件。
 * 用于封装触发kafka生产行为的各种事件信息（点赞、关注、评论），即生产者往阻塞队列中存放的消息。
 *
 * 既然只是一个消息，为什么不直接存放一个字符串？
 * 只存放一个字符串不灵活，直接存放一个字符串，后面消费者取出来想怎么拼消息都行。
 * （消费者取出消息后，会存放在数据库中）
 * @Date 7/19/2022
 */
public class Event {
    //消息主题
    private String topic;
    //触发这个事件的用户
    private int userId;
    //改用户对哪个实体操作了这个事件 如：给谁点了赞，给哪个帖子评论了
    private int entityType;
    private int entityId;
    //这个实体的作者是谁
    private int entityUserId;
    //封装一些其他信息，便于以后扩展
    private final Map<String, Object> data = new HashMap<>();

    // ------------------- 我们对set方法做了一些修改---------------------------
    // 1.由于该实体类属性比较多，把set方法的void返回值该为返回当前对象
    //   这样做之后，我们可以这么给这个对象赋值，比较方便：
    //   event.setTopic(xxx).seUserId(xxx).setEntityType(xxx).setEntityId(xxx).setEntityUserId(xxx)....

    // 2.对于属性data是一个Map，如果我们想给它赋值，还需要重新new一个map，我们可以做一下改造
    //   Event setData(String key, Object Objet) {
    //      this.data.put(key, value);
    //      return this;
    //   }
    //   这样我们可以很轻松的给它赋值

    public String getTopic() {
        return topic;
    }

    public Event setTopic(String topic) {
        this.topic = topic;
        return this;
    }

    public int getUserId() {
        return userId;
    }

    public Event setUserId(int userId) {
        this.userId = userId;
        return this;
    }

    public int getEntityType() {
        return entityType;
    }

    public Event setEntityType(int entityType) {
        this.entityType = entityType;
        return this;
    }

    public int getEntityId() {
        return entityId;
    }

    public Event setEntityId(int entityId) {
        this.entityId = entityId;
        return this;
    }

    public int getEntityUserId() {
        return entityUserId;
    }

    public Event setEntityUserId(int entityUserId) {
        this.entityUserId = entityUserId;
        return this;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public Event setData(String key, Object value) {
        this.data.put(key, value);
        return this;
    }

}
