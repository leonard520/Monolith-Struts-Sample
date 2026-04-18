<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<h2>订单详情</h2>
<logic:present name="order">
  <p>订单号: <bean:write name="order" property="orderNumber" filter="true"/></p>
  <p>状态: <bean:write name="order" property="status" filter="true"/></p>
  <p>支付状态: <bean:write name="order" property="paymentStatus" filter="true"/></p>
  <p>总金额: <bean:write name="order" property="totalAmount" filter="true"/></p>
  <logic:equal name="order" property="status" value="DELIVERED">
    <html:form action="/admin/order/refund.do" method="post">
      <html:hidden name="order" property="id"/>
      <html:token/>
      <html:submit value="退款处理"/>
    </html:form>
  </logic:equal>
</logic:present>
<logic:notPresent name="order">
  <p>无订单信息。</p>
</logic:notPresent>
<logic:present name="orderItems">
  <h3>商品明细</h3>
  <table border="1">
    <tr>
      <th>商品名称</th>
      <th>数量</th>
      <th>小计</th>
    </tr>
    <logic:iterate id="item" name="orderItems">
      <tr>
        <td><bean:write name="item" property="productName" filter="true"/></td>
        <td><bean:write name="item" property="quantity" filter="true"/></td>
        <td><bean:write name="item" property="subtotal" filter="true"/></td>
      </tr>
    </logic:iterate>
  </table>
</logic:present>
