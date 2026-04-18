<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ja">
<head>
  <meta charset="UTF-8">
  <title>优惠券管理</title>
  <link rel="stylesheet" href="<c:url value='/assets/css/app.css'/>" />
</head>
<body>
  <div class="app-header">
    <jsp:include page="/WEB-INF/jsp/common/header.jsp"/>
  </div>
  <div class="site-container">
    <jsp:include page="/WEB-INF/jsp/common/messages.jsp"/>
    <h2>优惠券管理</h2>
    <p><a href="<c:url value='/admin/coupon/edit'/>">添加优惠券</a></p>
    <c:if test="${empty coupons}">
      <p>暂无优惠券。</p>
    </c:if>
    <c:if test="${not empty coupons}">
      <table border="1">
        <tr>
          <th>代码</th>
          <th>类型</th>
          <th>折扣</th>
          <th>使用上限</th>
          <th>有效</th>
          <th>编辑</th>
        </tr>
        <c:forEach var="coupon" items="${coupons}">
          <tr>
            <td><c:out value="${coupon.code}"/></td>
            <td><c:out value="${coupon.couponType}"/></td>
            <td><c:out value="${coupon.discountValue}"/></td>
            <td><c:out value="${coupon.usageLimit}"/></td>
            <td><c:out value="${coupon.active}"/></td>
            <td>
              <a href="<c:url value='/admin/coupon/edit'/>?code=<c:out value='${coupon.code}'/>">编辑</a>
            </td>
          </tr>
        </c:forEach>
      </table>
    </c:if>
  </div>
  <jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
</body>
</html>
