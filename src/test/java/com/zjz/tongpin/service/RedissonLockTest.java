package com.zjz.tongpin.service;

import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@SpringBootTest
public class RedissonLockTest {
    
    @Resource
    private RedissonClient redissonClient;

    @Test
    public void test() throws InterruptedException {
        Thread[] threads = new Thread[5];
        RLock lock = redissonClient.getLock("zjz");
        for (int i = 0; i < 5; i++) {
            threads[i] =new Thread(() -> {
                try {
                    boolean tryLock = lock.tryLock(5, -1, TimeUnit.MILLISECONDS);
                    if (tryLock){
                        System.out.println("✅ 加锁成功:"+Thread.currentThread().getName());
                        System.out.println("业务逻辑执行中...");
                        Thread.sleep(20000);
                    }else {
                        System.out.println("❌ 加锁失败"+Thread.currentThread().getName());
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    if(lock.isHeldByCurrentThread()){
                        lock.unlock();
                        System.out.println("🔓 锁已释放");
                    }
                }
            });
        }

        for (int i = 0; i < 5; i++) {
            threads[i].start();
        }

        for (int i = 0; i < 5; i++) {
            threads[i].join();
        }
        System.out.println("🎉 所有线程执行完毕");
    }


    /**
     * 测试1：基本加锁解锁
     */
    @Test
    public void testBasicLock() {
        // 获取锁对象
        RLock lock = redissonClient.getLock("test:basic:lock");
        
        try {
            // 尝试加锁（立即返回，看门狗自动续期）
            boolean locked = lock.tryLock(0, -1, TimeUnit.SECONDS);
            
            if (locked) {
                System.out.println("✅ 加锁成功");
                
                // 模拟业务逻辑
                System.out.println("业务逻辑执行中...");
                Thread.sleep(20000);
                
            } else {
                System.out.println("❌ 加锁失败");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            // 释放锁
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                System.out.println("🔓 锁已释放");
            }
        }
    }
    
    /**
     * 测试2：看门狗机制验证
     * 观察锁在长时间执行业务时是否会自动续期
     */
    @Test
    public void testWatchdog() throws InterruptedException {
        RLock lock = redissonClient.getLock("test:watchdog:lock");
        
        try {
            System.out.println("⏰ 开始加锁...");
            boolean locked = lock.tryLock(0, -1, TimeUnit.SECONDS);
            
            if (locked) {
                System.out.println("✅ 加锁成功，锁将在 30 秒后过期（看门狗会续期）");
                
                // 模拟长时间业务（40秒，超过默认的30秒过期时间）
                for (int i = 1; i <= 40; i++) {
                    Thread.sleep(1000);
                    if (i % 10 == 0) {
                        System.out.println("⏱️  已执行 " + i + " 秒，锁仍然有效：" + lock.isLocked());
                    }
                }
                
                System.out.println("✅ 业务执行完成（40秒），锁仍然有效，证明看门狗在工作！");
            }
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                System.out.println("🔓 锁已释放");
            }
        }
    }
    
    /**
     * 测试3：并发抢锁（模拟多个线程竞争）
     */
    @Test
    public void testConcurrentLock() throws InterruptedException {
        String lockKey = "test:concurrent:lock";
        
        // 创建 5 个线程同时抢锁
        Thread[] threads = new Thread[5];
        for (int i = 0; i < 5; i++) {
            int threadNum = i;
            threads[i] = new Thread(() -> {
                RLock lock = redissonClient.getLock(lockKey);
                try {
                    System.out.println("🔵 线程 " + threadNum + " 尝试获取锁...");
                    
                    // 最多等待 3 秒，获取后 10 秒自动释放
                    boolean locked = lock.tryLock(3, 10, TimeUnit.SECONDS);
                    
                    if (locked) {
                        System.out.println("✅ 线程 " + threadNum + " 获取锁成功！");
                        
                        // 模拟业务
                        Thread.sleep(2000);
                        System.out.println("⚙️  线程 " + threadNum + " 执行业务中...");
                        
                    } else {
                        System.out.println("❌ 线程 " + threadNum + " 获取锁超时");
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    if (lock.isHeldByCurrentThread()) {
                        lock.unlock();
                        System.out.println("🔓 线程 " + threadNum + " 释放锁");
                    }
                }
            });
        }
        
        // 启动所有线程
        for (Thread thread : threads) {
            thread.start();
        }
        
        // 等待所有线程执行完毕
        for (Thread thread : threads) {
            thread.join();
        }
        
        System.out.println("🎉 所有线程执行完毕");
    }
    
    /**
     * 测试4：可重入锁（同一线程可以多次获取同一把锁）
     */
    @Test
    public void testReentrantLock() {
        RLock lock = redissonClient.getLock("test:reentrant:lock");
        
        try {
            System.out.println("🔑 第一次加锁...");
            lock.lock();
            System.out.println("✅ 第一次加锁成功");
            
            System.out.println("🔑 第二次加锁（可重入）...");
            lock.lock();
            System.out.println("✅ 第二次加锁成功");
            
            System.out.println("🔑 第三次加锁（可重入）...");
            lock.lock();
            System.out.println("✅ 第三次加锁成功");
            
            System.out.println("🔓 第一次解锁");
            lock.unlock();
            
            System.out.println("🔓 第二次解锁");
            lock.unlock();
            
            System.out.println("🔓 第三次解锁");
            lock.unlock();
            
            System.out.println("✅ 锁已完全释放：" + !lock.isLocked());
            
        } finally {
            // 确保锁被释放
            while (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
    
    /**
     * 测试5：自旋锁（你的项目中的实现方式）
     */
    @Test
    public void testSpinLock() {
        RLock lock = redissonClient.getLock("test:spin:lock");
        Thread currentThread = Thread.currentThread();
        
        try {
            System.out.println("🔄 开始自旋获取锁...");
            
            while (true) {
                // 尝试获取锁（不等待，立即返回）
                if (lock.tryLock(0, -1, TimeUnit.MILLISECONDS)) {
                    System.out.println("✅ 获取锁成功！");
                    System.out.println("当前线程ID: " + currentThread.getId());
                    
                    // 模拟业务
                    Thread.sleep(3000);
                    System.out.println("业务执行完成");
                    
                    break; // 退出循环
                }
                
                // 未获取到锁，等待 100ms 后重试
                System.out.println("⏳ 未获取到锁，等待 100ms 后重试...");
                Thread.sleep(100);
            }
            
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                System.out.println("🔓 锁已释放");
            }
        }
    }
    
    /**
     * 测试6：带超时的锁（防止永久等待）
     */
    @Test
    public void testLockWithTimeout() {
        RLock lock = redissonClient.getLock("test:timeout:lock");
        
        try {
            System.out.println("⏱️  尝试获取锁（最多等待 3 秒）...");
            
            // 最多等待 3 秒，获取后 30 秒自动释放
            boolean locked = lock.tryLock(3, 30, TimeUnit.SECONDS);
            
            if (locked) {
                System.out.println("✅ 获取锁成功");
                
                // 模拟业务
                Thread.sleep(2000);
                
            } else {
                System.out.println("❌ 获取锁超时，系统繁忙");
                throw new RuntimeException("系统繁忙，请稍后重试");
            }
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("⚠️  线程被中断");
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                System.out.println("🔓 锁已释放");
            }
        }
    }
}
