<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="skishop" uri="http://skishop.com/tags" %>
<h2>注文管理</h2>
<c:if test="${empty orders}"><p>注文データがありません。</p></c:if>
<c:if test="${not empty orders}">
  <table border="1">
    <tr><th>注文番号</th><th>状態</th><th>支払状態</th><th>合計</th><th>詳細</th><th>更新</th></tr>
    <c:forEach items="${orders}" var="order">
      <tr>
        <td><c:out value="${order.orderNumber}"/></td>
        <td><c:out value="${order.status}"/></td>
        <td><c:out value="${order.paymentStatus}"/></td>
        <td><c:out value="${order.totalAmount}"/></td>
        <td><a href="<c:url value='/admin/orders/detail'/>?orderId=${order.id}">詳細</a></td>
        <td>
          <form action="<c:url value='/admin/order/update'/>" method="post">
            <input type="hidden" name="orderId" value="${order.id}"/>
            <input type="text" name="status" value="${order.status}" size="10"/>
            <input type="text" name="paymentStatus" value="${order.paymentStatus}" size="10"/>
            <skishop:csrfToken/>
            <button type="submit">更新</button>
          </form>
        </td>
      </tr>
    </c:forEach>
  </table>
</c:if>
