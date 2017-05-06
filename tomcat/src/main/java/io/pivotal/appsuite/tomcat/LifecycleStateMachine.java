package io.pivotal.appsuite.tomcat;

import org.apache.catalina.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * The Apache Tomcat {@link Lifecycle} state machine.
 */
public class LifecycleStateMachine implements Lifecycle {

    private final Logger log;

    private final LifecycleContext context;

    private volatile Lifecycle state;

    private final Set<LifecycleListener> lifecycleListeners;

    LifecycleStateMachine(LifecycleContext context) {
        log = LoggerFactory.getLogger(context.getClass());
        this.context = context;
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
            log.debug("Transitioning to {}", getStateName());
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
            throw new LifecycleException(String.format("Invalid state transition: %s->init()", getStateName()));
        }

        @Override
        public void start() throws LifecycleException {
            throw new LifecycleException(String.format("Invalid state transition: %s->start()", getStateName()));
        }

        @Override
        public void stop() throws LifecycleException {
            throw new LifecycleException(String.format("Invalid state transition: %s->stop()", getStateName()));
        }

        @Override
        public void destroy() throws LifecycleException {
            throw new LifecycleException(String.format("Invalid state transition: %s->destroy()", getStateName()));
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

    private abstract class DestroyableState extends State {

        private DestroyableState(LifecycleState lifecycleState) {
            super(lifecycleState);
        }

        @Override
        public void destroy() throws LifecycleException {
            state = new DestroyingState();
            state.destroy();
        }

    }

    private class NewState extends DestroyableState {

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
            try {
                context.doInit();
            } catch (Throwable t) {
                state = new FailedState();
                throw new LifecycleException(t);
            }
            state = new InitializedState();
        }

    }

    private class InitializedState extends DestroyableState {

        private InitializedState() {
            super(LifecycleState.INITIALIZED);
        }

        @Override
        public void init() throws LifecycleException {
            // noop
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
            try {
                context.doStart();
            } catch (Throwable t) {
                state = new FailedState();
                throw new LifecycleException(t);
            }
            state = new StartedState();
        }

    }

    private class StartedState extends State {

        private StartedState() {
            super(LifecycleState.STARTED);
        }

        @Override
        public void start() throws LifecycleException {
            // noop
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
            try {
                context.doStop();
            } catch (Throwable t) {
                state = new FailedState();
                throw new LifecycleException(t);
            }
            state = new StoppedState();
        }

    }

    private class StoppedState extends DestroyableState {

        private StoppedState() {
            super(LifecycleState.STOPPED);
        }

        @Override
        public void stop() throws LifecycleException {
            // noop
        }

    }

    private class DestroyingState extends State {

        private DestroyingState() {
            super(LifecycleState.DESTROYING);
        }

        @Override
        public void destroy() throws LifecycleException {
            try {
                context.doDestroy();
            } catch (Throwable t) {
                state = new FailedState();
                throw new LifecycleException(t);
            }
            state = new DestroyedState();
        }

    }

    private class DestroyedState extends DestroyableState {

        private DestroyedState() {
            super(LifecycleState.DESTROYED);
        }

    }

    private class FailedState extends DestroyableState {

        private FailedState() {
            super(LifecycleState.FAILED);
        }

    }

}
