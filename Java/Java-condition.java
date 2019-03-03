/** Condition:
    1.Condition是一个多线程间协调通信的工具类，使得某个，或者某些线程一起等待某个条件（Condition）,只有当该条件具备( signal 或者 signalAll方法被带调用)时 ，这些等待线程才会被唤醒，从而重新争夺锁。
    2.Condition依赖于Lock接口，生成一个Condition的基本代码是lock.newCondition() 
    3.调用Condition的await()和signal()方法，都必须在lock保护之内，就是说必须在lock.lock()和lock.unlock之间才可以使用
        Conditon中的await()对应Object的wait()；
        Condition中的signal()对应Object的notify()；
        Condition中的signalAll()对应Object的notifyAll()。
condition应用场景:   生产者、消费者模式
*/
    import java.util.PriorityQueue;  
    import java.util.concurrent.locks.Condition;  
    import java.util.concurrent.locks.Lock;  
    import java.util.concurrent.locks.ReentrantLock;  
    
    public class ConditionTest {  
            private int queueSize = 10;  
            private PriorityQueue<Integer> queue = new PriorityQueue<Integer>(queueSize);  
            private Lock lock = new ReentrantLock();  
            private Condition notFull = lock.newCondition();  
            private Condition notEmpty = lock.newCondition();  
            
            public static void main(String[] args) throws InterruptedException  {  
                ConTest2 test = new ConTest2();  
                Producer producer = test.new Producer();  
                Consumer consumer = test.new Consumer();                
                producer.start();  
                consumer.start();  
                Thread.sleep(0);  
                producer.interrupt();  
                consumer.interrupt();  
            }  
                
            private class Consumer extends Thread{              
                @Override  
                public void run() {  
                    consume();  
                }  
                volatile boolean flag=true;    
                private void consume() {  
                    while(flag){  
                        lock.lock();  
                        try {  
                            while(queue.size() == 0){  
                                try {  
                                    System.out.println("队列空，等待数据");  
                                    notEmpty.await();  
                                } catch (InterruptedException e) {                              
                                    flag =false;  
                                }  
                            }  
                            queue.poll();                //每次移走队首元素  
                            notFull.signal();  
                            System.out.println("从队列取走一个元素，队列剩余"+queue.size()+"个元素");  
                        } finally{  
                            lock.unlock();  
                        }  
                    }  
                }  
            }  
                
            private class Producer extends Thread{              
                @Override  
                public void run() {  
                    produce();  
                }  
                volatile boolean flag=true;    
                private void produce() {  
                    while(flag){  
                        lock.lock();  
                        try {  
                            while(queue.size() == queueSize){  
                                try {  
                                    System.out.println("队列满，等待有空余空间");  
                                    notFull.await();  
                                } catch (InterruptedException e) {  
                                    
                                    flag =false;  
                                }  
                            }  
                            queue.offer(1);        //每次插入一个元素  
                            notEmpty.signal();  
                            System.out.println("向队列取中插入一个元素，队列剩余空间："+(queueSize-queue.size()));  
                        } finally{  
                            lock.unlock();  
                        }  
                    }  
                }  
            }  
        }  

    