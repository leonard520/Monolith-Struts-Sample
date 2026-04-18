<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ja">
<head>
  <meta charset="UTF-8">
  <title>注文確認</title>
  <link rel="stylesheet" href="<c:url value='/assets/css/app.css'/>" />
</head>
<body>
  <div class="app-header">
    <jsp:include page="/WEB-INF/jsp/common/header.jsp"/>
  </div>
  <div class="site-container">
    <jsp:include page="/WEB-INF/jsp/common/messages.jsp"/>
    <h2 class="page-title">注文確認</h2>
    <c:if test="${not empty order}">
      <div class="card">
        <p>注文番号: <c:out value="${order.orderNumber}"/></p>
        <p>ステータス: <c:out value="${order.status}"/></p>
        <p>合計金額: <c:out value="${order.totalAmount}"/></p>
      </div>
    </c:if>
    <c:if test="${empty order}">
      <div class="card">
        <p>注文情報がありません。</p>
      </div>
    </c:if>
    <p><a href="<c:url value='/orders'/>" class="btn">注文履歴へ</a></p>
  </div>
  <jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
</body>
</html>
