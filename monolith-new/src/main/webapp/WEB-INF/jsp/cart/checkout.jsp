<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="ja">
<head>
  <meta charset="UTF-8">
  <title>結算</title>
  <link rel="stylesheet" href="<c:url value='/assets/css/app.css'/>" />
</head>
<body>
  <div class="app-header">
    <jsp:include page="/WEB-INF/jsp/common/header.jsp"/>
  </div>
  <div class="site-container">
    <jsp:include page="/WEB-INF/jsp/common/messages.jsp"/>
    <h2 class="page-title">結算</h2>

    <c:if test="${not empty cartItems}">
      <div class="card table-responsive">
        <table>
          <tr>
            <th>商品名</th>
            <th>数量</th>
            <th>単価</th>
          </tr>
          <c:forEach var="item" items="${cartItems}">
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
      </div>
    </c:if>

    <div class="card">
      <form action="<c:url value='/checkout'/>" method="post">
        <input type="hidden" name="cartId" value="<c:out value='${cartId}'/>"/>
        <table>
          <tr>
            <th>クーポンコード</th>
            <td><input type="text" name="couponCode" size="20" value="<c:out value='${checkoutForm.couponCode}'/>"/></td>
          </tr>
          <tr>
            <th>支払方法</th>
            <td>
              <select name="paymentMethod">
                <option value="CARD">信用卡</option>
                <option value="COD">货到付款</option>
              </select>
            </td>
          </tr>
          <tr>
            <th>カード番号</th>
            <td><input type="text" name="cardNumber" size="20" value="<c:out value='${checkoutForm.cardNumber}'/>"/></td>
          </tr>
          <tr>
            <th>有効期限(月)</th>
            <td><input type="text" name="cardExpMonth" size="4" value="<c:out value='${checkoutForm.cardExpMonth}'/>"/></td>
          </tr>
          <tr>
            <th>有効期限(年)</th>
            <td><input type="text" name="cardExpYear" size="6" value="<c:out value='${checkoutForm.cardExpYear}'/>"/></td>
          </tr>
          <tr>
            <th>CVV</th>
            <td><input type="password" name="cardCvv" size="6"/></td>
          </tr>
          <tr>
            <th>請求先郵便番号</th>
            <td><input type="text" name="billingZip" size="10" value="<c:out value='${checkoutForm.billingZip}'/>"/></td>
          </tr>
          <tr>
            <th>ポイント利用</th>
            <td><input type="text" name="usePoints" size="6" value="<c:out value='${checkoutForm.usePoints}'/>"/></td>
          </tr>
        </table>
        <input type="hidden" name="_csrfToken" value="${_csrf.token}"/>
        <button type="submit" class="btn">確認下単</button>
      </form>
    </div>
  </div>
  <jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
</body>
</html>
