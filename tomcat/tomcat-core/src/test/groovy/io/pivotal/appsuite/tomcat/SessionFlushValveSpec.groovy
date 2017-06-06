package io.pivotal.appsuite.tomcat

import org.apache.catalina.Session
import org.apache.catalina.connector.Request
import org.apache.catalina.connector.Response
import spock.lang.Specification


class SessionFlushValveSpec extends Specification {

    SessionFlushValve valve
    BackendStore store
    Session session
    Request request
    Response response


    def setup() {
        store = Mock()
        valve = Spy()
        valve.store = store
        session = Mock()
        request = Stub()
        response = Stub()
    }

    def "invoke request with valid session"() {
        given:
        request.getSessionInternal(_) >> session
        session.isValid() >> true
        when:
        valve.invoke(request, response)
        then:
        1 * store.save(_)
    }

    def "invoke request with invalid session"() {
        given:
        request.getSessionInternal(_) >> session
        session.isValid() >> false
        when:
        valve.invoke(request, response)
        then:
        0 * store.save(_)
    }

    def "invoke request with no session"() {
        given:
        request.getSessionInternal(_) >> null
        when:
        valve.invoke(request, response)
        then:
        0 * store.save(_)
    }

}