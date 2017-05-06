package io.pivotal.appsuite.tomcat.jedis;

import io.pivotal.appsuite.tomcat.BackendStoreConfiguration;

/**
 * An interface defining configured {@link RedisClusterStore} properties.
 */
public interface RedisClusterConfiguration extends BackendStoreConfiguration {

    /**
     * Return the Redis cluster host name/port pairs.  The BNF of the {@code String} is:
     * <pre><i>
     *     hostPorts --&gt; hostPort | hostPort ',' hostPorts
     *     hostPort  --&gt; host | host ':' port
     * </i></pre>
     * <p>
     * Examples:
     * <ul>
     * <li>{@code "node0"}
     * <li>{@code "node1:1234"}
     * <li>{@code "node0,node1:1234,node3:4321"}
     * </ul>
     *
     * @return cluster host name/port pairs
     */
    String getHostPorts();

    /**
     * Return the Redis cluster password.
     *
     * @return cluster password
     */
    String getPassword();

    /**
     * Return the Redis cluster client connection timeout in milliseconds.
     *
     * @return cluster client connection timeout
     */
    int getConnectionTimeout();

    /**
     * Return the Redis cluster client connection pool size.
     *
     * @return cluster client connection pool size
     */
    int getConnectionPoolSize();

}
