package com.better.community.service;

import com.better.community.dao.LoginTicketMapper;
import com.better.community.dao.UserMapper;
import com.better.community.entity.LoginTicket;
import com.better.community.entity.Page;
import com.better.community.entity.User;
import com.better.community.util.CommunityConstant;
import com.better.community.util.CommunityUtil;
import com.better.community.util.MailClient;
import com.better.community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.context.IContext;
import sun.security.krb5.internal.Ticket;

import javax.mail.MessagingException;
import java.time.Duration;
import java.util.*;

/**
 * @Date 2022/5/4
 */
@Service
public class UserService implements CommunityConstant {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

//    @Autowired
//    private LoginTicketMapper loginTicketMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${community.path.domain}")
    private String domin;

    @Value("${server.servlet.context-path}")
    private String communityContext;

    //查询user的操作十分频繁，每次都从mysql数据库中取的话，效率太低
    //解决方案：使用redis做缓存
    //1.用户每次访问都尝试从redis缓存中取
    //2.如果取不到就初始化缓存（查MySQL数据库，查出来放到redis缓存中）
    //3.注意：当用户的个人信息发生改变时,需要刷新redis缓存（重新从MySQL数据库中取）
    //以前缓存逻辑定义在本类的 getCache() initCache() clearCache() 中
    //根据xx查询用户
    public User findUserById(int id){
        //1.尝试从缓存中获取
        User user = getCache(id);
        //如果获取到直接返回
        if (user == null)  {
            user = initCache(id);
        }
        //2.否则初始化缓存
        return user;
    }
    public User findUserByUserName(String username) {
        return userMapper.selectByUserName(username);
    }
    public User findUserByEmail(String email) {
        return userMapper.selectByEmail(email);
    }

    //添加一个用户
    public int insertUser(User user) {
        return userMapper.insertUser(user);
    }

    //通过id删除一个用户
    public int deleteUser(int id){
        return userMapper.deleteUser(id);
    }

    //更新用户信息的操作需要刷新缓存
    //更新一个用户的信息
    public int updateStatus(int id, int status){
        clearCache(id);
        return userMapper.updateStatus(id, status);
    }
    public int updateHeaderUrl(int id, String headerUrl) {
        clearCache(id);
        return userMapper.updateHeaderUrl(id, headerUrl);
    }
    //小细节：先更新MySQL，防止清完缓存MySQL命令失败（二者无法同时保证事务）
    //想了一下都一样
    public int updatePassword(int id, String password) {
        int rows = userMapper.updatePassword(id, password);
        clearCache(id);
        return rows;
    }

    public User findUserByTicket(String ticket) {
        return userMapper.selectByTicket(ticket);
    }

    public LoginTicket findLoginTicket(String ticket) {
        String ticketKey = RedisKeyUtil.getTicketKey(ticket);
        return (LoginTicket) redisTemplate.opsForValue().get(ticketKey);
    }

    public Map<String, Object> register(User user) {
        Map<String, Object> map = new HashMap<>();
        //验证空值
        if (StringUtils.isBlank(user.getUsername())) {
            map.put("usernameMsg", "用户名不能为空！");
            return map;
        }
        if (StringUtils.isBlank(user.getPassword())) {
            map.put("passwordMsg", "密码不能为空！");
            return map;
        }
        if (StringUtils.isBlank(user.getEmail())) {
            map.put("emailMsg", "邮箱不能为空！");
            return map;
        }

        //验证用户名或邮箱是否已被注册
        if (userMapper.selectByUserName(user.getUsername()) != null) {
            map.put("usernameMsg", "用户名已存在！");
            return map;
        }
        if (userMapper.selectByEmail(user.getEmail()) != null) {
            map.put("emailMsg", "该邮箱已被注册！");
            return map;
        }

        //如果用户名和邮箱都通过验证，则把用户信息存入数据库（但标识为为激活，通过发送给注册邮箱激活码的形式让用户激活。）
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setSalt(CommunityUtil.generateUUID().substring(0,5));
        user.setCreateTime(new Date());
        user.setType(0);    //普通用户
        user.setStatus(0);  //未激活
        String password = CommunityUtil.md5(user.getPassword() + user.getSalt());
        user.setPassword(password);
        userMapper.insertUser(user);    //id由mybatis自动管理


        //给未激活用户发送激活邮件（由模板引擎构建HTML）
        //构建HTML
        Context context = new Context();
        context.setVariable("email", user.getEmail());
        String url = domin + communityContext + "/activity/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url", url);

        String html = templateEngine.process("/mail/activation", context);
        //发送邮件

        mailClient.sendMail(user.getEmail(), "激活账户", html);


