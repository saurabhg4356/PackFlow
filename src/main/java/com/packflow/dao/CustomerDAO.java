package com.packflow.dao;

import com.packflow.model.Customer;

import java.util.List;
import java.util.Optional;

/**
 * Data Access Object Interface for Customer entities.
 */
public interface CustomerDAO {
    Optional<Customer> findById(int customerId);
    Optional<Customer> findByUserId(int userId);
    Optional<Customer> findByEmail(String email);
    List<Customer> findAll();
    List<Customer> findPaginated(int offset, int limit, String search);
    int countTotal(String search);
    boolean create(Customer customer);
    boolean update(Customer customer);
    boolean delete(int customerId);
}
