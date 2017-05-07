package io.pivotal.appsuite.tomcat.jedis;

import io.pivotal.appsuite.tomcat.Backend;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Transaction;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Set;

/**
 * A {@link Backend} that uses a <a href="https://github.com/xetorthio/jedis" target="_new">Jedis</a> client to
 * manage mappings in a <a href="https://redis.io/" target="_new">Redis</a> datastore.
 */
public class RedisBackend implements Backend {

    private final Logger log;

    // Key to Redis structure maintains session ID ist.
    private static final byte[] KEYS = "sessions".getBytes(Charset.forName("UTF-8"));

    private RedisConfiguration config;

    private JedisPool jedisPool;

    public RedisBackend(RedisConfiguration config) {
        log = LoggerFactory.getLogger(getClass());
        this.config = config;
    }

    @Override
    public void init() {
        log.debug("host -> {}", this.config.getHost());
        log.debug("port -> {}", this.config.getPort());
        log.debug("database -> {}", this.config.getDatabase());
        log.debug("password? -> {}", this.config.getPassword() != null ? "yes" : "no");
        log.debug("connection timeout -> {}ms", this.config.getConnectionTimeout());
        log.debug("connection pool size -> {}", this.config.getConnectionPoolSize());
    }

    @Override
    public void start() {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(config.getConnectionPoolSize());
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);
        poolConfig.setTestWhileIdle(true);
        jedisPool = new JedisPool(
                poolConfig,
                config.getHost(),
                config.getPort(),
                config.getConnectionTimeout(),
                config.getPassword(),
                config.getDatabase());
    }

    @Override
    public void stop() {
        jedisPool.close();
        jedisPool = null;
    }

    @Override
    public void put(byte[] key, byte[] value) throws IOException {
        try (Jedis j = jedisPool.getResource()) {
            Transaction t = j.multi();
            t.set(key, value);
            t.sadd(KEYS, key);
            t.exec();
        }
    }

    @Override
    public byte[] get(byte[] key) throws IOException {
        try (Jedis j = jedisPool.getResource()) {
            return j.get(key);
        }
    }

    @Override
    public void remove(byte[] key) throws IOException {
        try (Jedis j = jedisPool.getResource()) {
            Transaction t = j.multi();
            t.srem(KEYS, key);
            t.del(key);
            t.exec();
        }
    }

    @Override
    public void clear() throws IOException {
        byte[][] keys = keys();
        try (Jedis j = jedisPool.getResource()) {
            Transaction t = j.multi();
            t.srem(KEYS, keys);
            t.del(keys);
            t.exec();
        }
    }

    @Override
    public int size() throws IOException {
        try (Jedis j = jedisPool.getResource()) {
            return j.scard(KEYS).intValue();
        }
    }

    @Override
    public byte[][] keys() throws IOException {
        try (Jedis j = jedisPool.getResource()) {
            return j.smembers(KEYS).toArray(new byte[0][]);
        }
    }

}
