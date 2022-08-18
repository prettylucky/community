package com.better.community.actuator;

import com.better.community.util.CommunityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 自定义 spring actuator 端点
 * @Date 7/24/2022
 */
@Component
@Endpoint(id = "database")
public class DatabaseEndpoint {
    public static final Logger logger = LoggerFactory.getLogger(DatabaseEndpoint.class);

    @Autowired
    private DataSource dataSource;

    @ReadOperation      //表示该端点只能用get请求访问
    public String checkConnection() {
        try (
                Connection conn = dataSource.getConnection();
        ){
            return CommunityUtil.getJSONString(0, "获取数据库连接成功！");
        } catch (SQLException e){
            logger.error("获取连接失败：" + e.getMessage());
            return CommunityUtil.getJSONString(1, "获取数据库连接失败！");
        }
    }
}
