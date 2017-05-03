package io.pivotal.appsuite.tomcat.jedis;

/**
 * An interface defining configurable {@link RedisStore} properties.
 */
public interface RedisManagement extends RedisConfiguration {

    /**
     * Set the Redis host.
     * @param host Redis host
     */
    void setHost(String host);

    /**
     * Set the Redis port.
     * @param port Redis port
     */
    void setPort(int port);

    /**
     * Set the Redis database.
     * @param database Redis database
     */
    void setDatabase(int database);

    /**
     * Set the Redis password.
     * @param password Redis password
     */
    void setPassword(String password);

    /**
     * Set the Redis client connection timeout in milliseconds.
     * @param timeout Redis timeout
     */
    void setConnectionTimeout(int timeout);

    /**
     * Set the Redis client connection pool size.
     * @param size Redis size
     */
    void setConnectionPoolSize(int size);

}
