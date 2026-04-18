<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ja">
<head>
  <meta charset="UTF-8">
  <title>注文詳細</title>
  <link rel="stylesheet" href="<c:url value='/assets/css/app.css'/>" />
</head>
<body>
  <div class="app-header">
    <jsp:include page="/WEB-INF/jsp/common/header.jsp"/>
  </div>
  <div class="site-container">
    <jsp:include page="/WEB-INF/jsp/common/messages.jsp"/>
    <h2 class="page-title">注文詳細</h2>
    <c:if test="${not empty order}">
      <div class="card">
        <p>注文番号: <c:out value="${order.orderNumber}"/></p>
        <p>ステータス: <c:out value="${order.status}"/></p>
        <p>支払ステータス: <c:out value="${order.paymentStatus}"/></p>
        <p>合計金額: <c:out value="${order.totalAmount}"/></p>
      </div>
    </c:if>
    <c:if test="${empty order}">
      <div class="card">
        <p>注文情報がありません。</p>
      </div>
    </c:if>
    <c:if test="${not empty orderItems}">
      <h3>商品明細</h3>
      <div class="card table-responsive">
        <table>
          <tr>
            <th>商品名</th>
            <th>数量</th>
            <th>小計</th>
          </tr>
          <c:forEach var="item" items="${orderItems}">
            <tr>
              <td><c:out value="${item.productName}"/></td>
              <td><c:out value="${item.quantity}"/></td>
              <td><c:out value="${item.subtotal}"/></td>
            </tr>
          </c:forEach>
        </table>
      </div>
    </c:if>
    <p><a href="<c:url value='/orders'/>" class="btn">注文履歴へ戻る</a></p>
  </div>
  <jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
</body>
</html>
