package io.pivotal.appsuite.tomcat;

/**
 * Defines the interface of objects to be managed by a {@link LifecycleStateMachine}.
 */
public interface LifecycleContext {

    /**
     * Implements the function of {@link org.apache.catalina.Lifecycle#init()}.
     */
    void doInit();

    /**
     * Implements the function of {@link org.apache.catalina.Lifecycle#start()}.
     */
    void doStart();

    /**
     * Implements the function of {@link org.apache.catalina.Lifecycle#stop()}.
     */
    void doStop();

    /**
     * Implements the function of {@link org.apache.catalina.Lifecycle#destroy()} ()}.
     */
    void doDestroy();

}
