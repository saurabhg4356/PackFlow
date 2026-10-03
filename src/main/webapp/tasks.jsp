<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="pageTitle" value="Packaging Floor Tasks" />
<c:set var="pageSubtitle" value="Production floor assembly, packaging jobs, and line staff allocation" />
<c:set var="activeNav" value="tasks" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<jsp:include page="/WEB-INF/views/common/sidebar.jsp" />

<div id="main-content" class="d-flex flex-column min-vh-100">
    <jsp:include page="/WEB-INF/views/common/navbar.jsp" />

    <div class="content-wrapper">
        <div class="d-flex flex-wrap justify-content-between align-items-center mb-4 gap-2">
            <div>
                <nav aria-label="breadcrumb">
                    <ol class="breadcrumb mb-1 small">
                        <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/dashboard">Dashboard</a></li>
                        <li class="breadcrumb-item active">Packaging Tasks</li>
                    </ol>
                </nav>
                <h4 class="fw-bold mb-0">Packaging Floor Tasks (${totalTasks})</h4>
            </div>
        </div>

        <c:if test="${not empty successMessage}">
            <div class="alert alert-success alert-dismissible fade show mb-4" role="alert">
                <i class="bi bi-check-circle-fill me-2"></i>${successMessage}
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
        </c:if>

        <c:if test="${not empty errorMessage}">
            <div class="alert alert-danger alert-dismissible fade show mb-4" role="alert">
                <i class="bi bi-exclamation-triangle-fill me-2"></i>${errorMessage}
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
        </c:if>

        <!-- Filter Card -->
        <div class="card card-table mb-4">
            <div class="p-3">
                <form action="${pageContext.request.contextPath}/tasks" method="GET" class="row g-2 align-items-center">
                    <div class="col-md-4">
                        <select name="status" class="form-select form-select-sm" onchange="this.form.submit()">
                            <option value="">All Task Statuses</option>
                            <option value="PENDING" ${selectedStatus == 'PENDING' ? 'selected' : ''}>Pending Assignment</option>
                            <option value="IN_PROGRESS" ${selectedStatus == 'IN_PROGRESS' ? 'selected' : ''}>In Progress</option>
                            <option value="COMPLETED" ${selectedStatus == 'COMPLETED' ? 'selected' : ''}>Task Completed</option>
                        </select>
                    </div>
                    <div class="col-md-2">
                        <a href="${pageContext.request.contextPath}/tasks" class="btn btn-sm btn-outline-secondary">Reset Filter</a>
                    </div>
                </form>
            </div>
        </div>

        <!-- Tasks Table -->
        <div class="card card-table">
            <div class="table-responsive">
                <table class="table table-hover mb-0">
                    <thead>
                        <tr>
                            <th>Task ID</th>
                            <th>Order #</th>
                            <th>Customer</th>
                            <th>Progress</th>
                            <th>Assigned Staff</th>
                            <th>Status</th>
                            <th>Started At</th>
                            <th class="text-end">Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${not empty tasks}">
                                <c:forEach var="t" items="${tasks}">
                                    <tr>
                                        <td><span class="badge bg-light text-dark border">#TASK-${t.taskId}</span></td>
                                        <td>
                                            <a href="${pageContext.request.contextPath}/orders?action=view&id=${t.orderId}" class="fw-bold text-decoration-none">
                                                ${t.orderNumber}
                                            </a>
                                        </td>
                                        <td>${t.companyName}</td>
                                        <td style="width: 220px;">
                                            <div class="d-flex justify-content-between small text-muted mb-1">
                                                <span>${t.completedQuantity} / ${t.quantity} pcs</span>
                                                <span class="fw-bold">${t.progressPercentage}%</span>
                                            </div>
                                            <div class="progress" style="height: 6px;">
                                                <div class="progress-bar ${t.status == 'COMPLETED' ? 'bg-success' : 'bg-primary'}"
                                                     style="width: ${t.progressPercentage}%;"></div>
                                            </div>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${not empty t.assignedToName}">
                                                    <span class="badge bg-info-subtle text-info border">${t.assignedToName}</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge bg-warning-subtle text-warning border text-dark">Unassigned</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td><span class="${t.status.badgeClass}">${t.status.displayName}</span></td>
                                        <td class="small text-muted">${t.startedAt}</td>
                                        <td class="text-end">
                                            <button type="button" class="btn btn-sm btn-outline-primary me-1"
                                                    onclick="openProgressModal('${t.taskId}', '${t.orderNumber}', '${t.completedQuantity}', '${t.quantity}', '${t.status}')">
                                                <i class="bi bi-pencil-square"></i> Progress
                                            </button>
                                            <button type="button" class="btn btn-sm btn-outline-secondary"
                                                    onclick="openAssignModal('${t.taskId}', '${t.orderNumber}', '${t.assignedTo}')">
                                                <i class="bi bi-person-plus"></i> Assign
                                            </button>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr>
                                    <td colspan="8" class="empty-state">
                                        <i class="bi bi-clipboard-x empty-state-icon"></i>
                                        <div class="empty-state-title">No packaging tasks found</div>
                                        <div class="empty-state-text">No active operational tasks for the selected filter.</div>
                                    </td>
                                </tr>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>
            </div>

            <!-- Server-Side Pagination -->
            <c:if test="${totalPages > 1}">
                <div class="card-footer bg-white d-flex justify-content-between align-items-center py-3">
                    <span class="text-muted small">Showing page <strong>${currentPage}</strong> of <strong>${totalPages}</strong></span>
                    <nav>
                        <ul class="pagination pagination-sm mb-0">
                            <li class="page-item ${currentPage <= 1 ? 'disabled' : ''}">
                                <a class="page-link" href="${pageContext.request.contextPath}/tasks?page=${currentPage - 1}&status=${selectedStatus}">Previous</a>
                            </li>
                            <c:forEach var="i" begin="1" end="${totalPages}">
                                <li class="page-item ${currentPage == i ? 'active' : ''}">
                                    <a class="page-link" href="${pageContext.request.contextPath}/tasks?page=${i}&status=${selectedStatus}">${i}</a>
                                </li>
                            </c:forEach>
                            <li class="page-item ${currentPage >= totalPages ? 'disabled' : ''}">
                                <a class="page-link" href="${pageContext.request.contextPath}/tasks?page=${currentPage + 1}&status=${selectedStatus}">Next</a>
                            </li>
                        </ul>
                    </nav>
                </div>
            </c:if>
        </div>
    </div> <!-- /.content-wrapper -->

    <jsp:include page="/WEB-INF/views/common/footer.jsp" />

