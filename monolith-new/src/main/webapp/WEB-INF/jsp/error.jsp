<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ja">
<head>
  <meta charset="UTF-8">
  <title>エラー</title>
  <link rel="stylesheet" href="<c:url value='/assets/css/app.css'/>" />
</head>
<body>
  <div class="app-header">
    <jsp:include page="/WEB-INF/jsp/common/header.jsp"/>
  </div>
  <div class="site-container">
    <h2>エラーが発生しました</h2>
    <c:if test="${not empty errorMessage}">
      <p><c:out value="${errorMessage}"/></p>
    </c:if>
    <c:if test="${empty errorMessage}">
      <p>予期しないエラーが発生しました。後ほどもう一度お試しください。</p>
    </c:if>
    <p><a href="<c:url value='/home'/>">ホームに戻る</a></p>
  </div>
  <jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
</body>
</html>
