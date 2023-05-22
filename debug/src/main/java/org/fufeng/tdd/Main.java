package org.fufeng.tdd;

import jakarta.ws.rs.core.UriBuilder;
import org.glassfish.jersey.jetty.JettyHttpContainerFactory;

/**
 * {@link <a href="https://mkyong.com/webservices/jax-rs/jersey-and-hk2-dependency-injection-auto-scanning/">...</a>}
 */
public class Main {
    public static void main(String[] args) throws Exception {
        // pass the ServiceLocator to Grizzly Http Server
        JettyHttpContainerFactory
                .createServer(UriBuilder.fromUri("http://localhost/").port(8080).build(), new Application())
                .start();
    }
}