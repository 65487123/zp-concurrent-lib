import com.lzp.util.concurrent.blockingQueue.nolock.DependenOneTOneBlocQue;
import com.lzp.util.concurrent.blockingQueue.nolock.NoLockBlockingQueue;
import com.lzp.util.concurrent.blockingQueue.nolock.OneToOneBlockingQueue;
import com.lzp.util.concurrent.blockingQueue.withlock.OptimizedArrBlockQueue;
import com.lzp.util.concurrent.map.NoResizeConHashMap;
import com.lzp.util.concurrent.threadpool.*;
import com.lzp.util.concurrent.threadpool.ThreadPoolExecutor;

import java.io.*;
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

    public static void main(String[] args) throws InterruptedException, ExecutionException, TimeoutException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, IOException, ClassNotFoundException {
        CountDownLatch countDownLatch = new CountDownLatch(120000);
        //Map<String,String> map = new ConcurrentHashMap(150000);
        Map<String,String> map = new NoResizeConHashMap(150000);

        new Thread(() -> {
            for (int i = 0; i < 20000; i++) {
                map.put(String.valueOf(i),String.valueOf(i));
                countDownLatch.countDown();
            }
        }).start();
        new Thread(() -> {
            for (int i = 20000; i < 40000; i++) {
                map.put(String.valueOf(i),String.valueOf(i));
                countDownLatch.countDown();
            }
        }).start();
        new Thread(() -> {
            for (int i = 40000; i < 60000; i++) {
                map.put(String.valueOf(i),String.valueOf(i));
                countDownLatch.countDown();
            }
        }).start();
        new Thread(() -> {
            for (int i = 60000; i < 80000; i++) {
                map.put(String.valueOf(i),String.valueOf(i));
                countDownLatch.countDown();
            }
        }).start();
        new Thread(() -> {
            for (int i = 80000; i < 100000; i++) {
                map.put(String.valueOf(i),String.valueOf(i));
                countDownLatch.countDown();
            }
        }).start();
        new Thread(() -> {
            for (int i = 100000; i < 120000; i++) {
                map.put(String.valueOf(i),String.valueOf(i));
                countDownLatch.countDown();
            }
        }).start();
        countDownLatch.await();
        long now = System.currentTimeMillis();

        ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream("a.txt"));
        objectOutputStream.writeObject(map);
        objectOutputStream.flush();
        ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream("a.txt"));
        //ConcurrentHashMap map1 = (ConcurrentHashMap) objectInputStream.readObject();
        NoResizeConHashMap map1 = (NoResizeConHashMap) objectInputStream.readObject();

        /*for (String entry : map.keySet()) {
        }
*/
        System.out.println(System.currentTimeMillis() - now);
        System.out.println(map1.size());
        now = System.currentTimeMillis();
        /*for (Map.Entry<String, String> entry : map.entrySet()) {
        }
        System.out.println(System.currentTimeMillis() - now);*/

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
