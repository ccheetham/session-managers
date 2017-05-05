package io.pivotal.appsuite.tomcat.jedis;

import io.pivotal.appsuite.tomcat.BackendStoreConfiguration;

/**
 * An interface defining configured {@link RedisStore} properties.
 */
public interface RedisConfiguration extends BackendStoreConfiguration {

    /**
     * Return the Redis host.
     * @return Redis host
     */
    String getHost();

    /**
     * Return the Redis port.
     * @return Redis port
     */
    int getPort();

    /**
     * Return the Redis database.
     * @return Redis database
     */
    int getDatabase();

    /**
     * Return the Redis password.
     * @return Redis password
     */
    String getPassword();

    /**
     * Return the Redis client connection timeout in milliseconds.
     * @return Redis client connection timeout
     */
    int getConnectionTimeout();

    /**
     * Return the Redis client connection pool size.
     * @return Redis client connection pool size
     */
    int getConnectionPoolSize();

}
