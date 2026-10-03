<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<c:set var="pageTitle" value="Order ${order.orderNumber}" />
<c:set var="pageSubtitle" value="Lifecycle timeline, reserved materials &amp; fulfillment" />
<c:set var="activeNav" value="orders" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<jsp:include page="/WEB-INF/views/common/sidebar.jsp" />

<div id="main-content" class="d-flex flex-column min-vh-100">
    <jsp:include page="/WEB-INF/views/common/navbar.jsp" />

    <div class="content-wrapper">
        <!-- Breadcrumbs & Header -->
        <div class="d-flex flex-wrap justify-content-between align-items-center mb-4 gap-2">
            <div>
                <nav aria-label="breadcrumb">
                    <ol class="breadcrumb mb-1 small">
                        <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/dashboard">Dashboard</a></li>
                        <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/orders">Orders</a></li>
                        <li class="breadcrumb-item active">${order.orderNumber}</li>
                    </ol>
                </nav>
                <div class="d-flex align-items-center gap-2">
                    <h4 class="fw-bold mb-0">${order.orderNumber}</h4>
                    <span class="${order.status.badgeClass} fs-6">${order.status.displayName}</span>
                </div>
            </div>

            <!-- Workflow Action Buttons based on Role and Status -->
            <div class="d-flex flex-wrap gap-2">
                <a href="${pageContext.request.contextPath}/orders" class="btn btn-outline-secondary">
                    <i class="bi bi-arrow-left me-1"></i>Back to Orders
                </a>

                <c:if test="${sessionScope.userRole != 'CUSTOMER'}">
                    <%-- PENDING -> APPROVE --%>
                    <c:if test="${order.status == 'PENDING'}">
                        <form action="${pageContext.request.contextPath}/orders" method="POST" onsubmit="return confirm('Approve order and reserve packaging materials?');">
                            <input type="hidden" name="action" value="approve">
                            <input type="hidden" name="orderId" value="${order.orderId}">
                            <button type="submit" class="btn btn-success shadow-sm">
                                <i class="bi bi-check-circle me-1"></i>Approve &amp; Reserve Inventory
                            </button>
                        </form>
                    </c:if>

                    <%-- APPROVED -> PROCESS --%>
                    <c:if test="${order.status == 'APPROVED'}">
                        <form action="${pageContext.request.contextPath}/orders" method="POST">
                            <input type="hidden" name="action" value="process">
                            <input type="hidden" name="orderId" value="${order.orderId}">
                            <button type="submit" class="btn btn-primary shadow-sm">
                                <i class="bi bi-play-circle me-1"></i>Start Packaging Task
                            </button>
                        </form>
                    </c:if>

                    <%-- PROCESSING -> SEND TO QC --%>
                    <c:if test="${order.status == 'PROCESSING'}">
                        <form action="${pageContext.request.contextPath}/orders" method="POST">
                            <input type="hidden" name="action" value="to_qc">
                            <input type="hidden" name="orderId" value="${order.orderId}">
                            <button type="submit" class="btn btn-warning text-dark shadow-sm">
                                <i class="bi bi-shield-check me-1"></i>Submit for Quality Check
                            </button>
                        </form>
                    </c:if>

                    <%-- QUALITY_CHECK -> INSPECTION MODAL TRIGGER --%>
                    <c:if test="${order.status == 'QUALITY_CHECK'}">
                        <button type="button" class="btn btn-warning text-dark shadow-sm" data-bs-toggle="modal" data-bs-target="#qcModal">
                            <i class="bi bi-clipboard-check me-1"></i>Perform Quality Inspection
                        </button>
                    </c:if>

                    <%-- COMPLETED -> DISPATCH MODAL TRIGGER --%>
                    <c:if test="${order.status == 'COMPLETED'}">
                        <button type="button" class="btn btn-dark shadow-sm" data-bs-toggle="modal" data-bs-target="#dispatchModal">
                            <i class="bi bi-truck me-1"></i>Dispatch Shipment
                        </button>
                    </c:if>

                    <%-- DISPATCHED -> CONFIRM DELIVERED --%>
                    <c:if test="${order.status == 'DISPATCHED'}">
                        <form action="${pageContext.request.contextPath}/dispatches" method="POST" onsubmit="return confirm('Confirm order has been delivered?');">
                            <input type="hidden" name="action" value="deliver">
                            <input type="hidden" name="orderId" value="${order.orderId}">
                            <button type="submit" class="btn btn-success shadow-sm">
                                <i class="bi bi-box2-heart me-1"></i>Confirm Delivered
                            </button>
                        </form>
                    </c:if>
                </c:if>

                <%-- CANCEL ORDER --%>
                <c:if test="${order.status == 'PENDING' || (sessionScope.userRole != 'CUSTOMER' && (order.status == 'APPROVED' || order.status == 'PROCESSING'))}">
                    <button type="button" class="btn btn-outline-danger" data-bs-toggle="modal" data-bs-target="#cancelModal">
                        <i class="bi bi-x-circle me-1"></i>Cancel Order
                    </button>
                </c:if>
            </div>
        </div>

        <!-- Alert Feedback -->
        <c:if test="${not empty errorMessage}">
            <div class="alert alert-danger alert-dismissible fade show mb-4" role="alert">
                <i class="bi bi-exclamation-triangle-fill me-2 fs-5"></i>
                <strong>Action Failed:</strong> ${errorMessage}
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
        </c:if>

        <c:if test="${not empty successMessage}">
            <div class="alert alert-success alert-dismissible fade show mb-4" role="alert">
                <i class="bi bi-check-circle-fill me-2 fs-5"></i>
                ${successMessage}
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
        </c:if>

        <!-- Visual Workflow Progress Steps (Except if Cancelled) -->
        <c:choose>
            <c:when test="${order.status == 'CANCELLED'}">
                <div class="alert alert-danger mb-4 py-3 d-flex align-items-center">
                    <i class="bi bi-x-octagon-fill fs-3 me-3"></i>
                    <div>
                        <h6 class="fw-bold mb-0">This order has been CANCELLED.</h6>
                        <span class="small">Any previously reserved materials have been released back into available inventory.</span>
                    </div>
                </div>
            </c:when>
            <c:otherwise>
                <div class="card card-table mb-4 p-4">
                    <div class="workflow-timeline">
                        <div class="timeline-step ${order.status != 'PENDING' ? 'completed' : 'active'}">
                            <div class="step-icon"><i class="bi bi-file-earmark-text"></i></div>
                            <div class="step-title">1. Request</div>
                        </div>
                        <div class="timeline-step ${order.status == 'APPROVED' ? 'active' : (order.status != 'PENDING' ? 'completed' : '')}">
                            <div class="step-icon"><i class="bi bi-check2-circle"></i></div>
                            <div class="step-title">2. Approved &amp; Reserved</div>
                        </div>
                        <div class="timeline-step ${order.status == 'PROCESSING' ? 'active' : (order.status == 'QUALITY_CHECK' || order.status == 'COMPLETED' || order.status == 'DISPATCHED' || order.status == 'DELIVERED' ? 'completed' : '')}">
                            <div class="step-icon"><i class="bi bi-gear"></i></div>
                            <div class="step-title">3. Packaging Floor</div>
                        </div>
                        <div class="timeline-step ${order.status == 'QUALITY_CHECK' ? 'active' : (order.status == 'COMPLETED' || order.status == 'DISPATCHED' || order.status == 'DELIVERED' ? 'completed' : '')}">
                            <div class="step-icon"><i class="bi bi-shield-check"></i></div>
                            <div class="step-title">4. Quality Check</div>
                        </div>
                        <div class="timeline-step ${order.status == 'COMPLETED' ? 'active' : (order.status == 'DISPATCHED' || order.status == 'DELIVERED' ? 'completed' : '')}">
                            <div class="step-icon"><i class="bi bi-box-seam"></i></div>
                            <div class="step-title">5. Packaged</div>
                        </div>
                        <div class="timeline-step ${order.status == 'DISPATCHED' ? 'active' : (order.status == 'DELIVERED' ? 'completed' : '')}">
                            <div class="step-icon"><i class="bi bi-truck"></i></div>
                            <div class="step-title">6. Dispatched</div>
                        </div>
                        <div class="timeline-step ${order.status == 'DELIVERED' ? 'completed' : ''}">
                            <div class="step-icon"><i class="bi bi-house-check"></i></div>
                            <div class="step-title">7. Delivered</div>
                        </div>
                    </div>
                </div>
            </c:otherwise>
        </c:choose>

        <!-- Information Cards: Customer & Order Metadata -->
        <div class="row g-4 mb-4">
            <div class="col-md-6">
                <div class="card card-table h-100">
                    <div class="card-header">
                        <h6 class="fw-bold mb-0"><i class="bi bi-building me-2 text-primary"></i>Client / Customer Information</h6>
                    </div>
                    <div class="p-3">
                        <table class="table table-sm table-borderless mb-0">
                            <tr>
                                <th class="text-muted w-35">Company:</th>
                                <td class="fw-bold">${order.companyName}</td>
                            </tr>
                            <tr>
                                <th class="text-muted">Contact Person:</th>
                                <td>${order.contactPerson}</td>
                            </tr>
                            <tr>
                                <th class="text-muted">Email:</th>
                                <td>${order.customerEmail}</td>
                            </tr>
                            <tr>
                                <th class="text-muted">Phone:</th>
                                <td>${order.customerPhone}</td>
                            </tr>
                        </table>
                    </div>
                </div>
            </div>

            <div class="col-md-6">
                <div class="card card-table h-100">
                    <div class="card-header">
                        <h6 class="fw-bold mb-0"><i class="bi bi-info-circle me-2 text-primary"></i>Order Overview</h6>
                    </div>
                    <div class="p-3">
                        <table class="table table-sm table-borderless mb-0">
                            <tr>
                                <th class="text-muted w-35">Order Date:</th>
                                <td class="fw-semibold">${order.orderDate}</td>
                            </tr>
                            <tr>
                                <th class="text-muted">Lifecycle Status:</th>
                                <td><span class="${order.status.badgeClass}">${order.status.displayName}</span></td>
                            </tr>
                            <tr>
                                <th class="text-muted">Total Quantity:</th>
                                <td><span class="badge bg-light text-dark border">${order.totalQuantity} items</span></td>
                            </tr>
                            <tr>
                                <th class="text-muted">Client Notes:</th>
                                <td>${not empty order.notes ? order.notes : '<span class="text-muted fst-italic">None provided</span>'}</td>
                            </tr>
                        </table>
                    </div>
                </div>
            </div>
        </div>

        <!-- Packaging Task & Quality Check & Dispatch Status Info -->
        <div class="row g-4 mb-4">
            <c:if test="${not empty order.task}">
                <div class="col-md-4">
                    <div class="card card-table h-100">
                        <div class="card-header">
                            <h6 class="fw-bold mb-0"><i class="bi bi-list-task me-2 text-primary"></i>Packaging Task</h6>
                        </div>
                        <div class="p-3">
                            <div class="d-flex justify-content-between mb-1">
                                <span class="small text-muted">Progress</span>
                                <span class="small fw-bold">${order.task.completedQuantity} / ${order.task.quantity} (${order.task.progressPercentage}%)</span>
                            </div>
                            <div class="progress mb-3" style="height: 8px;">
                                <div class="progress-bar bg-primary" style="width: ${order.task.progressPercentage}%"></div>
                            </div>
                            <div class="small text-muted mb-1">Assigned: <strong>${not empty order.task.assignedToName ? order.task.assignedToName : 'Unassigned'}</strong></div>
                            <div class="small text-muted">Status: <span class="${order.task.status.badgeClass}">${order.task.status.displayName}</span></div>
                        </div>
                    </div>
                </div>
            </c:if>

            <c:if test="${not empty order.qualityCheck}">
                <div class="col-md-4">
                    <div class="card card-table h-100">
                        <div class="card-header">
                            <h6 class="fw-bold mb-0"><i class="bi bi-shield-check me-2 text-warning"></i>Quality Inspection</h6>
                        </div>
                        <div class="p-3">
                            <div class="d-flex justify-content-between align-items-center mb-2">
                                <span class="small text-muted">Inspection Result:</span>
                                <span class="${order.qualityCheck.status.badgeClass}">${order.qualityCheck.status.displayName}</span>
                            </div>
                            <div class="small mb-1">Passed: <strong class="text-success">${order.qualityCheck.quantityPassed}</strong> | Failed: <strong class="text-danger">${order.qualityCheck.quantityFailed}</strong></div>
                            <div class="small text-muted mb-1">Inspector: <strong>${order.qualityCheck.checkedByName}</strong></div>
                            <div class="small text-muted">Remarks: <em>${order.qualityCheck.remarks}</em></div>
                        </div>
                    </div>
                </div>
            </c:if>

            <c:if test="${not empty order.dispatch}">
                <div class="col-md-4">
                    <div class="card card-table h-100">
                        <div class="card-header">
                            <h6 class="fw-bold mb-0"><i class="bi bi-truck me-2 text-dark"></i>Shipment &amp; Courier</h6>
                        </div>
                        <div class="p-3">
                            <div class="d-flex justify-content-between align-items-center mb-2">
                                <span class="small text-muted">Courier:</span>
                                <strong class="text-dark">${order.dispatch.deliveryPartner}</strong>
                            </div>
                            <div class="small mb-1">Tracking #: <span class="badge bg-light text-dark border font-monospace">${order.dispatch.trackingNumber}</span></div>
                            <div class="small text-muted mb-1">Dispatched Date: <strong>${order.dispatch.dispatchDate}</strong></div>
                            <div class="small text-muted">Status: <span class="${order.dispatch.status.badgeClass}">${order.dispatch.status.displayName}</span></div>
                        </div>
                    </div>
                </div>
            </c:if>
        </div>

        <!-- Line Items Table -->
        <div class="card card-table mb-4">
            <div class="card-header">
                <h6 class="fw-bold mb-0"><i class="bi bi-box me-2 text-primary"></i>Packaging Line Items</h6>
            </div>
            <div class="table-responsive">
                <table class="table table-hover mb-0">
                    <thead>
                        <tr>
                            <th>#</th>
                            <th>Product Code &amp; Name</th>
                            <th>Packaging Service</th>
                            <th class="text-center">Quantity</th>
                            <th class="text-end">Service Unit Price</th>
                            <th class="text-end">Total Price</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="item" items="${order.items}" varStatus="status">
                            <tr>
                                <td>${status.count}</td>
                                <td>
                                    <div class="fw-bold text-dark">${item.productName}</div>
                                    <span class="badge bg-light text-muted border">${item.productCode}</span>
                                </td>
                                <td><span class="badge bg-info-subtle text-info border">${item.serviceName}</span></td>
                                <td class="text-center fw-semibold">${item.quantity}</td>
                                <td class="text-end"><fmt:formatNumber value="${item.unitPrice}" type="currency" currencySymbol="$"/></td>
                                <td class="text-end fw-bold"><fmt:formatNumber value="${item.totalPrice}" type="currency" currencySymbol="$"/></td>
                            </tr>
                        </c:forEach>
                    </tbody>
                    <tfoot class="table-light">
                        <tr>
                            <td colspan="5" class="text-end fw-bold fs-6">Subtotal:</td>
                            <td class="text-end fw-bold fs-6"><fmt:formatNumber value="${order.subtotal}" type="currency" currencySymbol="$"/></td>
                        </tr>
                        <tr>
                            <td colspan="5" class="text-end fw-bold fs-5 text-primary">Total Packaging Cost:</td>
                            <td class="text-end fw-bold fs-5 text-primary"><fmt:formatNumber value="${order.totalCost}" type="currency" currencySymbol="$"/></td>
                        </tr>
                    </tfoot>
                </table>
            </div>
        </div>
    </div> <!-- /.content-wrapper -->

    <jsp:include page="/WEB-INF/views/common/footer.jsp" />

