<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="pageTitle" value="User Management" />
<c:set var="pageSubtitle" value="System security credentials, access levels, and role management" />
<c:set var="activeNav" value="users" />
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
                        <li class="breadcrumb-item active">Users</li>
                    </ol>
                </nav>
                <h4 class="fw-bold mb-0">System Users &amp; Roles</h4>
            </div>
            <div>
                <button type="button" class="btn btn-primary shadow-sm" onclick="openUserModal()">
                    <i class="bi bi-person-plus me-1"></i>Create New User
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

        <!-- Users Table -->
        <div class="card card-table">
            <div class="table-responsive">
                <table class="table table-hover mb-0">
                    <thead>
                        <tr>
                            <th>User ID</th>
                            <th>Full Name</th>
                            <th>Email Address</th>
                            <th>System Role</th>
                            <th>Account Status</th>
                            <th>Registered Date</th>
                            <th class="text-end">Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="u" items="${users}">
                            <tr>
                                <td><span class="badge bg-light text-dark border">#UID-${u.userId}</span></td>
                                <td class="fw-bold text-dark">${u.name}</td>
                                <td>${u.email}</td>
                                <td>
                                    <c:choose>
                                        <c:when test="${u.role == 'ADMIN'}">
                                            <span class="badge bg-danger-subtle text-danger border border-danger-subtle">ADMIN</span>
                                        </c:when>
                                        <c:when test="${u.role == 'MANAGER'}">
                                            <span class="badge bg-primary-subtle text-primary border border-primary-subtle">MANAGER</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge bg-success-subtle text-success border border-success-subtle">CUSTOMER</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <span class="badge ${u.active ? 'bg-success' : 'bg-secondary'}">${u.status}</span>
                                </td>
                                <td class="small text-muted">${u.createdAt}</td>
                                <td class="text-end">
                                    <button type="button" class="btn btn-sm btn-outline-secondary me-1"
                                            onclick="editUser('${u.userId}', '${u.name}', '${u.email}', '${u.role}', '${u.status}')">
                                        <i class="bi bi-pencil"></i>
                                    </button>
                                    <c:if test="${u.userId != sessionScope.userId}">
                                        <form action="${pageContext.request.contextPath}/users" method="POST" class="d-inline" onsubmit="return confirm('Delete user ${u.name}?');">
                                            <input type="hidden" name="action" value="delete">
                                            <input type="hidden" name="userId" value="${u.userId}">
                                            <button type="submit" class="btn btn-sm btn-outline-danger">
                                                <i class="bi bi-trash"></i>
                                            </button>
                                        </form>
                                    </c:if>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>
    </div> <!-- /.content-wrapper -->

    <jsp:include page="/WEB-INF/views/common/footer.jsp" />

<!-- User Add/Edit Modal -->
<div class="modal fade" id="userModal" tabindex="-1">
    <div class="modal-dialog">
        <form action="${pageContext.request.contextPath}/users" method="POST" class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title fw-bold" id="userModalTitle">Create User</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body">
                <input type="hidden" name="userId" id="modalUserId" value="">
                <div class="mb-3">
                    <label class="form-label small fw-semibold">Full Name <span class="text-danger">*</span></label>
                    <input type="text" name="name" id="modalUserName" class="form-control" placeholder="e.g. John Doe" required>
                </div>
                <div class="mb-3">
                    <label class="form-label small fw-semibold">Email Address <span class="text-danger">*</span></label>
                    <input type="email" name="email" id="modalUserEmail" class="form-control" placeholder="user@packflow.com" required>
                </div>
                <div class="row g-2 mb-3">
                    <div class="col-6">
                        <label class="form-label small fw-semibold">System Role <span class="text-danger">*</span></label>
                        <select name="role" id="modalUserRole" class="form-select" required>
                            <option value="ADMIN">ADMIN</option>
                            <option value="MANAGER">MANAGER</option>
                            <option value="CUSTOMER">CUSTOMER</option>
                        </select>
                    </div>
                    <div class="col-6">
                        <label class="form-label small fw-semibold">Account Status</label>
                        <select name="status" id="modalUserStatus" class="form-select">
                            <option value="ACTIVE">ACTIVE</option>
                            <option value="INACTIVE">INACTIVE</option>
                        </select>
                    </div>
                </div>
                <div class="mb-3" id="pwdGroup">
                    <label class="form-label small fw-semibold">Password <span class="text-danger" id="pwdReqStar">*</span></label>
                    <input type="password" name="password" id="modalUserPassword" class="form-control" placeholder="Minimum 6 characters">
                    <div class="form-text small" id="pwdHelp">Leave blank when editing if you do not wish to change password.</div>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
                <button type="submit" class="btn btn-primary">Save User Account</button>
            </div>
        </form>
    </div>
</div>

<script>
    const userModal = new bootstrap.Modal(document.getElementById('userModal'));

    function openUserModal() {
        document.getElementById('userModalTitle').innerText = 'Create User Account';
        document.getElementById('modalUserId').value = '';
        document.getElementById('modalUserName').value = '';
        document.getElementById('modalUserEmail').value = '';
        document.getElementById('modalUserRole').value = 'MANAGER';
        document.getElementById('modalUserStatus').value = 'ACTIVE';
        document.getElementById('modalUserPassword').required = true;
        document.getElementById('pwdReqStar').style.display = 'inline';
        userModal.show();
    }

    function editUser(id, name, email, role, status) {
        document.getElementById('userModalTitle').innerText = 'Edit User Account';
        document.getElementById('modalUserId').value = id;
        document.getElementById('modalUserName').value = name;
        document.getElementById('modalUserEmail').value = email;
        document.getElementById('modalUserRole').value = role;
        document.getElementById('modalUserStatus').value = status;
        document.getElementById('modalUserPassword').required = false;
        document.getElementById('pwdReqStar').style.display = 'none';
        userModal.show();
    }
</script>
