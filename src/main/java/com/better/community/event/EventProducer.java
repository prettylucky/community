package com.better.community.event;

import com.alibaba.fastjson.JSONObject;
import com.better.community.entity.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * 事件生产者，使用kafkaTemplate.send(event)产生事件并加入到阻塞队列
 * @Date 7/19/2022
 */
@Component
public class EventProducer {
    @Autowired
    private KafkaTemplate kafkaTemplate;
    //触发事件，向指定事件发送消息
    public void fireEvent(Event event) {
        kafkaTemplate.send(event.getTopic(), JSONObject.toJSONString(event));
    }

}
