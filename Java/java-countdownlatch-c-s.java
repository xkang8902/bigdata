/** 
一、CountDownLatch
CountDownLatch是一个计数器，在它的构造方法中需要指定一个值，用来设定计数的次数。
每调用一次countDown()方法，数值便会减一，CountDownLatch会一直阻塞着调用await()方法的线程，直到计数器的值变为0。
*/
    import java.util.Date;
    import java.util.Random;
    import java.util.concurrent.*;
    /**
    * @description CountDownLatch
    * @author: 
    * @date: 
    */

    public class CountDownLatchDemo {

        private static CountDownLatch countDownLatch = new CountDownLatch(4);
        private static ExecutorService executor = Executors.newFixedThreadPool(4);
        private static int THREAD_COUNT = 4;

        public static void main(String[] args) throws InterruptedException {
            for (int i = 0; i < THREAD_COUNT; i++) {
                executor.execute(new Runnable() {
                    public void run() {
                        try {
                            // 模拟业务逻辑的耗时
                            int timer = new Random().nextInt(5);
                            TimeUnit.SECONDS.sleep(timer);

                            System.out.printf("%s时完成磁盘的统计任务,耗费%d秒.\n", new Date().toString(), timer);
                            // 业务处理完成之后,计数器减一
                            countDownLatch.countDown();//每调用一次countDown()方法，数值便会减一
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }

            // CountDownLatch会一直阻塞着调用await()方法的线程，直到计数器的值变为0。
            countDownLatch.await();
            System.out.printf("%s时全部任务都完成,执行合并计算.\n", new Date().toString());
            executor.shutdown();
        }
    }
/**
二、CyclicBarrier
    CyclicBarrier要做的事情是，让一组线程到达一个屏障（也可以叫同步点）时被阻塞，直到最后一个线程到达屏障时，屏障才会开门，所有被屏障拦截的线程才会继续运行。
    CyclicBarrier初始化的时候，设置一个屏障数。线程调用await()方法的时候，这个线程就会被阻塞，当调用await()的线程数量到达屏障数的时候，主线程就会取消所有被阻塞线程的状态。

    其构造方法如下：
    public CyclicBarrier(int parties){}     参数parties则为初始化时的屏障数
    CyclicBarrier还提供一个更高级的构造函数
    public CyclicBarrier(int parties, Runnable barrierAction) {}

    用于在线程到达屏障时，优先执行barrierAction，方便处理更复杂的业务场景
    例如，用一个Excel保存了用户所有的银行流水，每个sheet保存一个账户近一年的每笔交易流水，现在需要统计用户的日均交易流水，先用多线程处理每个sheet里的交易流水，都处理完后，得到每个sheet的日均交易流水，
    最后再用barrierAction用这些线程的计算结果，计算出整个Excel的日均银行流水.
    代码如下：
*/

    import java.util.Map;
    import java.util.concurrent.*;

    /**
    * @description 银行交易流水服务类
    * @author: 
    * @date:
    */

    public class BankWaterService implements Runnable {
        /**
        * 创建4个屏障,处理完之后,执行当前类的run方法
        */
        private CyclicBarrier cyclicBarrier = new CyclicBarrier(4, this);

        private Executor executor = Executors.newFixedThreadPool(4);

       /**
        * 保存每个sheet计算出来的银行交易流水结果
        */
        private ConcurrentHashMap<String, Integer> sheetBankWaterCount = new ConcurrentHashMap<String, Integer>();

        /**
        * 交易流水统计
        */
        private void count() {
            for (int i = 0; i < 4; i++) {
                executor.execute(new Runnable() {
                    public void run() {
                        // 模拟计算当前sheet的银行交易流水数据的业务处理
                        sheetBankWaterCount.put(Thread.currentThread().getName(), 1);
                        // 银行交易流水计算完成后,插入一个屏障
                        try {
                            cyclicBarrier.await();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (BrokenBarrierException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }

        /**
        * 汇总计算结果
        */
        public void run() {
            int result = 0;
            for (Map.Entry<String, Integer> sheet : sheetBankWaterCount.entrySet()) {
                result += sheet.getValue();
            }
            // 设置计算结果,并输出
            sheetBankWaterCount.put("result", result);
            System.out.println(result);
        }

        public static void main(String[] args) {
            BankWaterService service = new BankWaterService();
            service.count();
        }
    }

/**
三、Semapphore
    Semaphore被用于控制特定资源在同一个时间被访问的线程数量，它通过协调各个线程，以保证资源可以被合理的使用。
        做个比喻，把Semaphore比作是控制流量的红绿灯，比如xx马路要控制流量，只允许同时有一百辆车在马路上行驶，其他的都必须在路口等待，
        所以前一百辆会看到绿灯，可以开进马路，后面的车会看到红灯，不能开进马路，但是如果前面一百辆车中有5辆已经离开了马路，那后面就允许有5辆车驶入马路，
        这里例子里说的车就是线程，驶入马路就代表线程正在执行，离开马路就表示线程执行完成，看到红灯就代表线程被阻塞，不能执行。
    应用场景
        Semaph可以用来做流量限制，特别是公共资源有限的应用场景，比如说数据库连接。
        假如有一个需求，要读取几万个文件的数据，因为都是IO密集型任务，我们可以启动几十个线程并发的读取，但是如果读取到内存后，还需要储存到数据库，而数据库的连接数只有10个，这时候我们就必须控制只有10个线程同时获取到数据库连接，否则会抛出异常提示无法连接数据库。针对这种情况，我们就可以使用Semaphore来做流量控制。代码如下：
*/

    import java.util.concurrent.*;

    /**
    * @description
    * @author: 
    * @date: 
    */
    public class SemaphoreDemo {

        private static final int THREAD_COUNT = 30;

        private static ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);

        private static Semaphore semaphore = new Semaphore(10);

        public static void main(String[] args) {
            for (int i = 0; i < THREAD_COUNT; i++) {
                executor.execute(new Runnable() {
                    public void run() {
                        try {
                            // 获取一个"许可证"
                            semaphore.acquire();

                            // 模拟数据保存
                            TimeUnit.SECONDS.sleep(2);
                            System.out.println("save date...");

                            // 执行完后,归还"许可证"
                            semaphore.release();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
            executor.shutdown();
        }
    }
/**
    在代码中，虽然有30个线程在执行，但是只运行10个并发的执行。所以我们可以看到在执行的过程中
    save data...是每10输出的。
    Semaphore的构造方法Semaphore(int permits)接受一个整形的数字，表示可用的许可证数量。
    Semaphore(10)表示运行10个线程获取许可证，也就是最大的并发数是10。
    Semaphore的用法也很简单，首先使用Semaphore.acquire()方法获取一个许可证，使用完之后调用
    release()方法归还许可证。
*/

