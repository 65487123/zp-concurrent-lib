import com.lzp.util.concurrent.blockingQueue.lockless.NoSideEffectLocklessQueue;
import com.lzp.util.concurrent.blockingQueue.lockless.OneToOneBlockingQueue;
import com.lzp.util.concurrent.blockingQueue.withlock.OptimizedArrBlockQueue;
import com.lzp.util.concurrent.latch.CountDownLatch;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.concurrent.*;


/**
 * Description:test
 *
 * @author: Zeping Lu
 * @date: 2020/8/9 11:46
 */
public class Test {


    public static void main(String[] args) throws InterruptedException, ExecutionException, TimeoutException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, IOException, ClassNotFoundException {
        int sum = 0;
        for (int j = 0; j < 2000; j++) {
            BlockingQueue blockingQueue = new NoSideEffectLocklessQueue(  16384);
            CountDownLatch countDownLatch = new CountDownLatch(50000);

            long now = System.currentTimeMillis();
            new Thread(() -> {
                for (int i = 0; i < 10000; i++) {

                    try {
                        blockingQueue.put(String.valueOf(i));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
            new Thread(() -> {
                for (int i = 0; i < 10000; i++) {
                    try {

                        blockingQueue.put(String.valueOf(i));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
            new Thread(() -> {
                for (int i = 0; i < 10000; i++) {
                    try {

                        blockingQueue.put(String.valueOf(i));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
            new Thread(() -> {
                for (int i = 0; i < 10000; i++) {
                    try {

                        blockingQueue.put(String.valueOf(i));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
            new Thread(() -> {
                for (int i = 0; i < 10000; i++) {
                    try {

                        blockingQueue.put(String.valueOf(i));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
            new Thread(() -> {
                for (int i = 0; i < 50000; i++) {
                    try {
                        blockingQueue.take();
                        countDownLatch.countDown();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
            countDownLatch.await();
            sum += System.currentTimeMillis() - now;
        }

        System.out.println(new BigDecimal(sum).divide(new BigDecimal(2000),3,BigDecimal.ROUND_CEILING));
        /*BlockingQueue<Runnable> blockingQueue = new DelayQueue();
        System.out.println(System.currentTimeMillis());
        blockingQueue.put(new ScheduledFutureImp(new Runnable() {
            @Override
            public void run() {
                System.out.println("第二个定时任务:"+System.currentTimeMillis());
            }
        },2000,3000, TimeUnit.MILLISECONDS, false, blockingQueue));
        System.out.println(blockingQueue.take());
        System.out.println(System.currentTimeMillis());

*/
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
