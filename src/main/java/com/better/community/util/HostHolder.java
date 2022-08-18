package com.better.community.util;

import com.better.community.entity.User;
import org.springframework.stereotype.Component;

/**
 * 持有用户对象的容器，代替session对象
 * 底层使用ThreadLocal，用于给当前对象绑定
 * @Date 2022/7/9
 */
//hostHolder存在于整个项目运行过程中，可以在任意的地方使用
//底层使用TheadLocal把数据存放到线程中，用完即使删除
@Component
public class HostHolder {
    private ThreadLocal<User> users = new ThreadLocal<User>();

    public void setUser(User user) {
        users.set(user);
    }

    public User getUser() {
        return users.get();
    }
    //使用完成后调用clear()，删除ThreadLocal中的数据
    public void clear() {
        users.remove();
    }
}
