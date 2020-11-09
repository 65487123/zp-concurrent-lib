import com.lzp.util.concurrent.blockingQueue.nolock.NoLockBlockingQueue;

import java.util.concurrent.CountDownLatch;

/**
 * Description:
 *
 * @author: Zeping Lu
 * @date: 2020/8/9 11:46
 */
public class Test {
    public static void main(String[] args) throws InterruptedException {
        final NoLockBlockingQueue oneToOneBlockingQueue = new NoLockBlockingQueue(80000000,4);
        final CountDownLatch countDownLatch = new CountDownLatch(100000000);
        long now  = System.currentTimeMillis();
        new Thread(){
            @Override
            public void run() {
                for (int i = 0; i <25000000 ; i++) {
                    try {
                        oneToOneBlockingQueue.put(String.valueOf(i),0);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
        new Thread(){
            @Override
            public void run() {
                for (int i = 0; i <25000000 ; i++) {
                    try {
                        oneToOneBlockingQueue.put(String.valueOf(i),1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
        new Thread(){
            @Override
            public void run() {
                for (int i = 0; i <25000000 ; i++) {
                    try {
                        oneToOneBlockingQueue.put(String.valueOf(i),2);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
        new Thread(){
            @Override
            public void run() {
                for (int i = 0; i <25000000 ; i++) {
                    try {
                        oneToOneBlockingQueue.put(String.valueOf(i),3);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
        new Thread(){
            @Override
            public void run() {
                for (int i = 0; i <100000000 ; i++) {
                    try {
                        oneToOneBlockingQueue.take();
                        countDownLatch.countDown();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
        countDownLatch.await();
        System.out.println(System.currentTimeMillis() - now);
    }
}
