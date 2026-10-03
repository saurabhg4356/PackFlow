<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="pageTitle" value="My Profile" />
<c:set var="pageSubtitle" value="Account credentials and profile settings" />
<c:set var="activeNav" value="profile" />
<jsp:include page="/WEB-INF/views/common/header.jsp" />
<jsp:include page="/WEB-INF/views/common/sidebar.jsp" />

<div id="main-content" class="d-flex flex-column min-vh-100">
    <jsp:include page="/WEB-INF/views/common/navbar.jsp" />

    <div class="content-wrapper">
        <div class="mb-4">
            <nav aria-label="breadcrumb">
                <ol class="breadcrumb mb-1 small">
                    <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/dashboard">Dashboard</a></li>
                    <li class="breadcrumb-item active">Profile</li>
                </ol>
            </nav>
            <h4 class="fw-bold mb-0">User Account Profile</h4>
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

        <div class="row g-4">
            <!-- Profile Details Card -->
            <div class="col-md-6">
                <div class="card card-table h-100">
                    <div class="card-header">
                        <h6 class="fw-bold mb-0"><i class="bi bi-person-badge me-2 text-primary"></i>Profile Details</h6>
                    </div>
                    <div class="p-4">
                        <div class="d-flex align-items-center mb-4">
                            <div class="bg-primary text-white rounded-circle d-flex align-items-center justify-content-center fw-bold fs-3 me-3" style="width: 64px; height: 64px;">
                                ${user.name.substring(0, 1).toUpperCase()}
                            </div>
                            <div>
                                <h5 class="fw-bold mb-0">${user.name}</h5>
                                <div class="text-muted small">${user.email}</div>
                                <span class="badge bg-primary-subtle text-primary border border-primary-subtle mt-1">${user.roleDisplayName}</span>
                            </div>
                        </div>

                        <table class="table table-sm table-borderless">
                            <tr>
                                <th class="text-muted w-35">User ID:</th>
                                <td>#UID-${user.userId}</td>
                            </tr>
                            <tr>
                                <th class="text-muted">Role:</th>
                                <td class="fw-semibold">${user.role}</td>
                            </tr>
                            <tr>
                                <th class="text-muted">Account Status:</th>
                                <td><span class="badge ${user.active ? 'bg-success' : 'bg-secondary'}">${user.status}</span></td>
                            </tr>
                            <tr>
                                <th class="text-muted">Member Since:</th>
                                <td class="small text-muted">${user.createdAt}</td>
                            </tr>
                        </table>
                    </div>
                </div>
            </div>

            <!-- Change Password Card -->
            <div class="col-md-6">
                <div class="card card-table h-100">
                    <div class="card-header">
                        <h6 class="fw-bold mb-0"><i class="bi bi-key me-2 text-warning"></i>Change Password</h6>
                    </div>
                    <div class="p-4">
                        <form action="${pageContext.request.contextPath}/profile" method="POST">
                            <div class="mb-3">
                                <label class="form-label small fw-semibold">Current Password</label>
                                <input type="password" name="oldPassword" class="form-control" required>
                            </div>
                            <div class="mb-3">
                                <label class="form-label small fw-semibold">New Password</label>
                                <input type="password" name="newPassword" class="form-control" minlength="6" required>
                            </div>
                            <div class="mb-4">
                                <label class="form-label small fw-semibold">Confirm New Password</label>
                                <input type="password" name="confirmPassword" class="form-control" minlength="6" required>
                            </div>
                            <button type="submit" class="btn btn-primary">
                                <i class="bi bi-shield-check me-1"></i>Update Password
                            </button>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </div> <!-- /.content-wrapper -->

    <jsp:include page="/WEB-INF/views/common/footer.jsp" />
