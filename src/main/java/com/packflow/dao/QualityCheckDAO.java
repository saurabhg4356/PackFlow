package com.packflow.dao;

import com.packflow.model.QualityCheck;
import com.packflow.model.QualityStatus;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object Interface for Quality Checks.
 */
public interface QualityCheckDAO {
    Optional<QualityCheck> findById(int qualityCheckId);
    Optional<QualityCheck> findByOrderId(int orderId);
    List<QualityCheck> findAll();
    List<QualityCheck> findPaginated(int offset, int limit, QualityStatus status);
    int countTotal(QualityStatus status);
    boolean create(QualityCheck qc);
    boolean create(QualityCheck qc, Connection conn);
    boolean update(QualityCheck qc);
    boolean delete(int qualityCheckId);
}
