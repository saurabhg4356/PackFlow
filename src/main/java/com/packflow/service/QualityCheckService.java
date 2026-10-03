package com.packflow.service;

import com.packflow.dao.QualityCheckDAO;
import com.packflow.dao.impl.QualityCheckDAOImpl;
import com.packflow.model.QualityCheck;
import com.packflow.model.QualityStatus;

import java.util.List;
import java.util.Optional;

/**
 * Service managing quality assurance inspections.
 */
public class QualityCheckService {

    private final QualityCheckDAO qualityCheckDAO;

    public QualityCheckService() {
        this.qualityCheckDAO = new QualityCheckDAOImpl();
    }

    public QualityCheckService(QualityCheckDAO qualityCheckDAO) {
        this.qualityCheckDAO = qualityCheckDAO;
    }

    public Optional<QualityCheck> getQualityCheckById(int id) {
        return qualityCheckDAO.findById(id);
    }

    public Optional<QualityCheck> getQualityCheckByOrderId(int orderId) {
        return qualityCheckDAO.findByOrderId(orderId);
    }

    public List<QualityCheck> getAllQualityChecks() {
        return qualityCheckDAO.findAll();
    }

    public List<QualityCheck> getPaginatedQualityChecks(int page, int pageSize, QualityStatus status) {
        int offset = Math.max(0, (page - 1) * pageSize);
        return qualityCheckDAO.findPaginated(offset, pageSize, status);
    }

    public int getTotalCount(QualityStatus status) {
        return qualityCheckDAO.countTotal(status);
    }
}
