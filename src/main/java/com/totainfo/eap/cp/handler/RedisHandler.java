package com.totainfo.eap.cp.handler;

import com.totainfo.eap.cp.util.LogUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.core.ScanOptions.ScanOptionsBuilder;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * redis cache 工具类
 *
 */
@Component
public final class RedisHandler {

    private  static RedisTemplate<String, Object> redisTemplate;

    /**
    * 指定缓存失效时间
    * @param key 键
    * @param time 时间(秒)
    * @return
    */
    public static boolean expire(String key, long time) {
        if (time > 0) {
            redisTemplate.expire(key, time, TimeUnit.SECONDS);
        }
        return true;
    }

    /**
    * 根据key 获取过期时间
    * @param key 键 不能为null
    * @return 时间(秒) 返回0代表为永久有效
    */
    public static long getExpire(String key) {
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    /**
    * 判断key是否存在
    * @param key 键
    * @return true 存在 false不存在
    */
    public static boolean exists(String key) {
        return redisTemplate.hasKey(key);
    }


    /**
    * 删除缓存
    * @param key 可以传一个值 或多个
    */
    @SuppressWarnings("unchecked")
    public static void remove(String... key) {
        if (key != null && key.length > 0) {
            if (key.length == 1) {
                redisTemplate.delete(key[0]);
            } else {
                redisTemplate.delete(Arrays.asList(key));
            }
        }
    }

    /**
    * 普通缓存获取
    * @param key 键
    * @return 值
    */
    public static <T>  T get(String key) {
        long crTime = System.currentTimeMillis();
        if(key == null){
            return null;
        }
        T t = (T) redisTemplate.opsForValue().get(key);
        long nxTime = System.currentTimeMillis();
        long diffTime = nxTime - crTime;
        if(diffTime >1000){
            LogUtils.warn("Redis 查询KEY:[{}]耗时:[{}ms]", key, diffTime);
        }
        return t;
    }


    public static Set<String> getKeys(final String key) {
        return redisTemplate.keys(key);
    }


    /**
     * @param key 键
     * @param value 值
     * @return true成功 false失败
     */
    public static boolean set(String key, Object value) {
        long crTime = System.currentTimeMillis();
        redisTemplate.opsForValue().set(key, value);
        long nxTime = System.currentTimeMillis();
        long diffTime = nxTime - crTime;
        if(diffTime >1000){
            LogUtils.warn("Redis 保存KEY:[{}]耗时:[{}ms]", key, diffTime);
        }
        return true;
    }


    /**
    * 普通缓存放入并设置时间
    * @param key 键
    * @param value 值
    * @param time 时间(秒) time要大于0 如果time小于等于0 将设置无限期
    * @return true成功 false 失败
    */
    public static boolean set(String key, Object value, long time) {
        if (time > 0) {
            redisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
        } else {
            set(key, value);
        }
        return true;
    }

    public static boolean setnx(String key, Object value, long time){
        if (time > 0) {
             return redisTemplate.opsForValue().setIfAbsent(key, value, time, TimeUnit.SECONDS);
        } else {
            return redisTemplate.opsForValue().setIfAbsent(key, value);
        }

    }

    /**
    * 递增
    * @param key 键
    * @param delta 要增加几(大于0)
    * @return
    */
    public static long incr(String key, long delta) {
        if (delta < 0) {
            throw new RuntimeException("递增因子必须大于0");
        }
        return redisTemplate.opsForValue().increment(key, delta);
    }


    /**
    * 递减
    * @param key 键
    * @param delta 要减少几(小于0)
    * @return
    */
    public static long decr(String key, long delta) {
        if (delta < 0) {
            throw new RuntimeException("递减因子必须大于0");
        }
        return redisTemplate.opsForValue().increment(key, -delta);
    }


    /**
    * HashGet
    * @param key 键 不能为null
    * @param item 项 不能为null
    * @return 值
    */
    public static <T> T hget(String key, String item) {
        return (T) redisTemplate.opsForHash().get(key, item);
    }


    /**
    * 获取hashKey对应的所有键值
    * @param key 键
    * @return 对应的多个键值
    */
    public static <K, V> Map<K, V> hmget(String key) {
        long crTime = System.currentTimeMillis();
        Map<K,V> map= (Map<K, V>) redisTemplate.opsForHash().entries(key);
        long nxTime = System.currentTimeMillis();
        long diffTime = nxTime - crTime;
        if((nxTime-crTime) >1000){
            LogUtils.warn("Redis 查询MAP KEY:[{}]耗时:["+(nxTime-crTime)+"ms]", key, diffTime);
        }
        return map;
    }

    public static <T> List<T> hvalue(String key){
        long crTime = System.currentTimeMillis();
        List<T> list = (List<T>) redisTemplate.opsForHash().values(key);
        long nxTime = System.currentTimeMillis();
        long diffTime = nxTime - crTime;
        if((nxTime-crTime) >1000){
            LogUtils.warn("Redis [hvalue] KEY:[{}]耗时:[{}ms]", key, diffTime);
        }
        return list;
    }


    /**
    * HashSet
    * @param key 键
    * @param map 对应多个键值
    * @return true 成功 false 失败
    */
    public static <T> boolean hmset(String key, Map<String, T> map) {
        long crTime = System.currentTimeMillis();
        redisTemplate.opsForHash().putAll(key, map);
        long nxTime = System.currentTimeMillis();
        long diffTime = nxTime- crTime;
        if(diffTime >1000){
            LogUtils.warn("Redis MAP Set KEY:[{}]耗时:[{}ms]", key, diffTime);
        }
        return true;
    }


    /**
    * HashSet 并设置时间
    * @param key 键
    * @param map 对应多个键值
    * @param time 时间(秒)
    * @return true成功 false失败
    */
    public static <T> boolean hmset(String key, Map<String, T> map, long time) {
        long crTime = System.currentTimeMillis();
        redisTemplate.opsForHash().putAll(key, map);
        if (time > 0) {
            expire(key, time);
        }
        long nxTime = System.currentTimeMillis();
        long diffTime = nxTime- crTime;
        if(diffTime >1000){
            LogUtils.warn("Redis MAP Set KEY:[{}]耗时:[{}ms]", key, diffTime);
        }
        return true;
    }


    /**
    * 向一张hash表中放入数据,如果不存在将创建
    * @param key 键
    * @param item 项
    * @param value 值
    * @return true 成功 false失败
    */
    public static <T> boolean hset(String key, String item, T value) {
        long crTime = System.currentTimeMillis();
        redisTemplate.opsForHash().put(key, item, value);
        long nxTime = System.currentTimeMillis();
        long diffTime = nxTime- crTime;
        if(diffTime >1000){
            LogUtils.warn("Redis MAP Set KEY:[{}]耗时:[{}ms]", key, diffTime);
        }
        return true;
    }


    /**
    * 向一张hash表中放入数据,如果不存在将创建
    * @param key 键
    * @param item 项
    * @param value 值
    * @param time 时间(秒) 注意:如果已存在的hash表有时间,这里将会替换原有的时间
    * @return true 成功 false失败
    */
    public static <T> boolean hset(String key, String item, T value, long time) {
        redisTemplate.opsForHash().put(key, item, value);
        if (time > 0) {
            expire(key, time);
        }
        return true;
    }


    /**
    * 删除hash表中的值
    * @param key 键 不能为null
    * @param item 项 可以使多个 不能为null
    */
    public static void hdel(String key, Object... item) {
        redisTemplate.opsForHash().delete(key, item);
    }


    /**
    * 判断hash表中是否有该项的值
    * @param key 键 不能为null
    * @param item 项 不能为null
    * @return true 存在 false不存在
    */
    public static boolean hHasKey(String key, String item) {
        return redisTemplate.opsForHash().hasKey(key, item);
    }


    /**
    * hash递增 如果不存在,就会创建一个 并把新增后的值返回
    * @param key 键
    * @param item 项
    * @param by 要增加几(大于0)
    * @return
    */
    public static double hincr(String key, String item, double by) {
        return redisTemplate.opsForHash().increment(key, item, by);
    }


    /**
    * hash递减
    * @param key 键
    * @param item 项
    * @param by 要减少记(小于0)
    * @return
    */
    public static double hdecr(String key, String item, double by) {
        return redisTemplate.opsForHash().increment(key, item, -by);
    }


    /**
    * 根据key获取Set中的所有值
    * @param key 键
    * @return
    */
    public static Set<Object> sGet(String key) {
        try {
            return redisTemplate.opsForSet().members(key);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
    * 根据value从一个set中查询,是否存在
    * @param key 键
    * @param value 值
    * @return true 存在 false不存在
    */
    public static boolean sHasKey(String key, Object value) {
        try {
            return redisTemplate.opsForSet().isMember(key, value);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
    * 将数据放入set缓存
    * @param key 键
    * @param values 值 可以是多个
    * @return 成功个数
    */
    public static long sSet(String key, Object... values) {
        try {
            return redisTemplate.opsForSet().add(key, values);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }


    /**
    * 将set数据放入缓存
    * @param key 键
    * @param time 时间(秒)
    * @param values 值 可以是多个
    * @return 成功个数
    */
    public static long sSetAndTime(String key, long time, Object... values) {
        try {
            Long count = redisTemplate.opsForSet().add(key, values);
            if (time > 0)
            expire(key, time);
            return count;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }


    /**
    * 获取set缓存的长度
    * @param key 键
    * @return
    */
    public static long sGetSetSize(String key) {
        try {
            return redisTemplate.opsForSet().size(key);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }



    /**
    * 移除值为value的
    * @param key 键
    * @param values 值 可以是多个
    * @return 移除的个数
    */
    public static long setRemove(String key, Object... values) {
        try {
            Long count = redisTemplate.opsForSet().remove(key, values);
            return count;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }



    /**
    * 获取list缓存的内容
    * @param key 键
    * @param start 开始
    * @param end 结束 0 到 -1代表所有值
    * @return
    */
    public static List<Object> lGet(String key, long start, long end) {
        try {
            return redisTemplate.opsForList().range(key, start, end);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }




    /**
    * 获取list缓存的长度
    * @param key 键
    * @return
    */
    public static long lGetListSize(String key) {
        try {
            return redisTemplate.opsForList().size(key);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }



    /**
    * 通过索引 获取list中的值
    * @param key 键
    * @param index 索引 index>=0时， 0 表头，1 第二个元素，依次类推；index<0时，-1，表尾，-2倒数第二个元素，依次类推
    * @return
    */
    public static <T> T lGetIndex(String key, long index) {
        return (T) redisTemplate.opsForList().index(key, index);
    }



    /**
    * 将list放入缓存
    * @param key 键
    * @param value 值
    * @return
    */
    public static <T> boolean lSet(String key, T value) {

        redisTemplate.opsForList().rightPush(key, value);
        return true;

    }


    /**
    * 将list放入缓存
    * @param key 键
    * @param value 值
    * @param time 时间(秒)
    * @return
    */
    public static boolean lSet(String key, Object value, long time) {

        redisTemplate.opsForList().rightPush(key, value);
        if (time > 0)
        expire(key, time);
        return true;

    }


    /**
    * 将list放入缓存
    * @param key 键
    * @param value 值
    * @return
    */
    public static boolean lSet(String key, List<Object> value) {
        try {
            redisTemplate.opsForList().rightPushAll(key, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
    * 将list放入缓存
    *
    * @param key 键
    * @param value 值
    * @param time 时间(秒)
    * @return
    */
    public static boolean lSet(String key, List<Object> value, long time) {
        try {
            redisTemplate.opsForList().rightPushAll(key, value);
            if (time > 0)
            expire(key, time);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }



    /**
    * 根据索引修改list中的某条数据
    * @param key 键
    * @param index 索引
    * @param value 值
    * @return
    */
    public static boolean lUpdateIndex(String key, long index, Object value) {
        try {
            redisTemplate.opsForList().set(key, index, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    /**
    * 移除N个值为value
    * @param key 键
    * @param count 移除多少个
    * @param value 值
    * @return 移除的个数
    */
    public static long lRemove(String key, long count, Object value) {
        try {
            Long remove = redisTemplate.opsForList().remove(key, count, value);
            return remove;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }


    /**
     * 删除对应的value
     *
     * @param key
     */
    public static void remove(String key) {
        if (exists(key)) {
            redisTemplate.delete(key);
        }
    }

    /**
     *
     * 有序集合:存储，值为有序集合，以score排序(long类型)，除时间外
     */

    public static <T> boolean zadd(final String key, T t) {
        long timeStamp=System.currentTimeMillis();
        ZSetOperations<String, Object> operations = redisTemplate.opsForZSet();
        return operations.add(key, t, timeStamp);
    }

    public static <T> boolean zadd(String key, T o, long index){
        ZSetOperations<String, Object> operations = redisTemplate.opsForZSet();
        return operations.add(key, o, index);
    }

    public static boolean zadd(final String key, Object o, double index ){
        ZSetOperations<String, Object> operations = redisTemplate.opsForZSet();
        return operations.add(key, o, index);
    }


    public static <T> List<T> zgetSortedByRange(final String key,double startRange, double endRange){
        ZSetOperations<String, Object> operations = redisTemplate.opsForZSet();
        Set<Object> objects = operations.reverseRangeByScore(key, startRange, endRange);
        return (List<T>) objects.stream().collect(Collectors.toList());
    }


    public static <T> List<T> zgetByKey(final String key) {
        List<T> objList = new ArrayList<>();
        ZSetOperations<String, Object> operations = redisTemplate.opsForZSet();
        Set<Object> objects = operations.reverseRange(key, 0, -1);
        if(objects != null){
            objList = (List<T>) objects.stream().collect(Collectors.toList());
        }
        return objList;
    }



    /*list 操作*/
    public static Long getListSize(final String key){
        return redisTemplate.opsForList().size(key);
    }

    public static <T> void listSet(final String key, long index, T t){
        redisTemplate.opsForList().set(key, index, t);
    }

    public static <T> void listSetRight(final String key, T value) {
        redisTemplate.opsForList().rightPush(key, value);
    }

    public static <T> void listSetLeft(final String key, T value){
        redisTemplate.opsForList().leftPush(key, value);
    }

    public static <T> List<T> getList(final String key, int start, int end){
        return (List<T>) redisTemplate.opsForList().range(key, start, end);
    }

    public static <T> T getList(final String key, long index){
        return (T) redisTemplate.opsForList().index(key, index);
    }

    public static <T> void removeList(final String key,int i, T value){
        redisTemplate.opsForList().remove(key, i, value);
    }

    @SuppressWarnings("unchecked")
    public static <T> T removeListFirst(final String key){
        return (T) redisTemplate.opsForList().leftPop(key);
    }

    public static <T> T removeListLast(final String key){
        return (T) redisTemplate.opsForList().rightPop(key);
    }

    /**
     *
     * 哈希类型:存储
     */
    public static boolean hashSet(final String key, String hashKey, Object object) {
        Boolean result = false;

        HashOperations<String, Serializable, Object> operations = redisTemplate.opsForHash();

        try {
            operations.put(key, hashKey, object);
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    /**
     *
     * 哈希类型:存储
     */
    public boolean hashSetByMap(final String key, Map<String, String> map) {
        Boolean result = false;

        HashOperations<String, Serializable, Object> operations = redisTemplate.opsForHash();

        try {
            operations.putAll(key, map);
            result = true;
        } catch (Exception e) {
           e.printStackTrace();
        }

        return result;
    }


    /**
     *
     * 哈希类型:取值
     */
    public static List<Object> hashGetByMutiHashKey(final String key, Collection<Serializable> hashKeys) {

        HashOperations<String, Serializable, Object> operations = redisTemplate.opsForHash();
        List<Object> objects = operations.multiGet(key, hashKeys);
        return objects;
    }



    public static void deleteAll(Set<String> keys){
        redisTemplate.delete(keys);
    }



    public Properties getRedisSpace(){
           return redisTemplate.getRequiredConnectionFactory().getConnection().info("memory");
    }


    @Autowired
    public void setRedisTemplate(RedisTemplate<String, Object> redisTemplate) {
        RedisHandler.redisTemplate = redisTemplate;
    }

    public static RedisTemplate<String, Object> getRedisTemplate() {
        return RedisHandler.redisTemplate;
    }
}
