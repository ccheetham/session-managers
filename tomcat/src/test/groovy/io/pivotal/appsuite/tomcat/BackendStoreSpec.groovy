package io.pivotal.appsuite.tomcat

import org.apache.catalina.*
import org.apache.catalina.session.StandardSession
import spock.lang.Specification

import java.nio.charset.Charset

class BackendStoreSpec extends Specification {

    BackendStore store
    MockBackend backend
    Pipeline pipeline
    Manager manager
    TomcatAdapter adapter
    SessionFlushValve valve


    def setup() {
        pipeline = Mock()
        Container container = Stub()
        container.getPipeline() >> pipeline
        adapter = new MockTomcatAdapter()
        Tomcat.setAdapterInstance(adapter)
        adapter.getPipeline() >> pipeline
        manager = Stub()
        manager.getContainer() >> container
        store = Spy()
        backend = Spy(MockBackend)
        store.createBackend() >> backend
        store.getManager() >> manager
        valve = Stub(SessionFlushValve)
    }

    def "init()"() {
        when:
        store.init()
        then:
        store.getState() == LifecycleState.INITIALIZED
        1 * backend.init()
    }

    def "start()"() {
        when:
        store.start()
        then:
        store.getState() == LifecycleState.STARTED
        1 * backend.start()
    }

    def "stop()"() {
        when:
        store.start()
        store.stop()
        then:
        store.getState() == LifecycleState.STOPPED
        1 * backend.stop()
    }

    def "destroy()"() {
        when:
        store.init()
        store.destroy()
        then:
        store.getState() == LifecycleState.DESTROYED
    }

    def "enable session flush valve"() {
        given:
        store.sessionFlushValveEnabled = true
        when:
        store.start()
        then:
        1 * pipeline.addValve(_)
        when:
        store.stop()
        then:
        1 * pipeline.removeValve(_)
    }

    def "disable session flush valve"() {
        given:
        store.sessionFlushValveEnabled = false
        when:
        store.start()
        then:
        0 * pipeline.addValve(_)
        when:
        store.stop()
        then:
        0 * pipeline.removeValve(_)
    }

    def "save(id,session)"() {
        given:
        def id = "savesession"
        Session session = session(id)
        when:
        store.start()
        store.save(session)
        then:
        backend.sessions[id]
    }

    def "load(id)"() {
        given:
        def id = "loadsession"
        Session session = session(id)
        store.start()
        store.save(session)
        assert backend.sessions[id]
        expect:
        store.load(id)
    }

    def "remove(id)"() {
        given:
        def id = "loadsession"
        Session session = session(id)
        store.start()
        store.save(session)
        assert backend.sessions[id]
        when:
        store.remove(id)
        then:
        !backend.sessions[id]
    }

    def "size()"() {
        given:
        store.start()
        assert store.size == 0
        store.save(session("1"))
        expect:
        store.size == 1
    }

    def "keys()"() {
        given:
        def keys = ["abc", "def", "ghi"]
        store.start()
        keys.each {
            store.save(session(it))
        }
        expect:
        store.keys() == keys
    }

    def "clear()"() {
        given:
        def id = "clearme"
        Session session = session(id)
        store.start()
        store.save(session)
        assert backend.sessions[id]
        when:
        store.remove(id)
        then:
        !backend.sessions[id]
    }

    def session(String id) {
        def session = new StandardSession(manager)
        session.setId(id, false)
        return session
    }

    class MockTomcatAdapter implements TomcatAdapter {

        @Override
        Pipeline getPipeline(Store store) {
            pipeline
        }

        @Override
        SessionFlushValve createSessionFlushValve() {
            valve
        }

        @Override
        Session sessionFromBytes(Store store, byte[] bytes) throws IOException, ClassNotFoundException {
            return session("")
        }

    }

    class MockBackend implements Backend {

        def sessions = [:]

        @Override
        void init() {}

        @Override
        void start() {}

        @Override
        void stop() {}

        @Override
        void put(byte[] key, byte[] value) throws IOException {
            String id = new String(key, Charset.forName("UTF-8"))
            sessions[id] = value
        }

        @Override
        byte[] get(byte[] key) throws IOException {
            String id = new String(key)
            sessions[id]
        }

        @Override
        void remove(byte[] key) throws IOException {
            sessions.remove(new String(key))
        }

        @Override
        void clear() throws IOException {
            sessions.clear()
        }

        @Override
        int size() throws IOException {
            sessions.size()
        }

        @Override
        byte[][] keys() throws IOException {
            byte[][] keys = new byte[sessions.size()][]
            def i = 0
            for (String key: sessions.keySet()) {
                keys[i++] = key.getBytes()
            }
            return keys
        }
    }

}
