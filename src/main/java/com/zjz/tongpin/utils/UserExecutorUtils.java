package com.zjz.tongpin.utils;

import java.util.concurrent.*;

public class UserExecutorUtils {

    private ExecutorService executorService;

    public UserExecutorUtils() {
        this.executorService = new ThreadPoolExecutor(
                16, 1000, 10000, TimeUnit.MINUTES, new ArrayBlockingQueue<>(10000)
        );
    }
}
