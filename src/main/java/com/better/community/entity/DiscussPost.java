package com.better.community.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Date;
import java.util.Objects;

/**
 * @Date 2022/5/4
 */
// Document文档，相当于关系型数据库中的行，此处把该实体类映射es中的一个document
// index 索引 --》 表
// field 字段 --> 字段
// shards 分片 --> 把一个表分成多片，提高并发效率
// replicas 副本 --> 一个分片的备份
@Document(indexName = "discusspost", shards = 6, replicas = 3)
public class DiscussPost {
    //主键字段
    @Id
    private int id;

    //普通整数字段
    @Field(type = FieldType.Integer)
    private int userId;

    // 在搜索中，主要就是搜索title和content中的内容，需要额外添加分词器 analyzer 和 搜索分词器 searchAnalyzer
    // analyzer：在存储的时候使用的分词器，应该选尽可能拆分出多个可能的词来建立索引，扩大存储范围
    //  如：互联网招聘 --> 互联网 招聘 互联 联网
    // searchAnalyzer：在搜索的时候使用的分词器，就没必要分出那么多的词，只需要提取重点
    // 如：互联网招聘 --> 互联网 招聘
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String title;
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String content;

    // 以下为普通字段
    @Field(type = FieldType.Integer)
    private int type;
    @Field(type = FieldType.Integer)
    private int status;
    @Field(type = FieldType.Date)
    private Date createTime;
    @Field(type = FieldType.Integer)
    private int commentCount;
    @Field(type = FieldType.Double)
    private double score;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return "discussPost{" +
                "id=" + id +
                ", userId='" + userId + '\'' +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", type=" + type +
                ", status=" + status +
                ", createTime=" + createTime +
                ", commentCount=" + commentCount +
                ", score=" + score +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DiscussPost that = (DiscussPost) o;
        return id == that.id && type == that.type && status == that.status && commentCount == that.commentCount && Double.compare(that.score, score) == 0 && Objects.equals(userId, that.userId) && Objects.equals(title, that.title) && Objects.equals(content, that.content) && Objects.equals(createTime, that.createTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId, title, content, type, status, createTime, commentCount, score);
    }
}
