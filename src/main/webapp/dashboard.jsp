<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<c:set var="pageTitle" value="Operational Dashboard" />
<c:set var="pageSubtitle" value="Live database metrics &amp; operational status" />
<c:set var="activeNav" value="dashboard" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<jsp:include page="/WEB-INF/views/common/sidebar.jsp" />

<div id="main-content" class="d-flex flex-column min-vh-100">
    <jsp:include page="/WEB-INF/views/common/navbar.jsp" />

    <div class="content-wrapper">
        <!-- Top Toolbar: Date Filter and Live Refresh -->
        <div class="d-flex flex-wrap justify-content-between align-items-center mb-4 gap-3">
            <div>
                <h4 class="fw-bold mb-1">
                    <c:choose>
                        <c:when test="${sessionScope.userRole == 'ADMIN'}">Executive Dashboard</c:when>
                        <c:when test="${sessionScope.userRole == 'MANAGER'}">Floor Operations Dashboard</c:when>
                        <c:otherwise>Customer Portal Overview</c:otherwise>
                    </c:choose>
                </h4>
                <div class="text-muted small">
                    <i class="bi bi-clock-history me-1"></i>Last Updated from MySQL:
                    <span class="fw-semibold text-dark">${stats.lastUpdated}</span>
                </div>
            </div>

            <!-- Date Filter & Actions -->
            <form action="${pageContext.request.contextPath}/dashboard" method="GET" class="d-flex flex-wrap align-items-center gap-2">
                <div class="input-group input-group-sm" style="width: auto;">
                    <span class="input-group-text bg-white"><i class="bi bi-calendar3"></i></span>
                    <select name="dateFilter" class="form-select form-select-sm" onchange="toggleCustomDates(this.value); this.form.submit();">
                        <option value="all" ${selectedFilter == 'all' ? 'selected' : ''}>All Time</option>
                        <option value="today" ${selectedFilter == 'today' ? 'selected' : ''}>Today</option>
                        <option value="this_week" ${selectedFilter == 'this_week' ? 'selected' : ''}>This Week</option>
                        <option value="this_month" ${selectedFilter == 'this_month' ? 'selected' : ''}>This Month</option>
                        <option value="this_year" ${selectedFilter == 'this_year' ? 'selected' : ''}>This Year</option>
                        <option value="custom" ${selectedFilter == 'custom' ? 'selected' : ''}>Custom Range...</option>
                    </select>
                </div>

                <div id="customDatesDiv" class="d-flex gap-2" style="display: ${selectedFilter == 'custom' ? 'flex' : 'none'} !important;">
                    <input type="date" name="startDate" class="form-control form-control-sm" value="${startDate}">
                    <input type="date" name="endDate" class="form-control form-control-sm" value="${endDate}">
                    <button type="submit" class="btn btn-sm btn-secondary">Apply</button>
                </div>

                <a href="${pageContext.request.contextPath}/dashboard" class="btn btn-sm btn-outline-primary" title="Refresh Live Database Data">
                    <i class="bi bi-arrow-clockwise me-1"></i>Refresh
                </a>
            </form>
        </div>

        <!-- Role View Separation -->
        <c:choose>
            <%-- CUSTOMER VIEW --%>
            <c:when test="${sessionScope.userRole == 'CUSTOMER'}">
                <div class="row g-3 mb-4">
                    <div class="col-md-4">
                        <div class="card-stat">
                            <div class="d-flex align-items-center justify-content-between">
                                <div>
                                    <div class="stat-label">My Total Orders</div>
                                    <div class="stat-val text-primary">${customerOrders != null ? customerOrders.size() : 0}</div>
                                </div>
                                <div class="stat-icon bg-primary-subtle text-primary"><i class="bi bi-cart3"></i></div>
                            </div>
                        </div>
                    </div>
                    <div class="col-md-4">
                        <div class="card-stat">
                            <div class="d-flex align-items-center justify-content-between">
                                <div>
                                    <div class="stat-label">Quick Action</div>
                                    <a href="${pageContext.request.contextPath}/orders?action=new" class="btn btn-sm btn-primary mt-2">
                                        <i class="bi bi-plus-circle me-1"></i>Create New Packaging Request
                                    </a>
                                </div>
                                <div class="stat-icon bg-success-subtle text-success"><i class="bi bi-box-seam"></i></div>
                            </div>
                        </div>
                    </div>
                    <div class="col-md-4">
                        <div class="card-stat">
                            <div class="d-flex align-items-center justify-content-between">
                                <div>
                                    <div class="stat-label">Company Profile</div>
                                    <div class="fw-bold mt-1 text-truncate">${sessionScope.currentUser.companyName}</div>
                                </div>
                                <div class="stat-icon bg-info-subtle text-info"><i class="bi bi-building"></i></div>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Customer Recent Orders Table -->
                <div class="card-table">
                    <div class="card-header d-flex justify-content-between align-items-center">
                        <h6 class="mb-0 fw-bold"><i class="bi bi-clock-history me-2 text-primary"></i>My Recent Packaging Orders</h6>
                        <a href="${pageContext.request.contextPath}/orders" class="btn btn-sm btn-outline-primary">View All</a>
                    </div>
                    <div class="table-responsive">
                        <table class="table table-hover mb-0">
                            <thead>
                                <tr>
                                    <th>Order #</th>
                                    <th>Date</th>
                                    <th>Total Cost</th>
                                    <th>Status</th>
                                    <th>Action</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:choose>
                                    <c:when test="${not empty customerOrders}">
                                        <c:forEach var="o" items="${customerOrders}">
                                            <tr>
                                                <td class="fw-bold text-primary">${o.orderNumber}</td>
                                                <td>${o.orderDate}</td>
                                                <td class="fw-semibold"><fmt:formatNumber value="${o.totalCost}" type="currency" currencySymbol="$"/></td>
                                                <td><span class="${o.status.badgeClass}">${o.status.displayName}</span></td>
                                                <td>
                                                    <a href="${pageContext.request.contextPath}/orders?action=view&id=${o.orderId}" class="btn btn-sm btn-light border">
                                                        <i class="bi bi-eye"></i> Details
                                                    </a>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </c:when>
                                    <c:otherwise>
                                        <tr>
                                            <td colspan="5" class="empty-state">
                                                <i class="bi bi-cart-x empty-state-icon"></i>
                                                <div class="empty-state-title">No orders found</div>
                                                <div class="empty-state-text">You haven't submitted any packaging orders yet.</div>
                                                <a href="${pageContext.request.contextPath}/orders?action=new" class="btn btn-sm btn-primary">Create Your First Order</a>
                                            </td>
                                        </tr>
                                    </c:otherwise>
                                </c:choose>
                            </tbody>
                        </table>
                    </div>
                </div>
            </c:when>

            <%-- ADMIN & MANAGER DASHBOARD --%>
            <c:otherwise>
                <!-- Row 1: KPI Stat Cards Directly Queried from MySQL -->
                <div class="row g-3 mb-4">
                    <div class="col-sm-6 col-xl-3">
                        <div class="card-stat">
                            <div class="d-flex align-items-center justify-content-between">
                                <div>
                                    <div class="stat-label">Total Orders</div>
                                    <div class="stat-val text-primary">${stats.totalOrders}</div>
                                </div>
                                <div class="stat-icon bg-primary-subtle text-primary"><i class="bi bi-cart-check"></i></div>
                            </div>
                        </div>
                    </div>

                    <div class="col-sm-6 col-xl-3">
                        <div class="card-stat">
                            <div class="d-flex align-items-center justify-content-between">
                                <div>
                                    <div class="stat-label">Pending Approval</div>
                                    <div class="stat-val text-warning">${stats.pendingOrders}</div>
                                </div>
                                <div class="stat-icon bg-warning-subtle text-warning"><i class="bi bi-hourglass-split"></i></div>
                            </div>
                        </div>
                    </div>

                    <div class="col-sm-6 col-xl-3">
                        <div class="card-stat">
                            <div class="d-flex align-items-center justify-content-between">
                                <div>
                                    <div class="stat-label">In Processing / QC</div>
                                    <div class="stat-val text-info">${stats.processingOrders}</div>
                                </div>
                                <div class="stat-icon bg-info-subtle text-info"><i class="bi bi-gear-wide"></i></div>
                            </div>
                        </div>
                    </div>

                    <div class="col-sm-6 col-xl-3">
                        <div class="card-stat">
                            <div class="d-flex align-items-center justify-content-between">
                                <div>
                                    <div class="stat-label">Completed / Dispatched</div>
                                    <div class="stat-val text-success">${stats.completedOrders}</div>
                                </div>
                                <div class="stat-icon bg-success-subtle text-success"><i class="bi bi-check-circle"></i></div>
                            </div>
                        </div>
                    </div>

                    <div class="col-sm-6 col-xl-3">
                        <div class="card-stat">
                            <div class="d-flex align-items-center justify-content-between">
                                <div>
                                    <div class="stat-label">Total Revenue</div>
                                    <div class="stat-val text-success">
                                        <fmt:formatNumber value="${stats.totalRevenue}" type="currency" currencySymbol="$" maxFractionDigits="0"/>
                                    </div>
                                </div>
                                <div class="stat-icon bg-success-subtle text-success"><i class="bi bi-currency-dollar"></i></div>
                            </div>
                        </div>
                    </div>

                    <div class="col-sm-6 col-xl-3">
                        <div class="card-stat">
                            <div class="d-flex align-items-center justify-content-between">
                                <div>
                                    <div class="stat-label">Active Customers</div>
                                    <div class="stat-val text-dark">${stats.totalCustomers}</div>
                                </div>
                                <div class="stat-icon bg-secondary-subtle text-dark"><i class="bi bi-people"></i></div>
                            </div>
                        </div>
                    </div>

                    <div class="col-sm-6 col-xl-3">
                        <div class="card-stat">
                            <div class="d-flex align-items-center justify-content-between">
                                <div>
                                    <div class="stat-label">Catalog Products</div>
                                    <div class="stat-val text-dark">${stats.totalProducts}</div>
                                </div>
                                <div class="stat-icon bg-secondary-subtle text-dark"><i class="bi bi-box"></i></div>
                            </div>
                        </div>
                    </div>

                    <div class="col-sm-6 col-xl-3">
                        <div class="card-stat ${stats.lowStockItemsCount > 0 ? 'border-danger bg-danger-subtle' : ''}">
                            <div class="d-flex align-items-center justify-content-between">
                                <div>
                                    <div class="stat-label ${stats.lowStockItemsCount > 0 ? 'text-danger fw-bold' : ''}">Low Stock Items</div>
                                    <div class="stat-val ${stats.lowStockItemsCount > 0 ? 'text-danger' : 'text-muted'}">${stats.lowStockItemsCount}</div>
                                </div>
                                <div class="stat-icon ${stats.lowStockItemsCount > 0 ? 'bg-danger text-white' : 'bg-secondary-subtle text-muted'}">
                                    <i class="bi bi-exclamation-triangle"></i>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Low Stock Alert Banner (Disappears automatically when replenished) -->
                <c:if test="${not empty stats.lowStockItems}">
                    <div class="alert alert-warning border-warning d-flex align-items-center justify-content-between mb-4 shadow-sm" role="alert">
                        <div class="d-flex align-items-center">
                            <i class="bi bi-exclamation-octagon-fill fs-3 text-warning me-3"></i>
                            <div>
                                <h6 class="alert-heading fw-bold mb-0">Low Packaging Inventory Alert!</h6>
                                <div class="small">The following materials have fallen below their configured reorder thresholds:</div>
                                <div class="d-flex flex-wrap gap-2 mt-1">
                                    <c:forEach var="low" items="${stats.lowStockItems}">
                                        <span class="badge bg-warning text-dark border border-dark border-opacity-25">
                                            <strong>${low.materialName}</strong> (${low.materialCode}):
                                            Available <strong>${low.quantityAvailable}</strong> / Reorder Level: ${low.reorderLevel} ${low.unit}
                                        </span>
                                    </c:forEach>
                                </div>
                            </div>
                        </div>
                        <a href="${pageContext.request.contextPath}/inventory?stockFilter=LOW" class="btn btn-sm btn-dark ms-3">
                            <i class="bi bi-box-arrow-in-down me-1"></i>Replenish
                        </a>
                    </div>
                </c:if>

                <!-- Row 2: Live Charts Section -->
                <div class="row g-3 mb-4">
                    <!-- Chart 1: Orders by Status -->
                    <div class="col-lg-6 col-xl-4">
                        <div class="card-table h-100">
                            <div class="card-header d-flex justify-content-between align-items-center">
                                <h6 class="mb-0 fw-bold"><i class="bi bi-pie-chart me-2 text-primary"></i>Orders by Status</h6>
                                <span class="badge bg-light text-muted border">Live DB</span>
                            </div>
                            <div class="p-3">
                                <div style="height: 260px;">
                                    <canvas id="chartOrdersByStatus"></canvas>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Chart 2: Monthly Orders Trend -->
                    <div class="col-lg-6 col-xl-4">
                        <div class="card-table h-100">
                            <div class="card-header d-flex justify-content-between align-items-center">
                                <h6 class="mb-0 fw-bold"><i class="bi bi-bar-chart me-2 text-primary"></i>Monthly Orders</h6>
                                <span class="badge bg-light text-muted border">Live DB</span>
                            </div>
                            <div class="p-3">
                                <div style="height: 260px;">
                                    <canvas id="chartMonthlyOrders"></canvas>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Chart 3: Monthly Revenue -->
                    <div class="col-lg-6 col-xl-4">
                        <div class="card-table h-100">
                            <div class="card-header d-flex justify-content-between align-items-center">
                                <h6 class="mb-0 fw-bold"><i class="bi bi-graph-up-arrow me-2 text-success"></i>Monthly Revenue ($)</h6>
                                <span class="badge bg-light text-muted border">Live DB</span>
                            </div>
                            <div class="p-3">
                                <div style="height: 260px;">
                                    <canvas id="chartMonthlyRevenue"></canvas>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Chart 4: Inventory Health Distribution -->
                    <div class="col-lg-6 col-xl-6">
                        <div class="card-table h-100">
                            <div class="card-header d-flex justify-content-between align-items-center">
                                <h6 class="mb-0 fw-bold"><i class="bi bi-archive me-2 text-info"></i>Inventory Health Distribution</h6>
                                <span class="badge bg-light text-muted border">Live DB</span>
                            </div>
                            <div class="p-3">
                                <div style="height: 260px;">
                                    <canvas id="chartInventoryStatus"></canvas>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Chart 5: Top Packaging Services -->
                    <div class="col-lg-12 col-xl-6">
                        <div class="card-table h-100">
                            <div class="card-header d-flex justify-content-between align-items-center">
                                <h6 class="mb-0 fw-bold"><i class="bi bi-award me-2 text-warning"></i>Top Packaging Services</h6>
                                <span class="badge bg-light text-muted border">Live DB</span>
                            </div>
                            <div class="p-3">
                                <div style="height: 260px;">
                                    <canvas id="chartTopServices"></canvas>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Row 3: Recent Orders Table -->
                <div class="card-table mb-4">
                    <div class="card-header d-flex justify-content-between align-items-center">
                        <h6 class="mb-0 fw-bold"><i class="bi bi-clock-history me-2 text-primary"></i>Recent Packaging Orders</h6>
                        <a href="${pageContext.request.contextPath}/orders" class="btn btn-sm btn-outline-primary">View All Orders</a>
                    </div>
                    <div class="table-responsive">
                        <table class="table table-hover mb-0">
                            <thead>
                                <tr>
                                    <th>Order #</th>
                                    <th>Customer</th>
                                    <th>Order Date</th>
                                    <th>Total Cost</th>
                                    <th>Status</th>
                                    <th>Action</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:choose>
                                    <c:when test="${not empty stats.recentOrders}">
                                        <c:forEach var="o" items="${stats.recentOrders}">
                                            <tr>
                                                <td class="fw-bold text-primary">${o.orderNumber}</td>
                                                <td>
                                                    <div class="fw-semibold">${o.companyName}</div>
                                                    <div class="text-muted small">${o.contactPerson}</div>
                                                </td>
                                                <td>${o.orderDate}</td>
                                                <td class="fw-bold"><fmt:formatNumber value="${o.totalCost}" type="currency" currencySymbol="$"/></td>
                                                <td><span class="${o.status.badgeClass}">${o.status.displayName}</span></td>
                                                <td>
                                                    <a href="${pageContext.request.contextPath}/orders?action=view&id=${o.orderId}" class="btn btn-sm btn-light border">
                                                        <i class="bi bi-eye"></i> View
                                                    </a>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </c:when>
                                    <c:otherwise>
                                        <tr>
                                            <td colspan="6" class="empty-state">
                                                <i class="bi bi-folder-x empty-state-icon"></i>
                                                <div class="empty-state-title">No orders found in database</div>
                                                <div class="empty-state-text">Create your first packaging order to populate real database analytics.</div>
                                            </td>
                                        </tr>
                                    </c:otherwise>
                                </c:choose>
                            </tbody>
                        </table>
                    </div>
                </div>
            </c:otherwise>
        </c:choose>
    </div> <!-- /.content-wrapper -->

    <jsp:include page="/WEB-INF/views/common/footer.jsp" />

