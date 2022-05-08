package com.better.community;

import com.better.community.dao.UserMapper;
import com.better.community.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeansException;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;

import java.text.SimpleDateFormat;
import java.util.Date;

@SpringBootTest
//引入Spring容器的配置类/配置文件
@ContextConfiguration(classes = CommunityApplication.class)
//实现ApplicationContextAware接口获取Spring容器，自动根据引入的配置文件注入applicationContext到applicationContext属性中。
class CommunityApplicationTests implements ApplicationContextAware {

	private ApplicationContext applicationContext;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	@Test
	public void testConfig() {
		SimpleDateFormat simpleDateFormat = applicationContext.getBean(SimpleDateFormat.class);
		System.out.println(simpleDateFormat.format(new Date()));
	}

	@Test
	public void testMybatis(){
		UserMapper userMapper = applicationContext.getBean("userMapper",UserMapper.class);
		User user = userMapper.selectByUserName("liubei");
		System.out.println(user);
		//主键id由mybatis管理，此处设定的无效。
		User user1 = new User(155,"xh","123456","123456","nowcoder150@sina.com",0,1,"xxx","www.baidu.com",new Date());
//		userMapper.deleteUser(152);
	}
}
