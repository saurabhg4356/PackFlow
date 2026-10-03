package com.packflow.controller;

import com.packflow.model.PackagingTask;
import com.packflow.model.Role;
import com.packflow.model.TaskStatus;
import com.packflow.model.User;
import com.packflow.service.OrderService;
import com.packflow.service.PackagingTaskService;
import com.packflow.service.UserService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

/**
 * Controller managing packaging line operational tasks and assembly progress.
 */
@WebServlet(name = "PackagingTaskServlet", urlPatterns = {"/tasks"})
public class PackagingTaskServlet extends HttpServlet {

    private PackagingTaskService taskService;
    private UserService userService;
    private OrderService orderService;

    @Override
    public void init() {
        this.taskService = new PackagingTaskService();
        this.userService = new UserService();
        this.orderService = new OrderService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User currentUser = (User) session.getAttribute("currentUser");

        if (currentUser.getRole() == Role.CUSTOMER) {
            resp.sendRedirect(req.getContextPath() + "/unauthorized");
            return;
        }

        int page = 1;
        int pageSize = 10;
        try {
            String p = req.getParameter("page");
            if (p != null) page = Math.max(1, Integer.parseInt(p));
        } catch (NumberFormatException ignored) {
        }

        String statusStr = req.getParameter("status");
        TaskStatus status = (statusStr != null && !statusStr.isEmpty()) ? TaskStatus.fromString(statusStr) : null;

        List<PackagingTask> tasks = taskService.getPaginatedTasks(page, pageSize, status, null);
        int totalTasks = taskService.getTotalTaskCount(status, null);
        int totalPages = (int) Math.ceil((double) totalTasks / pageSize);

        req.setAttribute("tasks", tasks);
        req.setAttribute("currentPage", page);
        req.setAttribute("totalPages", totalPages);
        req.setAttribute("totalTasks", totalTasks);
        req.setAttribute("selectedStatus", status != null ? status.name() : "");
        req.setAttribute("staffUsers", userService.getAllUsers());

        String msg = req.getParameter("msg");
        if ("progress_updated".equals(msg)) req.setAttribute("successMessage", "Packaging progress updated.");
        else if ("assigned".equals(msg)) req.setAttribute("successMessage", "Task assigned successfully.");

        req.getRequestDispatcher("/tasks.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        User currentUser = (User) session.getAttribute("currentUser");

        if (currentUser.getRole() == Role.CUSTOMER) {
            resp.sendRedirect(req.getContextPath() + "/unauthorized");
            return;
        }

        String action = req.getParameter("action");
        if ("updateProgress".equalsIgnoreCase(action)) {
            try {
                int taskId = Integer.parseInt(req.getParameter("taskId"));
                int completedQty = Integer.parseInt(req.getParameter("completedQuantity"));
                String statusStr = req.getParameter("status");
                TaskStatus status = TaskStatus.fromString(statusStr);

                taskService.updateTaskProgress(taskId, completedQty, status);

                // If task completed, automatically advance order to QUALITY_CHECK
                if (status == TaskStatus.COMPLETED) {
                    taskService.getTaskById(taskId).ifPresent(t -> {
                        try {
                            orderService.sendToQualityCheck(t.getOrderId());
                        } catch (Exception ignored) {
                        }
                    });
                }

                resp.sendRedirect(req.getContextPath() + "/tasks?msg=progress_updated");
            } catch (Exception e) {
                resp.sendRedirect(req.getContextPath() + "/tasks?error=" + java.net.URLEncoder.encode(e.getMessage(), "UTF-8"));
            }
        } else if ("assign".equalsIgnoreCase(action)) {
            try {
                int taskId = Integer.parseInt(req.getParameter("taskId"));
                int assignedTo = Integer.parseInt(req.getParameter("assignedTo"));
                taskService.assignTask(taskId, assignedTo);
                resp.sendRedirect(req.getContextPath() + "/tasks?msg=assigned");
            } catch (Exception e) {
                resp.sendRedirect(req.getContextPath() + "/tasks?error=" + java.net.URLEncoder.encode(e.getMessage(), "UTF-8"));
            }
        } else {
            resp.sendRedirect(req.getContextPath() + "/tasks");
        }
    }
}
