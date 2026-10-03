package com.packflow.service;

import com.packflow.dao.DispatchDAO;
import com.packflow.dao.impl.DispatchDAOImpl;
import com.packflow.model.DispatchRecord;
import com.packflow.model.DispatchStatus;

import java.util.List;
import java.util.Optional;

/**
 * Service managing shipping, couriers, and tracking numbers.
 */
public class DispatchService {

    private final DispatchDAO dispatchDAO;

    public DispatchService() {
        this.dispatchDAO = new DispatchDAOImpl();
    }

    public DispatchService(DispatchDAO dispatchDAO) {
        this.dispatchDAO = dispatchDAO;
    }

    public Optional<DispatchRecord> getDispatchById(int id) {
        return dispatchDAO.findById(id);
    }

    public Optional<DispatchRecord> getDispatchByOrderId(int orderId) {
        return dispatchDAO.findByOrderId(orderId);
    }

    public Optional<DispatchRecord> getDispatchByTracking(String tracking) {
        return dispatchDAO.findByTrackingNumber(tracking);
    }

    public List<DispatchRecord> getAllDispatches() {
        return dispatchDAO.findAll();
    }

    public List<DispatchRecord> getPaginatedDispatches(int page, int pageSize, DispatchStatus status) {
        int offset = Math.max(0, (page - 1) * pageSize);
        return dispatchDAO.findPaginated(offset, pageSize, status);
    }

    public int getTotalCount(DispatchStatus status) {
        return dispatchDAO.countTotal(status);
    }

    public boolean updateStatus(int dispatchId, DispatchStatus status) {
        return dispatchDAO.updateStatus(dispatchId, status);
    }
}
