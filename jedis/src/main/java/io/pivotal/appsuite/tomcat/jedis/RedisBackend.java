package io.pivotal.appsuite.tomcat.jedis;

import io.pivotal.appsuite.tomcat.Backend;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Transaction;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Set;

/**
 * A {@link Backend} that uses a <a href="https://github.com/xetorthio/jedis" target="_new">Jedis</a> client to
 * manage mappings with a Redis backend.
 */
class RedisBackend implements Backend {

    private final Logger log;

    // Key to Redis structure maintains session ID ist.
    private static final byte[] KEYS = "sessions".getBytes(Charset.forName("UTF-8"));

    private RedisConfiguration config;

    private JedisPool jedisPool;

    RedisBackend(RedisConfiguration config) {
        log = LoggerFactory.getLogger(RedisBackend.class);
        this.config = config;
    }

    @Override
    public void init() {
        log.info("initializing");
        log.debug("... host -> {}", this.config.getHost());
        log.debug("... port -> {}", this.config.getPort());
        log.debug("... database -> {}", this.config.getDatabase());
        log.debug("... password? -> {}", this.config.getPassword() != null ? "yes" : "no");
        log.debug("... connection timeout -> {}ms", this.config.getConnectionTimeout());
        log.debug("... connection pool size -> {}", this.config.getConnectionPoolSize());
    }

    @Override
    public void start() {
        log.info("starting");
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
        log.debug("... redis client -> {}", jedisPool);
    }

    @Override
    public void stop() {
        log.info("stopping");
        jedisPool.close();
        jedisPool = null;
    }

    @Override
    public void put(byte[] key, byte[] value) throws IOException {
        Transaction t = jedisPool.getResource().multi();
        t.set(key, value);
        t.sadd(KEYS, key);
        t.exec();
    }

    @Override
    public byte[] get(byte[] key) throws IOException {
        return jedisPool.getResource().get(key);
    }

    @Override
    public void remove(byte[] key) throws IOException {
        Transaction t = jedisPool.getResource().multi();
        t.srem(KEYS, key);
        t.del(key);
        t.exec();
    }

    @Override
    public void clear() throws IOException {
        byte[][] keys = keySet().toArray(new byte[0][]);
        Transaction t = jedisPool.getResource().multi();
        t.srem(KEYS, keys);
        for (int i = 0; i < keys.length; ++i) {
            t.del(keys[i]);
        }
        t.exec();
    }

    @Override
    public int size() throws IOException {
        return jedisPool.getResource().scard(KEYS).intValue();
    }

    @Override
    public Set<byte[]> keySet() throws IOException {
        return jedisPool.getResource().smembers(KEYS);
    }
}
