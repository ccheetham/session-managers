package io.pivotal.appsuite.tomcat;

import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.LifecycleState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * The Apache Tomcat {@link Lifecycle} state-machine for a {@link BackendStore}.
 */
class BackendStoreLifecycle implements Lifecycle {

    private final Logger log;

    private final BackendStore backendStore;

    private volatile Lifecycle state;

    private final Set<LifecycleListener> lifecycleListeners;


    BackendStoreLifecycle(BackendStore backendStore) {
        log = LoggerFactory.getLogger(getClass());
        this.backendStore = backendStore;
        lifecycleListeners = Collections.synchronizedSet(new HashSet<LifecycleListener>());
        state = new NewState();
    }

    @Override
    public void addLifecycleListener(LifecycleListener listener) {
        state.addLifecycleListener(listener);
    }

    @Override
    public LifecycleListener[] findLifecycleListeners() {
        return state.findLifecycleListeners();
    }

    @Override
    public void removeLifecycleListener(LifecycleListener listener) {
        state.removeLifecycleListener(listener);
    }

    @Override
    public void init() throws LifecycleException {
        state.init();
    }

    @Override
    public void start() throws LifecycleException {
        state.start();
    }

    @Override
    public void stop() throws LifecycleException {
        state.stop();
    }

    @Override
    public void destroy() throws LifecycleException {
        state.destroy();
    }

    @Override
    public LifecycleState getState() {
        return state.getState();
    }

    @Override
    public String getStateName() {
        return state.getStateName();
    }

    private abstract class State implements Lifecycle {

        private final LifecycleState lifecycleState;

        private State(LifecycleState lifecycleState) {
            this.lifecycleState = lifecycleState;
            log.debug("transitioning to {}", getStateName());
        }

        @Override
        public void addLifecycleListener(LifecycleListener listener) {
            lifecycleListeners.add(listener);
        }

        @Override
        public LifecycleListener[] findLifecycleListeners() {
            return lifecycleListeners.toArray(new LifecycleListener[0]);
        }

        @Override
        public void removeLifecycleListener(LifecycleListener listener) {
            lifecycleListeners.remove(listener);
        }

        @Override
        public void init() throws LifecycleException {
            throw new LifecycleException("unsupported state transition: init");

        }

        @Override
        public void start() throws LifecycleException {
            throw new LifecycleException("unsupported state transition: start");
        }

        @Override
        public void stop() throws LifecycleException {
            throw new LifecycleException("unsupported state transition: stop");
        }

        @Override
        public void destroy() throws LifecycleException {
            throw new LifecycleException("unsupported state transition: destroy");
        }

        @Override
        public LifecycleState getState() {
            return lifecycleState;
        }

        @Override
        public String getStateName() {
            return getState().name();
        }
    }

    private class NewState extends State {

        private NewState() {
            super(LifecycleState.NEW);
        }

        @Override
        public void init() throws LifecycleException {
            state = new InitializingState();
            state.init();
        }

        @Override
        public void start() throws LifecycleException {
            init();
            state.start();
        }

        @Override
        public void stop() throws LifecycleException {
            state = new StoppedState();
        }

    }

    private class InitializingState extends State {

        private InitializingState() {
            super(LifecycleState.INITIALIZING);
        }

        @Override
        public void init() throws LifecycleException {
            backendStore.getBackend().init();
            state = new InitializedState();
        }

    }

    private class InitializedState extends State {

        private InitializedState() {
            super(LifecycleState.INITIALIZED);
        }

        @Override
        public void start() throws LifecycleException {
            state = new StartingPrepState();
            state.start();
        }

    }

    private class StartingPrepState extends State {

        private StartingPrepState() {
            super(LifecycleState.STARTING_PREP);
        }

        @Override
        public void start() throws LifecycleException {
            state = new StartingState();
            state.start();
        }

    }

    private class StartingState extends State {

        private StartingState() {
            super(LifecycleState.STARTING);
        }

        @Override
        public void start() throws LifecycleException {
            backendStore.getBackend().start();
            state = new StartedState();
        }

    }

    private class StartedState extends State {

        private StartedState() {
            super(LifecycleState.STARTED);
        }

        @Override
        public void stop() throws LifecycleException {
            state = new StoppingPrepState();
            state.stop();
        }

    }

    private class StoppingPrepState extends State {

        private StoppingPrepState() {
            super(LifecycleState.STOPPING_PREP);
        }

        @Override
        public void stop() throws LifecycleException {
            state = new StoppingState();
            state.stop();
        }

    }

    private class StoppingState extends State {

        private StoppingState() {
            super(LifecycleState.STOPPING);
        }

        @Override
        public void stop() throws LifecycleException {
            backendStore.getBackend().stop();
            state = new StoppedState();
        }

    }

    private class StoppedState extends State {

        private StoppedState() {
            super(LifecycleState.STOPPED);
        }

        @Override
        public void destroy() throws LifecycleException {
            state = new DestroyingState();
            state.destroy();
        }

    }

    private class DestroyingState extends State {

        private DestroyingState() {
            super(LifecycleState.DESTROYING);
        }

        @Override
        public void destroy() throws LifecycleException {
            state = new DestroyedState();
        }

    }

    private class DestroyedState extends State {

        private DestroyedState() {
            super(LifecycleState.DESTROYED);
        }

    }

    private class FailedState extends State {

        private FailedState() {
            super(LifecycleState.FAILED);
        }

    }

}