<!-- Progress Modal -->
<div class="modal fade" id="progressModal" tabindex="-1">
    <div class="modal-dialog">
        <form action="${pageContext.request.contextPath}/tasks" method="POST" class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title fw-bold">Update Task Progress</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body">
                <input type="hidden" name="action" value="updateProgress">
                <input type="hidden" name="taskId" id="progressTaskId" value="">
                <p>Order: <strong id="progressOrderNumber" class="text-dark"></strong></p>
                <div class="mb-3">
                    <label class="form-label small fw-semibold">Completed Units / Pieces</label>
                    <input type="number" name="completedQuantity" id="progressCompletedQty" class="form-control" min="0" required>
                    <div class="form-text">Total required: <span id="progressTotalQty" class="fw-bold"></span> pcs</div>
                </div>
                <div class="mb-3">
                    <label class="form-label small fw-semibold">Task Status</label>
                    <select name="status" id="progressStatus" class="form-select">
                        <option value="IN_PROGRESS">IN_PROGRESS</option>
                        <option value="COMPLETED">COMPLETED (Advance order to Quality Check)</option>
                    </select>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
                <button type="submit" class="btn btn-primary">Save Progress</button>
            </div>
        </form>
    </div>
</div>

<!-- Assign Modal -->
<div class="modal fade" id="assignModal" tabindex="-1">
    <div class="modal-dialog">
        <form action="${pageContext.request.contextPath}/tasks" method="POST" class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title fw-bold">Assign Task to Staff</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body">
                <input type="hidden" name="action" value="assign">
                <input type="hidden" name="taskId" id="assignTaskId" value="">
                <p>Order: <strong id="assignOrderNumber" class="text-dark"></strong></p>
                <div class="mb-3">
                    <label class="form-label small fw-semibold">Assignee</label>
                    <select name="assignedTo" id="assignUserId" class="form-select" required>
                        <c:forEach var="u" items="${staffUsers}">
                            <option value="${u.userId}">${u.name} (${u.role})</option>
                        </c:forEach>
                    </select>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
                <button type="submit" class="btn btn-primary">Assign Task</button>
            </div>
        </form>
    </div>
</div>

<script>
    const progressModal = new bootstrap.Modal(document.getElementById('progressModal'));
    const assignModal = new bootstrap.Modal(document.getElementById('assignModal'));

    function openProgressModal(id, orderNum, completed, total, status) {
        document.getElementById('progressTaskId').value = id;
        document.getElementById('progressOrderNumber').innerText = orderNum;
        document.getElementById('progressCompletedQty').value = completed;
        document.getElementById('progressCompletedQty').max = total;
        document.getElementById('progressTotalQty').innerText = total;
        document.getElementById('progressStatus').value = status;
        progressModal.show();
    }

    function openAssignModal(id, orderNum, currentAssigned) {
        document.getElementById('assignTaskId').value = id;
        document.getElementById('assignOrderNumber').innerText = orderNum;
        if (currentAssigned) {
            document.getElementById('assignUserId').value = currentAssigned;
        }
        assignModal.show();
    }
</script>
