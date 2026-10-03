<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="pageTitle" value="Products" />
<c:set var="pageSubtitle" value="Client merchandise catalog configured for packaging" />
<c:set var="activeNav" value="products" />
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
                        <li class="breadcrumb-item active">Products</li>
                    </ol>
                </nav>
                <h4 class="fw-bold mb-0">Products Catalog (${totalProducts})</h4>
            </div>
            <div>
                <button type="button" class="btn btn-primary shadow-sm" onclick="openProductModal()">
                    <i class="bi bi-plus-lg me-1"></i>Add New Product
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

        <!-- Filter Card -->
        <div class="card card-table mb-4">
            <div class="p-3">
                <form action="${pageContext.request.contextPath}/products" method="GET" class="row g-2 align-items-center">
                    <div class="col-md-5">
                        <div class="input-group input-group-sm">
                            <span class="input-group-text bg-light"><i class="bi bi-search"></i></span>
                            <input type="text" name="search" class="form-control" placeholder="Search product name, SKU / code..." value="${search}">
                        </div>
                    </div>

                    <c:if test="${sessionScope.userRole != 'CUSTOMER'}">
                        <div class="col-md-4">
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

                    <div class="col-md-3 d-flex gap-2">
                        <button type="submit" class="btn btn-sm btn-dark">Filter</button>
                        <a href="${pageContext.request.contextPath}/products" class="btn btn-sm btn-outline-secondary">Reset</a>
                    </div>
                </form>
            </div>
        </div>

        <!-- Products Table -->
        <div class="card card-table">
            <div class="table-responsive">
                <table class="table table-hover mb-0">
                    <thead>
                        <tr>
                            <th>Code (SKU)</th>
                            <th>Product Name</th>
                            <th>Client Company</th>
                            <th>Unit</th>
                            <th>Status</th>
                            <th class="text-end">Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${not empty products}">
                                <c:forEach var="p" items="${products}">
                                    <tr>
                                        <td><span class="badge bg-light text-dark border font-monospace">${p.productCode}</span></td>
                                        <td>
                                            <div class="fw-bold text-dark">${p.productName}</div>
                                            <div class="text-muted small text-truncate" style="max-width: 300px;">${p.description}</div>
                                        </td>
                                        <td>${p.companyName}</td>
                                        <td><span class="badge bg-secondary-subtle text-dark">${p.unit}</span></td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${p.status == 'ACTIVE'}">
                                                    <span class="badge bg-success-subtle text-success">Active</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge bg-secondary">Inactive</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td class="text-end">
                                            <button type="button" class="btn btn-sm btn-outline-secondary me-1"
                                                    onclick="editProduct('${p.productId}', '${p.customerId}', '${p.productName}', '${p.productCode}', '${p.unit}', '${p.status}', '${p.description}')">
                                                <i class="bi bi-pencil"></i>
                                            </button>
                                            <form action="${pageContext.request.contextPath}/products" method="POST" class="d-inline" onsubmit="return confirm('Delete this product?');">
                                                <input type="hidden" name="action" value="delete">
                                                <input type="hidden" name="productId" value="${p.productId}">
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
                                        <i class="bi bi-box-seam empty-state-icon"></i>
                                        <div class="empty-state-title">No products found</div>
                                        <div class="empty-state-text">No merchandise products registered under this criteria.</div>
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
                                <a class="page-link" href="${pageContext.request.contextPath}/products?page=${currentPage - 1}&search=${search}&customerId=${selectedCustomer}">Previous</a>
                            </li>
                            <c:forEach var="i" begin="1" end="${totalPages}">
                                <li class="page-item ${currentPage == i ? 'active' : ''}">
                                    <a class="page-link" href="${pageContext.request.contextPath}/products?page=${i}&search=${search}&customerId=${selectedCustomer}">${i}</a>
                                </li>
                            </c:forEach>
                            <li class="page-item ${currentPage >= totalPages ? 'disabled' : ''}">
                                <a class="page-link" href="${pageContext.request.contextPath}/products?page=${currentPage + 1}&search=${search}&customerId=${selectedCustomer}">Next</a>
                            </li>
                        </ul>
                    </nav>
                </div>
            </c:if>
        </div>
    </div> <!-- /.content-wrapper -->

    <jsp:include page="/WEB-INF/views/common/footer.jsp" />

