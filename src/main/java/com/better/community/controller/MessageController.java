package com.better.community.controller;

import com.alibaba.fastjson.JSONObject;
import com.better.community.entity.Message;
import com.better.community.entity.Page;
import com.better.community.entity.User;
import com.better.community.service.MessageService;
import com.better.community.service.UserService;
import com.better.community.util.CommunityConstant;
import com.better.community.util.CommunityUtil;
import com.better.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 处理私信相关请求
 * @Date 2022/7/14
 */
@Controller
@RequestMapping("/letter")
public class MessageController implements CommunityConstant {
    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserService userService;
    @Value("${community.path.domain}")
    private String domain;
    @Value("${server.servlet.context-path}")
    private String contextPath;

    //查询私信列表，且需要做分页功能
    @GetMapping("/list")
    public String getLetterPage(Page page, Model model) {
        //获取当前用户
        User user = hostHolder.getUser();

        //补充分页信息
        page.setLimit(10);
        page.setPath("/letter/list");
        page.setRows(messageService.findConversationCountByUser(user));

        //查询所有会话 conversation 的最后一条信息
        List<Message> lastMessageList =  messageService.findLastMessageByUserId(
                user.getId(), page.getOffset(), page.getLimit());
        //将lastMessage和otherUser一起封装到map中，然后加到一个list里面
        List<Map<String, Object>> conversations = new ArrayList<>();
        if (lastMessageList != null) {
            for (Message lastMessage : lastMessageList) {
                Map<String, Object> map = new HashMap<>();
                map.put("lastMessage", lastMessage);
                //获取对方id
                int targetId = lastMessage.getFromId() == user.getId() ? lastMessage.getToId() : lastMessage.getFromId();
                map.put("target", userService.findUserById(targetId));
                //获取单个会话未读消息数
                map.put("unreadCount", messageService.findUnreadCount(user.getId(), lastMessage.getConversationId()));
                //获取一个会话中一共有多少条私信
                map.put("messageCount", messageService.findMessageCountByConversationId(lastMessage.getConversationId()));

                conversations.add(map);
            }
        }

        model.addAttribute("conversations", conversations);
        //获取所有未读消息数
        int unreadCount = messageService.findUnreadCount(user.getId(), null);
        model.addAttribute("unreadCount", unreadCount);

        //获取系统通知未读消息数
        int noticeUnreadCount = messageService.findNoticeUnreadCount(user.getId(), null);
        model.addAttribute("noticeUnreadCount", noticeUnreadCount);

        return "/site/letter";
    }

    @GetMapping("/detail/{conversationId}")
    public String getLetterDetailPage(@PathVariable("conversationId") String conversationId,
                                      Page page, Model model) {
        User user = hostHolder.getUser();

        //处理分页信息
        page.setRows(messageService.findMessageCountByConversationId(conversationId));
        page.setLimit(5);
        page.setPath("/letter/detail/" + conversationId);

        //封装私信信息
        ArrayList<Map<String, Object>> messageVOList = new ArrayList<>();
        //查询所有私信,以所有私信为准查询附加信息：发件人
        List<Message> messageList = messageService.findMessagesByConversationId(conversationId, page.getOffset(), page.getLimit());
        //封装未读消息的id，统一置其为已读
        List<Integer> ids = new ArrayList<>();
        if (messageList != null) {
            for (Message message : messageList) {
                //封装VO数据
                HashMap<String, Object> messageVO = new HashMap<>();
                messageVO.put("message", message);
                messageVO.put("user", userService.findUserById(message.getFromId()));

                messageVOList.add(messageVO);

                //封装未读私信id
                if (message.getStatus() == 0 && message.getToId() == user.getId()) {
                    ids.add(message.getId());
                }
            }
        }
        //让未读消息已读
        if (!ids.isEmpty()) {
            messageService.readMessage(ids);
        }

        //封装VO数据到model
        int targetId = findTargetUserIdByConversationId(conversationId);
        model.addAttribute("target", userService.findUserById(targetId));

        model.addAttribute("messages", messageVOList);



        return "/site/letter-detail";
    }

    private int findTargetUserIdByConversationId(String conversationId) {
        User user = hostHolder.getUser();

        String[] ids = conversationId.split("_");
        int id0 = Integer.parseInt(ids[0]);
        int id1 = Integer.parseInt(ids[1]);

        if (user.getId() == id0)
            return id1;
        else
            return id0;
    }

    //异步请求，返回JSON字符串
    @PostMapping("/send")
    @ResponseBody
    public String sendMessage(String content, String targetUsername) {
        User user = hostHolder.getUser();
        //前端需要传入message的 content targetUsername， 其余参数我们自己设定
        Message message = new Message();
        message.setFromId(user.getId());
        message.setContent(content);
        message.setToId(userService.findUserByUserName(targetUsername).getId());
        message.setConversationId(generateConversationId(user.getId(), message.getToId()));
        message.setStatus(0);
        message.setCreateTime(new Date());

        //添加私信
        int i = messageService.addMessage(message);
        if (i == 1) {
            return CommunityUtil.getJSONString(200, "发送成功！");
        } else {
            return CommunityUtil.getJSONString(400, "发送失败！");
        }
    }

    private String generateConversationId(int fromId, int toId) {
        return fromId < toId ? fromId + "_" + toId : toId + "_" + fromId;
    }


    @PostMapping("/delete")
    public String deleteMessage(int id, String conversationId) {
        ArrayList<Integer> ids = new ArrayList<>();
        ids.add(id);
        messageService.deleteMessage(ids);
        return "redirect:/letter/detail/" + conversationId;
    }

