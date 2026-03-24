<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="skishop" uri="http://skishop.com/tags" %>
<h2>チェックアウト</h2>
<form action="<c:url value='/checkout'/>" method="post">
  <input type="hidden" name="cartId" value="${sessionScope.cartId}"/>
  <table>
    <tr><th>クーポンコード</th><td><input type="text" name="couponCode" size="20"/></td></tr>
    <tr><th>支払方法</th><td>
      <select name="paymentMethod">
        <option value="CARD">カード</option>
        <option value="COD">代金引換</option>
      </select>
    </td></tr>
    <tr><th>カード番号</th><td><input type="text" name="cardNumber" size="20"/></td></tr>
    <tr><th>有効期限(月)</th><td><input type="text" name="cardExpMonth" size="4"/></td></tr>
    <tr><th>有効期限(年)</th><td><input type="text" name="cardExpYear" size="6"/></td></tr>
    <tr><th>CVV</th><td><input type="password" name="cardCvv" size="6"/></td></tr>
    <tr><th>請求先郵便番号</th><td><input type="text" name="billingZip" size="10"/></td></tr>
    <tr><th>ポイント利用</th><td><input type="text" name="usePoints" size="6"/></td></tr>
  </table>
  <skishop:csrfToken/>
  <button type="submit">注文確定</button>
</form>
