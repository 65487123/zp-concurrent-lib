import com.lzp.util.concurrent.blockingQueue.nolock.DependenOneTOneBlocQue;
import com.lzp.util.concurrent.blockingQueue.nolock.NoLockBlockingQueue;
import com.lzp.util.concurrent.blockingQueue.nolock.OneToOneBlockingQueue;
import com.lzp.util.concurrent.blockingQueue.withlock.OptimizedArrBlockQueue;
import com.lzp.util.concurrent.latch.CountDownLatch;
import com.lzp.util.concurrent.map.NoResizeConHashMap;
import com.lzp.util.concurrent.threadpool.*;
import com.lzp.util.concurrent.threadpool.ThreadPoolExecutor;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.Map;
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

    public static void main(String[] args) throws InterruptedException, ExecutionException, TimeoutException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        CountDownLatch countDownLatch = new CountDownLatch(12000000);
        //Map<String,String> map = new ConcurrentHashMap(15000000);
        Map<String,String> map = new NoResizeConHashMap(15000000);

        new Thread(() -> {
            for (int i = 0; i < 2000000; i++) {
                map.put(String.valueOf(i),String.valueOf(i));
                countDownLatch.countDown();
            }
        }).start();
        new Thread(() -> {
            for (int i = 2000000; i < 4000000; i++) {
                map.put(String.valueOf(i),String.valueOf(i));
                countDownLatch.countDown();
            }
        }).start();
        new Thread(() -> {
            for (int i = 4000000; i < 6000000; i++) {
                map.put(String.valueOf(i),String.valueOf(i));
                countDownLatch.countDown();
            }
        }).start();
        new Thread(() -> {
            for (int i = 6000000; i < 8000000; i++) {
                map.put(String.valueOf(i),String.valueOf(i));
                countDownLatch.countDown();
            }
        }).start();
        new Thread(() -> {
            for (int i = 8000000; i < 10000000; i++) {
                map.put(String.valueOf(i),String.valueOf(i));
                countDownLatch.countDown();
            }
        }).start();
        new Thread(() -> {
            for (int i = 10000000; i < 12000000; i++) {
                map.put(String.valueOf(i),String.valueOf(i));
                countDownLatch.countDown();
            }
        }).start();
        countDownLatch.await();
        /*for (Map.Entry<String, String> entry : map.entrySet()) {
            System.out.println(entry.getKey());
        }*/
        long now = System.currentTimeMillis();
        for(Map.Entry<String,String> entry : map.entrySet()){

        }
        System.out.println(System.currentTimeMillis() - now);
        now = System.currentTimeMillis();
        
        System.out.println(map.size());
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
