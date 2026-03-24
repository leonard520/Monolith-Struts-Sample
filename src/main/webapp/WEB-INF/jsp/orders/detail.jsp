<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<h2>注文詳細</h2>
<c:if test="${not empty order}">
  <p>注文番号: <c:out value="${order.orderNumber}"/></p>
  <p>状態: <c:out value="${order.status}"/></p>
  <p>支払状態: <c:out value="${order.paymentStatus}"/></p>
  <p>合計金額: <c:out value="${order.totalAmount}"/></p>
</c:if>
<c:if test="${empty order}"><p>注文情報がありません。</p></c:if>
<c:if test="${not empty orderItems}">
  <h3>商品明細</h3>
  <table border="1">
    <tr><th>商品名</th><th>数量</th><th>小計</th></tr>
    <c:forEach items="${orderItems}" var="item">
      <tr>
        <td><c:out value="${item.productName}"/></td>
        <td><c:out value="${item.quantity}"/></td>
        <td><c:out value="${item.subtotal}"/></td>
      </tr>
    </c:forEach>
  </table>
</c:if>
