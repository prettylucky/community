package com.better.community.dao.elasticsearch;

import com.better.community.entity.DiscussPost;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 * @Date 7/21/2022
 */
// @Repository 声明这是一个数据访问层的组件
// ElasticsearchRepository<实体类型, 主键类型>  这个接口中声明了有关操作es增删改查的相关操作。
// 想要操作es，有两个api，一个是继承ElasticsearchRepository接口，一个是使用 ElasticTemplate 组件
// 前者更方便，常用。后者功能更强大，也要会使用

@Repository
public interface DiscussPostRepository extends ElasticsearchRepository<DiscussPost, Integer> {


}
