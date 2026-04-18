<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ja">
<head>
  <meta charset="UTF-8">
  <title>订单管理</title>
  <link rel="stylesheet" href="<c:url value='/assets/css/app.css'/>" />
</head>
<body>
  <div class="app-header">
    <jsp:include page="/WEB-INF/jsp/common/header.jsp"/>
  </div>
  <div class="site-container">
    <jsp:include page="/WEB-INF/jsp/common/messages.jsp"/>
    <h2>订单管理</h2>
    <c:if test="${empty orders}">
      <p>暂无订单数据。</p>
    </c:if>
    <c:if test="${not empty orders}">
      <table border="1">
        <tr>
          <th>订单号</th>
          <th>状态</th>
          <th>支付状态</th>
          <th>总计</th>
          <th>详情</th>
          <th>更新</th>
        </tr>
        <c:forEach var="order" items="${orders}">
          <tr>
            <td><c:out value="${order.orderNumber}"/></td>
            <td><c:out value="${order.status}"/></td>
            <td><c:out value="${order.paymentStatus}"/></td>
            <td><c:out value="${order.totalAmount}"/></td>
            <td>
              <a href="<c:url value='/admin/orders/detail'/>?orderId=<c:out value='${order.id}'/>">详情</a>
            </td>
            <td>
              <form action="<c:url value='/admin/order/update'/>" method="post">
                <input type="hidden" name="orderId" value="<c:out value='${order.id}'/>"/>
                <input type="text" name="status" value="<c:out value='${order.status}'/>" size="10"/>
                <input type="text" name="paymentStatus" value="<c:out value='${order.paymentStatus}'/>" size="10"/>
                <input type="hidden" name="_csrfToken" value="${_csrf.token}"/>
                <button type="submit">更新</button>
              </form>
            </td>
          </tr>
        </c:forEach>
      </table>
    </c:if>
  </div>
  <jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
</body>
</html>
