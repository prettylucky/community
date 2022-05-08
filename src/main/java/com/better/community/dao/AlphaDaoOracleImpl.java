package com.better.community.dao;

import org.springframework.stereotype.Repository;

/**
 * @param
 * @author Bubble
 * @description
 * @return
 * @Date 2022/5/2
 */
@Repository
public class AlphaDaoOracleImpl implements AlphaDao{
    @Override
    public String select() {
        return "Oracle";
    }
}