<!-- Quality Check Modal -->
<div class="modal fade" id="qcModal" tabindex="-1">
    <div class="modal-dialog">
        <form action="${pageContext.request.contextPath}/quality-checks" method="POST" class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title fw-bold"><i class="bi bi-shield-check me-2 text-warning"></i>Perform Quality Check</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body">
                <input type="hidden" name="orderId" value="${order.orderId}">
                <div class="row g-2 mb-3">
                    <div class="col-6">
                        <label class="form-label small fw-semibold">Quantity Passed</label>
                        <input type="number" name="quantityPassed" class="form-control" value="${order.totalQuantity}" min="0" max="${order.totalQuantity}" required>
                    </div>
                    <div class="col-6">
                        <label class="form-label small fw-semibold">Quantity Failed</label>
                        <input type="number" name="quantityFailed" class="form-control" value="0" min="0" max="${order.totalQuantity}" required>
                    </div>
                </div>
                <div class="mb-3">
                    <label class="form-label small fw-semibold">Inspection Result Status</label>
                    <select name="status" class="form-select" required>
                        <option value="PASSED" selected>PASSED - All criteria met (Advance to Completed)</option>
                        <option value="PARTIAL">PARTIAL - Minor defects, rework needed</option>
                        <option value="FAILED">FAILED - Rejected, return to packaging</option>
                    </select>
                </div>
                <div class="mb-3">
                    <label class="form-label small fw-semibold">Inspection Remarks</label>
                    <textarea name="remarks" class="form-control" rows="3" placeholder="Sealing integrity, barcode verification, carton tape..."></textarea>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
                <button type="submit" class="btn btn-primary">Submit Inspection Record</button>
            </div>
        </form>
    </div>
