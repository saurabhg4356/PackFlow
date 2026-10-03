package com.packflow.dao;

import com.packflow.model.Product;

import java.util.List;
import java.util.Optional;

/**
 * Data Access Object Interface for Product entities.
 */
public interface ProductDAO {
    Optional<Product> findById(int productId);
    Optional<Product> findByCode(String productCode);
    List<Product> findAll();
    List<Product> findByCustomerId(int customerId);
    List<Product> findPaginated(int offset, int limit, String search, Integer customerId);
    int countTotal(String search, Integer customerId);
    boolean create(Product product);
    boolean update(Product product);
    boolean delete(int productId);
}
