package io.pivotal.appsuite.tomcat;

import org.apache.catalina.Pipeline;
import org.apache.catalina.Session;
import org.apache.catalina.Store;

import java.io.IOException;

/**
 * An interface for deferring Tomcat-specific implementations until runtime.
 */
public interface TomcatAdapter {

    /**
     * Return the {@link Pipeline} associated with the store.
     *
     * @param store associated {@code Store}
     * @return {@code Pipeline} associated with the store
     */
    Pipeline getPipeline(Store store);

    /**
     * Create a new {@link SessionFlushValve} suitable for this version of Tomcat.
     * @return new {@code SessionFlushValve}
     */
    SessionFlushValve createSessionFlushValve();

    /**
     * Deserialize a {@link Session} from the specified {@code bytes}.
     *
     * @param store acsociated {@code Store}
     * @param bytes serialized {@code Session}
     * @return deserialized session
     * @throws IOException if an I/O error occurs
     * @throws ClassNotFoundException if an error occurs during deserialization
     */
    Session sessionFromBytes(Store store, byte[] bytes) throws IOException, ClassNotFoundException;

}
