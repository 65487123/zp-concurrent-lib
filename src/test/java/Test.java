import com.lzp.util.concurrent.blockingQueue.nolock.OneToOneBlockingQueue;
import com.lzp.util.concurrent.latch.CountDownLatch;
import com.lzp.util.concurrent.list.ConcurrentArrayList;
import com.lzp.util.concurrent.map.NoResizeConHashMap;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Description:test
 *
 * @author: Zeping Lu
 * @date: 2020/8/9 11:46
 */
public class Test {
    static volatile int a = ThreadLocalRandom.current().nextInt(10000);
    static volatile int b = ThreadLocalRandom.current().nextInt(10000);
    static AtomicInteger c = new AtomicInteger();
    static Object[] d = new Object[ThreadLocalRandom.current().nextInt(10000)];
    static int r = d.length;

    public static void main(String[] args) throws InterruptedException, ExecutionException, TimeoutException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, IOException, ClassNotFoundException {
        //Map<String,String> map = new ConcurrentHashMap(15000000);
        CountDownLatch countDownLatch = new CountDownLatch(2000000);
        List list = new ConcurrentArrayList();

        long now  = System.currentTimeMillis();
        new Thread(() -> {
            for (int i = 0; i < 200000; i++) {
                list.add(String.valueOf(i));
                countDownLatch.countDown();
            }
        }).start();
        new Thread(() -> {
            for (int i = 200000; i < 400000; i++) {
                list.add(String.valueOf(i));
                countDownLatch.countDown();
            }
        }).start();
        new Thread(() -> {
            for (int i = 400000; i < 600000; i++) {
                list.add(String.valueOf(i));
                countDownLatch.countDown();
            }
        }).start();
        new Thread(() -> {
            for (int i = 600000; i < 800000; i++) {
                list.add(String.valueOf(i));
                countDownLatch.countDown();
            }
        }).start();
        new Thread(() -> {
            for (int i = 800000; i < 1000000; i++) {
                list.add(String.valueOf(i));
                countDownLatch.countDown();
            }
        }).start();
        new Thread(() -> {
            for (int i = 0; i < 500000; i++) {
                list.get(i);
                countDownLatch.countDown();
            }
        }).start();
        new Thread(() -> {
            for (int i = 0; i < 500000; i++) {
                list.get(i);
                countDownLatch.countDown();
            }
        }).start();
        countDownLatch.await();
        System.out.println(System.currentTimeMillis() - now);

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
