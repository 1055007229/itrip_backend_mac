package cn.itrip.common;

import redis.clients.jedis.Jedis;

public class RedisTest {
    public static void main(String[] args) {
        Jedis jedis = new Jedis("192.168.3.7:6379");
        System.out.println(jedis.ping());
    }
}
