import com.lzp.util.concurrent.blockingQueue.nolock.NoLockBlockingQueue;
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


    public static void main(String[] args) throws InterruptedException, ExecutionException, TimeoutException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, IOException, ClassNotFoundException {
        //Map<String,String> map = new ConcurrentHashMap(15000000);
        CountDownLatch countDownLatch = new CountDownLatch(2000000);
        List<String> list = new ConcurrentArrayList();
        for (int i = 0; i < 1000000; i++) {
            list.add(String.valueOf(i));
        }
        long now = System.currentTimeMillis();
        list.toString();
        System.out.println(System.currentTimeMillis()-now);
        /*NoLockBlockingQueue blockingQueue = new NoLockBlockingQueue(1000000,4);
        long now  = System.currentTimeMillis();
        new Thread(() -> {
            for (int i = 0; i < 500000; i++) {
                try {
                    blockingQueue.put(String.valueOf(i),0);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        new Thread(() -> {
            for (int i = 0; i < 500000; i++) {
                try {
                    blockingQueue.put(String.valueOf(i),1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        new Thread(() -> {
            for (int i = 0; i < 500000; i++) {
                try {
                    blockingQueue.put(String.valueOf(i),2);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        new Thread(() -> {
            for (int i = 0; i < 500000; i++) {
                try {
                    blockingQueue.put(String.valueOf(i),3);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        new Thread(() -> {
            for (int i = 0; i < 2000000; i++) {
                try {
                    blockingQueue.take();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                countDownLatch.countDown();
            }
        }).start();

        countDownLatch.await();
        System.out.println(System.currentTimeMillis() - now);*/

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
