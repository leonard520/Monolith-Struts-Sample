<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<h2>クーポン管理</h2>
<p><a href="<c:url value='/admin/coupon/edit'/>">クーポンを追加</a></p>
<c:if test="${empty coupons}"><p>クーポンがありません。</p></c:if>
<c:if test="${not empty coupons}">
  <table border="1">
    <tr><th>コード</th><th>種別</th><th>割引</th><th>利用上限</th><th>有効</th><th>編集</th></tr>
    <c:forEach items="${coupons}" var="coupon">
      <tr>
        <td><c:out value="${coupon.code}"/></td>
        <td><c:out value="${coupon.couponType}"/></td>
        <td><c:out value="${coupon.discountValue}"/></td>
        <td><c:out value="${coupon.usageLimit}"/></td>
        <td><c:out value="${coupon.active}"/></td>
        <td><a href="<c:url value='/admin/coupon/edit'/>?code=${coupon.code}">編集</a></td>
      </tr>
    </c:forEach>
  </table>
</c:if>
