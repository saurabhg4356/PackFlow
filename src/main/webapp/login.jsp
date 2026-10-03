<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Sign In | PackFlow – Smart Packaging &amp; Order Management</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <style>
        body {
            background: linear-gradient(135deg, #0f172a 0%, #1e293b 100%);
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 1.5rem;
        }
        .login-card {
            background: #ffffff;
            border-radius: 1rem;
            box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.3), 0 10px 10px -5px rgba(0, 0, 0, 0.2);
            overflow: hidden;
            width: 100%;
            max-width: 440px;
        }
        .login-header {
            background-color: #0f172a;
            color: #ffffff;
            padding: 2.25rem 2rem 2rem;
            text-align: center;
        }
    </style>
</head>
<body>

<div class="login-card">
    <div class="login-header">
        <div class="d-inline-flex align-items-center justify-content-center bg-primary rounded-circle mb-3" style="width: 56px; height: 56px;">
            <i class="bi bi-boxes fs-2 text-white"></i>
        </div>
        <h4 class="fw-bold mb-1">PackFlow</h4>
        <p class="text-secondary small mb-0">Smart Packaging &amp; Order Management System</p>
    </div>

    <div class="p-4 p-sm-5">
        <c:if test="${not empty errorMessage}">
            <div class="alert alert-danger alert-dismissible fade show d-flex align-items-center mb-4" role="alert">
                <i class="bi bi-exclamation-triangle-fill me-2 fs-5"></i>
                <div>${errorMessage}</div>
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
        </c:if>

        <c:if test="${not empty successMessage}">
            <div class="alert alert-success alert-dismissible fade show d-flex align-items-center mb-4" role="alert">
                <i class="bi bi-check-circle-fill me-2 fs-5"></i>
                <div>${successMessage}</div>
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
        </c:if>

        <form action="${pageContext.request.contextPath}/login" method="POST">
            <div class="mb-3">
                <label for="email" class="form-label fw-semibold small text-muted">Email Address</label>
                <div class="input-group">
                    <span class="input-group-text bg-light text-muted"><i class="bi bi-envelope"></i></span>
                    <input type="email" class="form-control" id="email" name="email"
                           value="${not empty inputEmail ? inputEmail : 'admin@packflow.com'}" required autofocus>
                </div>
            </div>

            <div class="mb-4">
                <label for="password" class="form-label fw-semibold small text-muted">Password</label>
                <div class="input-group">
                    <span class="input-group-text bg-light text-muted"><i class="bi bi-lock"></i></span>
                    <input type="password" class="form-control" id="password" name="password" value="admin123" required>
                </div>
            </div>

            <button type="submit" class="btn btn-primary w-100 py-2 fw-semibold shadow-sm">
                <i class="bi bi-box-arrow-in-right me-2"></i>Sign In
            </button>
        </form>

        <!-- Test Accounts Helper -->
        <div class="mt-4 pt-3 border-top">
            <div class="fw-semibold text-muted small mb-2 text-center">Quick Demo Credentials:</div>
            <div class="d-flex flex-wrap gap-1 justify-content-center">
                <button type="button" class="btn btn-sm btn-outline-danger" onclick="fillCreds('admin@packflow.com', 'admin123')">
                    Admin
                </button>
                <button type="button" class="btn btn-sm btn-outline-primary" onclick="fillCreds('manager@packflow.com', 'manager123')">
                    Manager
                </button>
                <button type="button" class="btn btn-sm btn-outline-success" onclick="fillCreds('apex@retail.com', 'customer123')">
                    Customer
                </button>
            </div>
        </div>
    </div>
</div>

<script>
    function fillCreds(email, pwd) {
        document.getElementById('email').value = email;
        document.getElementById('password').value = pwd;
    }
</script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
