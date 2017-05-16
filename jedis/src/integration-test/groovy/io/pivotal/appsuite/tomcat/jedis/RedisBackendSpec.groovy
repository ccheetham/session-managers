package io.pivotal.appsuite.tomcat.jedis

import spock.lang.Shared
import spock.lang.Specification

class RedisBackendSpec extends Specification {

    @Shared
    RedisConfiguration config
    @Shared
    RedisBackend backend


    def setupSpec() {
        config = new MockRedisConfiguration()
        backend = new RedisBackend(config)
        backend.init()
        backend.start()
    }

    def cleanupSpec() {
        backend.stop()
    }

    def "test put"() {
        when:
        backend.put("newkey".bytes, "newvalue".bytes)
        then:
        backend.size() == old(backend.size()) + 1
    }

    def "test get"() {
        when:
        backend.put("mykey".bytes, "myvalue".bytes)
        then:
        backend.get("mykey".bytes) == "myvalue".bytes
    }

    def "test remove"() {
        when:
        backend.put("tempkey".bytes, "tempvalue".bytes)
        backend.remove("tempkey".bytes)
        then:
        backend.size() == old(backend.size())
    }

    def "test clear"() {
        when:
        backend.put("fillerkey".bytes, "fillervalue".bytes)
        backend.clear()
        then:
        backend.size() == 0
        when:
        backend.clear()
        then:
        backend.size() == 0
    }

    class MockRedisConfiguration implements RedisConfiguration {

        @Override
        boolean isSpyEnabled() {
            return false
        }

        @Override
        boolean isSessionFlushValveEnabled() {
            return false
        }

        @Override
        String getHost() {
            return 'localhost'
        }

        @Override
        int getPort() {
            return System.properties['redis.port'] as int
        }

        @Override
        int getDatabase() {
            return 0
        }

        @Override
        String getPassword() {
            return null
        }

        @Override
        int getConnectionTimeout() {
            return 1000
        }

        @Override
        int getConnectionPoolSize() {
            return 5
        }
    }

}
