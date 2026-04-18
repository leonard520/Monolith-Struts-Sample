<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%
  String cartIdValue = session != null ? (String) session.getAttribute("cartId") : null;
%>
<h2>结算</h2>
<html:form action="/checkout.do" method="post">
  <html:hidden property="cartId" value="<%= org.apache.struts.util.ResponseUtils.filter(cartIdValue != null ? cartIdValue : \"\") %>"/>
  <table>
    <tr>
      <th><bean:message key="label.couponCode"/></th>
      <td><html:text property="couponCode" size="20"/></td>
    </tr>
    <tr>
      <th><bean:message key="label.paymentMethod"/></th>
      <td>
        <html:select property="paymentMethod">
          <html:option value="CARD">信用卡</html:option>
          <html:option value="COD">货到付款</html:option>
        </html:select>
      </td>
    </tr>
    <tr>
      <th><bean:message key="label.cardNumber"/></th>
      <td><html:text property="cardNumber" size="20"/></td>
    </tr>
    <tr>
      <th><bean:message key="label.cardExpMonth"/></th>
      <td><html:text property="cardExpMonth" size="4"/></td>
    </tr>
    <tr>
      <th><bean:message key="label.cardExpYear"/></th>
      <td><html:text property="cardExpYear" size="6"/></td>
    </tr>
    <tr>
      <th><bean:message key="label.cardCvv"/></th>
      <td><html:password property="cardCvv" size="6"/></td>
    </tr>
    <tr>
      <th><bean:message key="label.billingZip"/></th>
      <td><html:text property="billingZip" size="10"/></td>
    </tr>
    <tr>
      <th><bean:message key="label.usePoints"/></th>
      <td><html:text property="usePoints" size="6"/></td>
    </tr>
  </table>
  <html:token/>
  <html:submit value="确认下单"/>
</html:form>
