package com.packflow.dao;

import com.packflow.model.Role;
import com.packflow.model.User;

import java.util.List;
import java.util.Optional;

/**
 * Data Access Object Interface for User entity operations.
 */
public interface UserDAO {
    Optional<User> findById(int userId);
    Optional<User> findByEmail(String email);
    List<User> findAll();
    List<User> findByRole(Role role);
    boolean create(User user);
    boolean update(User user);
    boolean updatePassword(int userId, String newPasswordHash);
    boolean delete(int userId);
    int countTotal();
}
