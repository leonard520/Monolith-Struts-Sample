<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ja">
<head>
  <meta charset="UTF-8">
  <title>订单详情</title>
  <link rel="stylesheet" href="<c:url value='/assets/css/app.css'/>" />
</head>
<body>
  <div class="app-header">
    <jsp:include page="/WEB-INF/jsp/common/header.jsp"/>
  </div>
  <div class="site-container">
    <jsp:include page="/WEB-INF/jsp/common/messages.jsp"/>
    <h2>订单详情</h2>
    <c:if test="${not empty order}">
      <p>订单号: <c:out value="${order.orderNumber}"/></p>
      <p>状态: <c:out value="${order.status}"/></p>
      <p>支付状态: <c:out value="${order.paymentStatus}"/></p>
      <p>总金额: <c:out value="${order.totalAmount}"/></p>
      <c:if test="${order.status == 'DELIVERED'}">
        <form action="<c:url value='/admin/order/refund'/>" method="post">
          <input type="hidden" name="id" value="<c:out value='${order.id}'/>"/>
          <input type="hidden" name="_csrfToken" value="${_csrf.token}"/>
          <button type="submit">退款处理</button>
        </form>
      </c:if>
    </c:if>
    <c:if test="${empty order}">
      <p>无订单信息。</p>
    </c:if>
    <c:if test="${not empty orderItems}">
      <h3>商品明细</h3>
      <table border="1">
        <tr>
          <th>商品名称</th>
          <th>数量</th>
          <th>小计</th>
        </tr>
        <c:forEach var="item" items="${orderItems}">
          <tr>
            <td><c:out value="${item.productName}"/></td>
            <td><c:out value="${item.quantity}"/></td>
            <td><c:out value="${item.subtotal}"/></td>
          </tr>
        </c:forEach>
      </table>
    </c:if>
  </div>
  <jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
</body>
</html>
