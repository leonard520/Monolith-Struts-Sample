<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="ja">
<head>
  <meta charset="UTF-8">
  <title>商品詳細</title>
  <link rel="stylesheet" href="<c:url value='/assets/css/app.css'/>" />
</head>
<body>
  <div class="app-header">
    <jsp:include page="/WEB-INF/jsp/common/header.jsp"/>
  </div>
  <div class="site-container">
    <jsp:include page="/WEB-INF/jsp/common/messages.jsp"/>
    <h2 class="page-title">商品詳細</h2>
    <c:if test="${not empty product}">
      <div class="card">
        <h3><c:out value="${product.name}"/></h3>
        <p>ブランド: <c:out value="${product.brand}"/></p>
        <p>SKU: <c:out value="${product.sku}"/></p>
        <p>カテゴリ: <c:out value="${product.categoryId}"/></p>
        <p>説明: <c:out value="${product.description}"/></p>
        <p>価格: <c:out value="${product.priceDisplay}"/></p>
        <form action="<c:url value='/cart'/>" method="post">
          <input type="hidden" name="productId" value="<c:out value='${product.id}'/>"/>
          <input type="text" name="quantity" value="1" size="3"/>
          <input type="hidden" name="_csrfToken" value="${_csrf.token}"/>
          <button type="submit" class="btn">カートに追加</button>
        </form>
      </div>
    </c:if>
    <c:if test="${empty product}">
      <div class="card">
        <p>商品情報を取得できませんでした。</p>
      </div>
    </c:if>
    <p><a href="<c:url value='/products'/>">商品一覧に戻る</a></p>
  </div>
  <jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
</body>
</html>
