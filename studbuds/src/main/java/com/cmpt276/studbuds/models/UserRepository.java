package com.cmpt276.studbuds.models;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User,Integer> {
    List<User> findByNameAndPassword(String name, String password);
    boolean existsByName(String name);

}
