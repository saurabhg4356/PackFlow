package com.packflow.service;

import com.packflow.dao.PackagingTaskDAO;
import com.packflow.dao.impl.PackagingTaskDAOImpl;
import com.packflow.model.PackagingTask;
import com.packflow.model.TaskStatus;

import java.util.List;
import java.util.Optional;

/**
 * Service managing packaging line tasks and progress.
 */
public class PackagingTaskService {

    private final PackagingTaskDAO taskDAO;

    public PackagingTaskService() {
        this.taskDAO = new PackagingTaskDAOImpl();
    }

    public PackagingTaskService(PackagingTaskDAO taskDAO) {
        this.taskDAO = taskDAO;
    }

    public Optional<PackagingTask> getTaskById(int taskId) {
        return taskDAO.findById(taskId);
    }

    public Optional<PackagingTask> getTaskByOrderId(int orderId) {
        return taskDAO.findByOrderId(orderId);
    }

    public List<PackagingTask> getAllTasks() {
        return taskDAO.findAll();
    }

    public List<PackagingTask> getPaginatedTasks(int page, int pageSize, TaskStatus status, Integer assignedTo) {
        int offset = Math.max(0, (page - 1) * pageSize);
        return taskDAO.findPaginated(offset, pageSize, status, assignedTo);
    }

    public int getTotalTaskCount(TaskStatus status, Integer assignedTo) {
        return taskDAO.countTotal(status, assignedTo);
    }

    public boolean updateTaskProgress(int taskId, int completedQty, TaskStatus status) {
        return taskDAO.updateProgress(taskId, completedQty, status);
    }

    public boolean assignTask(int taskId, int assignedToUserId) {
        Optional<PackagingTask> opt = taskDAO.findById(taskId);
        if (opt.isPresent()) {
            PackagingTask task = opt.get();
            task.setAssignedTo(assignedToUserId);
            task.setStatus(TaskStatus.IN_PROGRESS);
            return taskDAO.update(task);
        }
        return false;
    }
}
