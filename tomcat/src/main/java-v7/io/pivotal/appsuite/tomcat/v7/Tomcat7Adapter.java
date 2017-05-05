package io.pivotal.appsuite.tomcat.v7;

import io.pivotal.appsuite.tomcat.SessionFlushValve;
import io.pivotal.appsuite.tomcat.TomcatAdapter;
import org.apache.catalina.*;
import org.apache.catalina.session.StandardSession;
import org.apache.catalina.util.CustomObjectInputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * Provides behavior specific to Tomcat 7.
 */
public class Tomcat7Adapter implements TomcatAdapter {


    @Override
    public Pipeline getPipeline(Store store) {
        return store.getManager().getContainer().getPipeline();
    }

    @Override
    public SessionFlushValve createSessionFlushValve() {
        return new Tomcat7SessionFlushValve();
    }

    /**
     * Tomcat 7-specific Session deserialization.  Implementation borrowed heavily from
     * <a href="http://svn.apache.org/repos/asf/tomcat/tc7.0.x/trunk/java/org/apache/catalina/session/FileStore.java" target="_new">org/apache/catalina/session/FileStore.java</a>.
     */
    @Override
    public Session sessionFromBytes(Store store, byte[] bytes) throws IOException, ClassNotFoundException {
        Manager manager = store.getManager();
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
