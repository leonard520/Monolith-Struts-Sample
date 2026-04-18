<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ja">
<head>
  <meta charset="UTF-8">
  <title>ポイント残高</title>
  <link rel="stylesheet" href="<c:url value='/assets/css/app.css'/>" />
</head>
<body>
  <div class="app-header">
    <jsp:include page="/WEB-INF/jsp/common/header.jsp"/>
  </div>
  <div class="site-container">
    <jsp:include page="/WEB-INF/jsp/common/messages.jsp"/>
    <h2 class="page-title">ポイント残高</h2>
    <c:if test="${not empty pointBalance}">
      <div class="card">
        <p>残高: <c:out value="${pointBalance.balance}"/></p>
        <p>累計獲得: <c:out value="${pointBalance.lifetimeEarned}"/></p>
        <p>累計使用: <c:out value="${pointBalance.lifetimeRedeemed}"/></p>
      </div>
    </c:if>
    <c:if test="${empty pointBalance}">
      <div class="card">
        <p>ポイント情報がありません。</p>
      </div>
    </c:if>
  </div>
  <jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
</body>
</html>
