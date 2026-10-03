<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<c:set var="pageTitle" value="Reports & Analytics" />
<c:set var="pageSubtitle" value="SQL Aggregated business, financial, and operational reports" />
<c:set var="activeNav" value="reports" />
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
                        <li class="breadcrumb-item active">Reports</li>
                    </ol>
                </nav>
                <h4 class="fw-bold mb-0">Business &amp; Operations Reports</h4>
            </div>
            <div>
                <button type="button" class="btn btn-outline-dark" onclick="window.print()">
                    <i class="bi bi-printer me-1"></i>Print Report
                </button>
            </div>
        </div>

        <!-- Report Tabs -->
        <ul class="nav nav-pills mb-4 border-bottom pb-3">
            <li class="nav-item">
                <a class="nav-link ${reportType == 'order' ? 'active' : ''}" href="${pageContext.request.contextPath}/reports?type=order">
                    <i class="bi bi-cart3 me-1"></i>Order Report
                </a>
            </li>
            <li class="nav-item">
                <a class="nav-link ${reportType == 'revenue' ? 'active' : ''}" href="${pageContext.request.contextPath}/reports?type=revenue">
                    <i class="bi bi-currency-dollar me-1"></i>Monthly Revenue
                </a>
            </li>
            <li class="nav-item">
                <a class="nav-link ${reportType == 'inventory' ? 'active' : ''}" href="${pageContext.request.contextPath}/reports?type=inventory">
                    <i class="bi bi-archive me-1"></i>Inventory Valuation
                </a>
            </li>
            <li class="nav-item">
                <a class="nav-link ${reportType == 'customer' ? 'active' : ''}" href="${pageContext.request.contextPath}/reports?type=customer">
                    <i class="bi bi-people me-1"></i>Customer Summary
                </a>
            </li>
            <li class="nav-item">
                <a class="nav-link ${reportType == 'service' ? 'active' : ''}" href="${pageContext.request.contextPath}/reports?type=service">
                    <i class="bi bi-gear me-1"></i>Service Utilization
                </a>
            </li>
        </ul>

        <!-- Filter Card -->
        <div class="card card-table mb-4">
            <div class="p-3">
                <form action="${pageContext.request.contextPath}/reports" method="GET" class="row g-2 align-items-center">
                    <input type="hidden" name="type" value="${reportType}">

                    <c:if test="${reportType == 'order' || reportType == 'revenue' || reportType == 'service'}">
                        <div class="col-md-2">
                            <label class="form-label small text-muted mb-0">From Date</label>
                            <input type="date" name="fromDate" class="form-control form-control-sm" value="${fromDate}">
                        </div>
                        <div class="col-md-2">
                            <label class="form-label small text-muted mb-0">To Date</label>
                            <input type="date" name="toDate" class="form-control form-control-sm" value="${toDate}">
                        </div>
                    </c:if>

                    <c:if test="${reportType == 'order' || reportType == 'revenue'}">
                        <div class="col-md-3">
                            <label class="form-label small text-muted mb-0">Customer</label>
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

                    <c:if test="${reportType == 'order'}">
                        <div class="col-md-2">
                            <label class="form-label small text-muted mb-0">Status</label>
                            <select name="status" class="form-select form-select-sm">
                                <option value="">All Statuses</option>
                                <option value="PENDING" ${selectedStatus == 'PENDING' ? 'selected' : ''}>PENDING</option>
                                <option value="APPROVED" ${selectedStatus == 'APPROVED' ? 'selected' : ''}>APPROVED</option>
                                <option value="PROCESSING" ${selectedStatus == 'PROCESSING' ? 'selected' : ''}>PROCESSING</option>
                                <option value="COMPLETED" ${selectedStatus == 'COMPLETED' ? 'selected' : ''}>COMPLETED</option>
                                <option value="DISPATCHED" ${selectedStatus == 'DISPATCHED' ? 'selected' : ''}>DISPATCHED</option>
                                <option value="DELIVERED" ${selectedStatus == 'DELIVERED' ? 'selected' : ''}>DELIVERED</option>
                                <option value="CANCELLED" ${selectedStatus == 'CANCELLED' ? 'selected' : ''}>CANCELLED</option>
                            </select>
                        </div>
                    </c:if>

                    <c:if test="${reportType == 'inventory'}">
                        <div class="col-md-4">
                            <label class="form-label small text-muted mb-0">Stock Status Filter</label>
                            <select name="stockFilter" class="form-select form-select-sm">
                                <option value="">All Materials</option>
                                <option value="LOW" ${stockFilter == 'LOW' ? 'selected' : ''}>Low Stock Alerts (<= Reorder Level)</option>
                                <option value="OUT" ${stockFilter == 'OUT' ? 'selected' : ''}>Out of Stock</option>
                            </select>
                        </div>
                    </c:if>

                    <div class="col-md-3 d-flex gap-2 align-self-end">
                        <button type="submit" class="btn btn-sm btn-dark">Run Report</button>
                        <a href="${pageContext.request.contextPath}/reports?type=${reportType}" class="btn btn-sm btn-outline-secondary">Reset</a>
                    </div>
                </form>
            </div>
        </div>

        <!-- Dynamic Report Table -->
        <div class="card card-table">
            <div class="card-header d-flex justify-content-between align-items-center">
                <h6 class="mb-0 fw-bold text-uppercase" style="letter-spacing: 0.05em;">
                    <i class="bi bi-file-earmark-spreadsheet me-2 text-primary"></i>${reportType} Report Results (${rows != null ? rows.size() : 0} Rows)
                </h6>
                <span class="badge bg-light text-muted border">Live MySQL Aggregations</span>
            </div>
            <div class="table-responsive">
                <table class="table table-hover mb-0">
                    <thead class="table-light">
                        <c:choose>
                            <%-- ORDER REPORT HEADERS --%>
                            <c:when test="${reportType == 'order'}">
                                <tr>
                                    <th>Order #</th>
                                    <th>Customer Company</th>
                                    <th>Status</th>
                                    <th>Order Date</th>
                                    <th class="text-center">Line Items</th>
                                    <th class="text-center">Total Units</th>
                                    <th class="text-end">Total Amount</th>
                                </tr>
                            </c:when>

                            <%-- REVENUE REPORT HEADERS --%>
                            <c:when test="${reportType == 'revenue'}">
                                <tr>
                                    <th>Month / Period</th>
                                    <th>Customer Company</th>
                                    <th class="text-center">Orders Count</th>
                                    <th class="text-end">Total Revenue ($)</th>
                                </tr>
                            </c:when>

                            <%-- INVENTORY REPORT HEADERS --%>
                            <c:when test="${reportType == 'inventory'}">
                                <tr>
                                    <th>Material Code</th>
                                    <th>Material Name</th>
                                    <th>Unit</th>
                                    <th class="text-center">Available Stock</th>
                                    <th class="text-center">Reserved for Orders</th>
                                </tr>
                            </c:when>

                            <%-- CUSTOMER REPORT HEADERS --%>
                            <c:when test="${reportType == 'customer'}">
                                <tr>
                                    <th>Company Name</th>
                                    <th>Contact Person</th>
                                    <th>Email</th>
                                    <th class="text-center">Total Orders</th>
                                    <th class="text-end">Total Lifetime Spend</th>
                                </tr>
                            </c:when>

                            <%-- SERVICE USAGE REPORT HEADERS --%>
                            <c:when test="${reportType == 'service'}">
                                <tr>
                                    <th>Service Name</th>
                                    <th>Base Price</th>
                                    <th>Status</th>
                                    <th class="text-center">Times Ordered</th>
                                    <th class="text-center">Units Packaged</th>
                                    <th class="text-end">Total Revenue</th>
                                </tr>
                            </c:when>
                        </c:choose>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${not empty rows}">
                                <c:set var="sumAmount" value="0" />
                                <c:set var="sumUnits" value="0" />

                                <c:forEach var="r" items="${rows}">
                                    <tr>
                                        <td class="fw-bold text-dark">${r.col1}</td>
                                        <td>${r.col2}</td>
                                        <c:choose>
                                            <c:when test="${reportType == 'order'}">
                                                <td><span class="badge bg-secondary">${r.col3}</span></td>
                                                <td>${r.dateCol}</td>
                                                <td class="text-center">${r.countVal}</td>
                                                <td class="text-center fw-semibold">${r.quantityVal}</td>
                                                <td class="text-end fw-bold"><fmt:formatNumber value="${r.amountVal}" type="currency" currencySymbol="$"/></td>
                                            </c:when>
                                            <c:when test="${reportType == 'revenue'}">
                                                <td class="text-center fw-semibold">${r.countVal}</td>
                                                <td class="text-end fw-bold text-success"><fmt:formatNumber value="${r.amountVal}" type="currency" currencySymbol="$"/></td>
                                            </c:when>
                                            <c:when test="${reportType == 'inventory'}">
                                                <td><span class="badge bg-light text-dark border">${r.col3}</span></td>
                                                <td class="text-center fw-bold ${r.quantityVal <= 0 ? 'text-danger' : 'text-success'}">${r.quantityVal}</td>
                                                <td class="text-center text-muted">${r.countVal}</td>
                                            </c:when>
                                            <c:when test="${reportType == 'customer'}">
                                                <td><a href="mailto:${r.col3}">${r.col3}</a></td>
                                                <td class="text-center fw-bold">${r.countVal}</td>
                                                <td class="text-end fw-bold text-success"><fmt:formatNumber value="${r.amountVal}" type="currency" currencySymbol="$"/></td>
                                            </c:when>
                                            <c:when test="${reportType == 'service'}">
                                                <td><span class="badge bg-success-subtle text-success">${r.col3}</span></td>
                                                <td class="text-center">${r.countVal}</td>
                                                <td class="text-center fw-semibold">${r.quantityVal}</td>
                                                <td class="text-end fw-bold text-primary"><fmt:formatNumber value="${r.amountVal}" type="currency" currencySymbol="$"/></td>
                                            </c:when>
                                        </c:choose>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr>
                                    <td colspan="7" class="empty-state">
                                        <i class="bi bi-file-earmark-x empty-state-icon"></i>
                                        <div class="empty-state-title">No report data found</div>
                                        <div class="empty-state-text">No matching records found in the database for the selected parameters.</div>
                                    </td>
                                </tr>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>
            </div>
        </div>
    </div> <!-- /.content-wrapper -->

    <jsp:include page="/WEB-INF/views/common/footer.jsp" />
