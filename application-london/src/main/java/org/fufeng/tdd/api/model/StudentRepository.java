package org.fufeng.tdd.api.model;

import java.util.List;
import java.util.Optional;

public interface StudentRepository {

    List<Student> all();

    Optional<Student> findById(long id);

    Optional<Student> findByEmail(String email);
}
