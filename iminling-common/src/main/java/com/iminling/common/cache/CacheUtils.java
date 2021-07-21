package com.iminling.common.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * 简单的缓存工具类, 改使用caffeine
 * @author yslao@outlook.com
 * @since 2021/2/19
 */
@Deprecated
public class CacheUtils {

    private CacheUtils(){}

    private static final Logger LOGGER = LoggerFactory.getLogger(CacheUtils.class);

    private static final Map<String, CacheData> CACHE_DATA = new ConcurrentHashMap<>();
    // 使用线程池去处理过期数据
    // private static final ScheduledExecutorService service = Executors.newScheduledThreadPool(1);

    /**
     * 获取数据
     * @param key key
     * @param load 加载方法
     * @param expire 毫秒
     * @param <T> 泛型
     * @return T
     */
    public static <T> T getData(String key, CacheLoad<T> load, int expire){
        T data = getData(key);
        if(data == null && load != null){
            data = load.load();
            if(data != null){
                setData(key,data,expire);
            }
        }
        return data;
    }

    /**
     * 获取数据
     * @param key key
     * @param <T> t
     * @return T
     */
    public static <T> T getData(String key){
        CacheData<T> data = CACHE_DATA.get(key);
        if(data != null && (data.getExpire() <= 0 || data.getSaveTime() >= System.currentTimeMillis())){
            return data.getData();
        }
        return null;
    }

    /**
     * 设置缓存
     * @param key key
     * @param data 数据
     * @param expire 过期时间，毫秒
     * @param <T> 泛型
     */
    public static <T> void setData(String key, T data, int expire){
        CACHE_DATA.put(key, new CacheData(data,expire));
    }
    public static void clear(String key){
        CACHE_DATA.remove(key);
    }
    public static void clearAll(){
        CACHE_DATA.clear();
    }

}
