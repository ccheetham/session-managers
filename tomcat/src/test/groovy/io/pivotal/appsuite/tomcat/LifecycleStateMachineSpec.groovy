package io.pivotal.appsuite.tomcat

import org.apache.catalina.LifecycleException
import org.apache.catalina.LifecycleListener
import org.apache.catalina.LifecycleState
import spock.lang.*

class LifecycleStateMachineSpec extends Specification {

    LifecycleContext context
    LifecycleStateMachine newMachine
    LifecycleStateMachine initializedMachine
    LifecycleStateMachine startedMachine
    LifecycleStateMachine stoppedMachine
    LifecycleStateMachine destroyedMachine
    LifecycleStateMachine failedMachine

    def setup() {
        context = Stub()

        newMachine = new LifecycleStateMachine(context)
        assert newMachine.state == LifecycleState.NEW

        initializedMachine = new LifecycleStateMachine(context)
        initializedMachine.init()
        assert initializedMachine.state == LifecycleState.INITIALIZED

        startedMachine = new LifecycleStateMachine(context)
        startedMachine.start()
        assert startedMachine.state == LifecycleState.STARTED

        stoppedMachine = new LifecycleStateMachine(context)
        stoppedMachine.stop()
        assert stoppedMachine.state == LifecycleState.STOPPED

        destroyedMachine = new LifecycleStateMachine(context)
        destroyedMachine.destroy()
        assert destroyedMachine.state == LifecycleState.DESTROYED

        LifecycleContext failed = Stub()
        failed.doInit() >> { throw new Error("failed to init") }
        failedMachine = new LifecycleStateMachine(failed)
        try {
            failedMachine.init()
        } catch (LifecycleException ignored){}
        assert failedMachine.state == LifecycleState.FAILED
    }

    def "transition NEW->init()->INITIALIZED"() {
        when:
        newMachine.init()
        then:
        newMachine.state == LifecycleState.INITIALIZED
    }

    def "transition NEW->init()->FAILED"() {
        given:
        context.doInit() >> { throw new Error("failed to init") }
        when:
        newMachine.init()
        then:
        LifecycleException e = thrown()
        e.message ==~ /.*failed to init/
        newMachine.state == LifecycleState.FAILED
    }

    def "transition NEW->start()->STARTED"() {
        when:
        newMachine.start()
        then:
        newMachine.state == LifecycleState.STARTED
    }

    def "transition NEW->start()->FAILED"() {
        given:
        context.doStart() >> { throw new Error("failed to start") }
        when:
        newMachine.start()
        then:
        LifecycleException e = thrown()
        e.message ==~ /.*failed to start/
        newMachine.state == LifecycleState.FAILED
    }

    def "transition NEW->stop()->STOPPED"() {
        when:
        newMachine.stop()
        then:
        newMachine.state == LifecycleState.STOPPED
    }

    def "transition NEW->destroy()->DESTROYED"() {
        when:
        newMachine.destroy()
        then:
        newMachine.state == LifecycleState.DESTROYED
    }

    def "transition INITIALIZED->init()->INITIALIZED"() {
        when:
        initializedMachine.init()
        then:
        initializedMachine.state == LifecycleState.INITIALIZED
    }

    def "transition INITIALIZED->start()->STARTED"() {
        when:
        initializedMachine.start()
        then:
        initializedMachine.state == LifecycleState.STARTED
    }

    def "transition INITIALIZED->start()->FAILED"() {
        given:
        context.doStart() >> { throw new Error("failed to start") }
        when:
        initializedMachine.start()
        then:
        LifecycleException e = thrown()
        e.message ==~ /.*failed to start/
        initializedMachine.state == LifecycleState.FAILED
    }

    def "transition INITIALIZED->stop() [invalid]"() {
        when:
        initializedMachine.stop()
        then:
        LifecycleException e = thrown()
        e.message == "Invalid state transition: INITIALIZED->stop()"
        initializedMachine.state == LifecycleState.INITIALIZED
    }

    def "transition INITIALIZED->destroy()->DESTROYED"() {
        when:
        initializedMachine.destroy()
        then:
        initializedMachine.state == LifecycleState.DESTROYED
    }

    def "transition STARTED->init() [invalid]"() {
        when:
        startedMachine.init()
        then:
        LifecycleException e = thrown()
        e.message == "Invalid state transition: STARTED->init()"
        startedMachine.state == LifecycleState.STARTED
    }

    def "transition STARTED->start()->STARTED"() {
        when:
        startedMachine.start()
        then:
        startedMachine.state == LifecycleState.STARTED
    }

    def "transition STARTED->stop()->STOPPED"() {
        when:
        startedMachine.stop()
        then:
        startedMachine.state == LifecycleState.STOPPED
    }

