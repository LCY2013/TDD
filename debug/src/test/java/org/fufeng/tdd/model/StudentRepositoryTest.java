package org.fufeng.tdd.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class StudentRepositoryTest {

    @Test
    public void should_save_student() {
        StudentRepository repository = new StudentRepository();

        assertEquals(4, repository.all().size());

        Student student = repository.save(new Student("john", "smith", "john.smith@email.com"));

        assertEquals(5, repository.all().size());
        assertNotEquals(0, student.getId());
    }

}