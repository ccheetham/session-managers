package io.pivotal.appsuite.tomcat;

import org.apache.catalina.util.ServerInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides access to Tomcat specifics.
 */
public class Tomcat {

    private static final Logger log = LoggerFactory.getLogger(Tomcat.class);

    private static TomcatAdapter adapter;

    /**
     * Return the {@link TomcatAdapter} instance.
     *
     * @return {@code TomcatAdapter} instance
     */
    public static synchronized TomcatAdapter getAdapterInstance() {
        if (adapter == null) {
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
                adapter = (TomcatAdapter) Class.forName(className).newInstance();
            } catch (Throwable t) {
                throw new ExceptionInInitializerError("unable to load tomcat adapter " + className + ": " + t);
            }
        }
        return adapter;
    }

    /**
     * Set the {@link TomcatAdapter} instance, overriding the default.
     *
     * @param instance {@code TomcatAdapter} instance
     */
    public static synchronized void  setAdapterInstance(TomcatAdapter instance) {
        adapter = instance;
    }

}
