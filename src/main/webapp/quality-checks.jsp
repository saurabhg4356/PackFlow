<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="pageTitle" value="Quality Assurance Checks" />
<c:set var="pageSubtitle" value="Package integrity inspection, barcode validation, and defect logging" />
<c:set var="activeNav" value="quality" />
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
                        <li class="breadcrumb-item active">Quality Checks</li>
                    </ol>
                </nav>
                <h4 class="fw-bold mb-0">Quality Assurance Inspections (${totalChecks})</h4>
            </div>
        </div>

        <c:if test="${not empty errorMessage}">
            <div class="alert alert-danger alert-dismissible fade show mb-4" role="alert">
                <i class="bi bi-exclamation-triangle-fill me-2 fs-5"></i>${errorMessage}
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
        </c:if>

        <!-- Filter Card -->
        <div class="card card-table mb-4">
            <div class="p-3">
                <form action="${pageContext.request.contextPath}/quality-checks" method="GET" class="row g-2 align-items-center">
                    <div class="col-md-4">
                        <select name="status" class="form-select form-select-sm" onchange="this.form.submit()">
                            <option value="">All Inspection Results</option>
                            <option value="PASSED" ${selectedStatus == 'PASSED' ? 'selected' : ''}>PASSED</option>
                            <option value="PARTIAL" ${selectedStatus == 'PARTIAL' ? 'selected' : ''}>PARTIAL (Rework Needed)</option>
                            <option value="FAILED" ${selectedStatus == 'FAILED' ? 'selected' : ''}>FAILED (Rejected)</option>
                        </select>
                    </div>
                    <div class="col-md-2">
                        <a href="${pageContext.request.contextPath}/quality-checks" class="btn btn-sm btn-outline-secondary">Reset</a>
                    </div>
                </form>
            </div>
        </div>

        <!-- QC Table -->
        <div class="card card-table">
            <div class="table-responsive">
                <table class="table table-hover mb-0">
                    <thead>
                        <tr>
                            <th>QC #</th>
                            <th>Order #</th>
                            <th>Customer</th>
                            <th>Inspector</th>
                            <th class="text-center">Checked</th>
                            <th class="text-center">Passed</th>
                            <th class="text-center">Failed</th>
                            <th class="text-center">Pass Rate</th>
                            <th>Result</th>
                            <th>Remarks</th>
                            <th>Inspection Date</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${not empty checks}">
                                <c:forEach var="q" items="${checks}">
                                    <tr>
                                        <td><span class="badge bg-light text-dark border">#QC-${q.qualityCheckId}</span></td>
                                        <td>
                                            <a href="${pageContext.request.contextPath}/orders?action=view&id=${q.orderId}" class="fw-bold text-decoration-none">
                                                ${q.orderNumber}
                                            </a>
                                        </td>
                                        <td>${q.companyName}</td>
                                        <td>${q.checkedByName}</td>
                                        <td class="text-center fw-semibold">${q.quantityChecked}</td>
                                        <td class="text-center text-success fw-bold">${q.quantityPassed}</td>
                                        <td class="text-center text-danger fw-bold">${q.quantityFailed}</td>
                                        <td class="text-center">
                                            <span class="badge ${q.passRatePercentage >= 95 ? 'bg-success' : 'bg-warning text-dark'}">
                                                ${q.passRatePercentage}%
                                            </span>
                                        </td>
                                        <td><span class="${q.status.badgeClass}">${q.status.displayName}</span></td>
                                        <td class="small text-muted text-truncate" style="max-width: 200px;" title="${q.remarks}">${q.remarks}</td>
                                        <td class="small text-muted">${q.checkedAt}</td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr>
                                    <td colspan="11" class="empty-state">
                                        <i class="bi bi-shield-x empty-state-icon"></i>
                                        <div class="empty-state-title">No quality checks recorded</div>
                                        <div class="empty-state-text">No QA inspection records found for the selected filter.</div>
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
                                <a class="page-link" href="${pageContext.request.contextPath}/quality-checks?page=${currentPage - 1}&status=${selectedStatus}">Previous</a>
                            </li>
                            <c:forEach var="i" begin="1" end="${totalPages}">
                                <li class="page-item ${currentPage == i ? 'active' : ''}">
                                    <a class="page-link" href="${pageContext.request.contextPath}/quality-checks?page=${i}&status=${selectedStatus}">${i}</a>
                                </li>
                            </c:forEach>
                            <li class="page-item ${currentPage >= totalPages ? 'disabled' : ''}">
                                <a class="page-link" href="${pageContext.request.contextPath}/quality-checks?page=${currentPage + 1}&status=${selectedStatus}">Next</a>
                            </li>
                        </ul>
                    </nav>
                </div>
            </c:if>
        </div>
    </div> <!-- /.content-wrapper -->

    <jsp:include page="/WEB-INF/views/common/footer.jsp" />
