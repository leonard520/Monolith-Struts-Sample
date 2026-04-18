<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<h2>可用优惠券</h2>
<logic:empty name="coupons">
  <p>暂无可用优惠券。</p>
</logic:empty>
<logic:notEmpty name="coupons">
  <table border="1">
    <tr>
      <th>代码</th>
      <th>类型</th>
      <th>折扣値</th>
      <th>最低金额</th>
      <th>最大折扣</th>
      <th>有效期</th>
    </tr>
    <logic:iterate id="coupon" name="coupons">
      <tr>
        <td><bean:write name="coupon" property="code" filter="true"/></td>
        <td><bean:write name="coupon" property="couponType" filter="true"/></td>
        <td><bean:write name="coupon" property="discountValue" filter="true"/></td>
        <td><bean:write name="coupon" property="minimumAmount" filter="true"/></td>
        <td><bean:write name="coupon" property="maximumDiscount" filter="true"/></td>
        <td><bean:write name="coupon" property="expiresAt" filter="true"/></td>
      </tr>
    </logic:iterate>
  </table>
</logic:notEmpty>
