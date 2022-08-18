package com.better.community.config;

import com.better.community.util.CommunityConstant;
import com.better.community.util.CommunityUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * SpringSecurity主要功能：认证，授权
 * 认证：就是在用户登录的时候识别用户是否合法（账号密码是否正确、或扫码登录、qq登录等其他登录手段是否合法）,且识别该用户的权限
 * 授权：根据用户权限判断该用户是否有资格访问当前请求,如果没有资格或者没有登录执行对应的逻辑
 * SpringSecurity是根据Filter实现的
 * Filter --> DispatcherServlet --> Interceptor --> Controller
 * @Date 7/22/2022
 */
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter implements CommunityConstant {

    //==========================忽略对静态资源的拦截==========================
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/resources/**");
    }

    //============================认证管理===================================
    // 这里我们不使用SpringSecurity的认证逻辑，而是使用我们自己写的认证逻辑
    // 但是没有了SpringSecurity的认证，SpringSecurity就无法从SecurityContext中获取到认证的结果
    // 所以我们要想办法把我们自己的认证结果存放到SpringSecurity的认证结果的容器中
    // 认证信息：xxxAuthenticationToken,xxx表示认证方式，我们这里是UsernamePasswordAuthenticationToken
    // 认证信息容器：SecurityContextHolder
    // 所以我们要在我们自己实现认证的逻辑中额外把认证信息(Authentication)存入SecurityContextHolder
    // 我们的认证逻辑在 LoginTicketInterceptor

    //============================授权管理===================================

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        //授权
        http.authorizeHttpRequests()
                .antMatchers(
                    "/user/setting",
                    "/user/upload",
                    "/discuss/add",
                    "/comment/add/**",
                    "/letter/**",
                    "/notice/**",
                    "/like",
                    "/follow",
                    "/unfollow"
                ).hasAnyAuthority(
                    AUTHORITY_USER,
                    AUTHORITY_ADMIN,
                    AUTHORITY_MODERATOR
                ).antMatchers(
                        "/discuss/top",
                        "/discuss/wonderful"
                )
                .hasAnyAuthority(
                        AUTHORITY_MODERATOR
                ).antMatchers(
                        "/discuss/delete",
                        "/data/**",
                        "/actuator/**"
                ).hasAnyAuthority(
                        AUTHORITY_ADMIN
                )
                .anyRequest().permitAll()     //除了以上路径，所有请求都不需要权限
                .and().csrf().disable();    //取消csrf验证（图省事，因为要该好多异步请求的地方）

        // 权限不够时的处理
        http.exceptionHandling()
                //没有登录时处理
                .authenticationEntryPoint(new AuthenticationEntryPoint() {
                    @Override
                    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
                        //判断请求是同步还是异步
                        String xRequestedWith = request.getHeader("x-requested-with");
                        //如果是异步请求，返回一段JSON的错误提示
                        if ("XMLHttpRequest".equals(xRequestedWith)) {
                            response.setContentType("application/plain;charset=utf-8");   //plain表示普通字符串，但我们要确保是JSON格式
                            PrintWriter writer = response.getWriter();
                            writer.write(CommunityUtil.getJSONString(403, "你还没有登录！"));
                        }
                        //如果是普通请求，则重定向到登录页面
                        else {
                            response.sendRedirect(request.getContextPath() + "/login");
                        }
                    }
                })
                //权限不足时的处理
                .accessDeniedHandler(new AccessDeniedHandler() {
                    @Override
                    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
                        //判断请求是同步还是异步
                        String xRequestedWith = request.getHeader("x-requested-with");
                        //如果是异步请求，返回一段JSON的错误提示
                        if ("XMLHttpRequest".equals(xRequestedWith)) {
                            response.setContentType("application/plain;charset=utf-8");   //plain表示普通字符串，但我们要确保是JSON格式
                            PrintWriter writer = response.getWriter();
                            writer.write(CommunityUtil.getJSONString(403, "您没有访问该功能的权限！"));
                        }
                        //如果是普通请求，则重定向到权限不足页面
                        else {
                            response.sendRedirect(request.getContextPath() + "/denied");
                        }

                    }
                });

        // Security底层默认会拦截/logout请求，进行退出的处理
        // 这里覆盖掉它默认的逻辑，使用我们自己的退出逻辑
        http.logout().logoutUrl("/securitylogout");
    }
}
