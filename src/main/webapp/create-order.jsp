<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<c:set var="pageTitle" value="Create Packaging Order" />
<c:set var="pageSubtitle" value="Configure products, packaging services and quantities" />
<c:set var="activeNav" value="create-order" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<jsp:include page="/WEB-INF/views/common/sidebar.jsp" />

<div id="main-content" class="d-flex flex-column min-vh-100">
    <jsp:include page="/WEB-INF/views/common/navbar.jsp" />

    <div class="content-wrapper">
        <!-- Breadcrumbs -->
        <nav aria-label="breadcrumb">
            <ol class="breadcrumb mb-2 small">
                <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/dashboard">Dashboard</a></li>
                <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/orders">Orders</a></li>
                <li class="breadcrumb-item active">New Order Request</li>
            </ol>
        </nav>
        <h4 class="fw-bold mb-4">Create Packaging Order Request</h4>

        <c:if test="${not empty errorMessage}">
            <div class="alert alert-danger alert-dismissible fade show mb-4" role="alert">
                <i class="bi bi-exclamation-triangle-fill me-2 fs-5"></i>
                ${errorMessage}
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
        </c:if>

        <form action="${pageContext.request.contextPath}/orders" method="POST" id="orderForm">
            <input type="hidden" name="action" value="create">

            <!-- Card 1: Client / Customer Details -->
            <div class="card card-table mb-4">
                <div class="card-header">
                    <h6 class="fw-bold mb-0"><i class="bi bi-building me-2 text-primary"></i>Customer Information</h6>
                </div>
                <div class="p-4">
                    <div class="row g-3">
                        <c:choose>
                            <c:when test="${sessionScope.userRole == 'CUSTOMER'}">
                                <div class="col-md-6">
                                    <label class="form-label fw-semibold small">Client Account</label>
                                    <input type="text" class="form-control bg-light" value="${customers[0].companyName} (${customers[0].contactPerson})" readonly>
                                    <input type="hidden" name="customerId" value="${customers[0].customerId}">
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div class="col-md-6">
                                    <label for="customerId" class="form-label fw-semibold small">Select Customer <span class="text-danger">*</span></label>
                                    <select name="customerId" id="customerId" class="form-select" required>
                                        <option value="">-- Choose Client Company --</option>
                                        <c:forEach var="c" items="${customers}">
                                            <option value="${c.customerId}">${c.companyName} (${c.contactPerson})</option>
                                        </c:forEach>
                                    </select>
                                </div>
                            </c:otherwise>
                        </c:choose>

                        <div class="col-md-6">
                            <label for="notes" class="form-label fw-semibold small">Packaging Notes &amp; Special Handling</label>
                            <input type="text" name="notes" id="notes" class="form-control" placeholder="e.g. Fragile items, expedited packaging requested...">
                        </div>
                    </div>
                </div>
            </div>

            <!-- Card 2: Packaging Items & Services -->
            <div class="card card-table mb-4">
                <div class="card-header d-flex justify-content-between align-items-center">
                    <h6 class="fw-bold mb-0"><i class="bi bi-box me-2 text-primary"></i>Packaging Line Items</h6>
                    <button type="button" class="btn btn-sm btn-outline-primary" onclick="addRow()">
                        <i class="bi bi-plus-circle me-1"></i>Add Another Product
                    </button>
                </div>
                <div class="p-3">
                    <div class="table-responsive">
                        <table class="table align-middle mb-0" id="itemsTable">
                            <thead class="table-light">
                                <tr>
                                    <th style="width: 35%;">Product <span class="text-danger">*</span></th>
                                    <th style="width: 35%;">Packaging Service <span class="text-danger">*</span></th>
                                    <th style="width: 15%;">Quantity <span class="text-danger">*</span></th>
                                    <th style="width: 10%;">Price Est.</th>
                                    <th style="width: 5%;"></th>
                                </tr>
                            </thead>
                            <tbody id="itemsBody">
                                <tr class="item-row">
                                    <td>
                                        <select name="productId[]" class="form-select form-select-sm product-select" required>
                                            <option value="">-- Choose Product --</option>
                                            <c:forEach var="p" items="${products}">
                                                <option value="${p.productId}">[${p.productCode}] ${p.productName}</option>
                                            </c:forEach>
                                        </select>
                                    </td>
                                    <td>
                                        <select name="serviceId[]" class="form-select form-select-sm service-select" onchange="calculateLineTotal(this)" required>
                                            <option value="" data-price="0">-- Choose Service --</option>
                                            <c:forEach var="s" items="${services}">
                                                <option value="${s.serviceId}" data-price="${s.basePrice}">
                                                    ${s.serviceName} ($<fmt:formatNumber value="${s.basePrice}" minFractionDigits="2"/>)
                                                </option>
                                            </c:forEach>
                                        </select>
                                    </td>
                                    <td>
                                        <input type="number" name="quantity[]" class="form-control form-control-sm qty-input" value="100" min="1" oninput="calculateLineTotal(this)" required>
                                    </td>
                                    <td class="text-end fw-semibold line-total text-dark">
                                        $0.00
                                    </td>
                                    <td class="text-center">
                                        <button type="button" class="btn btn-sm btn-outline-danger border-0" onclick="removeRow(this)" title="Remove item">
                                            <i class="bi bi-trash"></i>
                                        </button>
                                    </td>
                                </tr>
                            </tbody>
                            <tfoot class="border-top">
                                <tr>
                                    <td colspan="3" class="text-end fw-bold">Estimated Total:</td>
                                    <td class="text-end fw-bold fs-5 text-primary" id="estimatedTotal">$0.00</td>
                                    <td></td>
                                </tr>
                            </tfoot>
                        </table>
                    </div>
                    <div class="form-text text-muted small mt-2">
                        <i class="bi bi-info-circle me-1"></i>Final pricing is verified and calculated strictly server-side upon order submission.
                    </div>
                </div>
            </div>

            <!-- Submit Button -->
            <div class="d-flex justify-content-end gap-2 mb-5">
                <a href="${pageContext.request.contextPath}/orders" class="btn btn-outline-secondary">Cancel</a>
                <button type="submit" class="btn btn-primary px-4 shadow-sm">
                    <i class="bi bi-check2-circle me-1"></i>Submit Packaging Order
                </button>
            </div>
        </form>
    </div> <!-- /.content-wrapper -->

    <jsp:include page="/WEB-INF/views/common/footer.jsp" />

