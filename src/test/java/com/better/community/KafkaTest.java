package com.better.community;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;

/**
 * 测试kafka
 * Producer：需要我们服务器手动使用 kafkaTemplate.send() 发送消息
 * Consumer：只需要使用@KafkaListener注解标注，然后注入到容器中，就会自动监听生产者发送的消息。属于被动接收。
 * @Date 7/19/2022
 */
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class KafkaTest {

    @Autowired
    private KafkaProducer kafkaProducer;

    @Test
    public void testKafka() {

        kafkaProducer.send("test", "hello world");
        kafkaProducer.send("test", "你好");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
@Component
class KafkaProducer {
    @Autowired
    KafkaTemplate kafkaTemplate;

    public void send(String topic, String msg) {
        kafkaTemplate.send(topic, msg);
    }
}
@Component
class KafkaConsumer {
    @KafkaListener(topics = {"test"})
    public void handleMsg(ConsumerRecord record) {
        System.out.println(record.value());
    }
}
