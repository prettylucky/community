package com.better.community;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.*;
import org.springframework.test.context.ContextConfiguration;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * @param
 * @author Bubble
 * @description
 * @return
 * @Date 2022/7/16
 */
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class RedisTest {

    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    public void testStrings() {
        String redisKey = "test:count";

        redisTemplate.opsForValue().set(redisKey, 1);

        System.out.println(redisTemplate.opsForValue().get(redisKey));
    }

    @Test
    public void testHash() {
        String redisKey = "test:hash";

        redisTemplate.opsForHash().put(redisKey, "user:01", "tom");
        redisTemplate.opsForHash().put(redisKey, "user:02", "Jack");
        System.out.println(redisTemplate.opsForHash().get(redisKey, "user:01"));
        System.out.println(redisTemplate.opsForHash().get(redisKey, "user:02"));
    }

    @Test
    public void testList() {
        String redisKey = "test:list";
        //从左侧添加
        redisTemplate.opsForList().leftPush(redisKey, 100);
        redisTemplate.opsForList().leftPush(redisKey, 101);
        redisTemplate.opsForList().leftPush(redisKey, 102);

        //查询有几个数据
        System.out.println(redisTemplate.opsForList().size(redisKey));
        //查询 0-2 处数据
        System.out.println(redisTemplate.opsForList().range(redisKey,0, 2));
        //查询指定索引处的value
        System.out.println(redisTemplate.opsForList().index(redisKey, 0));
        //从右侧弹出
        System.out.println(redisTemplate.opsForList().rightPop(redisKey));
        //再次查询数量
        System.out.println(redisTemplate.opsForList().size(redisKey));

    }

    @Test
    public void testSet() {
        String redisKey = "test:set";

        redisTemplate.opsForSet().add(redisKey, "刘备", "关羽", "张飞", "赵云", "黄忠");

        System.out.println(redisTemplate.opsForSet().size(redisKey));
        System.out.println(redisTemplate.opsForSet().members(redisKey).toString());
        //随机弹出一个数据
        System.out.println(redisTemplate.opsForSet().pop(redisKey));
        //指定删除一个数据
        System.out.println(redisTemplate.opsForSet().remove(redisKey,"刘备"));
    }

    @Test
    public void testZSet() {
        String redisKey = "test:zset";

        redisTemplate.opsForZSet().add(redisKey, "唐僧", 100);
        redisTemplate.opsForZSet().add(redisKey, "猴子", 99);
        redisTemplate.opsForZSet().add(redisKey, "八戒", 59);
        redisTemplate.opsForZSet().add(redisKey, "沙僧", 80);

        //查看有元素数量
        System.out.println(redisTemplate.opsForZSet().zCard(redisKey));
        //默认从小到大排序
        System.out.println(redisTemplate.opsForZSet().range(redisKey, 0, 4));
        System.out.println(redisTemplate.opsForZSet().reverseRange(redisKey, 0, 4));
        //默认从小到大排序
        System.out.println(redisTemplate.opsForZSet().rank(redisKey, "八戒"));
    }

    @Test
    public void testKey() throws InterruptedException {
        //查询所有key
        System.out.println(redisTemplate.keys("*"));
        //删除一个key
        System.out.println(redisTemplate.delete("test:list"));
        //查询指定key是否存在
        System.out.println(redisTemplate.hasKey("test:list"));
        redisTemplate.opsForList().leftPush("test:expire", 1);
        //让一个key定时过期
        redisTemplate.expire("test:expire", 10, TimeUnit.SECONDS);
        //查询指定key是否存在
        System.out.println(redisTemplate.hasKey("test:expire"));
        //延时10s
        Thread.sleep(10000);
        System.out.println("延时10s");
        System.out.println(redisTemplate.hasKey("test:expire"));
    }

    //多次访问同一个key
    //可以通过template.boundXxxOps(String key)方法生成一个绑定key的BoundValueOperations的绑定对象
    //通过这个对象做出的操作都是针对这个key的
    @Test
    public void testBoundOperations() {
        BoundValueOperations operations = redisTemplate.boundValueOps("test:count");
        operations.increment();
        operations.increment();
        operations.increment();
        System.out.println(operations.get());
    }

    //redis事务
    //redis的事务机制并不满足ACID原则。
    //redis的事务机制：事务开始后，每执行一条命令都会把该命令放到一个队列中，等到事务提交的时候一起把所有命令发送给redis
    //所以：在redis事务中不要执行查询语句（不会有结果）。要么在事务之前查，要么在事务之后查。
    //redis在spring中同样是支持，声明式事务和编程式事务
    //对于redis事务中不能查询这个缺点，所有编程式事务更常用
    //因为声明式事务一下把整个方法都当做事务范围，我们就不能在这个方法中进行查询了，故使用编程式事务。

    //编程式事务
    @Test
    public void testTransactional() {
        Object obj = redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String redisKey = "test:tx";
                //开启事务
                operations.multi();

                operations.opsForSet().add(redisKey, "zhangSan");
                operations.opsForSet().add(redisKey, "LiSi");
                operations.opsForSet().add(redisKey, "WangWu");

                //测试事务中不能查询
                System.out.println(operations.opsForSet().members(redisKey));

                //提交事务
                return operations.exec();
            }
        });

        System.out.println(obj);
    }

    // HyperLogLog
    // 统计20万个重复数据的独立总数
    @Test
    public void testHyperLogLog() {
        String redisKey = "test:hll:01";

        for (int i = 1; i <= 100000; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey, i);
        }

        for (int i = 1; i <= 100000; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey, (int) (Math.random() * 100000 + 1));
        }

        System.out.println(redisTemplate.opsForHyperLogLog().size(redisKey));
    }

    // 将3组数据合并，统计合并后的独立总数
    @Test
    public void testHyperLogLogUnion() {
        String redisKey = "test:hll:2";
        for (int i = 1; i <= 10000; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey, i);
        }

        String redisKey1 = "test:hll:2";
        for (int i = 5001; i <= 15000; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey1, i);
        }

        String redisKey2 = "test:hll:2";
        for (int i = 10001; i <= 20000; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey2, i);
        }

        // 合并三组数据，并去重
        String unionKey = "test:hll:union";
        redisTemplate.opsForHyperLogLog().union(unionKey, redisKey, redisKey1, redisKey2);

        System.out.println(redisTemplate.opsForHyperLogLog().size(unionKey));
    }

    // bitMap
    // bitMap就是一个特殊的String，只是按位存储，只能存储 0 和 1
    // 统计一组数据的布尔值
    @Test
    public void testBitMap() {
        String redisKey = "test:bm:01";

        redisTemplate.opsForValue().setBit(redisKey, 1, true);
        redisTemplate.opsForValue().setBit(redisKey, 4, true);
        redisTemplate.opsForValue().setBit(redisKey, 7, true);

        //查询
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 0));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 1));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 2));

        System.out.println("======================");

        // 统计总共有多少个true
        System.out.println(redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                return connection.bitCount(redisKey.getBytes());
            }
        }));
    }

    // 统计3组数据的布尔值，并对三组数据做OR运算
    @Test
    public void testBitMapOperation() {
        String redisKey = "test:bm:02";
        redisTemplate.opsForValue().setBit(redisKey, 0, true);
        redisTemplate.opsForValue().setBit(redisKey, 1, true);
        redisTemplate.opsForValue().setBit(redisKey, 2, true);

        String redisKey2 = "test:bm:03";
        redisTemplate.opsForValue().setBit(redisKey2, 2, true);
        redisTemplate.opsForValue().setBit(redisKey2, 3, true);
        redisTemplate.opsForValue().setBit(redisKey2, 4, true);

        String redisKey3 = "test:bm:04";
        redisTemplate.opsForValue().setBit(redisKey3, 4, true);
        redisTemplate.opsForValue().setBit(redisKey3, 5, true);
        redisTemplate.opsForValue().setBit(redisKey3, 6, true);

        String redisKeyOr = "test:bm:or";
        Object obj = redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                connection.bitOp(RedisStringCommands.BitOperation.OR, redisKeyOr.getBytes(StandardCharsets.UTF_8), redisKey.getBytes(), redisKey2.getBytes(), redisKey3.getBytes());
                return connection.bitCount(redisKeyOr.getBytes(StandardCharsets.UTF_8));
            }
        });

        System.out.println(obj.toString());
        System.out.println(redisTemplate.opsForValue().getBit(redisKeyOr,0));
        System.out.println(redisTemplate.opsForValue().getBit(redisKeyOr,1));
        System.out.println(redisTemplate.opsForValue().getBit(redisKeyOr,2));
        System.out.println(redisTemplate.opsForValue().getBit(redisKeyOr,3));
        System.out.println(redisTemplate.opsForValue().getBit(redisKeyOr,4));
        System.out.println(redisTemplate.opsForValue().getBit(redisKeyOr,5));
        System.out.println(redisTemplate.opsForValue().getBit(redisKeyOr,6));
    }

}