<script>
    function calculateLineTotal(element) {
        const row = element.closest('.item-row');
        const serviceSelect = row.querySelector('.service-select');
        const qtyInput = row.querySelector('.qty-input');
        const lineTotalCell = row.querySelector('.line-total');

        const selectedOption = serviceSelect.options[serviceSelect.selectedIndex];
        const price = parseFloat(selectedOption ? selectedOption.getAttribute('data-price') : 0) || 0;
        const qty = parseInt(qtyInput.value) || 0;

        const total = price * qty;
        lineTotalCell.innerText = '$' + total.toFixed(2);

        updateGrandTotal();
    }

    function updateGrandTotal() {
        let grandTotal = 0;
        document.querySelectorAll('.item-row').forEach(row => {
            const serviceSelect = row.querySelector('.service-select');
            const qtyInput = row.querySelector('.qty-input');
            const price = parseFloat(serviceSelect.options[serviceSelect.selectedIndex]?.getAttribute('data-price')) || 0;
            const qty = parseInt(qtyInput.value) || 0;
            grandTotal += (price * qty);
        });
        document.getElementById('estimatedTotal').innerText = '$' + grandTotal.toFixed(2);
    }

    function addRow() {
        const tbody = document.getElementById('itemsBody');
        const firstRow = tbody.querySelector('.item-row');
        const newRow = firstRow.cloneNode(true);

        // Reset values
        newRow.querySelector('.product-select').selectedIndex = 0;
        newRow.querySelector('.service-select').selectedIndex = 0;
        newRow.querySelector('.qty-input').value = '100';
        newRow.querySelector('.line-total').innerText = '$0.00';

        tbody.appendChild(newRow);
        updateGrandTotal();
    }

    function removeRow(btn) {
        const rows = document.querySelectorAll('.item-row');
        if (rows.length > 1) {
            btn.closest('.item-row').remove();
            updateGrandTotal();
        } else {
            alert('An order must have at least one line item.');
        }
    }

    document.addEventListener("DOMContentLoaded", function() {
        updateGrandTotal();
    });
</script>
