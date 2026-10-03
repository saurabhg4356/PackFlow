<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="pageTitle" value="Customers" />
<c:set var="pageSubtitle" value="Client companies, billing contacts, and addresses" />
<c:set var="activeNav" value="customers" />
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
                        <li class="breadcrumb-item active">Customers</li>
                    </ol>
                </nav>
                <h4 class="fw-bold mb-0">Customer Accounts (${totalCustomers})</h4>
            </div>
            <div>
                <button type="button" class="btn btn-primary shadow-sm" onclick="openCustomerModal()">
                    <i class="bi bi-plus-lg me-1"></i>Add New Customer
                </button>
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

        <!-- Search Card -->
        <div class="card card-table mb-4">
            <div class="p-3">
                <form action="${pageContext.request.contextPath}/customers" method="GET" class="row g-2 align-items-center">
                    <div class="col-md-6">
                        <div class="input-group input-group-sm">
                            <span class="input-group-text bg-light"><i class="bi bi-search"></i></span>
                            <input type="text" name="search" class="form-control" placeholder="Search company, contact person, email..." value="${search}">
                            <button type="submit" class="btn btn-dark">Search</button>
                            <a href="${pageContext.request.contextPath}/customers" class="btn btn-outline-secondary">Reset</a>
                        </div>
                    </div>
                </form>
            </div>
        </div>

        <!-- Customers Table -->
        <div class="card card-table">
            <div class="table-responsive">
                <table class="table table-hover mb-0">
                    <thead>
                        <tr>
                            <th>Company Name</th>
                            <th>Contact Person</th>
                            <th>Phone</th>
                            <th>Email</th>
                            <th>Address</th>
                            <th class="text-end">Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${not empty customers}">
                                <c:forEach var="c" items="${customers}">
                                    <tr>
                                        <td class="fw-bold text-dark">${c.companyName}</td>
                                        <td>${c.contactPerson}</td>
                                        <td>${c.phone}</td>
                                        <td><a href="mailto:${c.email}" class="text-decoration-none">${c.email}</a></td>
                                        <td class="text-muted small text-truncate" style="max-width: 250px;" title="${c.address}">
                                            ${c.address}
                                        </td>
                                        <td class="text-end">
                                            <button type="button" class="btn btn-sm btn-outline-secondary me-1"
                                                    onclick="editCustomer('${c.customerId}', '${c.companyName}', '${c.contactPerson}', '${c.phone}', '${c.email}', '${c.address}')">
                                                <i class="bi bi-pencil"></i>
                                            </button>
                                            <form action="${pageContext.request.contextPath}/customers" method="POST" class="d-inline" onsubmit="return confirm('Are you sure you want to delete this customer?');">
                                                <input type="hidden" name="action" value="delete">
                                                <input type="hidden" name="customerId" value="${c.customerId}">
                                                <button type="submit" class="btn btn-sm btn-outline-danger">
                                                    <i class="bi bi-trash"></i>
                                                </button>
                                            </form>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <tr>
                                    <td colspan="6" class="empty-state">
                                        <i class="bi bi-people empty-state-icon"></i>
                                        <div class="empty-state-title">No customers found</div>
                                        <div class="empty-state-text">No customer accounts registered in the database yet.</div>
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
                                <a class="page-link" href="${pageContext.request.contextPath}/customers?page=${currentPage - 1}&search=${search}">Previous</a>
                            </li>
                            <c:forEach var="i" begin="1" end="${totalPages}">
                                <li class="page-item ${currentPage == i ? 'active' : ''}">
                                    <a class="page-link" href="${pageContext.request.contextPath}/customers?page=${i}&search=${search}">${i}</a>
                                </li>
                            </c:forEach>
                            <li class="page-item ${currentPage >= totalPages ? 'disabled' : ''}">
                                <a class="page-link" href="${pageContext.request.contextPath}/customers?page=${currentPage + 1}&search=${search}">Next</a>
                            </li>
                        </ul>
                    </nav>
                </div>
            </c:if>
        </div>
    </div> <!-- /.content-wrapper -->

    <jsp:include page="/WEB-INF/views/common/footer.jsp" />

<!-- Customer Add/Edit Modal -->
<div class="modal fade" id="customerModal" tabindex="-1">
    <div class="modal-dialog">
        <form action="${pageContext.request.contextPath}/customers" method="POST" class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title fw-bold" id="customerModalTitle">Add Customer</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body">
                <input type="hidden" name="customerId" id="modalCustomerId" value="">
                <div class="mb-3">
                    <label class="form-label small fw-semibold">Company Name <span class="text-danger">*</span></label>
                    <input type="text" name="companyName" id="modalCompanyName" class="form-control" required>
                </div>
                <div class="mb-3">
                    <label class="form-label small fw-semibold">Contact Person <span class="text-danger">*</span></label>
                    <input type="text" name="contactPerson" id="modalContactPerson" class="form-control" required>
                </div>
                <div class="row g-2 mb-3">
                    <div class="col-6">
                        <label class="form-label small fw-semibold">Phone <span class="text-danger">*</span></label>
                        <input type="text" name="phone" id="modalPhone" class="form-control" required>
                    </div>
                    <div class="col-6">
                        <label class="form-label small fw-semibold">Email <span class="text-danger">*</span></label>
                        <input type="email" name="email" id="modalEmail" class="form-control" required>
                    </div>
                </div>
                <div class="mb-3">
                    <label class="form-label small fw-semibold">Billing &amp; Delivery Address <span class="text-danger">*</span></label>
                    <textarea name="address" id="modalAddress" class="form-control" rows="3" required></textarea>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
                <button type="submit" class="btn btn-primary">Save Customer</button>
            </div>
        </form>
    </div>
</div>

<script>
    const customerModal = new bootstrap.Modal(document.getElementById('customerModal'));

    function openCustomerModal() {
        document.getElementById('customerModalTitle').innerText = 'Add Customer';
        document.getElementById('modalCustomerId').value = '';
        document.getElementById('modalCompanyName').value = '';
        document.getElementById('modalContactPerson').value = '';
        document.getElementById('modalPhone').value = '';
        document.getElementById('modalEmail').value = '';
        document.getElementById('modalAddress').value = '';
        customerModal.show();
    }

    function editCustomer(id, company, contact, phone, email, addr) {
        document.getElementById('customerModalTitle').innerText = 'Edit Customer';
        document.getElementById('modalCustomerId').value = id;
        document.getElementById('modalCompanyName').value = company;
        document.getElementById('modalContactPerson').value = contact;
        document.getElementById('modalPhone').value = phone;
        document.getElementById('modalEmail').value = email;
        document.getElementById('modalAddress').value = addr;
        customerModal.show();
    }
</script>
