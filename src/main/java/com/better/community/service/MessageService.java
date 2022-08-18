package com.better.community.service;

import com.better.community.dao.MessageMapper;
import com.better.community.entity.Message;
import com.better.community.entity.User;
import com.better.community.util.SensitiveWordsFilter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

/**
 * @param
 * @author Bubble
 * @description
 * @return
 * @Date 2022/7/14
 */
@Service
public class MessageService {
    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private SensitiveWordsFilter sensitiveWordsFilter;

    //根据用户查询会话条数
    public int findConversationCountByUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("参数不能为空！");
        }

        return messageMapper.selectConversationCountByUserId(user.getId());
    }

    //查询对话中的最后一条信息，用于展示私信列表
    public List<Message> findLastMessageByUserId(int userId, int offset, int limit) {

        return messageMapper.selectLastMessageByUserId(userId, offset, limit);
    }

    //查询一个会话中的未读消息数
    public int findUnreadCount(int userId, String conversationId) {
        return messageMapper.selectUnreadCount(userId , conversationId);
    }
    //查询一个会话中的所有信息数量
    public int findMessageCountByConversationId(String conversationId) {
        return messageMapper.selectMessageCountByConversationId(conversationId);
    }

    public List<Message> findMessagesByConversationId(String conversationId, int offset, int limit) {
        return messageMapper.selectMessagesByConversationId(conversationId, offset, limit);
    }

    //添加一条私信
    public int addMessage(Message message) {
        if (message == null) {
            throw new IllegalArgumentException("参数不能为空！！");
        }
        //转义HTML字符
        message.setContent(HtmlUtils.htmlEscape(message.getContent()));
        //过滤敏感词
        message.setContent(sensitiveWordsFilter.filter(message.getContent()));

        return messageMapper.insertMessage(message);
    }

    //读信息，把一个会话中所有未读信息的状态改为1
    public int readMessage(List<Integer> ids) {
        if (ids.isEmpty()) {
            throw new IllegalArgumentException("参数不能为空！");
        }
        return messageMapper.updateMessageStatus(ids, 1);
    }
    //删信息，把一个会话中所有未读信息的状态改为2，且ids中只有一个id
    public int deleteMessage(List<Integer> ids) {
        if (ids.isEmpty()) {
            throw new IllegalArgumentException("参数不能为空！");
        }
        return messageMapper.updateMessageStatus(ids, 2);
    }

    /**
     * 查询系统通知消息未读数量
     * @param userId 需要查询的用户id
     * @param topic 需要查询的主题，传入null表示所有主题
     * @return
     */
    public int findNoticeUnreadCount(int userId, String topic) {
        return messageMapper.selectNoticeUnreadCount(userId, topic);
    }

    /**
     * 返回一个主题下最新的一条消息
     * @param userId
     * @param topic 需要查询的主题
     * @return
     */
    public Message findLastNotice(int userId, String topic) {
        if (topic == null) {
            throw new IllegalArgumentException("参数不能为空!");
        }
        return messageMapper.selectLastNotice(userId, topic);
    }

    //查询某个主题下总共有多少会话
    public int findNoticeCount(int userId, String topic) {
        if (topic == null) {
            throw new IllegalArgumentException("参数不能为空!");
        }
        return messageMapper.selectNoticeCount(userId, topic);
    }

    //查询某个主题下所有会话
    public List<Message> findNotices(int userId, String topic, int offset, int limit) {
        if (StringUtils.isBlank(topic)){
            throw new IllegalArgumentException("参数不能为空！");
        }
        return messageMapper.selectNotices(userId, topic, offset, limit);
    }

}
