package com.packflow.dao;

import com.packflow.model.PackagingServiceItem;

import java.util.List;
import java.util.Optional;

/**
 * Data Access Object Interface for Packaging Services.
 */
public interface PackagingServiceDAO {
    Optional<PackagingServiceItem> findById(int serviceId);
    List<PackagingServiceItem> findAll();
    List<PackagingServiceItem> findActiveServices();
    boolean create(PackagingServiceItem service);
    boolean update(PackagingServiceItem service);
    boolean delete(int serviceId);
}
