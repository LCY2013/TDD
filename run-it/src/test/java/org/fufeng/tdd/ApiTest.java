package org.fufeng.tdd;

import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Response;
import org.fufeng.tdd.model.Student;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ApiTest extends JerseyTest {

    @Override
    protected Application configure() {
        return new org.fufeng.tdd.Application();
    }

    @Test
    public void should_fetch_all_students_from_api() {
        Student[] students = target("students").request().get(Student[].class);
        assertEquals(4, students.length);

        assertEquals("john1", students[0].getFirstName());
        assertEquals("smith", students[0].getLastName());
        assertEquals("john.smith@email1.com", students[0].getEmail());
        assertEquals(1, students[0].getId());
    }

    @Test
    public void should_be_able_fetch_student_by_id() {
        Student students = target("students/1").request().get(Student.class);

        assertEquals("john1", students.getFirstName());
        assertEquals("smith", students.getLastName());
        assertEquals("john.smith@email1.com", students.getEmail());
        assertEquals(1, students.getId());
    }

    @Test
    public void should_return_404_if_no_student_found() {
        Response response = target("students/5").request().get(Response.class);
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }
}
