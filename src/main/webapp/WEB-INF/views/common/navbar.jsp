<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<nav class="top-navbar d-flex align-items-center justify-content-between">
    <div class="d-flex align-items-center">
        <h5 class="mb-0 fw-bold text-dark">${not empty pageTitle ? pageTitle : 'PackFlow'}</h5>
        <c:if test="${not empty pageSubtitle}">
            <span class="text-muted ms-2 ps-2 border-start small">${pageSubtitle}</span>
        </c:if>
    </div>

    <div class="d-flex align-items-center gap-3">
        <!-- Role Indicator Badge -->
        <c:choose>
            <c:when test="${sessionScope.userRole == 'ADMIN'}">
                <span class="badge bg-danger-subtle text-danger border border-danger-subtle px-2 py-1">
                    <i class="bi bi-shield-lock me-1"></i>ADMIN
                </span>
            </c:when>
            <c:when test="${sessionScope.userRole == 'MANAGER'}">
                <span class="badge bg-primary-subtle text-primary border border-primary-subtle px-2 py-1">
                    <i class="bi bi-person-gear me-1"></i>MANAGER
                </span>
            </c:when>
            <c:otherwise>
                <span class="badge bg-success-subtle text-success border border-success-subtle px-2 py-1">
                    <i class="bi bi-person-check me-1"></i>CUSTOMER
                </span>
            </c:otherwise>
        </c:choose>

        <!-- User Dropdown Menu -->
        <div class="dropdown">
            <button class="btn btn-sm btn-outline-secondary dropdown-toggle d-flex align-items-center gap-2" type="button" data-bs-toggle="dropdown">
                <i class="bi bi-person-circle"></i>
                <span class="d-none d-md-inline">${sessionScope.userName}</span>
            </button>
            <ul class="dropdown-menu dropdown-menu-end shadow-sm">
                <li><h6 class="dropdown-header">${sessionScope.userEmail}</h6></li>
                <li><a class="dropdown-item" href="${pageContext.request.contextPath}/profile"><i class="bi bi-person me-2"></i>My Profile</a></li>
                <li><a class="dropdown-item" href="${pageContext.request.contextPath}/dashboard"><i class="bi bi-speedometer2 me-2"></i>Dashboard</a></li>
                <li><hr class="dropdown-divider"></li>
                <li><a class="dropdown-item text-danger" href="${pageContext.request.contextPath}/logout"><i class="bi bi-box-arrow-right me-2"></i>Sign Out</a></li>
            </ul>
        </div>
    </div>
</nav>
