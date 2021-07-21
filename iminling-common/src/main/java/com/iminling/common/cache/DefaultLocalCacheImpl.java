package com.iminling.common.cache;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * caffeine默认实现，key为string,value也为string
 */
public class DefaultLocalCacheImpl extends AbstractLocalCache<String, String> {

    private static final DefaultLocalCacheImpl instance = new DefaultLocalCacheImpl();

    private DefaultLocalCacheImpl(){}

    public static DefaultLocalCacheImpl getInstance() {
        return instance;
    }

    public String get(String key) {
        final CompletableFuture<String> future = super.getCache().get(key);
        try {
            return future.get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    protected String loadData(String key) {
        return key + System.currentTimeMillis();
    }
}
