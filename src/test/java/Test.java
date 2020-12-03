import com.lzp.util.concurrent.blockingQueue.nolock.OneToOneBlockingQueue;
import com.lzp.util.concurrent.blockingQueue.withlock.OptimizedArrBlockQueue;
import com.lzp.util.concurrent.latch.CountDownLatch;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Description:test
 *
 * @author: Zeping Lu
 * @date: 2020/8/9 11:46
 */
public class Test {


    public static void main(String[] args) throws InterruptedException, ExecutionException, TimeoutException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, IOException, ClassNotFoundException {
        //Map<String,String> map = new ConcurrentHashMap(15000000);
        CountDownLatch countDownLatch = new CountDownLatch(2000000);
        BlockingQueue<Integer> blockingQueue = new OptimizedArrBlockQueue<>(100);

        AtomicInteger sum = new AtomicInteger();
        long now  = System.currentTimeMillis();
        new Thread(() -> {
            for (int i = 0; i < 250000; i++) {
                try {
                    blockingQueue.put(ThreadLocalRandom.current().nextInt(100));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        new Thread(() -> {
            for (int i = 0; i < 250000; i++) {
                try {
                    blockingQueue.put(ThreadLocalRandom.current().nextInt(100));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();new Thread(() -> {
            for (int i = 0; i < 250000; i++) {
                try {
                    blockingQueue.put(ThreadLocalRandom.current().nextInt(100));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();new Thread(() -> {
            for (int i = 0; i < 250000; i++) {
                try {
                    blockingQueue.put(ThreadLocalRandom.current().nextInt(100));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();new Thread(() -> {
            for (int i = 0; i < 250000; i++) {
                try {
                    blockingQueue.put(ThreadLocalRandom.current().nextInt(100));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();new Thread(() -> {
            for (int i = 0; i < 250000; i++) {
                try {
                    blockingQueue.put(ThreadLocalRandom.current().nextInt(100));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();new Thread(() -> {
            for (int i = 0; i < 250000; i++) {
                try {
                    blockingQueue.put(ThreadLocalRandom.current().nextInt(100));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();new Thread(() -> {
            for (int i = 0; i < 250000; i++) {
                try {
                    blockingQueue.put(ThreadLocalRandom.current().nextInt(100));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        new Thread(() -> {
            for (int i = 0; i < 2000000; i++) {
                try {
                    sum.addAndGet(blockingQueue.take());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                countDownLatch.countDown();
            }
        }).start();

        countDownLatch.await();
        System.out.println(System.currentTimeMillis() - now);
        System.out.println(sum);
        /*now = System.currentTimeMillis();
        for (Map.Entry<String, String> entry : map.entrySet()) {
        }
        System.out.println(System.currentTimeMillis() - now);*/

        /*for (int i = 0; i <100000 ; i++) {
            map.put(String.valueOf(i), String.valueOf(i));
        }

        long now = System.currentTimeMillis();
        map.toString();
        System.out.println(System.currentTimeMillis()-now);*/

    }

    static void put(BlockingQueue arrayBlockingQueue){
        for (int i = 0; i <2000000 ; i++) {
            try {
                arrayBlockingQueue.put(String.valueOf(i));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    static void put(OneToOneBlockingQueue arrayBlockingQueue){
        for (int i = 0; i <800000 ; i++) {
            try {
                arrayBlockingQueue.put(String.valueOf(i));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }




    static int sum() {
        int a = 0;
        for (int i = 0; i < 1; i++) {
            a += i;
        }
        return a;
    }

    static void test() throws InterruptedException {

    }
}
