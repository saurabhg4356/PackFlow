<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Access Denied | PackFlow</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body class="bg-light d-flex align-items-center justify-content-center min-vh-100">

<div class="card border-0 shadow-sm text-center p-5" style="max-width: 500px; border-radius: 1rem;">
    <div class="mb-3 text-danger">
        <i class="bi bi-shield-x" style="font-size: 4rem;"></i>
    </div>
    <h3 class="fw-bold mb-2">Access Denied (403)</h3>
    <p class="text-muted mb-4">
        You do not possess the required role permissions to view or perform actions on this resource.
    </p>
    <div>
        <a href="${pageContext.request.contextPath}/dashboard" class="btn btn-primary px-4">
            <i class="bi bi-house me-1"></i>Return to Dashboard
        </a>
    </div>
</div>

</body>
</html>
