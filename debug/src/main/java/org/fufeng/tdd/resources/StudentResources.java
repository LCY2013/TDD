package org.fufeng.tdd.resources;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import org.fufeng.tdd.model.Student;
import org.fufeng.tdd.model.StudentRepository;
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

    @POST
    @Produces(MediaType.APPLICATION_FORM_URLENCODED)
    public Response save(MultivaluedMap<String, String> form) {
        FormHelper.toStudents(form).forEach(studentRepository::save);

        return Response.created(null).build();
    }

}
