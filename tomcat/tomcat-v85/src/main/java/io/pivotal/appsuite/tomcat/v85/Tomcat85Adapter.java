package io.pivotal.appsuite.tomcat.v85;

import io.pivotal.appsuite.tomcat.SessionFlushValve;
import io.pivotal.appsuite.tomcat.TomcatAdapter;
import org.apache.catalina.*;
import org.apache.catalina.session.StandardSession;
import org.apache.catalina.util.CustomObjectInputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * Provides behavior specific to Tomcat 8.5.
 */
public class Tomcat85Adapter implements TomcatAdapter {

    @Override
    public Pipeline getPipeline(Store store) {
        return store.getManager().getContext().getPipeline();
    }

    @Override
    public SessionFlushValve createSessionFlushValve() {
        return new Tomcat85SessionFlushValve();
    }

    /**
     * Tomcat 8-specific Session deserialization.  Implementation borrowed heavily from
     * <a href="http://svn.apache.org/repos/asf/tomcat/tc8.5.x/trunk/java/org/apache/catalina/session/FileStore.java" target="_new">org/apache/catalina/session/FileStore.java</a>.
     */
    @Override
    public Session sessionFromBytes(Store store, byte[] bytes) throws IOException, ClassNotFoundException {
        Manager manager = store.getManager();
        Context context = manager.getContext();
        ClassLoader originalClassLoader = context.bind(Globals.IS_SECURITY_ENABLED, null);
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new CustomObjectInputStream(bis, Thread.currentThread().getContextClassLoader());
        try {
            StandardSession session = (StandardSession) manager.createEmptySession();
            session.readObjectData(ois);
            session.setManager(manager);
            return session;
        } finally {
            try {
                ois.close();
            } finally {
                context.unbind(Globals.IS_SECURITY_ENABLED, originalClassLoader);
            }
        }
    }

}
