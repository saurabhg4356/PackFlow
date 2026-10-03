<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<div id="sidebar" class="d-flex flex-column flex-shrink-0">
    <!-- Brand Logo & Name -->
    <div class="brand-header d-flex align-items-center justify-content-between">
        <a href="${pageContext.request.contextPath}/dashboard" class="d-flex align-items-center text-decoration-none text-white">
            <span class="fs-4 fw-bold me-2 text-primary"><i class="bi bi-boxes"></i></span>
            <div>
                <span class="fs-5 fw-bold tracking-tight text-white">PackFlow</span>
                <span class="badge bg-secondary ms-1 fs-xs" style="font-size: 0.65rem;">v1.0</span>
            </div>
        </a>
    </div>

    <!-- Navigation Menu -->
    <ul class="nav nav-pills flex-column mb-auto py-3">
        <li class="nav-item">
            <a href="${pageContext.request.contextPath}/dashboard" class="nav-link ${activeNav == 'dashboard' ? 'active' : ''}">
                <i class="bi bi-speedometer2"></i> Dashboard
            </a>
        </li>

        <div class="sidebar-heading">Orders & Operations</div>
        <li>
            <a href="${pageContext.request.contextPath}/orders" class="nav-link ${activeNav == 'orders' ? 'active' : ''}">
                <i class="bi bi-cart3"></i> Orders
            </a>
        </li>
        <li>
            <a href="${pageContext.request.contextPath}/orders?action=new" class="nav-link ${activeNav == 'create-order' ? 'active' : ''}">
                <i class="bi bi-plus-circle"></i> Create Request
            </a>
        </li>
        <li>
            <a href="${pageContext.request.contextPath}/products" class="nav-link ${activeNav == 'products' ? 'active' : ''}">
                <i class="bi bi-box-seam"></i> Products
            </a>
        </li>

        <c:if test="${sessionScope.userRole == 'ADMIN' || sessionScope.userRole == 'MANAGER'}">
            <div class="sidebar-heading">Packaging Floor</div>
            <li>
                <a href="${pageContext.request.contextPath}/inventory" class="nav-link ${activeNav == 'inventory' ? 'active' : ''}">
                    <i class="bi bi-archive"></i> Materials Inventory
                </a>
            </li>
            <li>
                <a href="${pageContext.request.contextPath}/tasks" class="nav-link ${activeNav == 'tasks' ? 'active' : ''}">
                    <i class="bi bi-list-task"></i> Packaging Tasks
                </a>
            </li>
            <li>
                <a href="${pageContext.request.contextPath}/quality-checks" class="nav-link ${activeNav == 'quality' ? 'active' : ''}">
                    <i class="bi bi-shield-check"></i> Quality Checks
                </a>
            </li>
            <li>
                <a href="${pageContext.request.contextPath}/dispatches" class="nav-link ${activeNav == 'dispatches' ? 'active' : ''}">
                    <i class="bi bi-truck"></i> Dispatches
                </a>
            </li>

            <div class="sidebar-heading">Master Data & Reports</div>
            <li>
                <a href="${pageContext.request.contextPath}/customers" class="nav-link ${activeNav == 'customers' ? 'active' : ''}">
                    <i class="bi bi-building"></i> Customers
                </a>
            </li>
            <li>
                <a href="${pageContext.request.contextPath}/packaging-services" class="nav-link ${activeNav == 'services' ? 'active' : ''}">
                    <i class="bi bi-gear-wide-connected"></i> Packaging Services
                </a>
            </li>
            <li>
                <a href="${pageContext.request.contextPath}/reports" class="nav-link ${activeNav == 'reports' ? 'active' : ''}">
                    <i class="bi bi-file-earmark-bar-graph"></i> Reports & Analytics
                </a>
            </li>
        </c:if>

        <c:if test="${sessionScope.userRole == 'ADMIN'}">
            <div class="sidebar-heading">Administration</div>
            <li>
                <a href="${pageContext.request.contextPath}/users" class="nav-link ${activeNav == 'users' ? 'active' : ''}">
                    <i class="bi bi-people"></i> User Management
                </a>
            </li>
        </c:if>
    </ul>

    <!-- Bottom Profile Box -->
    <div class="p-3 border-top border-secondary border-opacity-25">
        <div class="d-flex align-items-center text-white">
            <div class="bg-primary rounded-circle d-flex align-items-center justify-content-center me-2 text-white fw-bold" style="width: 36px; height: 36px;">
                ${sessionScope.userName != null ? sessionScope.userName.substring(0, 1).toUpperCase() : 'U'}
            </div>
            <div class="overflow-hidden me-auto">
                <div class="fw-semibold text-truncate small">${sessionScope.userName}</div>
                <div class="text-muted text-truncate" style="font-size: 0.75rem;">${sessionScope.userRole}</div>
            </div>
            <a href="${pageContext.request.contextPath}/logout" class="text-secondary hover-text-white ms-1" title="Sign Out">
                <i class="bi bi-box-arrow-right fs-5 text-light"></i>
            </a>
        </div>
    </div>
</div>
