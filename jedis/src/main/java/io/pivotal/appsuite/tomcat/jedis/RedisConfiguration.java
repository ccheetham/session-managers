package io.pivotal.appsuite.tomcat.jedis;

import io.pivotal.appsuite.tomcat.BackendStoreConfiguration;

/**
 * An interface defining configured {@link RedisStore} properties.
 */
public interface RedisConfiguration extends BackendStoreConfiguration {

    /**
     * Return the Redis datastore host name.
     *
     * @return datastore host name
     */
    String getHost();

    /**
     * Return the Redis datastore port.
     *
     * @return datastore port
     */
    int getPort();

    /**
     * Return the Redis datastore database number.
     *
     * @return datastore database number
     */
    int getDatabase();

    /**
     * Return the Redis datastore password.
     *
     * @return datastore password
     */
    String getPassword();

    /**
     * Return the Redis client connection timeout in milliseconds.
     *
     * @return client connection timeout
     */
    int getConnectionTimeout();

    /**
     * Return the Redis client connection pool size.
     *
     * @return client connection pool size
     */
    int getConnectionPoolSize();

}
