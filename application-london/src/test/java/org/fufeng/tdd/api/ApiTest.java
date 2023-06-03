package org.fufeng.tdd.api;

import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Response;
import org.fufeng.tdd.StudentApplication;
import org.fufeng.tdd.api.model.Student;
import org.fufeng.tdd.api.model.StudentRepository;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * TDD-伦敦学派，先分层
 */
public class ApiTest extends JerseyTest {

    private static final StudentRepository studentRepository = mock(StudentRepository.class);

    List<Student> mockStudents = List.of(
            new Student(1, "john1", "smith", "john.smith@email1.com"),
            new Student(2, "john2", "smith", "john.smith@email2.com"),
            new Student(3, "john3", "smith", "john.smith@email3.com"),
            new Student(4, "john4", "smith", "john.smith@email4.com")
            );


    @Override
    protected Application configure() {
        return new StudentApplication(studentRepository);
    }

    @Test
    public void should_fetch_all_students_from_api() {
        when(studentRepository.all()).thenReturn(mockStudents);


        Student[] students = target("students").request().get(Student[].class);
        assertEquals(4, students.length);

        assertEquals("john1", students[0].getFirstName());
        assertEquals("smith", students[0].getLastName());
        assertEquals("john.smith@email1.com", students[0].getEmail());
        assertEquals(1, students[0].getId());
    }

    @Test
    public void should_be_able_fetch_student_by_id() {
        when(studentRepository.findById(eq(1L))).thenReturn(Optional.of(mockStudents.get(0)));

        Student students = target("students/1").request().get(Student.class);

        assertEquals("john1", students.getFirstName());
        assertEquals("smith", students.getLastName());
        assertEquals("john.smith@email1.com", students.getEmail());
        assertEquals(1, students.getId());
    }

    @Test
    public void should_return_404_if_no_student_found() {
        when(studentRepository.findById(eq(5L))).thenReturn(Optional.empty());

        Response response = target("students/5").request().get(Response.class);
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }
}
