package com.better.community.util;

/**
 * @param
 * @Date 2022/7/16
 */
public class RedisKeyUtil {
    //Redis采用 : 分割每个单词
    private static final String SPLIT = ":";
    //实体类赞前缀
    private static final String PREFIX_ENTITY_LIKE = "like:entity";
    //每个用户的赞前缀
    private static final String PREFIX_USER_LIKE = "like:user";
    //某个用户关注的实体（可以是其他用户或者帖子...） 称为：followee
    private static final String PREFIX_FOLLOWEE = "followee";
    //某个实体（用户、帖子）的粉丝（一定是一个用户） 成为: follower
    private static final String PREFIX_FOLLOWER = "follower";
    //存储验证码 验证码访问频率高，且原来存在session在分布式部署的时候存在Session共享问题，现重构到redis中存储
    private static final String PREFIX_KAPTCHA = "kaptcha";
    //存储LoginTicket的key
    private static final String PREFIX_TICKET = "ticket";
    //存储用户信息的key
    private static final String PREFIX_USER = "user";
    // UV 独立访客
    private static final String PREFIX_UV = "uv";
    // DAU 日活用户
    private static final String PREFIX_DAU = "dau";
    // 帖子
    private static final String PREFIX_POST = "post";

    //生成某个实体赞对应的id
    //示例： like:entity:1:12
    public static String getEntityLikeKey(int entityType, int entityId) {
        return PREFIX_ENTITY_LIKE + SPLIT + entityId + SPLIT + entityId;
    }

    //生成某个用户得到的所有赞的key（通过用户拥有所有实体的赞加一块也能算，但是效率太低）
    // like:user:111
    public static String getUserLikeKey(int userId) {
        return PREFIX_USER_LIKE + SPLIT + userId;
    }

    //生成某个用户的关注实体的key （followee使用zset存储，score字段为当前时间，这样方便后续通过时间排序）
    // followee:userId:entityType --> zset(entityId, nowTime)
    public static String getUserFolloweeKey(int userId ,int entityType) {
        return PREFIX_FOLLOWEE + SPLIT + userId + SPLIT + entityType;
    }

    //生成某个实体的粉丝
    //follower:entityType:entityId --> zset(userId, nowTime)
    public static String getEntityFollowerKey(int entityType, int entityId) {
        return PREFIX_FOLLOWER + SPLIT + entityType + SPLIT + entityId;
    }

    //生成某个验证码的key
    //由于用户没有登录还需要识别这个用户，需要使用cookie给用户颁发一个临时凭证：owner
    public static String getKaptchaKey(String owner) {
        return PREFIX_KAPTCHA + SPLIT + owner;
    }

    //生成loginTicket的key
    public static String getTicketKey(String ticket) {
        return PREFIX_TICKET + SPLIT + ticket;
    }

    //生成user的key
    public static String getUserKey(int userId) {
        return PREFIX_USER + SPLIT + userId;
    }

    // 单日UV 每天的独立访客
    public static String getUVKey(String date) {
        return PREFIX_UV + SPLIT + date;
    }

    // 区间UV，从某天到某天的UV
    public static String getUVKey(String startDate, String endDate) {
        return PREFIX_UV + SPLIT + startDate + SPLIT + endDate;
    }

    // 单日DAU
    public static String getDAUKey(String date) {
        return PREFIX_DAU + SPLIT + date;
    }
    // 区间 活跃用户
    public static String getDAUKey(String startDate, String endDate) {
        return PREFIX_DAU + SPLIT + startDate + SPLIT + endDate;
    }

    //帖子分数的key
    public static String getPostScoreKey() {
        return PREFIX_POST + SPLIT + "score";
    }
}
