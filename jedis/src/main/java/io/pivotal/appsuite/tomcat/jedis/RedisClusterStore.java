package io.pivotal.appsuite.tomcat.jedis;

import io.pivotal.appsuite.tomcat.Backend;
import io.pivotal.appsuite.tomcat.BackendStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;

/**
 * An Apache Tomcat session store backended by <a href="https://redis.io/" target="_new">Redis</a> datastore cluster.
 */
public class RedisClusterStore extends BackendStore implements RedisClusterManagement {

    private final Logger log;

    // RedisClusterManagement fields
    private String hostPorts;
    private String password;
    private int connectionTimeout;
    private int connectionPoolSize;

    /**
     * Create a new {@code RedisClusterStore}.
     */
    public RedisClusterStore() {
        log = LoggerFactory.getLogger(getClass());
        hostPorts = Protocol.DEFAULT_HOST;
        connectionTimeout = Protocol.DEFAULT_TIMEOUT;
        connectionPoolSize = JedisPoolConfig.DEFAULT_MAX_TOTAL;
        password = null;
        log.info(getDescription());
    }

    @Override
    public Backend createBackend() {
        return new RedisClusterBackend(this);
    }

    // -----------------------------------------------------------------------
    //   RedisClusterManagement implementation
    // -----------------------------------------------------------------------


    @Override
    public String getHostPorts() {
        return hostPorts;
    }

    @Override
    public void setHostPorts(String hostPorts) {

    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public void setPassword(String password) {
        log.debug("setting password to {}", password);
        this.password = password;
    }

    @Override
    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    @Override
    public void setConnectionTimeout(int timeout) {
        log.debug("setting timeout to {}", timeout);
        this.connectionTimeout = timeout;
    }

    @Override
    public int getConnectionPoolSize() {
        return connectionPoolSize;
    }

    @Override
    public void setConnectionPoolSize(int size) {
        log.debug("setting connection pool size to {}", size);
        this.connectionPoolSize = size;
    }

}
