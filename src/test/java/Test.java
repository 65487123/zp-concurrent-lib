import com.lzp.util.concurrent.blockingQueue.nolock.OneToOneBlockingQueue;
import com.lzp.util.concurrent.map.NoResizeConHashMap;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Random;
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
        CountDownLatch countDownLatch = new CountDownLatch(12000000);
        //Map<String,String> map = new ConcurrentHashMap(15000000);
        Map<String,String> map = new NoResizeConHashMap(15000000);
        long now = System.currentTimeMillis();

        CountDownLatch finalCountDownLatch6 = countDownLatch;
        new Thread(() -> {
            for (int i = 0; i < 2000000; i++) {
                map.put(String.valueOf(i),String.valueOf(i));
                finalCountDownLatch6.countDown();
            }
        }).start();
        new Thread(() -> {
            for (int i = 2000000; i < 4000000; i++) {
                map.put(String.valueOf(i),String.valueOf(i));
                finalCountDownLatch6.countDown();
            }
        }).start();
        new Thread(() -> {
            for (int i = 4000000; i < 6000000; i++) {
                map.put(String.valueOf(i),String.valueOf(i));
                finalCountDownLatch6.countDown();
            }
        }).start();
        new Thread(() -> {
            for (int i = 6000000; i < 8000000; i++) {
                map.put(String.valueOf(i),String.valueOf(i));
                finalCountDownLatch6.countDown();
            }
        }).start();
        new Thread(() -> {
            for (int i = 8000000; i < 10000000; i++) {
                map.put(String.valueOf(i),String.valueOf(i));
                finalCountDownLatch6.countDown();
            }
        }).start();
        new Thread(() -> {
            for (int i = 10000000; i < 12000000; i++) {
                map.put(String.valueOf(i),String.valueOf(i));
                finalCountDownLatch6.countDown();
            }
        }).start();
        countDownLatch.await();
        System.out.println(System.currentTimeMillis() - now);
        now = System.currentTimeMillis();
        for (Map.Entry entry : map.entrySet()) {
        }

        System.out.println(System.currentTimeMillis() - now);

        countDownLatch = new CountDownLatch(12000000);
        now = System.currentTimeMillis();

        CountDownLatch finalCountDownLatch = countDownLatch;
        new Thread(() -> {
            for (int i = 0; i < 3000000; i++) {
                map.remove(String.valueOf(i));
                finalCountDownLatch.countDown();
            }
        }).start();
        new Thread(() -> {
            for (int i = 3000000; i < 6000000; i++) {
                map.remove(String.valueOf(i));
                finalCountDownLatch.countDown();
            }
        }).start();
        new Thread(() -> {
            for (int i = 6000000; i < 9000000; i++) {
                map.remove(String.valueOf(i));
                finalCountDownLatch.countDown();
            }
        }).start();
        new Thread(() -> {
            for (int i = 9000000; i < 12000000; i++) {
                map.remove(String.valueOf(i));
                finalCountDownLatch.countDown();
            }
        }).start();
        countDownLatch.await();


/*

        for (String entry : map.keySet()) {
        }
*/

        System.out.println(System.currentTimeMillis() - now);
        System.out.println(map.size());

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
