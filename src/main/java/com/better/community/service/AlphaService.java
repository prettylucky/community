package com.better.community.service;

import com.better.community.dao.AlphaDao;
import com.better.community.dao.DiscussPostMapper;
import com.better.community.dao.UserMapper;
import com.better.community.entity.DiscussPost;
import com.better.community.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Date;

/**
 * @param
 * @author Bubble
 * @description
 * @return
 * @Date 2022/5/2
 */
@Service
public class AlphaService {

    private static final Logger logger = LoggerFactory.getLogger(AlphaService.class);

    //Spring自动注入Dao层对象
    @Autowired
    @Qualifier("alphaDaoOracleImpl")
    private AlphaDao alphaDao;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private TransactionTemplate transactionTemplate;

    public AlphaService() {
        System.out.println("执行了构造器！AlphaService对象创建成功！");
    }

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

    /*
    Spring事务分为声明式事务和编程式事务。
    声明式事务：通过@Transactional注解或xml文件配置配置一个方法开启事务管理。
        特点：简单易用，直接为一整个方法配置事务，适用于大部分场景。
    编程式事务：通过IOC容器里的 TransactionTemplate 组件，执行回调方法，回调方法中的业务逻辑就会被事务管理。
        特点：语法稍微复杂，可以为特定的几个步骤绑定事务，适用于一个方法中有很多业务逻辑，但只有其中的几步需要事务管理的场景。

     事务隔离级别（isolation）
        READ_UNCOMMITTED：读未提交，效率最高，但会出现脏读，基本不用。
        READ_COMMITTED：读已提交
        REPEATABLE_READ：可重复读
        SERIALIZABLE：串行，效率最低，基本不用。
     事务传播机制（propagation）
        - REQUIRED:支持当前事务,如果当前没有事务,则新建一个事务,默认使用这种,也是最常见的 。
        - REQUIRES_NEW:新建事务,如果当前存在事务,就把当前事务挂起。
        - NESTED:如果当前事务存在，则执行嵌套事务，否则执行类似REQUIRED的操作.
     */

    //声明式事务
    @Transactional(isolation = Isolation.DEFAULT, propagation = Propagation.REQUIRED)
    public Object save1() {
        //插入一个用户
        User user = new User();
        user.setUsername("TTT");
        user.setPassword("123");
        user.setCreateTime(new Date());     //只是测试用，其他属性就不赋值了

        userMapper.insertUser(user);    //在执行自动生成主键的sql后，mybatis会自动把生成的主键赋给实体类


        //如果声明了事务，必须保证插入用户和插入帖子同时完成或同时失败
        //此处故意抛出一个异常，看是否能保证本事务的原子性
        //如果声明了事务，且遇到了一个异常，就会进行回滚。
        if (true)
            throw new NullPointerException();


        //插入一个帖子
        DiscussPost discussPost = new DiscussPost();
        discussPost.setTitle("新人报道!");
        discussPost.setContent("新人报道!");
        discussPost.setUserId(user.getId());
        discussPost.setCreateTime(new Date());

        discussPostMapper.insertPost(discussPost);



        return "ok";
    }

    //编程式事务，只有在回调函数中的步骤才会被事务管理。
    public Object save2() {
        //配置事务隔离级别和事务传播机制
        transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        //执行事务
        transactionTemplate.execute(new TransactionCallback<Object>() {
            //定义业务步骤
            @Override
            public Object doInTransaction(TransactionStatus status) {
                //插入一个用户
                User user = new User();
                user.setUsername("TTT");
                user.setPassword("123");
                user.setCreateTime(new Date());     //只是测试用，其他属性就不赋值了

                userMapper.insertUser(user);    //在执行自动生成主键的sql后，mybatis会自动把生成的主键赋给实体类


                //如果声明了事务，必须保证插入用户和插入帖子同时完成或同时失败
                //此处故意抛出一个异常，看是否能保证本事务的原子性
                //如果声明了事务，且遇到了一个异常，就会进行回滚。
                if (true)
                    throw new NullPointerException();


                //插入一个帖子
                DiscussPost discussPost = new DiscussPost();
                discussPost.setTitle("新人报道!");
                discussPost.setContent("新人报道!");
                discussPost.setUserId(user.getId());
                discussPost.setCreateTime(new Date());

                discussPostMapper.insertPost(discussPost);
                return "ok";
            }
        });
        return "ok";
    }

    // @Async，让该方法在多线程的环境下，被异步的调用，（需要启用该功能 @EnableAsync,在ThreadPoolConfig类中开启过了）
    // 调用该方法时，会默认以多线程的方式启动
    // 在ThreadPoolTests测试类中演示调用
    @Async
    public void execute1() {
        logger.debug("execute1");
    }

    // 功能与@Async类似，是以固定时间或，固定时间间隔的定时任务的多线程
    // 该方法不需要被调用，只要程序运行就会不断定时自动调用
    //@Scheduled(initialDelay = 10000, fixedDelay = 1000)
    public void execute2() {
        logger.debug("execute2");
    }
}
