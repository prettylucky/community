package com.better.community.controller;

import com.better.community.annotation.LoginRequired;
import com.better.community.entity.User;
import com.better.community.service.FollowService;
import com.better.community.service.LikeService;
import com.better.community.service.UserService;
import com.better.community.util.CommunityConstant;
import com.better.community.util.CommunityUtil;
import com.better.community.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;

/**
 * @param
 * @author Bubble
 * @description
 * @return
 * @Date 2022/7/10
 */
@Controller
@RequestMapping("/user")
public class UserController implements CommunityConstant {
    private final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;
    
    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowService followService;

    @Value("${community.path.headImage}")
    private String uploadPath;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String context;

    //获取账户设置页面
    @LoginRequired
    @RequestMapping("/setting")
    public String getSettingPage() {
        return "/site/setting";
    }

    //更新用户头像
    @LoginRequired
    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    public String setHeadImage(MultipartFile headerImage, Model model) {
        //1.文件检查
        if (headerImage == null) {
            model.addAttribute("error", "文件为空！");
            return "/site/setting";
        }
        //获取文件大小单位：byte
        long size = headerImage.getSize();
        //tomcat允许最大上传文件大小：1048576 bytes
        if (size >= 1000000) {
            model.addAttribute("error", "文件过大！请重新上传！");
            return "/site/setting";
        }
        //获取文件名
        String filename = headerImage.getOriginalFilename();
        //获取文件后缀
        String suffix = filename.substring(filename.lastIndexOf('.') + 1);
        //验证文件格式
        if (StringUtils.isBlank(suffix)) {
            model.addAttribute("error","文件格式不正确！");
            return "/site/setting";
        }
        //System.out.println("文件后缀：" + suffix);
        if (!suffix.equals("jpg") && !suffix.equals("png") && !suffix.equals("jpeg")) {
            model.addAttribute("error","请选择 jpg/png/jpeg 格式的文件！");
            return "/site/setting";
        }

        //2.文件通过验证，把文件存入本地磁盘，且更新数据库中对应头像url。
        // 文件路径设置为属性方便修改，利用IO存入本地磁盘。
        //生成随件文件名
        filename =  CommunityUtil.generateUUID() + "." + suffix;
        File imageFile = new File(uploadPath + "/" +filename);

        try {
            headerImage.transferTo(imageFile);
        } catch (IOException e) {
            logger.debug("头像存入服务器发生错误！msg：" + e.getMessage());
            model.addAttribute("error","头像存入服务器发生错误！");
            return "/site/setting";
        }

        //3.调用service层更新用户头像地址
        //获取用户
        User user = hostHolder.getUser();
        //web路径
        //http://localhost:8080/community/user/headimage/{filename}
        String headerUrl = domain + context + "/user/headimage/" + filename;
        userService.updateHeaderUrl(user.getId(), headerUrl);


        return "redirect:/index";
    }

    //获取用户头像
    //利用流向浏览器返回二进制文件，返回值类型为void
    @GetMapping("/headimage/{filename}")
    public void getHeadImage(@PathVariable String filename, HttpServletResponse response) {
        String suffix = filename.substring(filename.lastIndexOf('.') + 1);
        //设置响应内容为图片
        response.setContentType("image/" + suffix);
        File file = new File(uploadPath + "/" + filename);

        try (
            //获取输入流读入文件
            FileInputStream fis = new FileInputStream(file);
            //获取输出流，把读入的文件响应到浏览器
            ServletOutputStream os = response.getOutputStream();
        ) {
            //缓冲区，当读取1024byte后再输出，效率较高
            byte[] buffer = new byte[1024];
            //一次读入不一定是1024字节，使用b记录读入了多少字节的数据
            int b;
            while ((b = fis.read(buffer)) != -1) {
                os.write(buffer, 0, b);
            }
        } catch (IOException e) {
            logger.debug("获取头像失败！msg:" + e.getMessage());
        }
    }

    //用户重置密码
    @LoginRequired
    @PostMapping("/setpassword")
    public String setPassword(String oldpassword, String newpassword, String newpasswordcheck,
                              Model model) {
        //空值处理
        if (StringUtils.isBlank(oldpassword) || StringUtils.isBlank(newpassword) || StringUtils.isBlank(newpasswordcheck)) {
            model.addAttribute("oldPasswordMsg", "信息填写不完整！");
            return "/site/setting";
        }
        //验证新密码是否合法
        if (!newpassword.equals(newpasswordcheck)) {
            model.addAttribute("newPasswordMsg", "两次密码输入不一致！");
            model.addAttribute("newPasswordCheckMsg", "两次密码输入不一致！");
            return "/site/setting";
        }

        //验证原密码是否正确
        User user = hostHolder.getUser();
        String password = user.getPassword();
        oldpassword = CommunityUtil.md5(oldpassword + user.getSalt());
        if (!password.equals(oldpassword)) {
            model.addAttribute("oldPasswordMsg", "原密码错误！");
            return "/site/setting";
        }

        //所有验证通过，调用数据库更改密码
        newpassword = CommunityUtil.md5(newpassword + user.getSalt());
        userService.updatePassword(user.getId(), newpassword);
        return "redirect:/logout";
    }

    //查询用户主页（根据查询的是不是自己的主页，显示的内容也稍有不同。）
    @GetMapping("/profile/{userId}")
    public String getProfilePage(@PathVariable("userId") int userId, Model model) {
        //查询当前登录用户
        User loginUser = hostHolder.getUser();

        //查询指定用户
        User user = userService.findUserById(userId);
        model.addAttribute("user", user);

        //判断查询的是不是自己的资料
        boolean isMe = false;
        if (loginUser != null && loginUser.getId() == userId) {
            isMe = true;
        }
        model.addAttribute("isMe", isMe);

        //查询用户获得了多少个赞
        int likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount", likeCount);

        //查询用户对实体的关注状态
        boolean followStatus = false;
        if (loginUser != null)
             followStatus =  followService.findFollowStatus(loginUser.getId(), ENTITY_TYPE_USER, userId);
        model.addAttribute("followStatus", followStatus);

        //查询实体有多少粉丝
        long followerCount = followService.findEntityFollowerCount(ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount", followerCount);

        //则查询该用户关注的用户数量
        long followeeCount = followService.findUserFolloweeCount(userId, ENTITY_TYPE_USER);
        model.addAttribute("followeeCount", followeeCount);



        return "/site/profile";
    }
}
