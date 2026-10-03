package com.packflow.service;

import com.packflow.dao.CustomerDAO;
import com.packflow.dao.UserDAO;
import com.packflow.dao.impl.CustomerDAOImpl;
import com.packflow.dao.impl.UserDAOImpl;
import com.packflow.exception.UserNotFoundException;
import com.packflow.model.Customer;
import com.packflow.model.CustomerUser;
import com.packflow.model.Role;
import com.packflow.model.User;
import com.packflow.util.PasswordUtil;

import java.util.List;
import java.util.Optional;

/**
 * Service orchestrating User authentication, profile operations, and account management.
 */
public class UserService {

    private final UserDAO userDAO;
    private final CustomerDAO customerDAO;

    public UserService() {
        this.userDAO = new UserDAOImpl();
        this.customerDAO = new CustomerDAOImpl();
    }

    public UserService(UserDAO userDAO, CustomerDAO customerDAO) {
        this.userDAO = userDAO;
        this.customerDAO = customerDAO;
    }

    /**
     * Authenticates user with email and password.
     *
     * @param email User email address
     * @param password Plain text password
     * @return Authenticated User object
     * @throws UserNotFoundException if invalid credentials or inactive account
     */
    public User authenticate(String email, String password) throws UserNotFoundException {
        if (email == null || password == null || email.trim().isEmpty() || password.isEmpty()) {
            throw new UserNotFoundException("Email and password are required.");
        }

        Optional<User> optUser = userDAO.findByEmail(email.trim());
        if (optUser.isEmpty()) {
            throw new UserNotFoundException("Invalid email or password.");
        }

        User user = optUser.get();
        if (!user.isActive()) {
            throw new UserNotFoundException("Your account is currently inactive. Please contact support.");
        }

        if (!PasswordUtil.checkPassword(password, user.getPasswordHash())) {
            throw new UserNotFoundException("Invalid email or password.");
        }

        // If Customer user, attach customer metadata
        if (user instanceof CustomerUser cu) {
            customerDAO.findByUserId(user.getUserId()).ifPresent(c -> {
                cu.setCustomerId(c.getCustomerId());
                cu.setCompanyName(c.getCompanyName());
            });
        }

        return user;
    }

    public List<User> getAllUsers() {
        return userDAO.findAll();
    }

    public Optional<User> getUserById(int userId) {
        return userDAO.findById(userId);
    }

    public boolean registerUser(User user, String plainPassword) {
        if (plainPassword == null || plainPassword.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters.");
        }
        if (userDAO.findByEmail(user.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email is already registered: " + user.getEmail());
        }

        user.setPasswordHash(PasswordUtil.hashPassword(plainPassword));
        return userDAO.create(user);
    }

    public boolean updateUser(User user) {
        return userDAO.update(user);
    }

    public boolean changePassword(int userId, String oldPassword, String newPassword) throws UserNotFoundException {
        Optional<User> opt = userDAO.findById(userId);
        if (opt.isEmpty()) {
            throw new UserNotFoundException("User not found.");
        }
        User user = opt.get();
        if (!PasswordUtil.checkPassword(oldPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Current password is incorrect.");
        }
        if (newPassword == null || newPassword.length() < 6) {
            throw new IllegalArgumentException("New password must be at least 6 characters.");
        }

        String newHash = PasswordUtil.hashPassword(newPassword);
        return userDAO.updatePassword(userId, newHash);
    }

    public boolean deleteUser(int userId) {
        return userDAO.delete(userId);
    }
}
