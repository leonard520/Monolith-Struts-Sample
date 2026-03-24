<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="skishop" uri="http://skishop.com/tags" %>
<h2 class="page-title">カート</h2>
<c:if test="${empty cartItems}">
  <div class="card">カートは空です。</div>
</c:if>
<c:if test="${not empty cartItems}">
  <div class="card table-responsive">
  <table>
    <tr><th>商品名</th><th>数量</th><th>単価</th></tr>
    <c:forEach items="${cartItems}" var="item">
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
<h3>クーポン適用</h3>
<form action="<c:url value='/coupon/apply'/>" method="post">
  <input type="text" name="code" size="20"/>
  <skishop:csrfToken/>
  <button type="submit">適用</button>
</form>
<p><a href="<c:url value='/checkout'/>" class="btn">チェックアウトへ</a></p>
