package com.better.community.controller.interceptor;

import com.better.community.entity.LoginTicket;
import com.better.community.entity.User;
import com.better.community.service.UserService;
import com.better.community.util.CookieUtil;
import com.better.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/**
 * @param
 * @author Bubble
 * @description
 * @return
 * @Date 2022/7/9
 */
@Component
public class LoginTicketInterceptor implements HandlerInterceptor {
    @Autowired
    private UserService userService;
    @Autowired
    private HostHolder hostHolder;

    //在执行Controller之前执行
    //在此预处理中做了：把用户传过来的cookie信息，查询出user信息，方便后面使用
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //获取ticket对应cookie
        String ticket = CookieUtil.getValue(request, "ticket");
        //查询LoginTicket
        LoginTicket loginTicket = userService.findLoginTicket(ticket);
        //判断当前loginTicket是否有效
        if (loginTicket != null && loginTicket.getStatus() == 0 && loginTicket.getExpired().after(new Date())) {
            //查询loginTicket对应user
            User user = userService.findUserById(loginTicket.getUserId());

            //把user对象封装进request域中，方便controller及template engine获取
            //request.setAttribute("user", user);

            //我自己的想法是把user存入request，讲师做法是存入ThreadLocal中
            //1.把user存入request对象虽然可行，但是实际上不会这么做
            //  原因：request是一个比较底层的对象，我们一般不直接使用它
            //       且request对象不被容器管理，不能想在哪注入就在哪注入，不灵活
            //2.使用ThreadLocal的原因
            //  使用ThreadLocal将user信息与当前线程绑定，保证多线程安全

            //hostHolder存放对象信息的容器，底层使用ThreadLocal是线程安全的。
            //hostHolder存在于整个项目运行过程中，可以在任意的地方使用，比较灵活
            hostHolder.setUser(user);

            //构建用户认证信息（Authentication），并存入SecurityContext，以便于Security进行后续的授权
            // Authentication 用于存放用户认证信息，不同的认证方式有不同的实现类
            // 我们这里使用的账号密码登录，实现类为 UsernamePasswordAuthenticationToken
            // 该接口的是实现类的构造器需要传三个参数
            // principal：认证的主要信息，此处传user
            // credentials：证书，此处为账号密码认证模式，此处传密码
            // authorities：权限
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    user, user.getPassword(), userService.getAuthorities(user.getId())
            );
            //把Authentication存入SecurityContext中(通过SecurityContextHolder存)
            SecurityContextHolder.setContext(new SecurityContextImpl(authentication));
        } else {
            // 如果 cookie 无效就把认证结果清了
            if(SecurityContextHolder.getContext() != null) {
                SecurityContextHolder.clearContext();
            }
        }

        return true;
    }

    //在Controller执行之后，模板引擎调用之前，把登录用户信息存到modelAndView中
    //以便可以在模板引擎中调用
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        User user = hostHolder.getUser();
        if (user != null && modelAndView != null) {
            modelAndView.addObject("loginUser",user);
        }
    }

    //在模板引擎执行完之后，用户登录信息就没用了，调用clear()方法清除，以节省内存。
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        hostHolder.clear();
//        SecurityContextHolder.clearContext();
    }
}
