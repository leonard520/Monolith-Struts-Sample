<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="ja">
<head>
  <meta charset="UTF-8">
  <title>クーポン一覧</title>
  <link rel="stylesheet" href="<c:url value='/assets/css/app.css'/>" />
</head>
<body>
  <div class="app-header">
    <jsp:include page="/WEB-INF/jsp/common/header.jsp"/>
  </div>
  <div class="site-container">
    <jsp:include page="/WEB-INF/jsp/common/messages.jsp"/>
    <h2 class="page-title">クーポン一覧</h2>
    <c:if test="${empty coupons}">
      <div class="card">現在利用可能なクーポンはありません。</div>
    </c:if>
    <c:if test="${not empty coupons}">
      <div class="card table-responsive">
        <table>
          <tr>
            <th>コード</th>
            <th>種類</th>
            <th>割引値</th>
            <th>最低金額</th>
            <th>最大割引</th>
            <th>有効期限</th>
          </tr>
          <c:forEach var="coupon" items="${coupons}">
            <tr>
              <td><c:out value="${coupon.code}"/></td>
              <td><c:out value="${coupon.couponType}"/></td>
              <td><c:out value="${coupon.discountValue}"/></td>
              <td><c:out value="${coupon.minimumAmount}"/></td>
              <td><c:out value="${coupon.maximumDiscount}"/></td>
              <td><c:out value="${coupon.expiresAt}"/></td>
            </tr>
          </c:forEach>
        </table>
      </div>
    </c:if>
  </div>
  <jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
</body>
</html>
