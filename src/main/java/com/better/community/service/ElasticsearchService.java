package com.better.community.service;

import com.alibaba.fastjson.JSONObject;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @Date 7/21/2022
 */
@Service
public class ElasticsearchService {

    @Autowired
    private DiscussPostRepository discussRepository;
    @Autowired
    private RestHighLevelClient restHighLevelClient;

    //记录总共命中的数量(做分页查询的时候记录下来)
    private int totalHitCount;

    public void saveDiscussPost(DiscussPost post) {
        discussRepository.save(post);
    }

    public void deleteDiscussPost(int id) {
        discussRepository.deleteById(id);
    }

    public List<DiscussPost> searchDiscussPost(String keyword, int current, int limit) throws IOException {
        SearchRequest searchRequest = new SearchRequest("discusspost");

        searchRequest.source(
                new SearchSourceBuilder()
                        //查询信息和查询字段
                        .query(QueryBuilders.multiMatchQuery(keyword, "title", "content"))
                        //排序， type > score > createTime  DESC 表倒序
                        .sort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                        .sort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                        .sort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                        .from(current)    //指定从那条开始查询
                        .size(limit)   // 查询条数     //这两个就是分页查询
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
            return null;
        }
        //获取命中结果总数
        totalHitCount = (int) searchResponse.getHits().getTotalHits().value;
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
            list.add(discussPost);
        }

        return list;
    }

    public int getTotalHitCountOfLastSearch() {
        return this.totalHitCount;
    }

}
