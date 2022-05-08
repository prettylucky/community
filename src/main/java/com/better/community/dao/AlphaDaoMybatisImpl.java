package com.better.community.dao;

import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;

/**
 * @param
 * @author Bubble
 * @description
 * @return
 * @Date 2022/5/2
 */
@Repository
@Primary
@Scope("singleton")
public class AlphaDaoMybatisImpl implements AlphaDao{
    @Override
    public String select() {
        return "MyBatis";
    }
}
