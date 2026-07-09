package com.ibm.auth.repository;

import com.ibm.auth.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {

    Optional<User> findByUsername(String username);

    Optional<User> findByUsernameAndDeletedFalse(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    Optional<User> findByIdAndDeletedFalse(String id);

    List<User> findAllByDeletedFalse();

    boolean existsByEmployeeId(String employeeId);

    Optional<User> findTopByOrderByEmployeeIdDesc();

    Optional<User> findByEmployeeId(String employeeId);

}