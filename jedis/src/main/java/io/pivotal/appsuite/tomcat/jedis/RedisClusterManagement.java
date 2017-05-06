package io.pivotal.appsuite.tomcat.jedis;

/**
 * An interface defining configurable {@link RedisClusterStore} properties.
 */
public interface RedisClusterManagement extends RedisClusterConfiguration {

    /**
     * Set the Redis cluster host name/port pairs.
     * See {@link RedisClusterConfiguration#getHostPorts()} for format.
     *
     * @param hostPorts cluster host name/port pairs
     */
    void setHostPorts(String hostPorts);

    /**
     * Set the Redis cluster password.
     *
     * @param password cluster password
     */
    void setPassword(String password);

    /**
     * Set the Redis cluster client connection timeout in milliseconds.
     *
     * @param timeout client connection timeout
     */
    void setConnectionTimeout(int timeout);

    /**
     * Set the Redis cluster client connection pool size.
     *
     * @param size client connection pool size
     */
    void setConnectionPoolSize(int size);

}
