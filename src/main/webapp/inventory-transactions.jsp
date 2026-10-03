<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<c:set var="pageTitle" value="Inventory Audit Transactions" />
<c:set var="pageSubtitle" value="Complete audit trail of all packaging material movements" />
<c:set var="activeNav" value="inventory" />
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
                        <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/inventory">Inventory</a></li>
                        <li class="breadcrumb-item active">Transactions Log</li>
                    </ol>
                </nav>
                <h4 class="fw-bold mb-0">Inventory Movement Transactions (${totalTransactions})</h4>
            </div>
            <div>
                <a href="${pageContext.request.contextPath}/inventory" class="btn btn-outline-secondary">
                    <i class="bi bi-arrow-left me-1"></i>Back to Inventory
                </a>
            </div>
        </div>

        <!-- Filter Card -->
        <div class="card card-table mb-4">
            <div class="p-3">
                <form action="${pageContext.request.contextPath}/inventory" method="GET" class="row g-2 align-items-center">
                    <input type="hidden" name="action" value="transactions">

                    <div class="col-md-5">
                        <select name="inventoryId" class="form-select form-select-sm">
                            <option value="">All Inventory Materials</option>
                            <c:forEach var="inv" items="${allInventory}">
                                <option value="${inv.inventoryId}" ${selectedInvId == inv.inventoryId ? 'selected' : ''}>
                                    [${inv.materialCode}] ${inv.materialName}
                                </option>
                            </c:forEach>
                        </select>
                    </div>

                    <div class="col-md-4">
                        <select name="type" class="form-select form-select-sm">
                            <option value="">All Movement Types</option>
                            <option value="PURCHASE" ${selectedType == 'PURCHASE' ? 'selected' : ''}>PURCHASE / RESTOCK</option>
                            <option value="RESERVATION" ${selectedType == 'RESERVATION' ? 'selected' : ''}>RESERVATION (Order Hold)</option>
                            <option value="RELEASE" ${selectedType == 'RELEASE' ? 'selected' : ''}>RELEASE (Cancelled Order)</option>
                            <option value="CONSUMPTION" ${selectedType == 'CONSUMPTION' ? 'selected' : ''}>CONSUMPTION (Packaged)</option>
                            <option value="ADJUSTMENT" ${selectedType == 'ADJUSTMENT' ? 'selected' : ''}>MANUAL ADJUSTMENT</option>
                        </select>
                    </div>

                    <div class="col-md-3 d-flex gap-2">
                        <button type="submit" class="btn btn-sm btn-dark">Filter</button>
                        <a href="${pageContext.request.contextPath}/inventory?action=transactions" class="btn btn-sm btn-outline-secondary">Reset</a>
                    </div>
                </form>
            </div>
        </div>

        <!-- Transactions Table -->
        <div class="card card-table">
            <div class="table-responsive">
                <table class="table table-hover mb-0">
                    <thead>
                        <tr>
                            <th>Timestamp</th>
                            <th>Material Name</th>
                            <th>Code</th>
                            <th>Transaction Type</th>
                            <th class="text-center">Quantity</th>
                            <th>Reference / Audit Note</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${not empty transactions}">
                                <c:forEach var="tx" items="${transactions}">
                                    <tr>
                                        <td class="text-muted small">${tx.createdAt}</td>
                                        <td class="fw-semibold text-dark">${tx.materialName}</td>
                                        <td><span class="badge bg-light text-dark border font-monospace">${tx.materialCode}</span></td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${tx.transactionType == 'PURCHASE'}">
                                                    <span class="badge bg-success-subtle text-success border border-success-subtle">
                                                        <i class="bi bi-box-arrow-in-down me-1"></i>Purchase
                                                    </span>
                                                </c:when>
                                                <c:when test="${tx.transactionType == 'RESERVATION'}">
                                                    <span class="badge bg-warning-subtle text-warning border border-warning-subtle text-dark">
                                                        <i class="bi bi-bookmark-check me-1"></i>Reservation
                                                    </span>
                                                </c:when>
                                                <c:when test="${tx.transactionType == 'RELEASE'}">
                                                    <span class="badge bg-info-subtle text-info border border-info-subtle">
                                                        <i class="bi bi-arrow-counterclockwise me-1"></i>Release
                                                    </span>
                                                </c:when>
                                                <c:when test="${tx.transactionType == 'CONSUMPTION'}">
                                                    <span class="badge bg-danger-subtle text-danger border border-danger-subtle">
                                                        <i class="bi bi-box-arrow-up me-1"></i>Consumption
                                                    </span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge bg-secondary-subtle text-secondary border">
                                                        <i class="bi bi-sliders me-1"></i>Adjustment
                                                    </span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td class="text-center fw-bold">
                                            <c:choose>
                                                <c:when test="${tx.transactionType == 'PURCHASE' || tx.transactionType == 'RELEASE'}">
                                                    <span class="text-success">+${tx.quantity} ${tx.unit}</span>
                                                </c:when>
                                                <c:when test="${tx.transactionType == 'RESERVATION' || tx.transactionType == 'CONSUMPTION'}">
                                                    <span class="text-danger">-${tx.quantity} ${tx.unit}</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="text-secondary">&plusmn;${tx.quantity} ${tx.unit}</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td class="small text-muted">
                                            <c:if test="${not empty tx.referenceType}">
                                                <span class="fw-semibold text-dark">${tx.referenceType}</span>
                                                <c:if test="${not empty tx.referenceId}"> #<strong>${tx.referenceId}</strong></c:if>
                                            </c:if>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr>
                                    <td colspan="6" class="empty-state">
                                        <i class="bi bi-card-checklist empty-state-icon"></i>
                                        <div class="empty-state-title">No transactions recorded</div>
                                        <div class="empty-state-text">No inventory movement audit records found for this selection.</div>
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
                                <a class="page-link" href="${pageContext.request.contextPath}/inventory?action=transactions&page=${currentPage - 1}&inventoryId=${selectedInvId}&type=${selectedType}">Previous</a>
                            </li>
                            <c:forEach var="i" begin="1" end="${totalPages}">
                                <li class="page-item ${currentPage == i ? 'active' : ''}">
                                    <a class="page-link" href="${pageContext.request.contextPath}/inventory?action=transactions&page=${i}&inventoryId=${selectedInvId}&type=${selectedType}">${i}</a>
                                </li>
                            </c:forEach>
                            <li class="page-item ${currentPage >= totalPages ? 'disabled' : ''}">
                                <a class="page-link" href="${pageContext.request.contextPath}/inventory?action=transactions&page=${currentPage + 1}&inventoryId=${selectedInvId}&type=${selectedType}">Next</a>
                            </li>
                        </ul>
                    </nav>
                </div>
            </c:if>
        </div>
    </div> <!-- /.content-wrapper -->

    <jsp:include page="/WEB-INF/views/common/footer.jsp" />
