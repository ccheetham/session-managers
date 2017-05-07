package io.pivotal.appsuite.tomcat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * A {@link Backend} decorator that instruments {@code Backend} call times.  The spy logs timings using the logger with
 * the same name as the instrumented {@code Backend}.
 */
public class BackendSpy implements Backend {

    private final Logger log;

    private final Backend backend;

    public BackendSpy(Backend backend) {
        log = LoggerFactory.getLogger(backend.getClass());
        this.backend = backend;
    }

    @Override
    public void init() {
        try {
            new Invoker().invoke(new Invokeable<Void>() {
                @Override
                public Void invoke() {
                    backend.init();
                    return null;
                }
            }, "initializing");
        } catch (IOException e) {
            // won't happen ... backend.init() doesn't throw IOExceptions
            throw new RuntimeException(e);
        }
    }

    @Override
    public void start() throws IOException {
        new Invoker().invoke(new Invokeable<Void>() {
            @Override
            public Void invoke() throws IOException {
                backend.start();
                return null;
            }
        }, "starting");
    }

    @Override
    public void stop() throws IOException {
        new Invoker().invoke(new Invokeable<Void>() {
            @Override
            public Void invoke() throws IOException {
                backend.stop();
                return null;
            }
        }, "stopping");
    }

    @Override
    public void put(final byte[] key, final byte[] value) throws IOException {
        new Invoker().invoke(new Invokeable<Void>() {
            @Override
            public Void invoke() throws IOException {
                backend.put(key, value);
                return null;
            }
        }, "putting " + new String(key));
    }

    @Override
    public byte[] get(final byte[] key) throws IOException {
        return new Invoker().invoke(new Invokeable<byte[]>() {
            @Override
            public byte[] invoke() throws IOException {
                return backend.get(key);
            }
        }, "getting " + new String(key));
    }

    @Override
    public void remove(final byte[] key) throws IOException {
        backend.remove(key);
        new Invoker().invoke(new Invokeable<Void>() {
            @Override
            public Void invoke() throws IOException {
                backend.remove(key);
                return null;
            }
        }, "removing " + new String(key));
    }

    @Override
    public void clear() throws IOException {
        new Invoker().invoke(new Invokeable<Void>() {
            @Override
            public Void invoke() throws IOException {
                backend.clear();
                return null;
            }
        }, "clearing");
    }

    @Override
    public int size() throws IOException {
        return new Invoker().invoke(new Invokeable<Integer>() {
            @Override
            public Integer invoke() throws IOException {
                return backend.size();
            }
        }, "getting count");
    }

    @Override
    public byte[][] keys() throws IOException {
        return new Invoker().invoke(new Invokeable<byte[][]>() {
            @Override
            public byte[][] invoke() throws IOException {
                return backend.keys();
            }
        }, "getting keys");
    }

    private class Invoker {

        <T> T invoke(Invokeable<T> invokeable, String description) throws IOException {
            Timer t = new Timer();
            T result = invokeable.invoke();
            double elapsed = t.elapsed();
            log.info("{} {} [{}ms]", backend.getClass().getSimpleName(), description, elapsed);
            return result;
        }

        private class Timer {

            private long start = System.nanoTime();

            private double elapsed() {
                return (System.nanoTime() - start)/1000000.0;
            }

        }

    }

    private interface Invokeable<T> {

        T invoke() throws IOException;

    }

}
