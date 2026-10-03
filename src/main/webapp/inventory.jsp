<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="pageTitle" value="Materials Inventory" />
<c:set var="pageSubtitle" value="Packaging supplies stock levels, reservations, and restock tracking" />
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
                        <li class="breadcrumb-item active">Inventory</li>
                    </ol>
                </nav>
                <h4 class="fw-bold mb-0">Packaging Materials Inventory (${totalItems})</h4>
            </div>
            <div class="d-flex gap-2">
                <a href="${pageContext.request.contextPath}/inventory?action=transactions" class="btn btn-outline-dark">
                    <i class="bi bi-clock-history me-1"></i>Audit Transactions Log
                </a>
                <button type="button" class="btn btn-primary shadow-sm" onclick="openMaterialModal()">
                    <i class="bi bi-plus-lg me-1"></i>Add Packaging Material
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
                <form action="${pageContext.request.contextPath}/inventory" method="GET" class="row g-2 align-items-center">
                    <div class="col-md-5">
                        <div class="input-group input-group-sm">
                            <span class="input-group-text bg-light"><i class="bi bi-search"></i></span>
                            <input type="text" name="search" class="form-control" placeholder="Search material name, material code..." value="${search}">
                        </div>
                    </div>
                    <div class="col-md-4">
                        <select name="stockFilter" class="form-select form-select-sm" onchange="this.form.submit()">
                            <option value="">All Stock Levels</option>
                            <option value="HEALTHY" ${stockFilter == 'HEALTHY' ? 'selected' : ''}>Healthy Stock (> Reorder Level)</option>
                            <option value="LOW" ${stockFilter == 'LOW' ? 'selected' : ''}>Low Stock Alerts (<= Reorder Level)</option>
                            <option value="OUT" ${stockFilter == 'OUT' ? 'selected' : ''}>Out of Stock (= 0)</option>
                        </select>
                    </div>
                    <div class="col-md-3 d-flex gap-2">
                        <button type="submit" class="btn btn-sm btn-dark">Filter</button>
                        <a href="${pageContext.request.contextPath}/inventory" class="btn btn-sm btn-outline-secondary">Reset</a>
                    </div>
                </form>
            </div>
        </div>

        <!-- Inventory Table -->
        <div class="card card-table">
            <div class="table-responsive">
                <table class="table table-hover mb-0">
                    <thead>
                        <tr>
                            <th>Code</th>
                            <th>Material Name</th>
                            <th class="text-center">Available Stock</th>
                            <th class="text-center">Reserved for Orders</th>
                            <th class="text-center">Reorder Threshold</th>
                            <th>Unit</th>
                            <th>Status Alert</th>
                            <th class="text-end">Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${not empty items}">
                                <c:forEach var="item" items="${items}">
                                    <tr class="${item.outOfStock ? 'table-danger' : (item.lowStock ? 'table-warning' : '')}">
                                        <td><span class="badge bg-light text-dark border font-monospace">${item.materialCode}</span></td>
                                        <td class="fw-bold text-dark">${item.materialName}</td>
                                        <td class="text-center fw-bold fs-6 ${item.outOfStock ? 'text-danger' : (item.lowStock ? 'text-warning' : 'text-success')}">
                                            ${item.quantityAvailable}
                                        </td>
                                        <td class="text-center">
                                            <span class="badge bg-light text-secondary border">${item.quantityReserved}</span>
                                        </td>
                                        <td class="text-center text-muted small">${item.reorderLevel}</td>
                                        <td><span class="badge bg-secondary-subtle text-dark">${item.unit}</span></td>
                                        <td>${item.stockStatusBadge}</td>
                                        <td class="text-end">
                                            <button type="button" class="btn btn-sm btn-outline-success me-1"
                                                    onclick="openRestockModal('${item.inventoryId}', '${item.materialName}', '${item.quantityAvailable}', '${item.unit}')"
                                                    title="Restock Material">
                                                <i class="bi bi-box-arrow-in-down"></i> Restock
                                            </button>
                                            <button type="button" class="btn btn-sm btn-outline-warning text-dark me-1"
                                                    onclick="openAdjustModal('${item.inventoryId}', '${item.materialName}', '${item.quantityAvailable}')"
                                                    title="Manual Count Adjustment">
                                                <i class="bi bi-sliders"></i> Adjust
                                            </button>
                                            <button type="button" class="btn btn-sm btn-outline-secondary me-1"
                                                    onclick="editMaterial('${item.inventoryId}', '${item.materialName}', '${item.materialCode}', '${item.quantityAvailable}', '${item.reorderLevel}', '${item.unit}')">
                                                <i class="bi bi-pencil"></i>
                                            </button>
                                            <form action="${pageContext.request.contextPath}/inventory" method="POST" class="d-inline" onsubmit="return confirm('Delete this inventory item?');">
                                                <input type="hidden" name="action" value="delete">
                                                <input type="hidden" name="inventoryId" value="${item.inventoryId}">
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
                                    <td colspan="8" class="empty-state">
                                        <i class="bi bi-archive empty-state-icon"></i>
                                        <div class="empty-state-title">No inventory materials found</div>
                                        <div class="empty-state-text">No packaging supply materials match the selected filter.</div>
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
                                <a class="page-link" href="${pageContext.request.contextPath}/inventory?page=${currentPage - 1}&search=${search}&stockFilter=${stockFilter}">Previous</a>
                            </li>
                            <c:forEach var="i" begin="1" end="${totalPages}">
                                <li class="page-item ${currentPage == i ? 'active' : ''}">
                                    <a class="page-link" href="${pageContext.request.contextPath}/inventory?page=${i}&search=${search}&stockFilter=${stockFilter}">${i}</a>
                                </li>
                            </c:forEach>
                            <li class="page-item ${currentPage >= totalPages ? 'disabled' : ''}">
                                <a class="page-link" href="${pageContext.request.contextPath}/inventory?page=${currentPage + 1}&search=${search}&stockFilter=${stockFilter}">Next</a>
                            </li>
                        </ul>
                    </nav>
                </div>
            </c:if>
        </div>
    </div> <!-- /.content-wrapper -->

    <jsp:include page="/WEB-INF/views/common/footer.jsp" />

