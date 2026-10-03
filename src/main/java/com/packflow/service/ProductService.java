package com.packflow.service;

import com.packflow.dao.ProductDAO;
import com.packflow.dao.impl.ProductDAOImpl;
import com.packflow.model.Product;

import java.util.List;
import java.util.Optional;

/**
 * Service managing client product catalog.
 */
public class ProductService {

    private final ProductDAO productDAO;

    public ProductService() {
        this.productDAO = new ProductDAOImpl();
    }

    public ProductService(ProductDAO productDAO) {
        this.productDAO = productDAO;
    }

    public Optional<Product> getProductById(int productId) {
        return productDAO.findById(productId);
    }

    public Optional<Product> getProductByCode(String code) {
        return productDAO.findByCode(code);
    }

    public List<Product> getAllProducts() {
        return productDAO.findAll();
    }

    public List<Product> getProductsByCustomerId(int customerId) {
        return productDAO.findByCustomerId(customerId);
    }

    public List<Product> getPaginatedProducts(int page, int pageSize, String search, Integer customerId) {
        int offset = Math.max(0, (page - 1) * pageSize);
        return productDAO.findPaginated(offset, pageSize, search, customerId);
    }

    public int getTotalProductCount(String search, Integer customerId) {
        return productDAO.countTotal(search, customerId);
    }

    public boolean saveProduct(Product product) {
        if (product.getProductName() == null || product.getProductName().trim().isEmpty()) {
            throw new IllegalArgumentException("Product name is required.");
        }
        if (product.getProductCode() == null || product.getProductCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Product code is required.");
        }

        Optional<Product> existing = productDAO.findByCode(product.getProductCode().trim());
        if (existing.isPresent() && existing.get().getProductId() != product.getProductId()) {
            throw new IllegalArgumentException("Product code already exists: " + product.getProductCode());
        }

        if (product.getProductId() > 0) {
            return productDAO.update(product);
        } else {
            return productDAO.create(product);
        }
    }

    public boolean deleteProduct(int productId) {
        return productDAO.delete(productId);
    }
}
