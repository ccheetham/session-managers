package io.pivotal.appsuite.tomcat.jedis;

import io.pivotal.appsuite.tomcat.Backend;
import io.pivotal.appsuite.tomcat.BackendStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;

/**
 * An Apache Tomcat session store backended by <a href="https://redis.io/" target="_new">Redis</a> data store.
 */
public class RedisStore extends BackendStore implements RedisManagement {

    private final Logger log;

    // RedisManagement fields
    private String host;
    private int port;
    private int database;
    private String password;
    private int connectionTimeout;
    private int connectionPoolSize;

    /**
     * Create a new {@code RedisStore}.
     */
    public RedisStore() {
        log = LoggerFactory.getLogger(getClass());
        host = Protocol.DEFAULT_HOST;
        port = Protocol.DEFAULT_PORT;
        database = Protocol.DEFAULT_DATABASE;
        connectionTimeout = Protocol.DEFAULT_TIMEOUT;
        connectionPoolSize = JedisPoolConfig.DEFAULT_MAX_TOTAL;
        password = null;
        log.info(getDescription());
    }

    @Override
    public Backend createBackend() {
        return new RedisBackend(this);
    }

    // -----------------------------------------------------------------------
    //   RedisManagement implementation
    // -----------------------------------------------------------------------


    @Override
    public String getHost() {
        return host;
    }

    @Override
    public void setHost(String host) {
        log.debug("setting host to {}", host);
        this.host = host;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public void setPort(int port) {
        log.debug("setting port to {}", port);
        this.port = port;
    }

    @Override
    public int getDatabase() {
        return database;
    }

    @Override
    public void setDatabase(int datbase) {
        log.debug("setting database to {}", database);
        this.database = database;
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
