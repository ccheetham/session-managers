package io.pivotal.appsuite.tomcat.jedis;

/**
 * An interface defining configurable {@link RedisStore} properties.
 */
public interface RedisManagement extends RedisConfiguration {

    /**
     * Set the Redis datastore host name.
     *
     * @param host datastore host name
     */
    void setHost(String host);

    /**
     * Set the Redis datastore port.
     *
     * @param port datastore port
     */
    void setPort(int port);

    /**
     * Set the Redis datastore database number.
     *
     * @param database datastore database number
     */
    void setDatabase(int database);

    /**
     * Set the Redis datastore password.
     *
     * @param password datastore password
     */
    void setPassword(String password);

    /**
     * Set the Redis client connection timeout in milliseconds.
     *
     * @param timeout client connection timeout
     */
    void setConnectionTimeout(int timeout);

    /**
     * Set the Redis client connection pool size.
     *
     * @param size client connection pool size
     */
    void setConnectionPoolSize(int size);

}
