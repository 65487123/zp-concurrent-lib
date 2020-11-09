import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;

/**
 * Description:
 *
 * @author: Zeping Lu
 * @date: 2020/8/9 11:46
 */
public class Test {
    public static void main(String[] args) throws InterruptedException {
        ArrayBlockingQueue oneToOneBlockingQueue = new ArrayBlockingQueue(80000000);
        CountDownLatch countDownLatch = new CountDownLatch(80000000);
        long now  = System.currentTimeMillis();
        new Thread(){
            @Override
            public void run() {
                for (int i = 0; i <20000000 ; i++) {
                    try {
                        oneToOneBlockingQueue.put(String.valueOf(i));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
        new Thread(){
            @Override
            public void run() {
                for (int i = 0; i <20000000 ; i++) {
                    try {
                        oneToOneBlockingQueue.put(String.valueOf(i));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
        new Thread(){
            @Override
            public void run() {
                for (int i = 0; i <20000000 ; i++) {
                    try {
                        oneToOneBlockingQueue.put(String.valueOf(i));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
        new Thread(){
            @Override
            public void run() {
                for (int i = 0; i <20000000 ; i++) {
                    try {
                        oneToOneBlockingQueue.put(String.valueOf(i));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
        new Thread(){
            @Override
            public void run() {
                for (int i = 0; i <80000000 ; i++) {
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
