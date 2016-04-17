package com.opentools.cache;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.net.SocketTimeoutException;

/**
 * Created by Aaron on 2016/4/17.
 */
public class RedisUtil {

    /**
     * 获取连接
     * @param jedisPool
     * @return
     * @throws InterruptedException
     */
    public static Jedis getResource(JedisPool jedisPool) throws InterruptedException {

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
     * @param jedisPool
     */
    public static void returnResource(Jedis jedis, JedisPool jedisPool) {

        jedisPool.returnBrokenResource(jedis);
    }
}