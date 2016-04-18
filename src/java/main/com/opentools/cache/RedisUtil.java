package com.opentools.cache;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.net.SocketTimeoutException;
import java.util.Map;

/**
 * Created by Aaron on 2016/4/17.
 */
public class RedisUtil {

    private static JedisPool jedisPool;

    static {

        jedisPool = new JedisPool("http://192.168.1.27");
    }

    /**
     * 获取连接
     * @return
     * @throws InterruptedException
     */
    public static Jedis getResource() {

        int count = 0;

        while (true) {

            try {

                Jedis jedis = jedisPool.getResource();
                return jedis;
            } catch (Exception e) {

                if (e instanceof JedisConnectionException || e instanceof SocketTimeoutException) {

                    count ++;
                    if (count > 5) break;

                } else {break;}
            }
        }
        return null;
    }

    /**
     * 返回连接
     * @param jedis
     */
    public static void returnResource(Jedis jedis) {

        jedisPool.returnBrokenResource(jedis);
    }

    /**
     * 新增一个key,value对
     * @param key
     * @param value
     */
    public static void set(String key, String value) {

        Jedis jedis = getResource();

        jedis.set(key, value);

        returnResource(jedis);
    }

    /**
     * 指定时间后删除
     * @param key
     * @param value
     * @param timeout 秒为单位
     */
    public static void setTimeOut(String key, String value, int timeout) {

        Jedis jedis = getResource();

        jedis.setex(key, timeout, value);

        returnResource(jedis);
    }
    
}