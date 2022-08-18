package com.better.community;

import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * 使用阻塞队列实现生产者-消费者模型
 * 印连续出现三次（生产者2次，消费者1次）还剩10的原因?
 * 打一开始生产者生产到10个并打印；
 * 然后消费者消费完（剩9个）但执行输出语句的时候切换了线程，生产者又生产了一个到10个且打印了出来；
 * 然后切换会消费者，执行未执行的输出语句（并未消费），导致又输出一次10个。
 * 想要解决就要加锁保证线程安全。
 * BlockingQueue是线程安全的，但是我们写那几行代码并没有保证线程安全。）
 *
 * @Date 2022/7/18
 */
public class BlockingQueueTests {
    public static void main(String[] args) {
        BlockingQueue<Integer> queue = new ArrayBlockingQueue<>(10);

        new Thread(new Producer(queue)).start();
        new Thread(new Consumer(queue)).start();
        new Thread(new Consumer(queue)).start();
        new Thread(new Consumer(queue)).start();
        new Thread(new Consumer(queue)).start();
    }
}

class Producer implements Runnable {

    private BlockingQueue<Integer> queue;

    public Producer(BlockingQueue<Integer> queue) {
        this.queue = queue;
    }

    @Override
    public void run() {
        try {
            for (int i = 0; i < 100; i++) {
                Thread.sleep(50);
                queue.put(i);
                System.out.println(Thread.currentThread().getName() + "生产，队列剩余：" + queue.size());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class Consumer implements Runnable {
    private BlockingQueue<Integer> queue;

    public Consumer(BlockingQueue<Integer> queue) {
        this.queue = queue;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(new Random().nextInt(500));
                Integer take = queue.take();
                if (take != null) {
                    System.out.println(Thread.currentThread().getName() + "消费了，队列剩余：" + queue.size());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
