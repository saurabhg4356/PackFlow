package com.packflow.service;

import com.packflow.dao.CustomerDAO;
import com.packflow.dao.impl.CustomerDAOImpl;
import com.packflow.model.Customer;

import java.util.List;
import java.util.Optional;

/**
 * Service managing client customer accounts and profiles.
 */
public class CustomerService {

    private final CustomerDAO customerDAO;

    public CustomerService() {
        this.customerDAO = new CustomerDAOImpl();
    }

    public CustomerService(CustomerDAO customerDAO) {
        this.customerDAO = customerDAO;
    }

    public Optional<Customer> getCustomerById(int customerId) {
        return customerDAO.findById(customerId);
    }

    public Optional<Customer> getCustomerByUserId(int userId) {
        return customerDAO.findByUserId(userId);
    }

    public List<Customer> getAllCustomers() {
        return customerDAO.findAll();
    }

    public List<Customer> getPaginatedCustomers(int page, int pageSize, String search) {
        int offset = Math.max(0, (page - 1) * pageSize);
        return customerDAO.findPaginated(offset, pageSize, search);
    }

    public int getTotalCustomerCount(String search) {
        return customerDAO.countTotal(search);
    }

    public boolean saveCustomer(Customer customer) {
        if (customer.getCompanyName() == null || customer.getCompanyName().trim().isEmpty()) {
            throw new IllegalArgumentException("Company name is required.");
        }
        if (customer.getEmail() == null || customer.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required.");
        }

        if (customer.getCustomerId() > 0) {
            return customerDAO.update(customer);
        } else {
            return customerDAO.create(customer);
        }
    }

    public boolean deleteCustomer(int customerId) {
        return customerDAO.delete(customerId);
    }
}
