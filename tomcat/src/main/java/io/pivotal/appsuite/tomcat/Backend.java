package io.pivotal.appsuite.tomcat;

import java.io.IOException;
import java.util.Set;

/**
 * A backend data store/database maintaining a map-like structure.
 * Used by a {@link BackendStore} to delegate session storage.
 */
public interface Backend {

    /**
     * Prepare the store backend to be started, but do not acquire any system resources.
     */
    void init();

    /**
     * Start store backend, acquiring any needed system resources.
     * When this returns, the store should be ready to manager session data.
     */
    void start();

    /**
     * Stop the store backend, releasing any acquired system resources.
     * When this returns, the store will no longer be able to manager session data.
     */
    void stop();

    /**
     * Associates the specified value with the specified key in this backend.
     * If the backend previously contained a mapping for the key, the old value is replaced.
     * @param key key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @throws IOException if an I/O error occurs
     */
    void put(byte[] key, byte[] value) throws IOException;

    /**
     * Returns the value to which the specified key is mapped, or {@code null} if this backend contains no mapping for the key.
     * @param key the key whose associated value is to be returned
     * @return the value to which the specified key is mapped, or {@code null} if this backend contains no mapping for the key
     * @throws IOException if an I/O error occurs
     */
    byte[] get(byte[] key) throws IOException;

    /**
     * Removes the mapping for the specified key from this backend if present.
     * @param key whose mapping is to be removed from the backend
     * @throws IOException if an I/O error occurs
     */
    void remove(byte[] key) throws IOException;

    /**
     * Removes all of the mappings from this backend. The backend will be empty after this call returns
     * @throws IOException if an I/O error occurs
     */
    void clear() throws IOException;

    /**
     * Return the number of sessions in the store.
     * @return number of sessions in store
     * @throws IOException if an I/O error occurs
     */
    int size() throws IOException;

    /**
     * Returns the keys contained in this backend.
     * @return the keys contained in this backend
     * @throws IOException if an I/O error occurs
     */
    Set<byte[]> keys() throws IOException;

}
