<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<h2>订单确认</h2>
<logic:present name="order">
  <p>订单号: <bean:write name="order" property="orderNumber" filter="true"/></p>
  <p>状态: <bean:write name="order" property="status" filter="true"/></p>
  <p>总金额: <bean:write name="order" property="totalAmount" filter="true"/></p>
</logic:present>
<logic:notPresent name="order">
  <p>无订单信息。</p>
</logic:notPresent>
<p><html:link page="/orders.do">前往订单历号</html:link></p>
