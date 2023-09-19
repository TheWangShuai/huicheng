package com.totainfo.eap.cp.util;


import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static com.totainfo.eap.cp.commdef.GenergicStatDef.Constant._SPACE;
import static com.totainfo.eap.cp.util.CacheMapUtils.CACHE_HOLD_TIME_1M;


/**
 * @author xiaobin.Guo
 * @date 2023年09月07日 13:29
 */
public class AsyncUtils {

    public static <T> T getResponse(String key, long timeout){
        T response = null;
        CompletableFuture<T> future =CompletableFuture.supplyAsync(() -> {
            T t = null;
            long nxTime = 0;
            long diffTime = 0;
            long crTime =System.currentTimeMillis();
            while (true && diffTime < timeout){
                t = (T) CacheMapUtils.get(key);
                if(t != null && !"".equals(t.toString())){
                    break;
                }
                try {
                    TimeUnit.MILLISECONDS.sleep(20);
                } catch (InterruptedException e) {
                    LogUtils.error("Key:[{}] Sleep Exception", key, e);
                    break;
                }
                nxTime = System.currentTimeMillis();
                diffTime = nxTime -crTime;
            }
            return t;
        });

        try {
            response = future.get();
        } catch (Exception e) {
           LogUtils.error("Future Get Exception, key:[{}]" ,key, e);
        }
        CacheMapUtils.remove(key);
        return response;
    }


    public static void setRequest(String key, long timeout){
        CacheMapUtils.put(key, _SPACE, timeout);
    }

    public static <T> void setResponse(String key, T response){
        CacheMapUtils.put(key, response, CACHE_HOLD_TIME_1M);
    }
}
