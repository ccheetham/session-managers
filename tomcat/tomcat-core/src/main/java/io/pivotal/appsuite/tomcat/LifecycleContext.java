package io.pivotal.appsuite.tomcat;

import org.apache.catalina.LifecycleException;

/**
 * Defines the interface of objects to be managed by a {@link LifecycleStateMachine}.
 */
public interface LifecycleContext {

    /**
     * Implements the function of {@link org.apache.catalina.Lifecycle#init()}.
     *
     * @throws LifecycleException if a transition error occurs
     */
    void doInit() throws LifecycleException;

    /**
     * Implements the function of {@link org.apache.catalina.Lifecycle#start()}.
     *
     * @throws LifecycleException if a transition error occurs
     */
    void doStart() throws LifecycleException;

    /**
     * Implements the function of {@link org.apache.catalina.Lifecycle#stop()}.
     *
     * @throws LifecycleException if a transition error occurs
     */
    void doStop() throws LifecycleException;

    /**
     * Implements the function of {@link org.apache.catalina.Lifecycle#destroy()} ()}.
     *
     * @throws LifecycleException if a transition error occurs
     */
    void doDestroy() throws LifecycleException;

}
