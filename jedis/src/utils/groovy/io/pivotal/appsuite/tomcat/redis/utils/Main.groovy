package io.pivotal.appsuite.tomcat.redis.utils

import io.pivotal.appsuite.tomcat.jedis.RedisBackend
import io.pivotal.appsuite.tomcat.jedis.RedisConfiguration
import redis.clients.jedis.JedisPoolConfig
import redis.clients.jedis.Protocol

import java.nio.charset.Charset

class Main {

    public static void main(String[] args) {
        def cli = new CliBuilder(usage:' ')
        cli.c(longOpt:'config', 'Config file')
        cli.h(longOpt:'help', 'Print this message')
        cli.l(longOpt:'list', 'List mapping keys')
        cli.R(longOpt:'remove', args:1, argName:'key', 'Remove a mapping')
        cli.C(longOpt:'clear', 'Clear mappings')
        cli._(longOpt:'host', args:1, argName:'host', 'Redis host')
        cli._(longOpt:'port', args:1, argName:'port', 'Redis port')
        cli._(longOpt:'database', args:1, argName:'number', 'Redis database')
        cli._(longOpt:'password', args:1, argName:'password', 'Redis password')
        def opts = cli.parse(args)
        if (opts.h) {
            cli.usage()
            return
        }
        if (opts.arguments()) {
            System.err.println("!!! unknown argument${opts.arguments().size() == 1 ? '' : 's'}: ${opts.arguments().join(' ')}")
            System.exit 1
        }
        try {
            def config = getConfiguration(opts)
            RedisBackend backend = new RedisBackend(config)
            backend.start()
            try {
                if (opts.l) {
                    def keys = backend.keys().collect {
                        new String(it, Charset.forName("UTF-8"))
                    }
                    if (!keys) {
                        println "no mapping keys"
                        return
                    }
                    keys.toSorted().each {
                        println it
                    }
                } else if (opts.R) {
                    def key = opts.R.getBytes(Charset.forName("UTF-8"))
                    if (!backend.get(key)) {
                        println "mapping does not exist: ${opts.R}"
                        return
                    }
                    backend.remove(key)
                    println "mapping removed"
                } else if (opts.C) {
                    backend.clear()
                    println "mappings cleared"
                } else {
                    println "run with -h for help"
                }
            } finally {
                backend.stop()
            }
        } catch (Throwable t) {
            System.err.println("!!! ${t.message}")
            System.exit 1

        }
    }

    private static def parseOptions(def args) {
    }

    static RedisConfiguration getConfiguration(def opts) {
        new RedisConfiguration() {
            @Override
            String getHost() {
                opts.host ?: 'localhost'
            }

            @Override
            int getPort() {
                try {
                    (opts.port ?: 6379) as int
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("port must be a number")
                }
            }

            @Override
            int getDatabase() {
                try {
                    (opts.database ?: 0) as int
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("database must be a number")
                }
            }

            @Override
            String getPassword() {
                opts.password ?: null
            }

            @Override
            int getConnectionTimeout() {
                Protocol.DEFAULT_TIMEOUT
            }

            @Override
            int getConnectionPoolSize() {
                JedisPoolConfig.DEFAULT_MAX_TOTAL
            }

            @Override
            boolean isSpyEnabled() {
                return false
            }

            @Override
            boolean isSessionFlushValveEnabled() {
                return false
            }
        }
    }

}
