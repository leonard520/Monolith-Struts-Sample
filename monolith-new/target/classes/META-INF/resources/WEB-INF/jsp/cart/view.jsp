<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="ja">
<head>
  <meta charset="UTF-8">
  <title>ショッピングカート</title>
  <link rel="stylesheet" href="<c:url value='/assets/css/app.css'/>" />
</head>
<body>
  <div class="app-header">
    <jsp:include page="/WEB-INF/jsp/common/header.jsp"/>
  </div>
  <div class="site-container">
    <jsp:include page="/WEB-INF/jsp/common/messages.jsp"/>
    <c:if test="${not empty errorMessage}">
      <div class="messages">
        <p><c:out value="${errorMessage}"/></p>
      </div>
    </c:if>
    <h2 class="page-title">ショッピングカート</h2>
    <c:if test="${empty cartItems}">
      <div class="card">カートは空です。</div>
    </c:if>
    <c:if test="${not empty cartItems}">
      <div class="card table-responsive">
        <table>
          <tr>
            <th>商品名</th>
            <th>数量</th>
            <th>単価</th>
          </tr>
          <c:forEach var="item" items="${cartItems}">
            <tr>
              <td><c:out value="${item.productName}"/></td>
              <td><c:out value="${item.quantity}"/></td>
              <td><c:out value="${item.unitPrice}"/></td>
            </tr>
          </c:forEach>
        </table>
      </div>
      <div class="card cart-summary">
        <p>小計: <strong><c:out value="${cartSubtotal}"/></strong></p>
        <c:if test="${not empty coupon}">
          <p>クーポン: <c:out value="${coupon.code}"/></p>
          <p>割引額: <c:out value="${discountAmount}"/></p>
        </c:if>
      </div>
    </c:if>

    <h3>クーポンを使用</h3>
    <div class="card">
      <form action="<c:url value='/coupons/apply'/>" method="post">
        <input type="text" name="code" size="20" placeholder="クーポンコード"/>
        <input type="hidden" name="_csrfToken" value="${_csrf.token}"/>
        <button type="submit" class="btn">適用</button>
      </form>
    </div>

    <p><a href="<c:url value='/checkout'/>" class="btn">レジに進む</a></p>
  </div>
  <jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
</body>
</html>
