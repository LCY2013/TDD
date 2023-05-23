package org.fufeng.tdd.model;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 *  状态验证
 */
class StudentsTest {
    private EntityManagerFactory factory;
    private EntityManager entityManager;

    private Students students;

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

        public Optional<Student> findByEmial(String email) {
            return entityManager.createQuery("SELECT s FROM Student s WHERE s.email = :email", Student.class).
                    setParameter("email", email).
                    getResultStream().findFirst();
        }
    }

    @BeforeEach
    public void before() {
        factory = Persistence.createEntityManagerFactory("student");
        entityManager = factory.createEntityManager();
        students = new Students(entityManager);
    }

    @AfterEach
    public void after() {
        entityManager.clear();
        entityManager.close();
        factory.close();
    }

    // save
    @Test
    public void should_save_student_to_db() {
        /*entityManager.getTransaction().begin();
        students.save(new Student("john1", "smith1", "john.smith@email1.com"));
        entityManager.getTransaction().commit();*/

        // 增量变更
        int before = entityManager.createNativeQuery("SELECT id, first_name, last_name, email FROM STUDENTS s").getResultList().size();

        // exercise
        entityManager.getTransaction().begin();
        Student saved = students.save(new Student("john", "smith", "john.smith@email.com"));
        entityManager.getTransaction().commit();

        // verify
        List resultList = entityManager.createNativeQuery("SELECT id, first_name, last_name, email FROM STUDENTS s").getResultList();

        assertEquals(before + 1, resultList.size());

        Object[] john = (Object[])resultList.get(before);
        assertEquals(saved.getId(), john[0]);
        assertEquals(saved.getFirstName(), john[1]);
        assertEquals(saved.getLastName(), john[2]);
        assertEquals(saved.getEmail(), john[3]);

        // teardown
    }

    // findById
    @Test
    public void should_be_able_to_load_saved_student_by_id() {
        // exercise
        entityManager.getTransaction().begin();
        Student saved = students.save(new Student("john", "smith", "john.smith@email.com"));
        entityManager.getTransaction().commit();

        Optional<Student> load = students.findById(saved.getId());

        // verify
        assertTrue(load.isPresent());

        assertEquals(saved.getId(), load.get().getId());
        assertEquals(saved.getFirstName(), load.get().getFirstName());
        assertEquals(saved.getLastName(), load.get().getLastName());
        assertEquals(saved.getEmail(), load.get().getEmail());

        // teardown
    }

    // findByEmail
    @Test
    public void should_be_able_to_load_saved_student_by_email() {
        // exercise
        entityManager.getTransaction().begin();
        Student saved = students.save(new Student("john", "smith", "john.smith@email.com"));
        entityManager.getTransaction().commit();

        Optional<Student> load = students.findByEmial(saved.getEmail());

        // verify
        assertTrue(load.isPresent());

        assertEquals(saved.getId(), load.get().getId());
        assertEquals(saved.getFirstName(), load.get().getFirstName());
        assertEquals(saved.getLastName(), load.get().getLastName());
        assertEquals(saved.getEmail(), load.get().getEmail());

        // teardown
    }

}