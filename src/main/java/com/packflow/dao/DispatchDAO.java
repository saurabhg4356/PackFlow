package com.packflow.dao;

import com.packflow.model.DispatchRecord;
import com.packflow.model.DispatchStatus;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object Interface for Dispatches and Shipments.
 */
public interface DispatchDAO {
    Optional<DispatchRecord> findById(int dispatchId);
    Optional<DispatchRecord> findByOrderId(int orderId);
    Optional<DispatchRecord> findByTrackingNumber(String trackingNumber);
    List<DispatchRecord> findAll();
    List<DispatchRecord> findPaginated(int offset, int limit, DispatchStatus status);
    int countTotal(DispatchStatus status);
    boolean create(DispatchRecord dispatch);
    boolean create(DispatchRecord dispatch, Connection conn);
    boolean update(DispatchRecord dispatch);
    boolean updateStatus(int dispatchId, DispatchStatus status);
    boolean delete(int dispatchId);
}
