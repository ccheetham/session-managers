package io.pivotal.appsuite.tomcat.v7;

import org.apache.catalina.Context;
import org.apache.catalina.Loader;
import org.apache.catalina.Manager;
import org.apache.catalina.Session;
import org.apache.catalina.session.StandardSession;
import org.apache.catalina.util.CustomObjectInputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * Implements {@link io.pivotal.appsuite.tomcat.BackendStore} behavior specific to Tomcat 7.
 */
public class Tomcat7Adapter implements io.pivotal.appsuite.tomcat.TomcatAdapter {


    /**
     * Tomcat 7-specific Session deserialization.  Implementation borrowed heavily from
     * <a href="http://svn.apache.org/repos/asf/tomcat/tc7.0.x/trunk/java/org/apache/catalina/session/FileStore.java" target="_new">org/apache/catalina/session/FileStore.java</a>.
     */
    @Override
    public Session sessionFromBytes(byte[] bytes, Manager manager) throws IOException, ClassNotFoundException {
        Context context = ((Context) manager.getContainer());
        Loader loader = null;
        ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
        ClassLoader tomcatClassLoader = null;
        {
            if (context != null) {
                loader = context.getLoader();
            }
            if (loader != null) {
                tomcatClassLoader = loader.getClassLoader();
            }
            if (tomcatClassLoader == null) {
                tomcatClassLoader = originalClassLoader;
            }
        }
        Thread.currentThread().setContextClassLoader(tomcatClassLoader);
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
             ObjectInputStream ois = new CustomObjectInputStream(bis, tomcatClassLoader)) {
            StandardSession session = (StandardSession) manager.createEmptySession();
            session.readObjectData(ois);
            session.setManager(manager);
            return session;
        } finally {
            Thread.currentThread().setContextClassLoader(originalClassLoader);
        }
    }

}