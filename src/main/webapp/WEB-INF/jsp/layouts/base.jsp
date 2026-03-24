<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <title><c:out value="${pageTitle}" default="Ski Resort Shop"/></title>
    <link rel="stylesheet" href="<c:url value='/assets/css/app.css'/>" />
</head>
<body>
    <div class="app-header">
        <jsp:include page="/WEB-INF/jsp/common/header.jsp"/>
    </div>
    <div class="site-container">
        <jsp:include page="/WEB-INF/jsp/common/messages.jsp"/>
        <jsp:include page="${bodyContent}"/>
    </div>
    <jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
</body>
</html>
