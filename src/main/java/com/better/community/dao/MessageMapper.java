package com.better.community.dao;

import com.better.community.entity.Message;
import com.better.community.entity.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @param
 */
@Mapper
public interface MessageMapper {
    //根据用户查询会话条数
    int selectConversationCountByUserId(int userId);
    //查询对话中的最后一条信息列表，用于展示私信列表
    List<Message> selectLastMessageByUserId(int userId, int offset, int limit);
    //查询一个会话中的未读消息数，或全部未读消息数
    int selectUnreadCount(int userId, String conversationId);
    //查询一个会话中的消息数
    int selectMessageCountByConversationId(String conversationId);
    //查询一个会话中所有信息
    List<Message> selectMessagesByConversationId(String conversationId, int offset, int limit);
    //添加一条私信
    int insertMessage(Message message);
    //更新信息状态
    int updateMessageStatus(List<Integer> ids, int status);

    //查询系统通知未读消息数
    int selectNoticeUnreadCount(int userId, String conversationId);
    //查询该主题下最后一条通知
    Message selectLastNotice(int userId, String conversationId);
    //查询该主题下一共有多少条会话
    int selectNoticeCount(int userId, String conversationId);
    //查询一个主题下的所有会话
    List<Message> selectNotices(int userId, String conversationId, int offset, int limit);
}
