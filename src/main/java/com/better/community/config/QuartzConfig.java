package com.better.community.config;

import com.better.community.quartz.AlphaJob;
import com.better.community.quartz.PostScoreRefreshJob;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;

/**
 * @Date 7/23/2022
 */
// 配置 -> 数据库 -> 调用
@Configuration
public class QuartzConfig {


    // ==================================Quartz配置实例==============================================
    // BeanFactory：Spring容器的顶级接口
    // FactoryBean可简化Bean的实例化过程：
    // 1.通过FactoryBean封装了Bean的实例化过程
    // 2.将FactoryBean装配到Spring容器中
    // 3.将FactoryBean注入给其他的Bean
    // 4.该Bean得到的是FactoryBean所管理的对象实例。

    // JobDetail：配置Job的信息：Job对应类，Job名字，Job分组..
    //@Bean
    public JobDetailFactoryBean alphaJobDetail() {
        JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
        factoryBean.setJobClass(AlphaJob.class);
        factoryBean.setName("alphaJob");
        factoryBean.setGroup("alphaJobGroup");
        factoryBean.setDurability(true);    //声明任务是持久的保存
        factoryBean.setRequestsRecovery(true);  //声明Job是可恢复的
        return factoryBean;
    }

    // Trigger：配置Job的触发信息
    //@Bean
    public SimpleTriggerFactoryBean alphaTrigger(JobDetail alphaJobDetail) {
        SimpleTriggerFactoryBean factoryBean = new SimpleTriggerFactoryBean();
        factoryBean.setJobDetail(alphaJobDetail);
        factoryBean.setName("alphaTrigger");
        factoryBean.setGroup("alphaTriggerGroup");
        factoryBean.setRepeatInterval(3000);    //每3000ms执行一次
        factoryBean.setJobDataMap(new JobDataMap());    //存储Job的数据，使用默认的实现类

        return factoryBean;
    }


    //=============================PostScoreRefreshJob 刷新帖子分数任务========================================
    @Bean
    public JobDetailFactoryBean postScoreRefreshJobDetail() {
        JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
        factoryBean.setJobClass(PostScoreRefreshJob.class);
        factoryBean.setName("postScoreRefreshJob");
        factoryBean.setGroup("communityJobGroup");
        factoryBean.setDurability(true);    //声明任务是持久的保存
        factoryBean.setRequestsRecovery(true);  //声明Job是可恢复的
        return factoryBean;
    }
    // Trigger：配置Job的触发信息
    @Bean
    public SimpleTriggerFactoryBean postScoreRefreshTrigger(JobDetail postScoreRefreshJobDetail) {
        SimpleTriggerFactoryBean factoryBean = new SimpleTriggerFactoryBean();
        factoryBean.setJobDetail(postScoreRefreshJobDetail);
        factoryBean.setName("postScoreRefreshTrigger");
        factoryBean.setGroup("communityTriggerGroup");
        factoryBean.setRepeatInterval(1000 * 60 * 5);    //每5分钟执行一次
        factoryBean.setJobDataMap(new JobDataMap());    //存储Job的数据，使用默认的实现类

        return factoryBean;
    }
}
