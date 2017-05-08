package io.pivotal.appsuite.tomcat;

import org.apache.catalina.*;
import org.apache.catalina.session.StandardSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyChangeListener;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.charset.Charset;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * An abstract implementation of an Apache Tomcat session store.
 * <p>
 * Store persistence is implemented by a {@link Backend}, specified by subclass implementations.
 * <p>
 * Store {@link Lifecycle} is implemented in {@link LifecycleStateMachine}.
 */
public abstract class BackendStore extends AbstractLifecycle implements Store, BackendStoreManagement {

    // default charset String encoding
    private static final Charset CHARSET = Charset.forName("UTF-8");

    private final Logger log;

    private Manager manager;

    private boolean spyEnabled;

    private boolean sessionFlushValveEnabled;

    private SessionFlushValve sessionFlushValve;

    private Backend backend;

    private final ReadWriteLock lock;


    /**
     * Create a new {@code BackendStore}.
     */
    protected BackendStore() {
        log = LoggerFactory.getLogger(getClass());
        lock = new ReentrantReadWriteLock();
        spyEnabled = false;
        sessionFlushValveEnabled = true;
    }

    /**
     * Return a description of this store in in Tomcat's preferred style.
     *
     * @return store description
     */
    public String getDescription() {
        Package pkg = getClass().getPackage();
        String vendor = pkg.getImplementationVendor();
        if (vendor == null) {
            vendor = "Pivotal";
        }
        String title = pkg.getImplementationTitle();
        if (title == null) {
            title = "Unknown";
        }
        String version = pkg.getImplementationVersion();
        if (version == null) {
            version = "0.0";
        }
        return String.format("%s %s/%s", vendor, title, version);
    }

    // -----------------------------------------------------------------------
    //   BackendStoreManagement implementation
    // -----------------------------------------------------------------------


    @Override
    public boolean isSpyEnabled() {
        return spyEnabled;
    }

    @Override
    public void setSpyEnabled(boolean enabled) {
        log.debug("Setting spy enabled to {}", enabled);
        spyEnabled = enabled;
    }

    @Override
    public boolean isSessionFlushValveEnabled() {
        return sessionFlushValveEnabled;
    }

    @Override
    public void setSessionFlushValveEnabled(boolean enabled) {
        log.debug("Setting session flush valve enabled to {}", enabled);
        sessionFlushValveEnabled = enabled;
    }

    // -----------------------------------------------------------------------
    //   LifecycleContext implementation
    // -----------------------------------------------------------------------

    @Override
    public void doInit() {
        lock.writeLock().lock();
        try {
            backend = createBackend();
            if (isSpyEnabled()) {
                log.info("Enabling backend spy");
                backend = new BackendSpy(backend);
            }
            backend.init();
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void doStart() throws LifecycleException {
        lock.writeLock().lock();
        try {
            if (isSessionFlushValveEnabled()) {
                log.info("Enabling session flush valve");
                sessionFlushValve = Tomcat.getAdapterInstance().createSessionFlushValve();
                sessionFlushValve.setStore(this);
                Tomcat.getAdapterInstance().getPipeline(this).addValve(sessionFlushValve);
            }
            backend.start();
        } catch (IOException e) {
            throw new LifecycleException(e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void doStop() throws LifecycleException {
        lock.writeLock().lock();
        try {
            if (isSessionFlushValveEnabled()) {
                log.info("Disabling session flush valve");
                Tomcat.getAdapterInstance().getPipeline(this).removeValve(sessionFlushValve);
                sessionFlushValve = null;
            }
            backend.stop();
        } catch (IOException e) {
            throw new LifecycleException(e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void doDestroy() {
        lock.writeLock().lock();
        try {
            backend = null;
        } finally {
            lock.writeLock().unlock();
        }
    }

    // -----------------------------------------------------------------------
    //   org.apache.catalina.Store implementation
    // -----------------------------------------------------------------------

    @Override
    public Manager getManager() {
        lock.readLock().lock();
        try {
            return manager;
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void setManager(Manager manager) {
        lock.readLock().lock();
        try {
            this.manager = manager;
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        throw new RuntimeException("NOT IMPLEMENTED addPropertyChangeListener");
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        throw new RuntimeException("NOT IMPLEMENTED removePropertyChangeListener");
    }

    @Override
    public void save(Session session) throws IOException {
        log.debug("Saving session {}", session.getId());
        lock.readLock().lock();
        try {
            backend.put(idToBytes(session.getId()), sessionToBytes((StandardSession) session));
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Session load(String id) throws ClassNotFoundException, IOException {
        log.debug("Loading session {}", id);
        lock.readLock().lock();
        try {
            return sessionFromBytes(backend.get(idToBytes(id)));
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void remove(String id) throws IOException {
        log.debug("Removing session {}", id);
        lock.readLock().lock();
        try {
            backend.remove(idToBytes(id));
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public int getSize() throws IOException {
        log.debug("Getting session count");
        lock.readLock().lock();
        try {
            return backend.size();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public String[] keys() throws IOException {
        log.debug("Getting session keys");
        lock.readLock().lock();
        try {
            byte[][] keyBytesArray = backend.keys();
            String[] keyStrings = new String[keyBytesArray.length];
            int i = 0;
            for (byte[] keyBytes : keyBytesArray) {
                keyStrings[i++] = idFromBytes(keyBytes);
            }
            return keyStrings;
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void clear() throws IOException {
        log.debug("Clearing sessions");
        lock.readLock().lock();
        try {
            backend.clear();
        } finally {
            lock.readLock().unlock();
        }
    }

    // -----------------------------------------------------------------------
    //   org.apache.catalina.BackendStore implementation for Tomcat 7
    // -----------------------------------------------------------------------

    /**
     * Returns an informative store string as required by Tomcat 7.
     * This implementation delegates to {@link #getDescription()}.
     * <p>
     * <i>Note: Required by Tomcat 7.0.</i>
     * @return informative string
     */
    public String getInfo() {
        return getDescription();
    }

    // -----------------------------------------------------------------------
    //   serialization utilities
    // -----------------------------------------------------------------------

    private byte[] idToBytes(String id) {
        return id.getBytes(CHARSET);
    }

    private String idFromBytes(byte[] id) {
        return new String(id, CHARSET);
    }


    private byte[] sessionToBytes(StandardSession session) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            session.writeObjectData(oos);
            oos.flush();
            return bos.toByteArray();
        }
    }

    private Session sessionFromBytes(byte[] bytes) throws IOException, ClassNotFoundException {
        if (bytes == null) {
            return null;
        }
        return Tomcat.getAdapterInstance().sessionFromBytes(this, bytes);
    }

    // -----------------------------------------------------------------------
    //   subclasses
    // -----------------------------------------------------------------------

    /**
     * Create a return a new {@link Backend} to be used by this store.
     *
     * @return a new {@link Backend} instance
     */
    protected abstract Backend createBackend();

}
