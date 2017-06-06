package io.pivotal.appsuite.tomcat;

import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.LifecycleState;

/**
 * An abstract implementation of a {@link Lifecycle} that uses a {@link LifecycleStateMachine}
 * to manage its state and transitions.
 */
public abstract class AbstractLifecycle implements Lifecycle, LifecycleContext {

    private final LifecycleStateMachine lifecycle;

    protected AbstractLifecycle() {
        this.lifecycle = new LifecycleStateMachine(this);
    }

    @Override
    public void addLifecycleListener(LifecycleListener listener) {
        lifecycle.addLifecycleListener(listener);
    }

    @Override
    public LifecycleListener[] findLifecycleListeners() {
        return lifecycle.findLifecycleListeners();
    }

    @Override
    public void removeLifecycleListener(LifecycleListener listener) {
        lifecycle.removeLifecycleListener(listener);
    }

    @Override
    public void init() throws LifecycleException {
        lifecycle.init();
    }

    @Override
    public void start() throws LifecycleException {
        lifecycle.start();
    }

    @Override
    public void stop() throws LifecycleException {
        lifecycle.stop();
    }

    @Override
    public void destroy() throws LifecycleException {
        lifecycle.destroy();
    }

    @Override
    public LifecycleState getState() {
        return lifecycle.getState();
    }

    @Override
    public String getStateName() {
        return lifecycle.getStateName();
    }

}