<!-- Restock Modal -->
<div class="modal fade" id="restockModal" tabindex="-1">
    <div class="modal-dialog">
        <form action="${pageContext.request.contextPath}/inventory" method="POST" class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title fw-bold text-success"><i class="bi bi-box-arrow-in-down me-2"></i>Restock Supply Material</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body">
                <input type="hidden" name="action" value="restock">
                <input type="hidden" name="inventoryId" id="restockInvId" value="">
                <p>Restocking material: <strong id="restockMaterialName" class="text-dark"></strong></p>
                <div class="mb-3">
                    <label class="form-label small fw-semibold">Quantity to Add <span class="text-danger">*</span></label>
                    <input type="number" name="quantity" class="form-control" min="1" value="100" required>
                </div>
                <div class="mb-3">
                    <label class="form-label small fw-semibold">Purchase Order Reference / Invoice #</label>
                    <input type="text" name="reference" class="form-control" placeholder="e.g. PO-2026-881" value="PURCHASE_RECEIPT">
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
                <button type="submit" class="btn btn-success">Confirm Restock</button>
            </div>
        </form>
    </div>
</div>

<!-- Manual Adjustment Modal -->
<div class="modal fade" id="adjustModal" tabindex="-1">
    <div class="modal-dialog">
        <form action="${pageContext.request.contextPath}/inventory" method="POST" class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title fw-bold text-warning"><i class="bi bi-sliders me-2"></i>Physical Count Adjustment</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body">
                <input type="hidden" name="action" value="adjust">
                <input type="hidden" name="inventoryId" id="adjustInvId" value="">
                <p>Adjusting material: <strong id="adjustMaterialName" class="text-dark"></strong></p>
                <div class="mb-3">
                    <label class="form-label small fw-semibold">New Actual Available Quantity <span class="text-danger">*</span></label>
                    <input type="number" name="newAvailable" id="adjustNewQty" class="form-control" min="0" required>
                </div>
                <div class="mb-3">
                    <label class="form-label small fw-semibold">Reason for Adjustment</label>
                    <input type="text" name="reason" class="form-control" placeholder="e.g. Warehouse count discrepancy, damaged rolls..." required>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
                <button type="submit" class="btn btn-warning text-dark">Save Adjustment</button>
            </div>
        </form>
    </div>
