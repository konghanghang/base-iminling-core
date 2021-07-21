package com.iminling.common.cache;

import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * caffeine缓存抽象类
 * @param <K>
 * @param <V>
 */
public abstract class AbstractLocalCache<K, V> {

    protected final AsyncLoadingCache<K, V> cache;

    public AbstractLocalCache() {
        this(300, 60, 1000);
    }

    private String getClassName() {
        return this.getClass().getSimpleName();
    }

    public AbstractLocalCache(int expireTime, int refreshTime, int maximumSize) {
        ThreadFactory factory = (new ThreadFactoryBuilder()).setNameFormat(this.getClassName() + "-local-cache-thread-%d").build();
        this.cache = Caffeine.newBuilder()
            .maximumSize(maximumSize)
            .expireAfterAccess(expireTime, TimeUnit.SECONDS)
            .refreshAfterWrite(refreshTime, TimeUnit.SECONDS)
            .executor(Executors.newSingleThreadExecutor(factory))
            .recordStats()
            .buildAsync((key, executor) -> CompletableFuture.supplyAsync(() -> this.loadData(key), executor));
    }

    public AsyncLoadingCache<K, V> getCache() {
        return this.cache;
    }

    protected abstract V loadData(K key);

}
