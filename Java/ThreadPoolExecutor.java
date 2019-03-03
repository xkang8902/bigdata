public class ThreadPoolExecutorTest {
    public static void main(String[] args) {
        BlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(10);
        //RejectedExecutionHandler handler = new ThreadPoolExecutor.AbortPolicy();
        /**
         * java.util.concurrent.ThreadPoolExecutor.AbortPolicy 这个是默认使用的拒绝策略,如果有要执行的任务队列已满,且还有任务提交,则直接抛出异常信息
            java.util.concurrent.ThreadPoolExecutor.DiscardPolicy 这个是忽略策略,如果有要执行的任务队列已满,且还有任务提交,则直接忽略掉这个任务,即不抛出异常也不做任何处理.
            java.util.concurrent.ThreadPoolExecutor.DiscardOldestPolicy 忽略最早提交的任务.如果有要执行的任务队列已满,此时若还有任务提交且线程池还没有停止,则把队列中最早提交的任务抛弃掉,然后把当前任务加入队列中.
            java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy 这个是来着不拒策略.如果有要执行的任务队列已满,此时若还有任务提交且线程池还没有停止,则直接运行任务的run方法.
         */
        RejectedExecutionHandler handler = new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                if (!executor.isShutdown()){
                    try {
                        executor.getQueue().put(r);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        /**
         * ThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, RejectedExecutionHandler handler) 这个是ThreadPoolExecutor完整的构造器,其他的构造器其实也是在内部调用这个.
            corePoolSize 核心线程数,线程池保留线程的数量,即使这些线程是空闲.除非设置了allowCoreThreadTimeOut
            maximumPoolSize 线程池最大允许的线程数.
            keepAliveTime 当当前的线程数大于核心线程数,那么这些多余的空闲的线程在被终止之前能等待新任务的时间.
            unit keepAliveTime时间的单位
            workQueue 这个是用来保留将要执行的工作队列.
            threadFactory 用于创建新线程的工厂
                如果构造不带threadFactory,那么默认使用java.util.concurrent.Executors.DefaultThreadFactory创建出一个新的工厂对象.通过阅读源代码,主要是在创建新的线程的时候修改了线程名为pool-全局线程池递增数编号-thread-当前线程池线程递增编号,让线程改为非守护线程,并设置线程的优先级为NORM_PRIORITY.
            handler 如果工作队列(workQueue)满了,那么这个handler是将会被执行.
            ThreadPoolExecutor还有几个可不带threadFactory或handler惨的构造器,说明java提供了一些默认的配置.
            具体流程如下：
                1）当池子大小小于corePoolSize就新建线程，并处理请求
                2）当池子大小等于corePoolSize，把请求放入workQueue中，池子里的空闲线程就去从workQueue中取任务并处理
                3）当workQueue放不下新入的任务时，新建线程入池，并处理请求，如果池子大小撑到了maximumPoolSize就用RejectedExecutionHandler来做拒绝处理
                4）另外，当池子的线程数大于corePoolSize的时候，多余的线程会等待keepAliveTime长的时间，如果无请求可处理就自行销毁
        */
        ThreadPoolExecutor pool = new ThreadPoolExecutor(3, 5, 0, TimeUnit.SECONDS, queue, handler);
        for (int i = 0; i < 20; i ++){
            final int temp = i;
            pool.execute(() -> {
                String name = Thread.currentThread().getName();
                System.out.println(name + "客户" + temp + "来了.......");
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }
        pool.shutdown();
    }
}
/**
队列的三种通用策略详解：
1 直接提交 SynchronousQueue  （凡是提交的任务都会执行，此时线程池相当于执行任务的线程集合）

    它将任务直接提交给线程而不保存它们。在此，如果不存在可用于立即运行任务的线程，则试图把任务加入队列将失败，因此会构造一个新的线程。*****
    线程池不对任务进行缓存。新进任务直接提交给线程池，当线程池中没有空闲线程时，创建一个新的线程处理此任务。这种策略需要线程池具有无限增长的可能性。 直接提交通常要求无界的maximumPoolSizes 以避免拒绝新提交的任务。
    SynchronousQueue 线程安全的Queue，可以存放若干任务（但当前只允许有且只有一个任务在等待），
    其中每个插入操作必须等待另一个线程的对应移除操作，也就是说A任务进入队列，B任务必须等A任务被移除之后才能进入队列，否则执行异常策略。你来一个我扔一个，所以说SynchronousQueue没有任何内部容量。
    比如：核心线程数为2，最大线程数为3；使用SynchronousQueue。
    当前有2个核心线程在运行，又来了个A任务，两个核心线程没有执行完当前任务，根据如果运行的线程等于或多于 corePoolSize，
    则 Executor 始终首选将请求加入队列，而不添加新的线程。所以A任务被添加到队列，此时的队列是SynchronousQueue，
    当前不存在可用于立即运行任务的线程，因此会构造一个新的线程，此时又来了个B任务，两个核心线程还没有执行完。
    新创建的线程正在执行A任务，所以B任务进入Queue后，最大线程数为3，发现没地方放了。就只能执行异常策略(RejectedExecutionException)。
代码示例：
    import java.util.concurrent.SynchronousQueue;  
    import java.util.concurrent.ThreadPoolExecutor;  
    import java.util.concurrent.TimeUnit;  
    
    public class PoolForSynchronousQueueTest {  
        
        public static void main(String[] args) {  
                
            ThreadPoolExecutor pool =  new ThreadPoolExecutor(2, 3, 3L,   
                            TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), new ThreadPoolExecutor.AbortPolicy());  
            for (int i = 0; i < 4; i++) {  
                pool.execute(new Runnable() {  
                    @Override  
                    public void run() {  
                        for (int i = 0; i < 10; i++) {  
                            System.out.println(Thread.currentThread().getName() + " 执行 - " + i);  
                        }  
                        System.out.println("run");  
                    }  
                });  
            }  
            pool.shutdown();  
    }     
 
    // 在添加第4个任务的时候，程序抛出 java.util.concurrent.RejectedExecutionException 
    运行结果：
        pool-1-thread-2 执行 - 0
        run
        pool-1-thread-1 执行 - 0
        run
        pool-1-thread-3 执行 - 0
        run
        Exception in thread "main" Java.util.concurrent.RejectedExecutionException: Taskzmx.jdkthreadpool.test.PoolForSynchronousQueueTest$1@bda96b rejected fromjava.util.concurrent.ThreadPoolExecutor@4e4b9101[Running, pool size = 3, active threads = 3, queued tasks = 0, completed tasks = 0]
        at java.util.concurrent.ThreadPoolExecutor$AbortPolicy.rejectedExecution(ThreadPoolExecutor.java:2048)
        at java.util.concurrent.ThreadPoolExecutor.reject(ThreadPoolExecutor.java:821)
        at java.util.concurrent.ThreadPoolExecutor.execute(ThreadPoolExecutor.java:1372)
        at zmx.jdkthreadpool.test.PoolForSynchronousQueueTest.main(PoolForSynchronousQueueTest.java:14)

2 无界队列 如LinkedBlockingQueue

    使用无界队列（例如，不具有预定义容量的 LinkedBlockingQueue）将导致在所有核心线程都在忙时新任务在队列中等待。
    这样，创建的线程就不会超过 corePoolSize。（因此，maximumPoolSize 的值也就没意义了。）也就不会有新线程被创建，都在那等着排队呢。
    如果未指定容量，则它等于 Integer.MAX_VALUE。如果设置了Queue预定义容量，则当核心线程忙碌时，新任务会在队列中等待，直到超过预定义容量(新任务没地方放了)，才会执行异常策略。你来一个我接一个，直到我容不下你了。FIFO，先进先出。
    比如：核心线程数为2，最大线程数为3；使用LinkedBlockingQueue(1)，设置容量为1。
    当前有2个核心线程在运行，又来了个A任务，两个核心线程没有执行完当前任务，根据如果运行的线程等于或多于 corePoolSize，
    则 Executor 始终首选将请求加入队列，而不添加新的线程。所以A任务被添加到队列，此时的队列是LinkedBlockingQueue，
    此时又来了个B任务，两个核心线程没有执行完当前任务，A任务在队列中等待，队列已满。所以根据如果无法将请求加入队列，则创建新的线程，
    B任务被新创建的线程所执行，此时又来个C任务，此时maximumPoolSize已满，队列已满，只能执行异常策略(RejectedExecutionException)。

代码示例：
    import java.util.concurrent.LinkedBlockingQueue;  
    import java.util.concurrent.ThreadPoolExecutor;  
    import java.util.concurrent.TimeUnit;  
    
    public class PoolForLinkedBlockingQueueTest {  
            
        public static void main(String[] args) {  
                
            ThreadPoolExecutor pool =  new ThreadPoolExecutor(2, 3, 3L,   
                                    TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(1),    
                                    new ThreadPoolExecutor.AbortPolicy());  
            for (int i = 0; i < 5; i++) {  
                pool.execute(new Runnable() {  
                    @Override  
                    public void run() {  
                        for (int i = 0; i < 10; i++) {  
                            System.out.println(Thread.currentThread().getName() + " 执行 - " + i);  
                        }  
                        System.out.println("run");  
                    }  
                });  
            }  
            pool.shutdown();  
        }  
    
    }  
    // 有一个任务在队列中等待，在添加第5个任务的时候，程序抛出 java.util.concurrent.RejectedExecutionException
    运行结果：
        pool-1-thread-1 执行 - 0
        pool-1-thread-2 执行 - 0
        run
        run
        pool-1-thread-2 执行 - 0
        run
        pool-1-thread-3 执行 - 0
        run
        Exception in thread "main" java.util.concurrent.RejectedExecutionException: Taskzmx.jdkthreadpool.test.PoolForLinkedBlockingQueueTest$1@7d557ee8 rejected fromjava.util.concurrent.ThreadPoolExecutor@2a97cec[Running, pool size = 3, active threads = 3, queued tasks = 1, completed tasks = 0]
        at java.util.concurrent.ThreadPoolExecutor$AbortPolicy.rejectedExecution(ThreadPoolExecutor.java:2048)
        at java.util.concurrent.ThreadPoolExecutor.reject(ThreadPoolExecutor.java:821)
        at java.util.concurrent.ThreadPoolExecutor.execute(ThreadPoolExecutor.java:1372)
        at zmx.jdkthreadpool.test.PoolForLinkedBlockingQueueTest.main(PoolForLinkedBlockingQueueTest.java:16)

    无界的话要防止任务增长的速度远远超过处理任务的速度。

3 有界队列 如ArrayBlockingQueue

    操作模式跟LinkedBlockingQueue差不多，只不过必须为其设置容量。所以叫有界队列。
    new ArrayBlockingQueue<Runnable>(Integer.MAX_VALUE) 跟 new LinkedBlockingQueue(Integer.MAX_VALUE)效果一样。
    LinkedBlockingQueue 底层是链表结构，ArrayBlockingQueue  底层是数组结构。你来一个我接一个，直到我容不下你了。FIFO，先进先出。

代码示例： 
    import java.util.concurrent.ArrayBlockingQueue;  
    import java.util.concurrent.ThreadPoolExecutor;  
    import java.util.concurrent.TimeUnit;  
    
    public class PoolForArrayBlockingQueueTest {  
        
        public static void main(String[] args) {  
                
            ThreadPoolExecutor pool =  new ThreadPoolExecutor(2, 3, 3L,   
                                    TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(1),    
                                    new ThreadPoolExecutor.AbortPolicy());  
            for (int i = 0; i < 5; i++) {  
                pool.execute(new Runnable() {  
                    @Override  
                    public void run() {  
                        for (int i = 0; i < 10; i++) {  
                            System.out.println(Thread.currentThread().getName() + " 执行 - " + i);  
                        }  
                        System.out.println("run");  
                    }  
                });  
            }  
            pool.shutdown();  
        }  
    
    }  
    // 有一个任务在队列中等待，在添加第5个任务的时候，程序抛出 java.util.concurrent.RejectedExecutionException</span>  
    运行结果：
        pool-1-thread-1 执行 - 0
        run
        pool-1-thread-1 执行 - 0
        run
        pool-1-thread-3 执行 - 0
        run
        pool-1-thread-2 执行 - 0
        run
        Exception in thread "main" java.util.concurrent.RejectedExecutionException: Taskzmx.jdkthreadpool.test.PoolForArrayBlockingQueueTest$1@6c3b0b1e rejected fromjava.util.concurrent.ThreadPoolExecutor@7d6ac92e[Running, pool size = 3, active threads = 3, queued tasks = 1, completed tasks = 0]
        at java.util.concurrent.ThreadPoolExecutor$AbortPolicy.rejectedExecution(ThreadPoolExecutor.java:2048)
        at java.util.concurrent.ThreadPoolExecutor.reject(ThreadPoolExecutor.java:821)
        at java.util.concurrent.ThreadPoolExecutor.execute(ThreadPoolExecutor.java:1372)
        at zmx.jdkthreadpool.test.PoolForArrayBlockingQueueTest.main(PoolForArrayBlockingQueueTest.java:15)

4、关于keepAliveTime
    JDK解释：当线程数大于核心时，此为终止前多余的空闲线程等待新任务的最长时间。
    也就是说啊，线程池中当前的空闲线程服务完某任务后的存活时间。如果时间足够长，那么可能会服务其它任务。
    这里的空闲线程包不包括后来新创建的服务线程呢？我的理解是包括的。

5、关于handler有四个选择
    ThreadPoolExecutor.AbortPolicy()  抛出java.util.concurrent.RejectedExecutionException异常。 
    ThreadPoolExecutor.CallerRunsPolicy()  重试添加当前的任务，他会自动重复调用execute()方法。 
    ThreadPoolExecutor.DiscardOldestPolicy()  抛弃旧的任务   ThreadPoolExecutor.DiscardPolicy()  抛弃当前的任务。 

6、结论及分析：
    通常来说对于静态任务可以归为：
        数量大，但是执行时间很短
        数量小，但是执行时间较长
        数量又大执行时间又长
    使用无界队列，要防止任务增长的速度远远超过处理任务的速度，控制不好可能导致的结果就是内存溢出。
    使用有界队列，关键在于调节线程数和Queue大小 ，线程数多，队列容量少，资源浪费。线程数少，队列容量多，性能低，还可能导致内存溢出。
     Executor和ThreadPoolExecutor的关系
        public interface ExecutorService extends Executor
        public abstract class AbstractExecutorService implements ExecutorService
        public class ThreadPoolExecutor extends AbstractExecutorService

        public static ExecutorService newFixedThreadPool(int nThreads) {  
            return new ThreadPoolExecutor(nThreads, nThreads,  
                                        0L, TimeUnit.MILLISECONDS,  
                                        new LinkedBlockingQueue<Runnable>());  
        }
        public static ExecutorService newCachedThreadPool() {  
            return new ThreadPoolExecutor(0, Integer.MAX_VALUE,  
                                        60L, TimeUnit.SECONDS,  
                                        new SynchronousQueue<Runnable>());  
        } 

        Executors.newCachedThreadPool()使用SynchronousQueue创建线程池。
        Executors.newFixedThreadPool(3)使用LinkedBlockingQueue创建线程池。
        Executors.newSingleThreadExecutor()使用LinkedBlockingQueue创建线程池。
        
*/