<c:if test="${sessionScope.userRole != 'CUSTOMER'}">
<script>
    function toggleCustomDates(val) {
        const div = document.getElementById('customDatesDiv');
        if (div) {
            div.style.display = (val === 'custom') ? 'flex' : 'none';
        }
    }

    // Chart.js Live Database Visualizations
    document.addEventListener("DOMContentLoaded", function() {
        const ordersByStatusData = ${ordersByStatusJson};
        const monthlyOrdersData = ${monthlyOrdersJson};
        const monthlyRevenueData = ${monthlyRevenueJson};
        const inventoryStatusData = ${inventoryStatusJson};
        const topServicesData = ${topServicesJson};

        // 1. Chart: Orders by Status
        const statusCtx = document.getElementById('chartOrdersByStatus');
        if (statusCtx) {
            new Chart(statusCtx, {
                type: 'doughnut',
                data: {
                    labels: Object.keys(ordersByStatusData),
                    datasets: [{
                        data: Object.values(ordersByStatusData),
                        backgroundColor: [
                            '#64748b', '#06b6d4', '#3b82f6', '#f59e0b',
                            '#10b981', '#1e293b', '#059669', '#ef4444'
                        ]
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: { legend: { position: 'bottom', labels: { boxWidth: 12 } } }
                }
            });
        }

        // 2. Chart: Monthly Orders
        const monthlyCtx = document.getElementById('chartMonthlyOrders');
        if (monthlyCtx) {
            new Chart(monthlyCtx, {
                type: 'bar',
                data: {
                    labels: monthlyOrdersData.map(d => d.label),
                    datasets: [{
                        label: 'Orders',
                        data: monthlyOrdersData.map(d => d.value),
                        backgroundColor: '#3b82f6',
                        borderRadius: 4
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    scales: { y: { beginAtZero: true, ticks: { stepSize: 1 } } }
                }
            });
        }

        // 3. Chart: Monthly Revenue
        const revCtx = document.getElementById('chartMonthlyRevenue');
        if (revCtx) {
            new Chart(revCtx, {
                type: 'line',
                data: {
                    labels: monthlyRevenueData.map(d => d.label),
                    datasets: [{
                        label: 'Revenue ($)',
                        data: monthlyRevenueData.map(d => d.value),
                        borderColor: '#10b981',
                        backgroundColor: 'rgba(16, 185, 129, 0.1)',
                        fill: true,
                        tension: 0.3
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    scales: { y: { beginAtZero: true } }
                }
            });
        }

        // 4. Chart: Inventory Health
        const invCtx = document.getElementById('chartInventoryStatus');
        if (invCtx) {
            new Chart(invCtx, {
                type: 'pie',
                data: {
                    labels: ['Healthy Stock', 'Low Stock', 'Out of Stock'],
                    datasets: [{
                        data: [
                            inventoryStatusData.HEALTHY || 0,
                            inventoryStatusData.LOW_STOCK || 0,
                            inventoryStatusData.OUT_OF_STOCK || 0
                        ],
                        backgroundColor: ['#10b981', '#f59e0b', '#ef4444']
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: { legend: { position: 'bottom' } }
                }
            });
        }

        // 5. Chart: Top Services
        const servCtx = document.getElementById('chartTopServices');
        if (servCtx) {
            new Chart(servCtx, {
                type: 'bar',
                data: {
                    labels: topServicesData.map(d => d.label),
                    datasets: [{
                        label: 'Usage Count',
                        data: topServicesData.map(d => d.value),
                        backgroundColor: '#f59e0b',
                        borderRadius: 4
                    }]
                },
                options: {
                    indexAxis: 'y',
                    responsive: true,
                    maintainAspectRatio: false,
                    scales: { x: { beginAtZero: true, ticks: { stepSize: 1 } } }
                }
            });
        }
    });
</script>
</c:if>
