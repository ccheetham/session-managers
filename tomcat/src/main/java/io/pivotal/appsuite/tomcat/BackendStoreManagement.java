package io.pivotal.appsuite.tomcat;

/**
 * An interface defining configurable {@link BackendStore} properties.
 */
public interface BackendStoreManagement extends BackendStoreConfiguration {

    /**
     * Set whether the {@link SessionFlushValve} should be enabled.
     * @param enabled set the {@link SessionFlushValve} to enabled/disabled
     */
    void setSessionFlushValveEnabled(boolean enabled);

}