package org.fufeng.tdd.model;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

// 行为验证
class StudentMockTest {
    private EntityManager entityManager;
    private Students studentRepository;

    private Student john = new Student("john", "smith", "john.smith@email.com");

    @BeforeEach
    public void before() {
        entityManager = mock(EntityManager.class);
        studentRepository = new Students(entityManager);
    }

    @AfterEach
    public void after() {
        entityManager.clear();
        entityManager.close();
    }

    static class Students {

        private EntityManager entityManager;

        public Students(EntityManager entityManager) {
            this.entityManager = entityManager;
        }

        public Student save(Student student) {
            entityManager.persist(student);
            return student;
        }

        public Optional<Student> findById(long id) {
            return Optional.ofNullable(entityManager.find(Student.class, id));
        }

        public Optional<Student> findByEmail(String email) {
            return entityManager.createQuery("SELECT s FROM Student s WHERE s.email = :email", Student.class).
                    setParameter("email", email).
                    getResultStream().findFirst();
        }
    }

    @Test
    @Order(1)
    public void should_generate_id_for_saved_entity() {
        studentRepository.save(john);

        verify(entityManager).persist(john);
    }

    @Test
    @Order(2)
    public void should_be_able_to_load_saved_student_by_id() {
        when(entityManager.find(any(), any())).thenReturn(john);
        //assertTrue(studentRepository.findById(1).isPresent());
        assertEquals(john, studentRepository.findById(1).get());

        verify(entityManager).find(Student.class, 1L);
    }

    @Test
    @Order(3)
    public void should_be_able_to_load_saved_student_by_email() {
        TypedQuery query = mock(TypedQuery.class);

        //when(entityManager.createQuery(any(), any())).thenReturn(query);
        when(entityManager.createQuery(any(), any())).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getResultStream()).thenReturn(Stream.of(john));
        when(query.getResultList()).thenReturn(asList(john));

        assertEquals(john, studentRepository.findByEmail("john.smith@email.com").get());

        verify(entityManager).createQuery("SELECT s FROM Student s WHERE s.email = :email", Student.class);
        verify(query.setParameter("email", "john.smith@email.com"));
    }

}