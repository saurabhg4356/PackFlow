package com.packflow.dao;

import com.packflow.model.PackagingTask;
import com.packflow.model.TaskStatus;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object Interface for Packaging Tasks.
 */
public interface PackagingTaskDAO {
    Optional<PackagingTask> findById(int taskId);
    Optional<PackagingTask> findByOrderId(int orderId);
    List<PackagingTask> findAll();
    List<PackagingTask> findPaginated(int offset, int limit, TaskStatus status, Integer assignedTo);
    int countTotal(TaskStatus status, Integer assignedTo);
    boolean create(PackagingTask task);
    boolean create(PackagingTask task, Connection conn);
    boolean update(PackagingTask task);
    boolean updateProgress(int taskId, int completedQuantity, TaskStatus status);
    boolean delete(int taskId);
}
