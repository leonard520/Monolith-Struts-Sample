<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="ja">
<head>
  <meta charset="UTF-8">
  <title>ホーム</title>
  <link rel="stylesheet" href="<c:url value='/assets/css/app.css'/>" />
</head>
<body>
  <div class="app-header">
    <jsp:include page="/WEB-INF/jsp/common/header.jsp"/>
  </div>
  <div class="site-container">
    <jsp:include page="/WEB-INF/jsp/common/messages.jsp"/>
    <div class="hero">
      <h1>Ski Resort Shop へようこそ</h1>
      <p>最新のスキー用品をご覧ください。</p>
      <div class="hero-actions">
        <a href="<c:url value='/products'/>" class="btn">商品を見る</a>
      </div>
    </div>

    <h2 class="page-title">おすすめスキー用品</h2>
    <c:if test="${not empty featuredProducts}">
      <div class="products-grid">
        <c:forEach var="product" items="${featuredProducts}">
          <div class="product-card">
            <div class="name">
              <a href="<c:url value='/product'/>?id=${product.id}">
                <c:out value="${product.name}"/>
              </a>
            </div>
            <div class="price">¥<c:out value="${product.price}"/></div>
            <div class="tags">
              <c:if test="${not empty product.brand}"><span class="tag"><c:out value="${product.brand}"/></span></c:if>
            </div>
            <div>
              <a href="<c:url value='/product'/>?id=${product.id}" class="btn">詳細を見る</a>
            </div>
          </div>
        </c:forEach>
      </div>
    </c:if>
    <c:if test="${empty featuredProducts}">
      <p>おすすめ商品は準備中です。</p>
    </c:if>
  </div>
  <jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
</body>
</html>
