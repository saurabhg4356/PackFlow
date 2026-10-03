package com.packflow.dao;

import com.packflow.model.Order;
import com.packflow.model.OrderStatus;

import java.sql.Connection;
import java.sql.Date;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object Interface for Order lifecycle management.
 * Includes overloaded methods accepting an external Connection for JDBC transactions.
 */
public interface OrderDAO {
    Optional<Order> findById(int orderId);
    Optional<Order> findByOrderNumber(String orderNumber);
    List<Order> findAll();
    List<Order> findByCustomerId(int customerId);
    List<Order> findPaginated(int offset, int limit, String search, Integer customerId, OrderStatus status, Date fromDate, Date toDate);
    int countTotal(String search, Integer customerId, OrderStatus status, Date fromDate, Date toDate);

    int create(Order order);
    int create(Order order, Connection conn);

    boolean update(Order order);
    boolean updateStatus(int orderId, OrderStatus status);
    boolean updateStatus(int orderId, OrderStatus status, Connection conn);

    String generateNextOrderNumber();
    String generateNextOrderNumber(Connection conn);

    boolean delete(int orderId);
}
