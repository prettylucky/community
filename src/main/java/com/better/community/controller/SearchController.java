package com.better.community.controller;

import com.better.community.entity.DiscussPost;
import com.better.community.entity.Page;
import com.better.community.service.DiscussPostService;
import com.better.community.service.ElasticsearchService;
import com.better.community.service.LikeService;
import com.better.community.service.UserService;
import com.better.community.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 处理搜索相关请求，使用es进行搜索
 * @Date 7/21/2022
 */
@Controller
public class SearchController implements CommunityConstant {
    @Autowired
    private ElasticsearchService elasticsearchService;
    @Autowired
    private UserService userService;
    @Autowired
    private LikeService likeService;

    @GetMapping("/search")
    public String search(String keyword, Page page, Model model) throws IOException {

        //处理分页相关信息
        page.setLimit(10);
        page.setPath("/search?keyword=" + keyword);

        List<DiscussPost> postList = elasticsearchService.searchDiscussPost(keyword, page.getCurrent() - 1, page.getLimit());

        //封装前端所需数据：帖子，作者信息，几个点赞，几条回复(帖子中有)，发布事件（帖子中有）
        ArrayList<Map<String, Object>> posts = new ArrayList<Map<String, Object>>();
        if (postList != null) {
            page.setRows(elasticsearchService.getTotalHitCountOfLastSearch());
            for (DiscussPost post : postList) {
                HashMap<String, Object> postVO = new HashMap<>();
                postVO.put("post", post);
                postVO.put("user", userService.findUserById(post.getUserId()));
                postVO.put("likeCount", likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId()));
                posts.add(postVO);
            }
        }

        model.addAttribute("posts", posts);
        model.addAttribute("keyword", keyword);

        return "site/search";
    }
}
