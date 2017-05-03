package io.pivotal.appsuite.tomcat;

import org.apache.catalina.*;
import org.apache.catalina.session.StandardSession;
import org.apache.catalina.util.ServerInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyChangeListener;
import java.io.*;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * An abstract, thread-safe, implementation of an Apache Tomcat session store.
 * <p>
 * Store persistence is implemented by a {@link Backend}, specified by subclass implementations.
 * <p>
 * Store {@link Lifecycle} is implemented in {@link BackendStoreLifecycle}.
 * <p>
 * Thread safety is implemented using a {@link ReadWriteLock} which subclasses can be access by calling {@link #lock()}.
 * <p>
 * Methods that obtain and lock an exclusive "write" lock:
 * <ul>
 * <li>{@link #init()}</li>
 * <li>{@link #start()}</li>
 * <li>{@link #stop()}</li>
 * <li>{@link #destroy()}</li>
 * <li>{@link #setManager(Manager)}</li>
 * </ul>
 * <p>
 * Methods that obtain and lock a non-exclusive "read" lock:
 * <ul>
 * <li>{@link #getState()}</li>
 * <li>{@link #save(Session)}</li>
 * <li>{@link #load(String)}</li>
 * <li>{@link #remove(String)}</li>
 * <li>{@link #keys()}</li>
 * <li>{@link #clear()}</li>
 * </ul>
 * <p>
 * Subclasses wishing to change from non-exclusive/"read" to exclusive/"write" behavior must first unlock the read lock
 * before obtaining the write lock.  Before returning, the read lock should be restored.  Sample subclass code
 * exhibiting change from from non-exclusive/"read" to exclusive/"write" lock behavior:
 * <pre> {@code
 * protected void storeClear() {
 *     lock().readLock().unlock();     // release the read lock
 *     lock().writeLock().lock();      // obtain the write lock
 *     try {
 *         // clear the store
 *         lock.readLock().lock();     // re-obtain the read lock
 *     } finally {
 *         lock.writeLock().unlock();  // release the write lock
 *     }
 * }}</pre>
 */
public abstract class BackendStore implements Store, Lifecycle {

    // default charset String encoding
    private static final Charset CHARSET = Charset.forName("UTF-8");

    private final Logger log;

    private final ReadWriteLock lock;

    // tomcat sets manager lazily
    private Manager manager;

    private volatile BackendStoreLifecycle lifecycle;

    private final Set<LifecycleListener> lifecycleListeners;

    private final TomcatAdapter tomcatAdapter;


    /**
     * Create a new {@code BackendStore}.
     */
    protected BackendStore() {
        log = LoggerFactory.getLogger(getClass());
        lock = new ReentrantReadWriteLock();
        lifecycleListeners = Collections.synchronizedSet(new HashSet<LifecycleListener>());
        lifecycle = new BackendStoreLifecycle(this);
        tomcatAdapter = loadTomcatAdapter();
        log.info(getDescription());
    }

    /**
     * Return a description of this store.
     * Description is of the form <i>"vendor title/version"</i> as specified by
     * {@link Package#getImplementationVendor()}, {@link Package#getImplementationTitle}, and
     * {@link Package#getImplementationVersion()}, respectively.
     *
     * @return store description
     */
    public String getDescription() {
        Package pkg = getClass().getPackage();
        return String.format("%1s %2s/%3s", pkg.getImplementationVendor(), pkg.getImplementationTitle(),
                pkg.getImplementationVersion());
    }

    // -----------------------------------------------------------------------
    //   org.apache.catalina.Lifecycle implementation
    // -----------------------------------------------------------------------

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
        lock().writeLock().lock();
        try {
            lifecycle.init();
        } finally {
            lock().writeLock().unlock();
        }
    }

    @Override
    public void start() throws LifecycleException {
        lock().writeLock().lock();
        try {
            lifecycle.start();
        } finally {
            lock().writeLock().unlock();
        }
    }

    @Override
    public void stop() throws LifecycleException {
        lock().writeLock().lock();
        try {
            lifecycle.stop();
        } finally {
            lock().writeLock().unlock();
        }
    }

    @Override
    public void destroy() throws LifecycleException {
        lock().writeLock().lock();
        try {
            lifecycle.destroy();
        } finally {
            lock().writeLock().unlock();
        }
    }

    @Override
    public LifecycleState getState() {
        lock().readLock().lock();
        try {
            return lifecycle.getState();
        } finally {
            lock().readLock().unlock();
        }
    }

    @Override
    public String getStateName() {
        return lifecycle.getStateName();
    }

    // -----------------------------------------------------------------------
    //   org.apache.catalina.Store implementation
    // -----------------------------------------------------------------------

    @Override
    public final Manager getManager() {
        log.info("setting manager");
        lock().readLock().lock();
        try {
            return manager;
        } finally {
            lock().readLock().unlock();
        }
    }

    @Override
    public final void setManager(Manager manager) {
        lock().writeLock().lock();
        try {
            this.manager = manager;
        } finally {
            lock().writeLock().unlock();
        }
    }

    @Override
    public final void addPropertyChangeListener(PropertyChangeListener listener) {
        lock().readLock().lock();
        try {
            throw new RuntimeException("NOT IMPLEMENTED addPropertyChangeListener");
        } finally {
            lock().readLock().unlock();
        }
    }

    @Override
    public final void removePropertyChangeListener(PropertyChangeListener listener) {
        lock().readLock().lock();
        try {
            throw new RuntimeException("NOT IMPLEMENTED removePropertyChangeListener");
        } finally {
            lock().readLock().unlock();
        }
    }

    @Override
    public final void save(Session session) throws IOException {
        log.debug("saving session {}", session.getId());
        lock().readLock().lock();
        try {
            getBackend().put(idToBytes(session.getId()), sessionToBytes((StandardSession) session));
        } finally {
            lock().readLock().unlock();
        }
    }

    @Override
    public final Session load(String id) throws ClassNotFoundException, IOException {
        log.debug("loading session {}", id);
        lock().readLock().lock();
        try {
            return sessionFromBytes(getBackend().get(idToBytes(id)));
        } finally {
            lock().readLock().unlock();
        }
    }

    @Override
    public final void remove(String id) throws IOException {
        log.debug("removing session {}", id);
        lock().readLock().lock();
        try {
            getBackend().remove(idToBytes(id));
        } finally {
            lock().readLock().unlock();
        }
    }

    @Override
    public final int getSize() throws IOException {
        log.debug("getting session count");
        lock().readLock().lock();
        try {
            return getBackend().size();
        } finally {
            lock().readLock().unlock();
        }
    }

    @Override
    public final String[] keys() throws IOException {
        log.debug("getting session keys");
        lock().readLock().lock();
        try {
            Set<byte[]> keySet = getBackend().keySet();
            String[] keys = new String[keySet.size()];
            int i = 0;
            for (byte[] key : keySet) {
                keys[i++] = idFromBytes(key);
            }
            return keys;
        } finally {
            lock().readLock().unlock();
        }
    }

    @Override
    public final void clear() throws IOException {
        log.debug("clearing sessions");
        lock().readLock().lock();
        try {
            getBackend().clear();
        } finally {
            lock().readLock().unlock();
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

    /**
     * Serialize a {@code Session} ID to a byte array.  ID is encoded using the {@link #CHARSET} character set.
     */
    private byte[] idToBytes(String id) {
        return id.getBytes(CHARSET);
    }

    /**
     * Deserialize a {@code Session} ID to a byte array.  ID is decoded using the {@link #CHARSET} character set.
     */
    private String idFromBytes(byte[] id) {
        return new String(id, CHARSET);
    }


    /**
     * Serialize a {@code StandardSession} to a byte array.  Serialization uses {@code StandardSession}'s
     * <a href="https://tomcat.apache.org/tomcat-8.5-doc/api/org/apache/catalina/session/StandardSession.html#writeObjectData(java.io.ObjectOutputStream)">{@code writeObjectData}</a>.
     *
     * @param session session to be serialized
     * @return serialized session as a byte array
     * @throws IOException if an I/O error occurs
     * @see org.apache.catalina.session.StandardSession#writeObjectData(ObjectOutputStream)
     */
    private byte[] sessionToBytes(StandardSession session) throws IOException {

        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            session.writeObjectData(oos);
            oos.flush();
            return bos.toByteArray();
        }
    }

    /**
     * Deserialize a {@code StandardSession} from a byte array.  Deserialization uses {@code StandardSession}'s
     * <a href="https://tomcat.apache.org/tomcat-8.5-doc/api/org/apache/catalina/session/StandardSession.html#readObjectData(java.io.ObjectInputStream)">{@code readObjectData}</a>.
     *
     * @param bytes byte array to be de-serialized
     * @return deserialized session
     * @throws ClassNotFoundException if an error occurs during deserialization
     * @throws IOException            if an I/O error occurs
     * @see org.apache.catalina.session.StandardSession#readObjectData(ObjectInputStream)
     */
    private Session sessionFromBytes(byte[] bytes) throws IOException, ClassNotFoundException {
        if (bytes == null) {
            return null;
        }
        return tomcatAdapter.sessionFromBytes(bytes, getManager());
    }

    /**
     * Load a {@link TomcatAdapter} for the Tomcat version in which this store is running.
     *
     * @return adapter for the running version of Tomcat
     */
    private TomcatAdapter loadTomcatAdapter() {
        String version = ServerInfo.getServerNumber();
        String[] versionNodes = version.split("\\.");
        String major = versionNodes[0];
        if (!versionNodes[1].equals("0")) {
            major += versionNodes[1];
        }
        String className =
                String.format("io.pivotal.appsuite.tomcat.v%1$s.Tomcat%1$sAdapter", major);
        try {
            log.debug("loading tomcat adapter {}", className);
            return (TomcatAdapter) Class.forName(className).newInstance();
        } catch (Throwable t) {
            throw new InstantiationError("unable to load tomcat adapter " + className + ": " + t);
        }
    }

    // -----------------------------------------------------------------------
    //   subclasses
    // -----------------------------------------------------------------------

    /**
     * Return this store's {@link Backend};
     *
     * @return this store's {@code Backend}
     */
    protected abstract Backend getBackend();

    /**
     * Return the read/write lock.
     *
     * @return this read/write lock
     */
    protected final ReadWriteLock lock() {
        return lock;
    }

}
