import com.lzp.util.concurrent.blockingQueue.lockless.NoSideEffectLocklessQueue;
import com.lzp.util.concurrent.blockingQueue.lockless.OneToOneBlockingQueue;
import com.lzp.util.concurrent.blockingQueue.withlock.OptimizedArrBlockQueue;
import com.lzp.util.concurrent.latch.CountDownLatch;
import com.lzp.util.concurrent.threadpool.RejectExecuHandler;
import com.lzp.util.concurrent.threadpool.ThreadPoolExecutor;

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
    static void a() throws InterruptedException {
            BlockingQueue blockingQueue = new ArrayBlockingQueue(500000);
            com.lzp.util.concurrent.threadpool.ThreadPoolExecutor threadPoolExecutor = new com.lzp.util.concurrent.threadpool.ThreadPoolExecutor(4, 4, 0, blockingQueue, (r, executor) -> r.run());
            //java.util.concurrent.ThreadPoolExecutor threadPoolExecutor = new java.util.concurrent.ThreadPoolExecutor(4, 4, 0,TimeUnit.SECONDS, blockingQueue, (r, executor) -> r.run());
        for (int j = 0; j < 10; j++) {

            CountDownLatch countDownLatch = new CountDownLatch(5000000);
            //Runnable runnable = () -> countDownLatch.countDown();
            long now = System.currentTimeMillis();
            for (int i = 0; i < 5000000; i++) {
                threadPoolExecutor.execute(countDownLatch::countDown);
            }

            /*new Thread(() -> {
                try {
                    for (int i = 0; i < 5000000; i++) {
                        blockingQueue.take();
                        countDownLatch.countDown();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
            new Thread(() -> {
                for (int i = 0; i < 1250000; i++) {
                    blockingQueue.offer(runnable);
                }
            }).start();
            new Thread(() -> {
                for (int i = 0; i < 1250000; i++) {
                    blockingQueue.offer(runnable);
                }
            }).start();
            new Thread(() -> {
                for (int i = 0; i < 1250000; i++) {
                    blockingQueue.offer(runnable);
                }
            }).start();
            new Thread(() -> {
                for (int i = 0; i < 1250000; i++) {
                    blockingQueue.offer(runnable);
                }
            }).start();*/
            countDownLatch.await();
            System.out.println(System.currentTimeMillis() - now);
        }
            //threadPoolExecutor.shutdown();
    }

    public static void main(String[] args) throws InterruptedException, ExecutionException, TimeoutException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, IOException, ClassNotFoundException {
        a();
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
