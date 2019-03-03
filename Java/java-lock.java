/*
1.可重入(Reentrant)锁
    如果锁具备可重入性，则称作为可重入锁。像synchronized和 ReentrantLock都是可重入锁，可重入性实际上表明了锁的分配机制：基于线程的分配，而不是基于方法调用的分配。
    举个简单的例子，当一个线程执行到某个synchronized方法时，比如说method1，而在method1中会调用另外一个synchronized方法 method2，此时线程不必重新去申请锁，而是可以直接执行方法method2。
    看下面这段代码就明白了：
        classMyClass {
            public synchronized void method1() {
                method2();
            }
            public synchronized void method2() {
            }
        }
    上述代码中的两个方法method1和method2都用synchronized修饰了，假如某一时刻，线程A执行到了method1，此时线程 A获取了这个对象的锁，而由于method2也是synchronized方法，
    假如synchronized不具备可重入性，此时线程A需要重新申请锁。但是这就会造成一个问题，因为线程A已经持有了该对象的锁，而又在申请获取该对象的锁，这样就会线程A一直等待永远不会获取到的锁。　　
    而由于synchronized和Lock都具备可重入性，所以不会发生上述现象。

2.可中断锁
　　可中断锁：顾名思义，就是可以相应中断的锁。
　　在Java中，synchronized就不是可中断锁，而Lock是可中断锁。
　　如果某一线程A正在执行锁中的代码，另一线程B正在等待获取该锁，可能由于等待时间过长，线程B不想等待了，想先处理其他事情，我们可以让它中断自己或者在别的线程中中断它，这种就是可中断锁。

3.公平锁
    公平锁即尽量以请求锁的顺序来获取锁。比如同是有多个线程在等待一个锁，当这个锁被释放时，等待时间最久的线程（最先请求的线程）会获得该所，这种就是公平锁。
    非公平锁即无法保证锁的获取是按照请求锁的顺序进行的。这样就可能导致某个或者一些线程永远获取不到锁。
    在Java中，synchronized就是非公平锁，它无法保证等待的线程获取锁的顺序。
    而对于ReentrantLock和ReentrantReadWriteLock，它默认情况下是非公平锁，但是可以设置为公平锁。设置方法如下：ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);
*/
/** 
ReentrantReadWriteLock，首先要做的是与ReentrantLock划清界限。彼此之间没有继承或实现的关系。这个锁机制的特性： 
    (a).重入方面其内部的WriteLock可以直接获取ReadLock，但是反过来ReadLock想要获得WriteLock则永远都不要想,必须去竞争。 
    (b).ReadLock可以被多个线程持有并且在作用时排斥任何的WriteLock，而WriteLock则是完全的互斥。这一特性最为重要，因为对于高读取频率而相对较低写入的数据结构，使用此类锁同步机制则可以提高并发量。 
    (c).不管是ReadLock还是WriteLock都支持Interrupt，语义与ReentrantLock一致。 
    (d).WriteLock支持Condition并且与ReentrantLock语义一致，而ReadLock则不能使用Condition，否则抛出UnsupportedOperationException异常。 

    public ReentrantReadWriteLock(boolean fair) {
        sync = fair ? new FairSync() : new NonfairSync();
        readerLock = new ReadLock(this);
        writerLock = new WriteLock(this);
    }
*/

/** 
 * ReentrantReadWriteLock 应用场景:
    1.锁降级（一个方法中即存在读又存在写）:
*/

public class CachedData {
    private Object data;
    //volatile修饰，保持内存可见性
    private volatile boolean cacheValid;
    //可重入读写锁
    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();

    public void processCachedData() {
        //首先获取读锁
        rwl.readLock().lock();
        //发现没有缓存数据则放弃读锁，获取写锁
        if (!cacheValid) {
            // 读锁不能升级为写锁 所以必须先释放                **********************
            rwl.readLock().unlock();
            rwl.writeLock().lock();
            try {
                // Recheck state because another thread might have
                // acquired write lock and changed state before we did.
                if (!cacheValid) {
                    data = value;
                    cacheValid = true;
                }
            // Downgrade by acquiring read lock before releasing write lock
            rwl.readLock().lock();
            } finally {
                //进行锁降级
                rwl.writeLock().unlock();
                // Unlock write, still hold read
            }
        }        
        try {
            use(data);
        } finally {
            rwl.readLock().unlock();
        }
    }
}

/**
    2.集合使用场景（单方法存在读写中的一种）:
        通常可以在集合使用场景中看到ReentrantReadWriteLock的身影。不过只有在集合比较大，读操作比写操作多，操作开销大于同步开销的时候才是值得的。
*/
public class RWDictionary {
    //集合对象
    private final Map<String, Data> m = new TreeMap<String, Data>();
    //读写锁
    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    //获取读锁
    private final Lock r = rwl.readLock();
    //获取写锁
    private final Lock w = rwl.writeLock();

    public Data get(String key) {
        r.lock();
        try { return m.get(key); }
        finally { r.unlock(); }
    }
    public String[] allKeys() {
        r.lock();
        try { return m.keySet().toArray(); }
        finally { r.unlock(); }
    }
    public Data put(String key, Data value) {
        w.lock();
        try { return m.put(key, value); }
        finally { w.unlock(); }
    }
    public void clear() {
        w.lock();
        try { m.clear(); }
        finally { w.unlock(); }
    }
}





