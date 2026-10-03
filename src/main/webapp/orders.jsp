<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<c:set var="pageTitle" value="Packaging Orders" />
<c:set var="pageSubtitle" value="Manage order fulfillment and status workflow" />
<c:set var="activeNav" value="orders" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<jsp:include page="/WEB-INF/views/common/sidebar.jsp" />

<div id="main-content" class="d-flex flex-column min-vh-100">
    <jsp:include page="/WEB-INF/views/common/navbar.jsp" />

    <div class="content-wrapper">
        <!-- Breadcrumbs & Action Button -->
        <div class="d-flex flex-wrap justify-content-between align-items-center mb-4 gap-2">
            <div>
                <nav aria-label="breadcrumb">
                    <ol class="breadcrumb mb-1 small">
                        <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/dashboard">Dashboard</a></li>
                        <li class="breadcrumb-item active">Orders</li>
                    </ol>
                </nav>
                <h4 class="fw-bold mb-0">Packaging Orders (${totalOrders})</h4>
            </div>
            <div>
                <a href="${pageContext.request.contextPath}/orders?action=new" class="btn btn-primary shadow-sm">
                    <i class="bi bi-plus-lg me-1"></i>Create New Order
                </a>
            </div>
        </div>

        <!-- Filter Card -->
        <div class="card card-table mb-4">
            <div class="p-3">
                <form action="${pageContext.request.contextPath}/orders" method="GET" class="row g-2 align-items-center">
                    <div class="col-md-3">
                        <div class="input-group input-group-sm">
                            <span class="input-group-text bg-light"><i class="bi bi-search"></i></span>
                            <input type="text" name="search" class="form-control form-control-sm" placeholder="Search order #, customer..." value="${search}">
                        </div>
                    </div>

                    <div class="col-md-2">
                        <select name="status" class="form-select form-select-sm">
                            <option value="">All Statuses</option>
                            <option value="PENDING" ${selectedStatus == 'PENDING' ? 'selected' : ''}>Pending Review</option>
                            <option value="APPROVED" ${selectedStatus == 'APPROVED' ? 'selected' : ''}>Approved &amp; Reserved</option>
                            <option value="PROCESSING" ${selectedStatus == 'PROCESSING' ? 'selected' : ''}>Processing</option>
                            <option value="QUALITY_CHECK" ${selectedStatus == 'QUALITY_CHECK' ? 'selected' : ''}>Quality Check</option>
                            <option value="COMPLETED" ${selectedStatus == 'COMPLETED' ? 'selected' : ''}>Completed</option>
                            <option value="DISPATCHED" ${selectedStatus == 'DISPATCHED' ? 'selected' : ''}>Dispatched</option>
                            <option value="DELIVERED" ${selectedStatus == 'DELIVERED' ? 'selected' : ''}>Delivered</option>
                            <option value="CANCELLED" ${selectedStatus == 'CANCELLED' ? 'selected' : ''}>Cancelled</option>
                        </select>
                    </div>

                    <c:if test="${sessionScope.userRole != 'CUSTOMER'}">
                        <div class="col-md-3">
                            <select name="customerId" class="form-select form-select-sm">
                                <option value="">All Customers</option>
                                <c:forEach var="c" items="${customersList}">
                                    <option value="${c.customerId}" ${selectedCustomer == c.customerId ? 'selected' : ''}>
                                        ${c.companyName}
                                    </option>
                                </c:forEach>
                            </select>
                        </div>
                    </c:if>

                    <div class="col-md-2">
                        <input type="date" name="fromDate" class="form-control form-control-sm" value="${fromDate}" title="From Date">
                    </div>
                    <div class="col-md-2 d-flex gap-2">
                        <input type="date" name="toDate" class="form-control form-control-sm" value="${toDate}" title="To Date">
                        <button type="submit" class="btn btn-sm btn-dark">Filter</button>
                        <a href="${pageContext.request.contextPath}/orders" class="btn btn-sm btn-outline-secondary" title="Clear Filters">Reset</a>
                    </div>
                </form>
            </div>
        </div>

        <!-- Orders Table -->
        <div class="card card-table">
            <div class="table-responsive">
                <table class="table table-hover mb-0">
                    <thead>
                        <tr>
                            <th>Order #</th>
                            <th>Customer</th>
                            <th>Order Date</th>
                            <th>Total Items</th>
                            <th>Total Cost</th>
                            <th>Status</th>
                            <th class="text-end">Action</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${not empty orders}">
                                <c:forEach var="o" items="${orders}">
                                    <tr>
                                        <td>
                                            <a href="${pageContext.request.contextPath}/orders?action=view&id=${o.orderId}" class="fw-bold text-decoration-none">
                                                ${o.orderNumber}
                                            </a>
                                        </td>
                                        <td>
                                            <div class="fw-semibold">${o.companyName}</div>
                                            <div class="text-muted small">${o.contactPerson}</div>
                                        </td>
                                        <td>${o.orderDate}</td>
                                        <td><span class="badge bg-light text-dark border">${o.totalQuantity} pcs</span></td>
                                        <td class="fw-bold"><fmt:formatNumber value="${o.totalCost}" type="currency" currencySymbol="$"/></td>
                                        <td><span class="${o.status.badgeClass}">${o.status.displayName}</span></td>
                                        <td class="text-end">
                                            <a href="${pageContext.request.contextPath}/orders?action=view&id=${o.orderId}" class="btn btn-sm btn-outline-primary">
                                                <i class="bi bi-eye me-1"></i>View
                                            </a>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr>
                                    <td colspan="7" class="empty-state">
                                        <i class="bi bi-folder-x empty-state-icon"></i>
                                        <div class="empty-state-title">No orders found</div>
                                        <div class="empty-state-text">No packaging orders match your search and filter criteria.</div>
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
                                <a class="page-link" href="${pageContext.request.contextPath}/orders?page=${currentPage - 1}&search=${search}&status=${selectedStatus}&customerId=${selectedCustomer}&fromDate=${fromDate}&toDate=${toDate}">Previous</a>
                            </li>
                            <c:forEach var="i" begin="1" end="${totalPages}">
                                <li class="page-item ${currentPage == i ? 'active' : ''}">
                                    <a class="page-link" href="${pageContext.request.contextPath}/orders?page=${i}&search=${search}&status=${selectedStatus}&customerId=${selectedCustomer}&fromDate=${fromDate}&toDate=${toDate}">${i}</a>
                                </li>
                            </c:forEach>
                            <li class="page-item ${currentPage >= totalPages ? 'disabled' : ''}">
                                <a class="page-link" href="${pageContext.request.contextPath}/orders?page=${currentPage + 1}&search=${search}&status=${selectedStatus}&customerId=${selectedCustomer}&fromDate=${fromDate}&toDate=${toDate}">Next</a>
                            </li>
                        </ul>
                    </nav>
                </div>
            </c:if>
        </div>
    </div> <!-- /.content-wrapper -->

    <jsp:include page="/WEB-INF/views/common/footer.jsp" />
