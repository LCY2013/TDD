package org.fufeng.tdd.resources;

import jakarta.ws.rs.core.Form;
import org.fufeng.tdd.model.Student;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class FormHelperTest {

    @Test
    public void should_read_student_from_form() {
        Form form = new Form();
        form.param("students[first_name]", "Hannah").
                param("students[last_name]", "Abbott").
                param("students[email]", "Hannah.Abbott@email.com");
        form.param("students[first_name]", "Cuthbert").
                param("students[last_name]", "Binne").
                param("students[email]", "Cuthbert.Binne@email.com");

        Student[] students = FormHelper.toStudents(form.asMap()).toArray(Student[]::new);

        assertEquals(2, students.length);

        assertEquals("Hannah", students[0].getFirstName());
        assertEquals("Abbott", students[0].getLastName());
        assertEquals("Hannah.Abbott@email.com", students[0].getEmail());
        assertEquals(0, students[0].getId());

        assertEquals("Cuthbert", students[1].getFirstName());
        assertEquals("Binne", students[1].getLastName());
        assertEquals("Cuthbert.Binne@email.com", students[1].getEmail());
        assertEquals(0, students[1].getId());
    }

}