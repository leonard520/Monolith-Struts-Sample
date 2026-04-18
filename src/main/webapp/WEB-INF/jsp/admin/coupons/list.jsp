<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<h2>优惠券管理</h2>
<p><html:link page="/admin/coupon/edit.do">添加优惠券</html:link></p>
<logic:empty name="coupons">
  <p>暂无优惠券。</p>
</logic:empty>
<logic:notEmpty name="coupons">
  <table border="1">
    <tr>
      <th>代码</th>
      <th>类型</th>
      <th>折扣</th>
      <th>使用上限</th>
      <th>有效</th>
      <th>编辑</th>
    </tr>
    <logic:iterate id="coupon" name="coupons">
      <tr>
        <td><bean:write name="coupon" property="code" filter="true"/></td>
        <td><bean:write name="coupon" property="couponType" filter="true"/></td>
        <td><bean:write name="coupon" property="discountValue" filter="true"/></td>
        <td><bean:write name="coupon" property="usageLimit" filter="true"/></td>
        <td><bean:write name="coupon" property="active" filter="true"/></td>
        <td>
          <html:link page="/admin/coupon/edit.do" paramId="code" paramName="coupon" paramProperty="code">编辑</html:link>
        </td>
      </tr>
    </logic:iterate>
  </table>
</logic:notEmpty>