    def "transition STARTED->stop()->FAILED"() {
        given:
        context.doStop() >> { throw new Error("failed to stop") }
        when:
        startedMachine.stop()
        then:
        LifecycleException e = thrown()
        e.message ==~ /.*failed to stop/
        startedMachine.state == LifecycleState.FAILED
    }

    def "transition STARTED->destroy() [invalid]"() {
        when:
        startedMachine.destroy()
        then:
        LifecycleException e = thrown()
        e.message == "Invalid state transition: STARTED->destroy()"
        startedMachine.state == LifecycleState.STARTED
    }

    def "transition STOPPED->init() [invalid]"() {
        when:
        stoppedMachine.init()
        then:
        LifecycleException e = thrown()
        e.message == "Invalid state transition: STOPPED->init()"
        stoppedMachine.state == LifecycleState.STOPPED
    }

    def "transition STOPPED->start() [invalid]"() {
        when:
        stoppedMachine.start()
        then:
        LifecycleException e = thrown()
        e.message == "Invalid state transition: STOPPED->start()"
        stoppedMachine.state == LifecycleState.STOPPED
    }

    def "transition STOPPED->stop()->STOPPED"() {
        when:
        stoppedMachine.stop()
        then:
        stoppedMachine.state == LifecycleState.STOPPED
    }

    def "transition STOPPED->destroy()->DESTROYED"() {
        when:
        stoppedMachine.destroy()
        then:
        stoppedMachine.state == LifecycleState.DESTROYED
    }

    def "transition DESTROYED->init() [invalid]"() {
        when:
        destroyedMachine.init()
        then:
        LifecycleException e = thrown()
        e.message == "Invalid state transition: DESTROYED->init()"
        destroyedMachine.state == LifecycleState.DESTROYED
    }

    def "transition DESTROYED->start() [invalid]"() {
        when:
        destroyedMachine.start()
        then:
        LifecycleException e = thrown()
        e.message == "Invalid state transition: DESTROYED->start()"
        destroyedMachine.state == LifecycleState.DESTROYED
    }

    def "transition DESTROYED->stop() [invalid]"() {
        when:
        destroyedMachine.stop()
        then:
        LifecycleException e = thrown()
        e.message == "Invalid state transition: DESTROYED->stop()"
        destroyedMachine.state == LifecycleState.DESTROYED
    }

    def "transition DESTROYED->destroy->DESTROYED"() {
        when:
        destroyedMachine.destroy()
        then:
        destroyedMachine.state == LifecycleState.DESTROYED
    }

    def "transition FAILED->init() [invalid]"() {
        when:
        failedMachine.init()
        then:
        LifecycleException e = thrown()
        e.message == "Invalid state transition: FAILED->init()"
        failedMachine.state == LifecycleState.FAILED
    }

    def "transition FAILED->start() [invalid]"() {
        when:
        failedMachine.start()
        then:
        LifecycleException e = thrown()
        e.message == "Invalid state transition: FAILED->start()"
        failedMachine.state == LifecycleState.FAILED
    }

    def "transition FAILED->stop() [invalid]"() {
        when:
        failedMachine.stop()
        then:
        LifecycleException e = thrown()
        e.message == "Invalid state transition: FAILED->stop()"
        failedMachine.state == LifecycleState.FAILED
    }

    def "transition FAILED->destroy()->DESTROYED"() {
        when:
        failedMachine.destroy()
        then:
        failedMachine.state == LifecycleState.DESTROYED
    }

    def "life cycle listener support"() {
        given:
        LifecycleListener listener1 = Stub()
        LifecycleListener listener2 = Stub()

        expect:
        newMachine.findLifecycleListeners().length == 0

        when:
        newMachine.addLifecycleListener(listener1)

        then:
        newMachine.findLifecycleListeners().length == old(newMachine.findLifecycleListeners().length) + 1
        listener1 in newMachine.findLifecycleListeners()
        !(listener2 in newMachine.findLifecycleListeners())

        when:
        newMachine.addLifecycleListener(listener2)

        then:
        newMachine.findLifecycleListeners().length == old(newMachine.findLifecycleListeners().length) + 1
        listener1 in newMachine.findLifecycleListeners()
        listener2 in newMachine.findLifecycleListeners()

        when:
        newMachine.removeLifecycleListener(listener1)

        then:
        newMachine.findLifecycleListeners().length == old(newMachine.findLifecycleListeners().length) - 1
        !(listener1 in newMachine.findLifecycleListeners())
        listener2 in newMachine.findLifecycleListeners()

        when:
        newMachine.removeLifecycleListener(listener2)

        then:
        newMachine.findLifecycleListeners().length == 0
    }

}