</div>

<!-- Dispatch Shipment Modal -->
<div class="modal fade" id="dispatchModal" tabindex="-1">
    <div class="modal-dialog">
        <form action="${pageContext.request.contextPath}/dispatches" method="POST" class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title fw-bold"><i class="bi bi-truck me-2 text-dark"></i>Dispatch Packaging Order</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body">
                <input type="hidden" name="action" value="dispatch">
                <input type="hidden" name="orderId" value="${order.orderId}">
                <div class="mb-3">
                    <label class="form-label small fw-semibold">Delivery Partner / Carrier</label>
                    <select name="deliveryPartner" class="form-select" required>
                        <option value="Blue Dart Express">Blue Dart Express</option>
                        <option value="Delhivery Surface">Delhivery Surface</option>
                        <option value="DHL Supply Chain">DHL Supply Chain</option>
                        <option value="FedEx Logistics">FedEx Logistics</option>
                        <option value="DTDC Courier">DTDC Courier</option>
                    </select>
                </div>
                <div class="mb-3">
                    <label class="form-label small fw-semibold">Tracking Number / AWB</label>
                    <input type="text" name="trackingNumber" class="form-control" placeholder="e.g. BD-892301928" required>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
                <button type="submit" class="btn btn-dark">Confirm Dispatch</button>
            </div>
        </form>
    </div>
</div>

<!-- Cancel Order Modal -->
<div class="modal fade" id="cancelModal" tabindex="-1">
    <div class="modal-dialog">
        <form action="${pageContext.request.contextPath}/orders" method="POST" class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title fw-bold text-danger"><i class="bi bi-exclamation-triangle me-2"></i>Cancel Order</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body">
                <input type="hidden" name="action" value="cancel">
                <input type="hidden" name="orderId" value="${order.orderId}">
                <p>Are you sure you want to cancel order <strong>${order.orderNumber}</strong>?</p>
                <c:if test="${order.status == 'APPROVED' || order.status == 'PROCESSING'}">
                    <div class="alert alert-warning small">
                        <i class="bi bi-info-circle me-1"></i>Any materials reserved for this order will be released back into available inventory.
                    </div>
                </c:if>
                <div class="mb-3">
                    <label class="form-label small fw-semibold">Reason for Cancellation</label>
                    <textarea name="reason" class="form-control" rows="2" placeholder="e.g. Client requested postponement..." required></textarea>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Close</button>
                <button type="submit" class="btn btn-danger">Confirm Cancellation</button>
            </div>
        </form>
    </div>
</div>
