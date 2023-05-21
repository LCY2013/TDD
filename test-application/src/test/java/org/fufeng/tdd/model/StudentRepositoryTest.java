package org.fufeng.tdd.model;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class StudentRepositoryTest {
    private EntityManagerFactory factory;
    private EntityManager entityManager;
    private StudentRepository studentRepository;

    private Student john;

    @BeforeEach
    public void before() {
        factory = Persistence.createEntityManagerFactory("student");
        entityManager = factory.createEntityManager();
        studentRepository = new StudentRepository(entityManager);

        entityManager.getTransaction().begin();
        john = studentRepository.save(new Student("john", "smith", "john.smith@email.com"));
        entityManager.getTransaction().commit();
    }

    @AfterEach
    public void after() {
        entityManager.clear();
        entityManager.close();
        factory.close();
    }

    @Test
    public void should_generate_id_for_saved_entity() {
        assertNotEquals(0, john.getId());
    }


    @Test
    public void should_be_able_to_load_saved_student_by_id() {
        Optional<Student> loaded = studentRepository.findById(john.getId());

        assertTrue(loaded.isPresent());

        assertEquals(john.getId(), loaded.get().getId());
        assertEquals(john.getFirstName(), loaded.get().getFirstName());
        assertEquals(john.getLastName(), loaded.get().getLastName());
        assertEquals(john.getEmail(), loaded.get().getEmail());
    }

    @Test
    public void should_be_able_to_load_saved_student_by_email() {
        Optional<Student> loaded = studentRepository.findByEmail(john.getEmail());

        assertTrue(loaded.isPresent());

        assertEquals(john.getId(), loaded.get().getId());
        assertEquals(john.getFirstName(), loaded.get().getFirstName());
        assertEquals(john.getLastName(), loaded.get().getLastName());
        assertEquals(john.getEmail(), loaded.get().getEmail());
    }

}