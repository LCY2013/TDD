package org.fufeng.tdd.api;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.fufeng.tdd.api.model.Student;
import org.fufeng.tdd.api.model.StudentRepository;
import org.jvnet.hk2.annotations.Service;

import java.util.List;

@Path("/students")
@Service
public class StudentResources {

    private final StudentRepository studentRepository;

    @Inject
    public StudentResources(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Student> all() {
        return studentRepository.all();
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findById(@PathParam("id") long id) {
        return studentRepository.findById(id).
                map(Response::ok).
                orElse(Response.status(Response.Status.NOT_FOUND)).build();
    }

}
