package org.fufeng.tdd;

import jakarta.ws.rs.core.UriBuilder;
import org.fufeng.tdd.api.StudentResources;
import org.fufeng.tdd.api.model.StudentRepository;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.jetty.JettyHttpContainerFactory;
import org.glassfish.jersey.server.ResourceConfig;

public class StudentApplication extends ResourceConfig {

    public StudentApplication() {
        // scan packages
        this.register(StudentResources.class);
        this.register(new AbstractBinder(){
            @Override
            protected void configure() {
                // map this service to this contract
                bind(StudentRepository.class).to(StudentRepository.class);
            }
        });
    }

    public static void main(String[] args) throws Exception {
        // pass the ServiceLocator to Grizzly Http Server
        JettyHttpContainerFactory
                .createServer(UriBuilder.fromUri("http://localhost/").port(8080).build(), new StudentApplication())
                .start();
    }
}
