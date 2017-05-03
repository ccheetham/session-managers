package io.pivotal.appsuite.tomcat;

import org.apache.catalina.Manager;
import org.apache.catalina.Session;

import java.io.IOException;

/**
 * An interface specifying behavior for Tomcat version-specific behavior.  When a {@link BackendStore} is constructed,
 * a concrete implementation of this interface will be loaded based on the version of Tomcat in which the store is
 * contained.
 */
public interface TomcatAdapter {

    /**
     * Deserialize a {@link Session} from the specified {@code bytes}.
     *
     * @param bytes serialized {@code Session}
     * @param manager Tomcat manager associated with the {@link BackendStore}
     * @return deserialized session
     * @throws IOException if an I/O error occurs
     * @throws ClassNotFoundException if an error occurs during deserialization
     */
    Session sessionFromBytes(byte[] bytes, Manager manager) throws IOException, ClassNotFoundException;

}
