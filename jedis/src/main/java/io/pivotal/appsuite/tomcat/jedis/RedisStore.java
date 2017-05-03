package io.pivotal.appsuite.tomcat.jedis;

import io.pivotal.appsuite.tomcat.Backend;
import io.pivotal.appsuite.tomcat.BackendStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;

/**
 * An Apache Tomcat session store using a <a href="https://github.com/xetorthio/jedis" target="_new">Jedis</a> client
 * to connect to a <a href="https://redis.io/" target="_new">Redis</a> data store.
 */
public class RedisStore extends BackendStore implements RedisManagement {

    private final Logger logger;

    private final RedisBackend backend;

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
        logger = LoggerFactory.getLogger(getClass());
        backend = new RedisBackend(this);
        host = Protocol.DEFAULT_HOST;
        port = Protocol.DEFAULT_PORT;
        database = Protocol.DEFAULT_DATABASE;
        connectionTimeout = Protocol.DEFAULT_TIMEOUT;
        connectionPoolSize = JedisPoolConfig.DEFAULT_MAX_TOTAL;
        password = null;
    }

    @Override
    public Backend getBackend() {
        return backend;
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
        logger.debug("setting host to {}", host);
        this.host = host;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public void setPort(int port) {
        logger.debug("setting port to {}", port);
        this.port = port;
    }

    @Override
    public int getDatabase() {
        return database;
    }

    @Override
    public void setDatabase(int datbase) {
        logger.debug("setting database to {}", database);
        this.database = database;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public void setPassword(String password) {
        logger.debug("setting password to {}", password);
        this.password = password;
    }

    @Override
    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    @Override
    public void setConnectionTimeout(int timeout) {
        logger.debug("setting timeout to {}", timeout);
        this.connectionTimeout = timeout;
    }

    @Override
    public int getConnectionPoolSize() {
        return connectionPoolSize;
    }

    @Override
    public void setConnectionPoolSize(int size) {
        logger.debug("setting connection pool size to {}", size);
        this.connectionPoolSize = size;
    }

}
