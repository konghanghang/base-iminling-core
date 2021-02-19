package com.iminling.common;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

/**
 * @author yslao@outlook.com
 * @since 2020/11/26
 */
public class GlobalThreadPool {

    private GlobalThreadPool(){}

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalThreadPool.class);

    private static ExecutorService executor;

    private static final float BLOCKING_COEFFICIENT = 0.5F;

    static {
        init();
    }

    private static void init() {
        if (null != executor){
            executor.shutdownNow();
        }
        Integer globalThreadPoolCoreSize = (int)((float)Runtime.getRuntime().availableProcessors() / (1.0F - BLOCKING_COEFFICIENT));
        Integer globalThreadPoolMaxSize = 10;
        executor = new ThreadPoolExecutor(globalThreadPoolCoreSize, globalThreadPoolMaxSize, 120, TimeUnit.SECONDS,
                new SynchronousQueue<>(), new ThreadFactoryBuilder()
                .setNameFormat("global-thread-pool-%d")
                .setUncaughtExceptionHandler((t, e) -> LOGGER.error(e.getMessage(), e)).build());
        LOGGER.info("GlobalThreadPool init, globalThreadPoolCoreSize:{}, globalThreadPoolMaxSize:{}", globalThreadPoolCoreSize, globalThreadPoolMaxSize);
    }

    public static ExecutorService getExecutor(){
        return executor;
    }

    public static void execute(Runnable runnable){
        try {
            executor.execute(runnable);
        } catch (Exception e){
            LOGGER.error(e.getMessage(), e);
        }
    }

    public static <T> Future<T> submit(Callable<T> task){
        return executor.submit(task);
    }

    public static Future<?> submit(Runnable runnable){
        return executor.submit(runnable);
    }

    public static synchronized void shutDown(){
        if (executor != null){
            executor.shutdown();
            try {
                if (executor.awaitTermination(2, TimeUnit.SECONDS)){
                    executor.shutdownNow();
                    if (executor.awaitTermination(3, TimeUnit.SECONDS)){
                        LOGGER.warn("GlobalThreadPool did not terminate");
                    }
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

}
