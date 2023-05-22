package org.fufeng.tdd;

import org.fufeng.tdd.model.StudentRepository;
import org.fufeng.tdd.resources.StudentResources;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;

public class Application extends ResourceConfig {

    public Application() {
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
}
