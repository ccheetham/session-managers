package io.pivotal.appsuite.tomcat.v8;

import io.pivotal.appsuite.tomcat.SessionFlushValve;
import org.apache.catalina.comet.CometEvent;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;

import javax.servlet.ServletException;
import java.io.IOException;

public class Tomcat8SessionFlushValve extends SessionFlushValve {

    @Override
    public void event(Request request, Response response, CometEvent event) throws IOException, ServletException {
    }

}