</div>

<!-- Material Add/Edit Modal -->
<div class="modal fade" id="materialModal" tabindex="-1">
    <div class="modal-dialog">
        <form action="${pageContext.request.contextPath}/inventory" method="POST" class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title fw-bold" id="materialModalTitle">Add Material</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body">
                <input type="hidden" name="action" value="save">
                <input type="hidden" name="inventoryId" id="modalInvId" value="">
                <div class="mb-3">
                    <label class="form-label small fw-semibold">Material Name <span class="text-danger">*</span></label>
                    <input type="text" name="materialName" id="modalMaterialName" class="form-control" placeholder="e.g. Medium Corrugated Box (12x10x6 in)" required>
                </div>
                <div class="row g-2 mb-3">
                    <div class="col-6">
                        <label class="form-label small fw-semibold">Material Code / SKU <span class="text-danger">*</span></label>
                        <input type="text" name="materialCode" id="modalMaterialCode" class="form-control" placeholder="e.g. MAT-BOX-M" required>
                    </div>
                    <div class="col-6">
                        <label class="form-label small fw-semibold">Unit</label>
                        <select name="unit" id="modalMaterialUnit" class="form-select">
                            <option value="PCS">PCS</option>
                            <option value="ROLL">ROLL</option>
                            <option value="PACK">PACK</option>
                            <option value="METER">METER</option>
                        </select>
                    </div>
                </div>
                <div class="row g-2 mb-3">
                    <div class="col-6">
                        <label class="form-label small fw-semibold">Available Quantity</label>
                        <input type="number" name="quantityAvailable" id="modalQtyAvailable" class="form-control" min="0" value="100" required>
                    </div>
                    <div class="col-6">
                        <label class="form-label small fw-semibold">Reorder Threshold Alert</label>
                        <input type="number" name="reorderLevel" id="modalReorderLevel" class="form-control" min="1" value="25" required>
                    </div>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
                <button type="submit" class="btn btn-primary">Save Material</button>
            </div>
        </form>
    </div>
</div>

<script>
    const restockModal = new bootstrap.Modal(document.getElementById('restockModal'));
    const adjustModal = new bootstrap.Modal(document.getElementById('adjustModal'));
    const materialModal = new bootstrap.Modal(document.getElementById('materialModal'));

    function openRestockModal(id, name, available, unit) {
        document.getElementById('restockInvId').value = id;
        document.getElementById('restockMaterialName').innerText = name + ' (' + available + ' ' + unit + ' available)';
        restockModal.show();
    }

    function openAdjustModal(id, name, available) {
        document.getElementById('adjustInvId').value = id;
        document.getElementById('adjustMaterialName').innerText = name;
        document.getElementById('adjustNewQty').value = available;
        adjustModal.show();
    }

    function openMaterialModal() {
        document.getElementById('materialModalTitle').innerText = 'Add Packaging Material';
        document.getElementById('modalInvId').value = '';
        document.getElementById('modalMaterialName').value = '';
        document.getElementById('modalMaterialCode').value = '';
        document.getElementById('modalMaterialUnit').value = 'PCS';
        document.getElementById('modalQtyAvailable').value = '100';
        document.getElementById('modalReorderLevel').value = '25';
        materialModal.show();
    }

    function editMaterial(id, name, code, available, reorder, unit) {
        document.getElementById('materialModalTitle').innerText = 'Edit Packaging Material';
        document.getElementById('modalInvId').value = id;
        document.getElementById('modalMaterialName').value = name;
        document.getElementById('modalMaterialCode').value = code;
        document.getElementById('modalMaterialUnit').value = unit;
        document.getElementById('modalQtyAvailable').value = available;
        document.getElementById('modalReorderLevel').value = reorder;
        materialModal.show();
    }
</script>
