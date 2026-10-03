<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<c:set var="pageTitle" value="Packaging Services" />
<c:set var="pageSubtitle" value="Packaging operational capabilities and baseline pricing catalog" />
<c:set var="activeNav" value="services" />
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
                        <li class="breadcrumb-item active">Packaging Services</li>
                    </ol>
                </nav>
                <h4 class="fw-bold mb-0">Packaging Services &amp; Pricing</h4>
            </div>
            <div>
                <button type="button" class="btn btn-primary shadow-sm" onclick="openServiceModal()">
                    <i class="bi bi-plus-lg me-1"></i>Add Packaging Service
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

        <!-- Services Cards / Table -->
        <div class="card card-table">
            <div class="table-responsive">
                <table class="table table-hover mb-0">
                    <thead>
                        <tr>
                            <th>Service Name</th>
                            <th>Description</th>
                            <th class="text-end">Base Price / Unit</th>
                            <th>Status</th>
                            <th class="text-end">Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="s" items="${services}">
                            <tr>
                                <td class="fw-bold text-dark fs-6">${s.serviceName}</td>
                                <td class="text-muted small">${s.description}</td>
                                <td class="text-end fw-bold text-primary fs-6">
                                    <fmt:formatNumber value="${s.basePrice}" type="currency" currencySymbol="$"/>
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${s.status == 'ACTIVE'}">
                                            <span class="badge bg-success-subtle text-success">Active</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge bg-secondary">Inactive</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td class="text-end">
                                    <button type="button" class="btn btn-sm btn-outline-secondary me-1"
                                            onclick="editService('${s.serviceId}', '${s.serviceName}', '${s.description}', '${s.basePrice}', '${s.status}')">
                                        <i class="bi bi-pencil"></i>
                                    </button>
                                    <form action="${pageContext.request.contextPath}/packaging-services" method="POST" class="d-inline" onsubmit="return confirm('Delete this packaging service?');">
                                        <input type="hidden" name="action" value="delete">
                                        <input type="hidden" name="serviceId" value="${s.serviceId}">
                                        <button type="submit" class="btn btn-sm btn-outline-danger">
                                            <i class="bi bi-trash"></i>
                                        </button>
                                    </form>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>
    </div> <!-- /.content-wrapper -->

    <jsp:include page="/WEB-INF/views/common/footer.jsp" />

<!-- Service Add/Edit Modal -->
<div class="modal fade" id="serviceModal" tabindex="-1">
    <div class="modal-dialog">
        <form action="${pageContext.request.contextPath}/packaging-services" method="POST" class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title fw-bold" id="serviceModalTitle">Add Packaging Service</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body">
                <input type="hidden" name="serviceId" id="modalServiceId" value="">
                <div class="mb-3">
                    <label class="form-label small fw-semibold">Service Name <span class="text-danger">*</span></label>
                    <input type="text" name="serviceName" id="modalServiceName" class="form-control" placeholder="e.g. Corrugated Box Packing" required>
                </div>
                <div class="row g-2 mb-3">
                    <div class="col-6">
                        <label class="form-label small fw-semibold">Base Price ($) <span class="text-danger">*</span></label>
                        <input type="number" step="0.01" name="basePrice" id="modalBasePrice" class="form-control" min="0" value="10.00" required>
                    </div>
                    <div class="col-6">
                        <label class="form-label small fw-semibold">Status</label>
                        <select name="status" id="modalStatus" class="form-select">
                            <option value="ACTIVE">ACTIVE</option>
                            <option value="INACTIVE">INACTIVE</option>
                        </select>
                    </div>
                </div>
                <div class="mb-3">
                    <label class="form-label small fw-semibold">Description &amp; Specifications</label>
                    <textarea name="description" id="modalDescription" class="form-control" rows="3" placeholder="Materials used, labor, compliance..."></textarea>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
                <button type="submit" class="btn btn-primary">Save Service</button>
            </div>
        </form>
    </div>
</div>

<script>
    const serviceModal = new bootstrap.Modal(document.getElementById('serviceModal'));

    function openServiceModal() {
        document.getElementById('serviceModalTitle').innerText = 'Add Packaging Service';
        document.getElementById('modalServiceId').value = '';
        document.getElementById('modalServiceName').value = '';
        document.getElementById('modalBasePrice').value = '10.00';
        document.getElementById('modalStatus').value = 'ACTIVE';
        document.getElementById('modalDescription').value = '';
        serviceModal.show();
    }

    function editService(id, name, desc, price, status) {
        document.getElementById('serviceModalTitle').innerText = 'Edit Packaging Service';
        document.getElementById('modalServiceId').value = id;
        document.getElementById('modalServiceName').value = name;
        document.getElementById('modalBasePrice').value = price;
        document.getElementById('modalStatus').value = status;
        document.getElementById('modalDescription').value = desc;
        serviceModal.show();
    }
</script>
