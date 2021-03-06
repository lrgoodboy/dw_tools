package com.anjuke.dw.tools.dao;

import org.springframework.data.repository.CrudRepository;

import com.anjuke.dw.tools.model.User;

public interface UserRepository extends CrudRepository<User, Long> {
    User findByUsername(String username);
    Iterable<User> findByTruenameIn(Iterable<String> truenames);
}
