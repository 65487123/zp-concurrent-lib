import com.lzp.util.concurrent.blockingQueue.nolock.DependenOneTOneBlocQue;
import com.lzp.util.concurrent.blockingQueue.nolock.NoLockBlockingQueue;
import com.lzp.util.concurrent.blockingQueue.nolock.OneToOneBlockingQueue;
import com.lzp.util.concurrent.threadpool.*;
import com.lzp.util.concurrent.threadpool.ThreadPoolExecutor;

import java.util.concurrent.*;


/**
 * Description:test
 *
 * @author: Zeping Lu
 * @date: 2020/8/9 11:46
 */
public class Test {
    public static void main(String[] args) throws InterruptedException, ExecutionException, TimeoutException {
        LinkedBlockingQueue arrayBlockingQueue = new LinkedBlockingQueue();
        CountDownLatch countDownLatch = new CountDownLatch(800000);
        long now = System.currentTimeMillis();
        new Thread(() -> put(arrayBlockingQueue)).start();new Thread(() -> put(arrayBlockingQueue)).start();
        new Thread(() -> put(arrayBlockingQueue)).start();new Thread(() -> put(arrayBlockingQueue)).start();
        new Thread(() -> {
            for (int i = 0; i <800000 ; i++) {
                try {
                    arrayBlockingQueue.take();
                } catch (InterruptedException ignored){}
                countDownLatch.countDown();
            }
        }).start();
        countDownLatch.await();
        System.out.println(System.currentTimeMillis()-now);
    }


    static void put(LinkedBlockingQueue arrayBlockingQueue){
        for (int i = 0; i <200000 ; i++) {
            try {
                arrayBlockingQueue.put(String.valueOf(i));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    static void put(NoLockBlockingQueue arrayBlockingQueue,int threadId){
        for (int i = 0; i <200000 ; i++) {
            try {
                arrayBlockingQueue.put(String.valueOf(i),threadId);
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
