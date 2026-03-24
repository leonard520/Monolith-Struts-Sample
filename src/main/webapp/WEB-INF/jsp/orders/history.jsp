<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="skishop" uri="http://skishop.com/tags" %>
<h2>注文履歴</h2>
<c:if test="${empty orders}"><p>注文履歴がありません。</p></c:if>
<c:if test="${not empty orders}">
  <table border="1">
    <tr><th>注文番号</th><th>状態</th><th>合計</th><th>操作</th></tr>
    <c:forEach items="${orders}" var="order">
      <tr>
        <td><c:out value="${order.orderNumber}"/></td>
        <td><c:out value="${order.status}"/></td>
        <td><c:out value="${order.totalAmount}"/></td>
        <td>
          <a href="<c:url value='/orders/detail'/>?orderId=${order.id}">詳細</a><br/>
          <form action="<c:url value='/orders/cancel'/>" method="post">
            <input type="hidden" name="orderId" value="${order.id}"/>
            <skishop:csrfToken/>
            <button type="submit">キャンセル</button>
          </form>
          <form action="<c:url value='/orders/return'/>" method="post">
            <input type="hidden" name="orderId" value="${order.id}"/>
            <skishop:csrfToken/>
            <button type="submit">返品</button>
          </form>
        </td>
      </tr>
    </c:forEach>
  </table>
</c:if>