    //获取系统通知界面
    @RequestMapping("/notice")
    public String getNoticePage(Model model) {
        User user = hostHolder.getUser();
        int userId = user.getId();

        //前端页面需要信息：朋友私信未读数量、系统通知未读数量
        //        三个事件各自的信息包括：三个事件各自的未读数量、三个事件最后一条消息、三个事件分别总共有几个会话

        //1.朋友私信未读数量
        int letterUnReadCount = messageService.findUnreadCount(userId, null);
        //2.系统通知总的未读消息数量
        int noticeUnreadCount = messageService.findNoticeUnreadCount(userId, null);

        //3.comment事件包含的信息
        HashMap<String, Object> commentVO = new HashMap<>();
        Message lastNotice = messageService.findLastNotice(userId, TOPIC_COMMENT);
        if (lastNotice != null) {
            //notice中的content为JSON格式，不能直接传给前端，需要解析后再传
            String content = lastNotice.getContent();
            HashMap data = JSONObject.parseObject(content, HashMap.class);

            commentVO.put("lastNotice", lastNotice);
            commentVO.put("user", userService.findUserById((int) data.get("userId")));
            commentVO.put("entityType", data.get("entityType"));
            commentVO.put("entityId", data.get("entityId"));
            commentVO.put("postId", data.get("postId"));
            commentVO.put("unreadCount",messageService.findNoticeUnreadCount(userId, TOPIC_COMMENT));
            commentVO.put("noticeCount", messageService.findNoticeCount(userId, TOPIC_COMMENT));
        }

        //4.like事件包含的信息
        HashMap<String, Object> likeVO = new HashMap<>();
        Message likeLastNotice = messageService.findLastNotice(userId, TOPIC_LIKE);
        if (likeLastNotice != null){
            //解析content中的内容
            String content = likeLastNotice.getContent();
            HashMap data = JSONObject.parseObject(content, HashMap.class);

            likeVO.put("lastNotice", likeLastNotice);
            likeVO.put("user", userService.findUserById((Integer) data.get("userId")));
            likeVO.put("entityType", data.get("entityType"));
            likeVO.put("entityId", data.get("entityId"));
            likeVO.put("postId", data.get("postId"));
            likeVO.put("unreadCount",messageService.findNoticeUnreadCount(userId, TOPIC_LIKE));
            likeVO.put("noticeCount", messageService.findNoticeCount(userId,TOPIC_LIKE));
        }

        //5.follow事件包含的信息
        HashMap<String, Object> followVO = new HashMap<>();
        Message followLastNotice = messageService.findLastNotice(userId, TOPIC_FOLLOW);
        if (followLastNotice != null) {
            //解析content
            String content = followLastNotice.getContent();
            HashMap data = JSONObject.parseObject(content, HashMap.class);

            followVO.put("user", userService.findUserById((Integer) data.get("userId")));
            followVO.put("entityType", data.get("entityType"));
            followVO.put("entityId", data.get("entityTId"));
            followVO.put("unreadCount", messageService.findNoticeUnreadCount(userId, TOPIC_FOLLOW));
            followVO.put("lastNotice", followLastNotice);
            followVO.put("noticeCount", messageService.findNoticeCount(userId,TOPIC_FOLLOW));
        }

        //封装数据
        model.addAttribute("letterUnreadCount", letterUnReadCount);
        model.addAttribute("noticeUnreadCount", noticeUnreadCount);
        model.addAttribute("commentVO", commentVO);
        model.addAttribute("likeVO", likeVO);
        model.addAttribute("followVO", followVO);

        return "/site/notice";
    }

    //获取系统通知详情页面
    @GetMapping("/notice/detail/{topic}")
    public String getNoticeDetailPage(@PathVariable("topic") String topic, Page page, Model model) {
        User user = hostHolder.getUser();

        //前端需要数据：所有通知信息（触发该事件的username、触发实体对象、帖子链接/个人主页链接）、分页数据
        //处理分页信息
        page.setRows(messageService.findNoticeCount(user.getId(), topic));
        page.setPath("/letter/notice/detail/" + topic);
        page.setLimit(5);

        ArrayList<Map<String, Object>> notices = new ArrayList<>();
        ArrayList<Integer> ids = new ArrayList<>();
        //查询该主题下所有系统通知
        List<Message> noticesList = messageService.findNotices(user.getId(), topic, page.getOffset(), page.getLimit());
        //根据通知查询附加信息
        if (noticesList != null) {
            for (Message notice : noticesList) {
                HashMap<String, Object> noticeVO = new HashMap<>();

                //解析JSON格式的content
                String content = notice.getContent();
                HashMap data = JSONObject.parseObject(content, HashMap.class);

                noticeVO.put("notice", notice);
                noticeVO.put("user", userService.findUserById((Integer) data.get("userId")));
                noticeVO.put("entityType", data.get("entityType"));
                noticeVO.put("entityId", data.get("entityId"));
                //封装一个链接，根据事件触发于帖子（评论）、或是用户 分别链向帖子或用户主页
                String url = null;
                if (data.get("postId") != null) {       //链向帖子
                    url = domain + contextPath + "/discuss/detail/" + data.get("postId");
                } else {        //链向用户主页
                    url = domain + contextPath + "/user/profile/" + data.get("userId");
                }
                noticeVO.put("url", url);

                notices.add(noticeVO);

                ids.add(notice.getId());

            }
        }
        //把消息状态设置为已读
        if (!ids.isEmpty())
            messageService.readMessage(ids);

        //封装数据，page会自动装到model里
        model.addAttribute("notices", notices);
        return "/site/notice-detail";
    }

}
