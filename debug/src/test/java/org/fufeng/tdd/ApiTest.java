package org.fufeng.tdd;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Form;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.fufeng.tdd.model.Student;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class ApiTest extends JerseyTest {

    @Override
    protected Application configure() {
        return new Application();
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

    @Test
    public void should_create_students_via_api() {
        Student[] students = target("students").request().get(Student[].class);
        assertEquals(4, students.length);

        Form form = new Form();
        form.param("students[first_name]", "Hannah").
                param("students[last_name]", "Abbott").
                param("students[email]", "Hannah.Abbott@email.com");
        form.param("students[first_name]", "Cuthbert").
                param("students[last_name]", "Binne").
                param("students[email]", "Cuthbert.Binne@email.com");

        Response response = target("students").request().post(Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED));

        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());

        Student[] after = target("students").request().get(Student[].class);
        assertEquals(6, after.length);

        assertEquals("Hannah", after[4].getFirstName());
        assertEquals("Abbott", after[4].getLastName());
        assertEquals("Hannah.Abbott@email.com", after[4].getEmail());
        assertNotEquals(0, after[4].getId());

        assertEquals("Cuthbert", after[5].getFirstName());
        assertEquals("Binne", after[5].getLastName());
        assertEquals("Cuthbert.Binne@email.com", after[5].getEmail());
        assertNotEquals(0, after[5].getId());
    }

}
