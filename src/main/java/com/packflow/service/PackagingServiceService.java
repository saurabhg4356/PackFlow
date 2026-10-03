package com.packflow.service;

import com.packflow.dao.PackagingServiceDAO;
import com.packflow.dao.impl.PackagingServiceDAOImpl;
import com.packflow.model.PackagingServiceItem;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Service managing packaging service catalog and pricing models.
 */
public class PackagingServiceService {

    private final PackagingServiceDAO serviceDAO;

    public PackagingServiceService() {
        this.serviceDAO = new PackagingServiceDAOImpl();
    }

    public PackagingServiceService(PackagingServiceDAO serviceDAO) {
        this.serviceDAO = serviceDAO;
    }

    public Optional<PackagingServiceItem> getServiceById(int serviceId) {
        return serviceDAO.findById(serviceId);
    }

    public List<PackagingServiceItem> getAllServices() {
        return serviceDAO.findAll();
    }

    public List<PackagingServiceItem> getActiveServices() {
        return serviceDAO.findActiveServices();
    }

    public boolean saveService(PackagingServiceItem service) {
        if (service.getServiceName() == null || service.getServiceName().trim().isEmpty()) {
            throw new IllegalArgumentException("Service name is required.");
        }
        if (service.getBasePrice() == null || service.getBasePrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Base price cannot be negative.");
        }

        if (service.getServiceId() > 0) {
            return serviceDAO.update(service);
        } else {
            return serviceDAO.create(service);
        }
    }

    public boolean deleteService(int serviceId) {
        return serviceDAO.delete(serviceId);
    }
}