        return map;
    }

    public int activity(int id, String activationCode) {
        if (StringUtils.isBlank(activationCode)) {
            return ACTIVATION_FAILURE;
        }
        if (userMapper.selectById(id).getStatus() == 1) {
            return ACTIVATION_REPEAT;
        }
        if (activationCode.equals(userMapper.selectById(id).getActivationCode())) {
            userMapper.updateStatus(id, 1);
            clearCache(id);
            return ACTIVATION_SUCCESS;
        }
        return ACTIVATION_FAILURE;
    }

    public Map<String, String> login(String username, String password, int expiredSeconds) {
        Map<String, String> map = new HashMap<>();

        //账户判空
        if (StringUtils.isBlank(username)) {
            map.put("usernameMsg", "账户不能为空！");
            return map;
        }
        //密码判空
        if (StringUtils.isBlank(password)) {
            map.put("passwordMsg", "密码不能为空！");
            return map;
        }

        User user = userMapper.selectByUserName(username);

        //账户验证，查询数据库判断账户是否存在
        if (user == null) {
            map.put("usernameMsg", "账户不存在！");
            return map;
        }

        //验证账户状态
        if (user.getStatus() == 0) {
            map.put("usernameMsg", "账户未激活！");
            return map;
        }

        //密码验证，查询数据库验证密码是否正确
        if (!CommunityUtil.md5(password + user.getSalt()).equals(user.getPassword())) {
            map.put("passwordMsg", "密码不正确！");
            return map;
        }

        //如果通过所有验证，则生成登录凭证，并存入数据库
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setExpired(new Date(System.currentTimeMillis() + 1000L * expiredSeconds));
        loginTicket.setStatus(0);   //初始状态为0，表示生效
        loginTicket.setTicket(CommunityUtil.generateUUID());

//        loginTicketMapper.insertLoginTicket(loginTicket);
        //改为存在redis中
        String ticketKey = RedisKeyUtil.getTicketKey(loginTicket.getTicket());
        //直接以String的形式把 loginTicket 对象存入，redis会自动做序列化
        redisTemplate.opsForValue().set(ticketKey, loginTicket);

        //返回登录凭证 ticket
        map.put("ticket", loginTicket.getTicket());
        return map;
    }

    // 只需要更改状态，真实项目中一般不直接删除数据，这样方便以后做一定的统计
    public void logout(String ticket) {
        //空值处理
        if (StringUtils.isBlank(ticket)) {
            return;
        }
        //验证ticket
//        LoginTicket loginTicket = loginTicketMapper.selectLoginTicket(ticket);
        String ticketKey = RedisKeyUtil.getTicketKey(ticket);
        //会自动反序列化
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(ticketKey);
        if (loginTicket == null)
            return;
        //把对应loginTicket的状态该为1
//        loginTicketMapper.updateLoginTicket(loginTicket.getTicket(), 1);
        loginTicket.setStatus(1);
        //再存入redis
        redisTemplate.opsForValue().set(ticketKey, loginTicket);
    }

    public Map<String, String> sendVerifyCode(String email) {
        Map<String, String> map = new HashMap<>();
        if (StringUtils.isBlank(email)) {
            map.put("error", "邮箱不能为空！");
            return map;
        }
        //邮件主题
        String subject = "重置密码";
        //邮件内容为一个html，使用templateEngine把html文件动态渲染出来
        Context context = new Context();
        //添加动态属性，让templateEngine渲染到页面上
        context.setVariable("email", email);
        String verifyCode = CommunityUtil.generateUUID().substring(0,6);
        context.setVariable("verifyCode", verifyCode);
        String content = templateEngine.process("/mail/forget", context);

        //发送邮件
        mailClient.sendMail(email, subject, content);

        map.put("verifyCode", verifyCode);
        return map;
    }

    //缓存逻辑
    //1.用户每次访问都尝试从redis缓存中取
    //2.如果取不到就初始化缓存（查MySQL数据库，查出来返回并放到redis缓存中）
    //3.注意：当用户的个人信息发生改变时,需要刷新redis缓存（直接删除key就行了，再次获取会初始化）

    //初始化缓存：从MySQL中查出user信息并放入redis，设置超时时间1h，对于普通用户这个时间足够了
    private User initCache(int userId) {
        String userKey = RedisKeyUtil.getUserKey(userId);
        //从mysql中查找
        User user = userMapper.selectById(userId);
        //放入redis(使用String存储，redis会自动把user对象序列化成字符串)，并设置超时时间 1h
        redisTemplate.opsForValue().set(userKey, user, Duration.ofHours(1));
        //返回user
        return user;
    }

    //从缓存中获取用户信息
    private User getCache(int userId) {
        String userKey = RedisKeyUtil.getUserKey(userId);
        return (User) redisTemplate.opsForValue().get(userKey);
    }

    //刷新缓存:直接删除key
    private void clearCache(int userId) {
        String userKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(userKey);
    }

    //获取用户权限
    public Collection<? extends GrantedAuthority> getAuthorities(int userId) {
        User user = userMapper.selectById(userId);
        List<GrantedAuthority> list = new ArrayList<>();
        list.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                switch (user.getType()) {
                    case 1:
                        return AUTHORITY_ADMIN;
                    case 2:
                        return AUTHORITY_MODERATOR;
                    default:
                        return AUTHORITY_USER;
                }
            }
        });

        return list;
    }
}
