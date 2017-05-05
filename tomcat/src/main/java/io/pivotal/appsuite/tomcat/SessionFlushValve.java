package io.pivotal.appsuite.tomcat;

import org.apache.catalina.*;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import java.io.IOException;

/**
 * A {@link Valve} that flushes a {@link Session} to a {@link BackendStore} after an HTTP request.
 */
public abstract class SessionFlushValve implements Valve, Contained {

    private final Logger log;

    private Valve next;

    private Container container;

    private Store store;

    public SessionFlushValve() {
        log = LoggerFactory.getLogger(getClass());
        log.info(getInfo());
    }

    /**
     * Called by a {@link BackendStore} upon receiving this instance.
     *
     * @param store the associated {@code BackendStore}
     */
    void setStore(BackendStore store) {
        this.store = store;
    }

    /**
     * Returns a descriptive {@code String} in Tomcat's preferred style.
     *
     * @return Tomcat-friendly description
     */
    public String getInfo() {
        StringBuilder buf = new StringBuilder();
        Package pkg = getClass().getPackage();
        String vendor = pkg.getImplementationVendor();
        if (vendor != null) {
            buf.append(vendor);
        } else {
            buf.append("Pivotal");
        }
        buf.append(" Session Flush Valve");
        String version = pkg.getImplementationVersion();
        if (version != null) {
            buf.append("/").append(version);
        }
        return buf.toString();
    }

    /**
     * Invokes the next {@link Valve} in the {@link Pipeline} and then saves the {@link Session}, if one exists,
     * in the associated {@link BackendStore}.
     *
     * @param request  HTTP request
     * @param response HTTP response
     * @throws IOException      if an I/O error occurs
     * @throws ServletException if an error occurs processing the request
     */
    @Override
    public void invoke(Request request, Response response) throws IOException, ServletException {
        if (next != null) {
            next.invoke(request, response);
        }
        Session session = request.getSessionInternal(false);
        if (session != null && session.isValid()) {
            log.debug("flushing {}", session.getId());
            store.save(session);
        }
    }

    @Override
    public Valve getNext() {
        return next;
    }

    @Override
    public void setNext(Valve valve) {
        this.next = valve;
    }

    @Override
    public void backgroundProcess() {
        log.info("BACKGROUND PROCESS");
    }

    @Override
    public boolean isAsyncSupported() {
        return false;
    }

    @Override
    public Container getContainer() {
        return container;
    }

    @Override
    public void setContainer(Container container) {
        this.container = container;
    }

}
