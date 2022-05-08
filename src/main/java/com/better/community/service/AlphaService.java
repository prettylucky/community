package com.better.community.service;

import com.better.community.dao.AlphaDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * @param
 * @author Bubble
 * @description
 * @return
 * @Date 2022/5/2
 */
@Service
public class AlphaService {
    public AlphaService() {
        System.out.println("执行了构造器！AlphaService对象创建成功！");
    }

    //Spring自动注入Dao层对象
    @Autowired
    @Qualifier("alphaDaoOracleImpl")
    private AlphaDao alphaDao;

    //执行完构造器后执行该初始化方法（Tomcat自动调用的）
    @PostConstruct
    private void init() {
        System.out.println("AlphaService在执行完构造器之后自动执行了初始化方法!");
    }

    public String select() {
        return alphaDao.select();
    }

    //在对象销毁之前执行该方法（Tomcat自动调用）
    @PreDestroy
    private void destroy() {
        System.out.println("AlphaService在对象销毁之前执行了此方法！");
    }
}
