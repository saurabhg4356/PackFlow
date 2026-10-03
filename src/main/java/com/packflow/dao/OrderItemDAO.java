package com.packflow.dao;

import com.packflow.model.OrderItem;

import java.sql.Connection;
import java.util.List;

/**
 * Data Access Object Interface for Order Line Items.
 */
public interface OrderItemDAO {
    List<OrderItem> findByOrderId(int orderId);
    List<OrderItem> findByOrderId(int orderId, Connection conn);
    boolean create(OrderItem item);
    boolean create(OrderItem item, Connection conn);
    boolean createBatch(List<OrderItem> items, int orderId, Connection conn);
    boolean deleteByOrderId(int orderId);
    boolean deleteByOrderId(int orderId, Connection conn);
}