<!-- Product Add/Edit Modal -->
<div class="modal fade" id="productModal" tabindex="-1">
    <div class="modal-dialog">
        <form action="${pageContext.request.contextPath}/products" method="POST" class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title fw-bold" id="productModalTitle">Add Product</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body">
                <input type="hidden" name="productId" id="modalProductId" value="">

                <c:choose>
                    <c:when test="${sessionScope.userRole == 'CUSTOMER'}">
                        <input type="hidden" name="customerId" value="${sessionScope.currentUser.customerId}">
                    </c:when>
                    <c:otherwise>
                        <div class="mb-3">
                            <label class="form-label small fw-semibold">Customer / Client <span class="text-danger">*</span></label>
                            <select name="customerId" id="modalCustomerId" class="form-select" required>
                                <c:forEach var="c" items="${customersList}">
                                    <option value="${c.customerId}">${c.companyName}</option>
                                </c:forEach>
                            </select>
                        </div>
                    </c:otherwise>
                </c:choose>

                <div class="mb-3">
                    <label class="form-label small fw-semibold">Product Name <span class="text-danger">*</span></label>
                    <input type="text" name="productName" id="modalProductName" class="form-control" placeholder="e.g. Wireless Bluetooth Headphones" required>
                </div>

                <div class="row g-2 mb-3">
                    <div class="col-6">
                        <label class="form-label small fw-semibold">Product SKU / Code <span class="text-danger">*</span></label>
                        <input type="text" name="productCode" id="modalProductCode" class="form-control" placeholder="e.g. PRD-ZEN-101" required>
                    </div>
                    <div class="col-6">
                        <label class="form-label small fw-semibold">Packaging Unit</label>
                        <select name="unit" id="modalUnit" class="form-select">
                            <option value="PCS">PCS (Pieces)</option>
                            <option value="SET">SET (Pack/Set)</option>
                            <option value="BOX">BOX</option>
                            <option value="BTL">BTL (Bottle)</option>
                            <option value="JAR">JAR</option>
                        </select>
                    </div>
                </div>

                <div class="mb-3">
                    <label class="form-label small fw-semibold">Status</label>
                    <select name="status" id="modalStatus" class="form-select">
                        <option value="ACTIVE">ACTIVE</option>
                        <option value="INACTIVE">INACTIVE</option>
                    </select>
                </div>

                <div class="mb-3">
                    <label class="form-label small fw-semibold">Product Description</label>
                    <textarea name="description" id="modalDescription" class="form-control" rows="2" placeholder="Dimensions, fragile notes, accessories..."></textarea>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
                <button type="submit" class="btn btn-primary">Save Product</button>
            </div>
        </form>
    </div>
</div>

<script>
    const productModal = new bootstrap.Modal(document.getElementById('productModal'));

    function openProductModal() {
        document.getElementById('productModalTitle').innerText = 'Add Product';
        document.getElementById('modalProductId').value = '';
        document.getElementById('modalProductName').value = '';
        document.getElementById('modalProductCode').value = '';
        document.getElementById('modalUnit').value = 'PCS';
        document.getElementById('modalStatus').value = 'ACTIVE';
        document.getElementById('modalDescription').value = '';
        productModal.show();
    }

    function editProduct(id, customerId, name, code, unit, status, desc) {
        document.getElementById('productModalTitle').innerText = 'Edit Product';
        document.getElementById('modalProductId').value = id;
        const custSelect = document.getElementById('modalCustomerId');
        if (custSelect) custSelect.value = customerId;
        document.getElementById('modalProductName').value = name;
        document.getElementById('modalProductCode').value = code;
        document.getElementById('modalUnit').value = unit;
        document.getElementById('modalStatus').value = status;
        document.getElementById('modalDescription').value = desc;
        productModal.show();
    }
</script>
