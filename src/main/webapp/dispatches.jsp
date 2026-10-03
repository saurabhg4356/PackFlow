<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="pageTitle" value="Dispatches & Shipments" />
<c:set var="pageSubtitle" value="Couriers, tracking numbers, logistics carrier assignment, and delivery verification" />
<c:set var="activeNav" value="dispatches" />
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
                        <li class="breadcrumb-item active">Dispatches</li>
                    </ol>
                </nav>
                <h4 class="fw-bold mb-0">Dispatches &amp; Logistics (${totalDispatches})</h4>
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
                <form action="${pageContext.request.contextPath}/dispatches" method="GET" class="row g-2 align-items-center">
                    <div class="col-md-4">
                        <select name="status" class="form-select form-select-sm" onchange="this.form.submit()">
                            <option value="">All Shipping Statuses</option>
                            <option value="IN_TRANSIT" ${selectedStatus == 'IN_TRANSIT' ? 'selected' : ''}>In Transit</option>
                            <option value="OUT_FOR_DELIVERY" ${selectedStatus == 'OUT_FOR_DELIVERY' ? 'selected' : ''}>Out for Delivery</option>
                            <option value="DELIVERED" ${selectedStatus == 'DELIVERED' ? 'selected' : ''}>Delivered</option>
                            <option value="RETURNED" ${selectedStatus == 'RETURNED' ? 'selected' : ''}>Returned</option>
                        </select>
                    </div>
                    <div class="col-md-2">
                        <a href="${pageContext.request.contextPath}/dispatches" class="btn btn-sm btn-outline-secondary">Reset</a>
                    </div>
                </form>
            </div>
        </div>

        <!-- Dispatches Table -->
        <div class="card card-table">
            <div class="table-responsive">
                <table class="table table-hover mb-0">
                    <thead>
                        <tr>
                            <th>Order #</th>
                            <th>Customer</th>
                            <th>Carrier / Courier</th>
                            <th>Tracking #</th>
                            <th>Dispatch Date</th>
                            <th>Shipping Status</th>
                            <th>Delivered At</th>
                            <th class="text-end">Action</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${not empty dispatches}">
                                <c:forEach var="d" items="${dispatches}">
                                    <tr>
                                        <td>
                                            <a href="${pageContext.request.contextPath}/orders?action=view&id=${d.orderId}" class="fw-bold text-decoration-none">
                                                ${d.orderNumber}
                                            </a>
                                        </td>
                                        <td>
                                            <div class="fw-semibold">${d.companyName}</div>
                                            <div class="text-muted small">${d.contactPerson}</div>
                                        </td>
                                        <td class="fw-semibold text-dark"><i class="bi bi-truck me-1 text-muted"></i>${d.deliveryPartner}</td>
                                        <td><span class="badge bg-light text-dark border font-monospace">${d.trackingNumber}</span></td>
                                        <td>${d.dispatchDate}</td>
                                        <td><span class="${d.status.badgeClass}">${d.status.displayName}</span></td>
                                        <td class="small text-muted">${not empty d.deliveredAt ? d.deliveredAt : '-'}</td>
                                        <td class="text-end">
                                            <c:if test="${d.status != 'DELIVERED'}">
                                                <form action="${pageContext.request.contextPath}/dispatches" method="POST" class="d-inline" onsubmit="return confirm('Confirm delivery for this shipment?');">
                                                    <input type="hidden" name="action" value="deliver">
                                                    <input type="hidden" name="orderId" value="${d.orderId}">
                                                    <button type="submit" class="btn btn-sm btn-success">
                                                        <i class="bi bi-check2"></i> Mark Delivered
                                                    </button>
                                                </form>
                                            </c:if>
                                            <a href="${pageContext.request.contextPath}/orders?action=view&id=${d.orderId}" class="btn btn-sm btn-outline-primary ms-1">
                                                <i class="bi bi-eye"></i> Details
                                            </a>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr>
                                    <td colspan="8" class="empty-state">
                                        <i class="bi bi-truck empty-state-icon"></i>
                                        <div class="empty-state-title">No dispatches found</div>
                                        <div class="empty-state-text">No active shipments matching your selected filter.</div>
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
                                <a class="page-link" href="${pageContext.request.contextPath}/dispatches?page=${currentPage - 1}&status=${selectedStatus}">Previous</a>
                            </li>
                            <c:forEach var="i" begin="1" end="${totalPages}">
                                <li class="page-item ${currentPage == i ? 'active' : ''}">
                                    <a class="page-link" href="${pageContext.request.contextPath}/dispatches?page=${i}&status=${selectedStatus}">${i}</a>
                                </li>
                            </c:forEach>
                            <li class="page-item ${currentPage >= totalPages ? 'disabled' : ''}">
                                <a class="page-link" href="${pageContext.request.contextPath}/dispatches?page=${currentPage + 1}&status=${selectedStatus}">Next</a>
                            </li>
                        </ul>
                    </nav>
                </div>
            </c:if>
        </div>
    </div> <!-- /.content-wrapper -->

    <jsp:include page="/WEB-INF/views/common/footer.jsp" />
