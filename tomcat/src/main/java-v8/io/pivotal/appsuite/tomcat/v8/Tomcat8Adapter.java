package io.pivotal.appsuite.tomcat.v8;

import org.apache.catalina.Context;
import org.apache.catalina.Globals;
import org.apache.catalina.Manager;
import org.apache.catalina.Session;
import org.apache.catalina.session.StandardSession;
import org.apache.catalina.util.CustomObjectInputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * Implements {@link io.pivotal.appsuite.tomcat.BackendStore} behavior specific to Tomcat 8.
 */
public class Tomcat8Adapter implements io.pivotal.appsuite.tomcat.TomcatAdapter {

    /**
     * Tomcat 8-specific Session deserialization.  Implementation borrowed heavily from
     * <a href="http://svn.apache.org/repos/asf/tomcat/tc8.0.x/trunk/java/org/apache/catalina/session/FileStore.java" target="_new">org/apache/catalina/session/FileStore.java</a>.
     */
    @Override
    public Session sessionFromBytes(byte[] bytes, Manager manager) throws IOException, ClassNotFoundException {
        Context context = manager.getContext();
        ClassLoader originalClassLoader = context.bind(Globals.IS_SECURITY_ENABLED, null);
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
             ObjectInputStream ois = new CustomObjectInputStream(bis, Thread.currentThread().getContextClassLoader())) {
            StandardSession session = (StandardSession) manager.createEmptySession();
            session.readObjectData(ois);
            session.setManager(manager);
            return session;
        } finally {
            context.unbind(Globals.IS_SECURITY_ENABLED, originalClassLoader);
        }
    }

}
