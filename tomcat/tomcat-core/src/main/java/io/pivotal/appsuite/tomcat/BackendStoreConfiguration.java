package io.pivotal.appsuite.tomcat;

/**
 * An interface defining configured {@link BackendStore} properties.
 */
public interface BackendStoreConfiguration {

    /**
     * Return whether the {@link BackendSpy} should be enabled.
     *
     * @return whether the {@code BackendSpy} should be enabled
     */
    boolean isSpyEnabled();

    /**
     * Return whether the {@link SessionFlushValve} should be enabled.
     *
     * @return whether the {@code SessionFlushValve} should be enabled.
     */
    boolean isSessionFlushValveEnabled();

}
