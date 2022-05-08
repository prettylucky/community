package com.better.community.dao;

import com.better.community.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 *
 * @Date 2022/5/3
 */
//也可以使用@Repository标注为dao层对象，但是要开启MapperScan才能将配置的xml文件生效
//@Mapper不用配置MapperScan，可自动将对应的xml文件生效
@Mapper
public interface UserMapper {
    //根据xx查询用户
    User selectById(int id);
    User selectByUserName(String username);
    User selectByEmail(String email);

    //添加一个用户
    int insertUser(User user);

    //通过id删除一个用户
    int deleteUser(int id);

    //更新一个用户的信息
    int updateStatus(int id, int status);
    int updateHeaderUrl(int id, String headerUrl);
    int updatePassword(int id, String password);

}
