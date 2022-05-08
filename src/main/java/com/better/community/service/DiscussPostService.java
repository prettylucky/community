package com.better.community.service;

import com.better.community.dao.DiscussPostMapper;
import com.better.community.entity.DiscussPost;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @param
 * @author Bubble
 * @description
 * @return
 * @Date 2022/5/4
 */
@Service
public class DiscussPostService {
    @Autowired
    private DiscussPostMapper discussPostMapper;

    public List<DiscussPost> findPosts(int userId, int offset, int limit){
        return discussPostMapper.selectPosts(userId, offset, limit);
    }

    public int findRows(int userId){
        return discussPostMapper.selectRows(userId);
    }
}
