<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<h2>利用可能なクーポン</h2>
<c:if test="${empty coupons}"><p>利用可能なクーポンはありません。</p></c:if>
<c:if test="${not empty coupons}">
  <table border="1">
    <tr><th>コード</th><th>タイプ</th><th>割引値</th><th>最低金額</th><th>最大割引</th><th>期限</th></tr>
    <c:forEach items="${coupons}" var="coupon">
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
</c:if>
