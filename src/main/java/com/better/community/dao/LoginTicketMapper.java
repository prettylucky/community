package com.better.community.dao;

import com.better.community.entity.LoginTicket;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * @param
 * @author Bubble
 * @description
 * @return
 * @Date 2022/7/8
 */
@Mapper
@Deprecated
public interface LoginTicketMapper {
    @Insert({
            "insert into login_ticket(user_id, ticket, status, expired) ",
            "values(#{userId}, #{ticket}, #{status}, #{expired})"
    })
    int insertLoginTicket(LoginTicket loginTicket);

    @Select({
            "select id,user_id,ticket,status,expired from login_ticket ",
            "where ticket = #{ticket}"
    })
    LoginTicket selectLoginTicket(String ticket);

    @Update({
            "<script>",
            "update login_ticket set status = #{status} ",
            "where ticket = #{ticket}",
            "<if test=\"ticket != null\">",     //练习动态sql，此处其实并不需要动态sql
            "and 1=1",
            "</if>",
            "</script>"
    })
    int updateLoginTicket(String ticket, int status);
}
