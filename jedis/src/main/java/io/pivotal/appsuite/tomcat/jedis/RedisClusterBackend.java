package io.pivotal.appsuite.tomcat.jedis;

import io.pivotal.appsuite.tomcat.Backend;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.*;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Set;

/**
 * A {@link Backend} that uses a <a href="https://github.com/xetorthio/jedis" target="_new">Jedis</a> client to
 * manage mappings in a <a href="https://redis.io/" target="_new">Redis</a> datastore cluster.
 */
class RedisClusterBackend implements Backend {

    private final Logger log;

    // Key to Redis structure maintains session ID ist.
    private static final byte[] KEYS = "sessions".getBytes(Charset.forName("UTF-8"));

    private RedisClusterConfiguration  config;

    private JedisCluster jedisCluster;

    RedisClusterBackend(RedisClusterConfiguration config) {
        log = LoggerFactory.getLogger(getClass());
        this.config = config;
    }

    @Override
    public void init() {
        log.debug("initializing");
        log.debug("... host:port pairs -> {}", this.config.getHostPorts());
        log.debug("... password? -> {}", this.config.getPassword() != null ? "yes" : "no");
        log.debug("... connection timeout -> {}ms", this.config.getConnectionTimeout());
        log.debug("... connection pool size -> {}", this.config.getConnectionPoolSize());
    }

    @Override
    public void start() {
        log.debug("starting");
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(config.getConnectionPoolSize());
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);
        poolConfig.setTestWhileIdle(true);
        jedisCluster = new JedisCluster(
                (Set< HostAndPort>)null,
                config.getConnectionTimeout(),
                2000,
                5,
                config.getPassword(),
                poolConfig);
        log.debug("... redis client -> {}");
    }

    @Override
    public void stop() throws IOException {
        log.debug("stopping");
        jedisCluster.close();
        jedisCluster = null;
    }

    @Override
    public void put(byte[] key, byte[] value) throws IOException {
        log.debug("putting {}", new String(key));
        jedisCluster.set(key, value);
        jedisCluster.sadd(KEYS, key);
    }

    @Override
    public byte[] get(byte[] key) throws IOException {
        return jedisCluster.get(key);
    }

    @Override
    public void remove(byte[] key) throws IOException {
        jedisCluster.srem(KEYS, key);
        jedisCluster.del(key);
    }

    @Override
    public void clear() throws IOException {
        byte[][] keys = keys();
        jedisCluster.srem(KEYS, keys);
        jedisCluster.del(keys);
    }

    @Override
    public int size() throws IOException {
        return jedisCluster.scard(KEYS).intValue();
    }

    @Override
    public byte[][] keys() throws IOException {
        return jedisCluster.smembers(KEYS).toArray(new byte[0][]);
    }

}
