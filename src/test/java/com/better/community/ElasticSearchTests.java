package com.better.community;

import com.alibaba.fastjson.JSONObject;
import com.better.community.dao.DiscussPostMapper;
import com.better.community.dao.elasticsearch.DiscussPostRepository;
import com.better.community.entity.DiscussPost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.test.context.ContextConfiguration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @Date 7/21/2022
 */

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class ElasticSearchTests {
    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private DiscussPostRepository discussRepository;

    @Autowired
    private ElasticsearchRestTemplate restTemplate;

    // es7之后，elasticSearchTemplate逐渐被废弃了，官网推荐使用 RestHighLevelClient进行搜索。
    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Test
    public void testInsert() {
        //save方法，插入一条文档
        discussRepository.save(discussPostMapper.selectDiscussPostById(241));
        discussRepository.save(discussPostMapper.selectDiscussPostById(242));
        discussRepository.save(discussPostMapper.selectDiscussPostById(243));
    }

    @Test
    public void testInsertList() {
        //saveAll方法，插入多条文档
        discussRepository.saveAll(discussPostMapper.selectPosts(101, 0, 100, 0));
        discussRepository.saveAll(discussPostMapper.selectPosts(102, 0, 100, 0));
        discussRepository.saveAll(discussPostMapper.selectPosts(103, 0, 100, 0));
        discussRepository.saveAll(discussPostMapper.selectPosts(111, 0, 100, 0));
        discussRepository.saveAll(discussPostMapper.selectPosts(112, 0, 100, 0));
        discussRepository.saveAll(discussPostMapper.selectPosts(131, 0, 100, 0));
        discussRepository.saveAll(discussPostMapper.selectPosts(133, 0, 100, 0));
        discussRepository.saveAll(discussPostMapper.selectPosts(132, 0, 100, 0));
        discussRepository.saveAll(discussPostMapper.selectPosts(134, 0, 100, 0));
    }

    @Test
    public void testUpdate() {
        DiscussPost post = discussPostMapper.selectDiscussPostById(231);
        post.setContent("我是新人，使用es修改了这条内容");

        //使用save方法，覆盖原来的数据，就是修改
        discussRepository.save(post);

    }

    @Test
    public void testDelete() {
        //删掉一条数据
        discussRepository.deleteById(231);
        discussRepository.deleteAll();  //可以删掉所有的数据，危险操作，警告！
    }

    @Test
    public void testSearch() throws IOException {


//
//        //高亮
//        HighlightBuilder highlightBuilder = new HighlightBuilder()
//                .field("title")
//                .field("content")
//                .requireFieldMatch(false)
//                .preTags("<em>")
//                .postTags("</em>");
//
//        //构建搜索条件
//        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
//                .query(QueryBuilders.multiMatchQuery("互联网寒冬", "title", "content"))
//                .sort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
//                .sort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
//                .sort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
//                .from(0)
//                .size(10)
//                .highlighter(highlightBuilder);
        SearchRequest searchRequest = new SearchRequest("discusspost");

        searchRequest.source(
                new SearchSourceBuilder()
                        //查询信息和查询字段
                        .query(QueryBuilders.multiMatchQuery("互联网寒冬", "title", "content"))
                        //排序， type > score > createTime  DESC 表倒序
                        .sort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                        .sort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                        .sort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                        .from(0)    //指定从那条开始查询
                        .size(10)   // 查询条数     //这两个就是分页查询
                        //搜索结果高亮 在哪些字段中高亮，拼接的前后标签
                        .highlighter(new HighlightBuilder()
                                .field("title")
                                .field("content")
                                .requireFieldMatch(false)
                                .preTags("<em>")
                                .postTags("</em>")
                        )
        );
        //真正的搜索方法
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        //获取所有命中
        SearchHit[] hits = searchResponse.getHits().getHits();
        if (hits.length <= 0) {     //所有结果条数
            return;
        }
        System.out.println(searchResponse.getHits().getTotalHits()+"===============================");
        //处理查询结果
        List<DiscussPost> list = new ArrayList<>();
        for (SearchHit hit : hits) {
            //解析JOSN为disscussPost对象(不包含高亮信息)
            DiscussPost discussPost = JSONObject.parseObject(hit.getSourceAsString(), DiscussPost.class);

            //处理高亮显示结果
            HighlightField titleField = hit.getHighlightFields().get("title");
            if (titleField != null) {
                discussPost.setTitle(titleField.getFragments()[0].toString());
            }
            HighlightField contentField = hit.getHighlightFields().get("content");
            if (contentField != null) {
                discussPost.setContent(contentField.getFragments()[0].toString());
            }
            System.out.println(discussPost);
            list.add(discussPost);
        }


    }
}
