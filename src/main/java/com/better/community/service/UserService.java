package com.better.community.service;

import com.better.community.dao.UserMapper;
import com.better.community.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @param
 * @author Bubble
 * @description
 * @return
 * @Date 2022/5/4
 */
@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    //根据xx查询用户
    public User findUserById(int id){
        return userMapper.selectById(id);
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

    //更新一个用户的信息
    public int updateStatus(int id, int status){
        return userMapper.updateStatus(id, status);
    }
    public int updateHeaderUrl(int id, String headerUrl) {
        return userMapper.updateHeaderUrl(id, headerUrl);
    }
    public int updatePassword(int id, String password) {
        return userMapper.updatePassword(id, password);
    }
}
