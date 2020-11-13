import com.lzp.util.concurrent.blockingQueue.nolock.DependenOneTOneBlocQue;
import com.lzp.util.concurrent.blockingQueue.nolock.NoLockBlockingQueue;
import com.lzp.util.concurrent.blockingQueue.nolock.OneToOneBlockingQueue;
import com.lzp.util.concurrent.threadpool.RejectExecuHandler;
import com.lzp.util.concurrent.threadpool.ThreadFactoryImpl;

import java.util.concurrent.*;

/**
 * Description:test
 *
 * @author: Zeping Lu
 * @date: 2020/8/9 11:46
 */
public class Test {
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        ExecutorService executorService = new ThreadPoolExecutor(4, 4, 2,TimeUnit.SECONDS, new LinkedBlockingQueue());
        Future future = executorService.submit(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                return sum();
            }
        });
        System.out.println(future.cancel(true));
        System.out.println(future.cancel(true));
        System.out.println(future.isDone());
        System.out.println(future.isCancelled());
        System.out.println(future.get());

        /*System.out.println(((ThreadPoolExecutor)executorService).getPoolSize());
        Thread.sleep(15000);
        System.out.println(((ThreadPoolExecutor)executorService).getPoolSize());
        //now = System.currentTimeMillis();
        for (int i = 0; i < 50000; i++) {
            executorService.execute(runnable);
        }
        executorService.shutdown();
        System.out.println(((ThreadPoolExecutor)executorService).getPoolSize());*/
    }




    static int sum() {
        int a = 0;
        for (int i = 0; i < 10000000; i++) {
            a += i;
        }
        return a;
    }

    static void test() throws InterruptedException {

    }
}
