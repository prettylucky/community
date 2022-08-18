package com.better.community.util;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;
import sun.security.provider.MD5;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

/**
 * @param
 * @author Bubble
 * @description
 * @return
 * @Date 2022/5/8
 */
public class CommunityUtil {
    // 生成随机字符串
    public static String generateUUID() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }
    // MD5加密
    public static String md5(String key){
        //判断空串
        if (key == null || StringUtils.isBlank(key))
            return "";

        return DigestUtils.md5DigestAsHex(key.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 根据提供数据返回封装好的JSON字符串
     * 使用 fastjson 实现，用于AJAX异步通信
     */
    public static String getJSONString(int code, String msg, Map<String, Object> map) {
        JSONObject json = new JSONObject();
        json.put("code", code);
        json.put("msg", msg);
        //把map打散再加入json对象
        if (map != null) {
            for(String key : map.keySet()) {
                json.put(key, map.get(key));
            }
        }
        //JSON对象转换为JSON字符串，并返回
        return json.toString();
    }

    public static String getJSONString(int code, String msg) {
        return getJSONString(code, msg, null);
    }

    public static String getJSONString(int code) {
        return getJSONString(code, null, null);
    }
}
