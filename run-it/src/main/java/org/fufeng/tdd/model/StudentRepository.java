package org.fufeng.tdd.model;

import jakarta.inject.Named;
import jakarta.inject.Singleton;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.TypedQuery;
import org.jvnet.hk2.annotations.Contract;
import org.jvnet.hk2.annotations.Service;

import java.util.List;
import java.util.Optional;

@Service
@Contract
@Singleton
public class StudentRepository {

    private static final EntityManager manager;

    private static final EntityManagerFactory factory;

    static {
        factory = Persistence.createEntityManagerFactory("student");
        manager = factory.createEntityManager();
        manager.getTransaction().begin();
        manager.persist(new Student("john1", "smith", "john.smith@email1.com"));
        manager.persist(new Student("john2", "smith", "john.smith@email2.com"));
        manager.persist(new Student("john3", "smith", "john.smith@email3.com"));
        manager.persist(new Student("john4", "smith", "john.smith@email4.com"));
        manager.getTransaction().commit();
        System.out.println(manager.createQuery("SELECT s FROM Student s", Student.class).getResultList());
    }

    public StudentRepository() {
    }

    public Student save(Student student) {
        manager.persist(student);
        return student;
    }

    public List<Student> all() {
        return manager.createQuery("SELECT s FROM Student s", Student.class).getResultList();
    }

    public Optional<Student> findById(long id) {
        return Optional.ofNullable(manager.find(Student.class, id));
    }

    public Optional<Student> findByEmail(String email) {
        TypedQuery<Student> query = manager.createQuery("SELECT s FROM Student s WHERE s.email = :email", Student.class);
        return query.setParameter("email", email).getResultStream().findFirst();
    }
}
