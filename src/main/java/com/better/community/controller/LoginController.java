package com.better.community.controller;

import com.better.community.config.KaptchaConfig;
import com.better.community.entity.User;
import com.better.community.service.UserService;
import com.better.community.util.CommunityConstant;
import com.better.community.util.CommunityUtil;
import com.better.community.util.MailClient;
import com.better.community.util.RedisKeyUtil;
import com.google.code.kaptcha.Producer;
import com.jhlabs.image.ImageUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Insert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @param
 * @author Bubble
 * @description
 * @return
 * @Date 2022/5/8
 */
@Controller
@PropertySource("classpath:")
public class LoginController implements CommunityConstant {
    private static Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private Producer kaptchaProducer;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Value("${community.path.domain}")
    private String domain;


    //获取注册页面
    @GetMapping("/register")
    public String getRegisterPage(){
        return "/site/register";
    }

    //处理注册表单，符合条件注册成功，并发送激活邮件
    @PostMapping("/register")
    public String register(Model model, User user){
        if (user == null){
            return "/site/register";
        }
        Map<String, Object> map = userService.register(user);
        if (map.isEmpty()){
            model.addAttribute("msg", "注册成功，请注意查收邮箱进行激活！");
            model.addAttribute("target", "/index");
            return "/site/operate-result";
        }else {
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            model.addAttribute("emailMsg", map.get("emailMsg"));
            return "/site/register";
        }

    }

    //处理激活请求，激活成功则返回登录界面
    @GetMapping("/activity/{id}/{activationCode}")
    public String activity(Model model, @PathVariable("id") int id, @PathVariable("activationCode") String activationCode) {
        int result = userService.activity(id, activationCode);
        if (result == ACTIVATION_FAILURE) {
            model.addAttribute("msg", "激活码错误！");
            model.addAttribute("target", "/index");
        }
        if (result == ACTIVATION_REPEAT) {
            model.addAttribute("msg", "账户已激活，无需重复激活！");
            model.addAttribute("target", "/index");
        }
        if (result == ACTIVATION_SUCCESS) {
            model.addAttribute("msg", "账户已成功激活，快去登录吧！");
            model.addAttribute("target", "/login");
        }
        return "/site/operate-result";
    }

    //
    @GetMapping("/login")
    public String getLoginPage() {
        return "/site/login";
    }

    // 获取验证码功能重构，把验证码存放在redis中，因为存在session中存在分布式部署的共享问题
    @GetMapping("/kaptcha")
    public void kaptcha(HttpServletResponse response /*HttpSession session*/) {
        //生成验证码
        String text = kaptchaProducer.createText();

        //把验证码存到session中
//        session.setAttribute("kaptcha", text);

        //使用cookie给用户颁发临时凭证，用于识别用户
        String owner = CommunityUtil.generateUUID();    //生成临时凭证`
        Cookie cookie = new Cookie("kaptchaOwner", owner); //存入cookie
        cookie.setMaxAge(60); //设置cookie生效时间 60s
        cookie.setPath(contextPath);    //生效路径
        response.addCookie(cookie); //把cookie发送给前端

        //把生成的验证码存放到redis中
        String kaptchaOwner = RedisKeyUtil.getKaptchaKey(owner);
        redisTemplate.opsForValue().set(kaptchaOwner, text);
        redisTemplate.expire(kaptchaOwner, 60, TimeUnit.SECONDS);   //设置生效时间60s

        //生成验证码图片
        BufferedImage image = kaptchaProducer.createImage(text);

        //设置响应格式为 image/png
        response.setContentType("image/png");
        try {
            //获取输出流
            ServletOutputStream outputStream = response.getOutputStream();

            //利用输出流把图片传到前端
            ImageIO.write(image, "png", outputStream);

        } catch (IOException e) {
            logger.error("验证码生成失败：" + e.getMessage());
        }

    }

    //获取验证码重构，从redis中获取验证码
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public String login(String username, String password, String code, boolean rememberMe,
                        Model model/* HttpSession session*/, HttpServletResponse response,
                        @CookieValue("kaptchaOwner") String owner) {
        //验证码判空
        if (StringUtils.isBlank(code)) {
            model.addAttribute("codeMsg","验证码不能为空！");
            return "/site/login";
        }
        //验证码验证
        String kaptchaOwner = RedisKeyUtil.getKaptchaKey(owner);    //通过cookie传入的临时凭证生成key
        String kaptcha = null;
        if (!StringUtils.isBlank(kaptchaOwner)) {
            kaptcha = (String) redisTemplate.opsForValue().get(kaptchaOwner);
        }
        if (!code.equalsIgnoreCase(kaptcha)) {
            model.addAttribute("codeMsg", "验证码不正确！");
            return "/site/login";
        }

        int expiredSeconds = rememberMe ? REMEMBER_EXPIRED_SECONDS : DEFAULT_EXPIRED_SECONDS;

        System.out.println("=============");
        System.out.println(rememberMe + "...." + expiredSeconds);

        Map<String, String> map = userService.login(username, password, expiredSeconds);

        String ticket = map.get("ticket");
        if (ticket != null) {
            //创建cookie对象
            Cookie cookie = new Cookie("ticket", ticket);
            //设置cookie生效时间和生效路径
            cookie.setMaxAge(expiredSeconds);
            cookie.setPath(contextPath);
            response.addCookie(cookie);
            return "redirect:/index";
        } else {
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            return "/site/login";
        }
    }

    @GetMapping("/logout")
    public String logout(@CookieValue("ticket") String ticket){
        userService.logout(ticket);
        SecurityContextHolder.clearContext();
        return "redirect:/login";
    }

    @GetMapping("/forget")
    public String getForgetPage() {
        return "/site/forget";
    }

    //ajax异步请求
    //只返回JSON字符串
    @GetMapping("/sendverifycode")
    @ResponseBody
    public String sendVerifyCode(String email, HttpSession session) {
        //调用业务层发送邮件
        Map<String, String> map = userService.sendVerifyCode(email);
        if (map.get("error") != null) {
            return CommunityUtil.getJSONString(400, "参数不能为空！");
        }

        String verifyCode = map.get("verifyCode");
        if (StringUtils.isBlank(verifyCode)) {
            return CommunityUtil.getJSONString(400, "发送失败！");
        }

        //把发送的验证码存储到session中，并设定超时时间5分钟
        session.setAttribute("verifyCode", verifyCode);

        return CommunityUtil.getJSONString(200, "发送成功！");
    }

    @PostMapping("/reset")
    public String resetPassword(String email, String verifyCode, String password,
                                HttpSession session, Model model) {
        model.addAttribute("target", domain + contextPath + "/index");

        if (StringUtils.isBlank(verifyCode) || StringUtils.isBlank(password)) {
            model.addAttribute("msg", "参数不能为空！重置密码失败！");
        }
        //验证验证码是否正确
        if (verifyCode.equalsIgnoreCase((String) session.getAttribute("verifyCode"))) {
            //密码正确调用业务层重置密码
            User user = userService.findUserByEmail(email);
            password = CommunityUtil.md5(password + user.getSalt());
            int i = userService.updatePassword(user.getId(), password);
            if (i == 1) {
                model.addAttribute("msg", "密码重置成功！");
            } else {
                model.addAttribute("msg", "密码重置失败！");
            }
        }
        return "/site/operate-result";
    }
}
