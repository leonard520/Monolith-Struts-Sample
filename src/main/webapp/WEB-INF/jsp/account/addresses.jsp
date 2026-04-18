<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<h2>地址簿</h2>
<p><html:link page="/addresses/save.do">添加新地址</html:link></p>
<logic:empty name="addresses">
  <p>暂无已保存的地址。</p>
</logic:empty>
<logic:notEmpty name="addresses">
  <table border="1">
    <tr>
      <th>标签</th>
      <th>收件人</th>
      <th>地址</th>
      <th>电话</th>
      <th>默认</th>
    </tr>
    <logic:iterate id="address" name="addresses">
      <tr>
        <td><bean:write name="address" property="label" filter="true"/></td>
        <td><bean:write name="address" property="recipientName" filter="true"/></td>
        <td>
          <bean:write name="address" property="postalCode" filter="true"/>
          <bean:write name="address" property="prefecture" filter="true"/>
          <bean:write name="address" property="address1" filter="true"/>
          <bean:write name="address" property="address2" filter="true"/>
        </td>
        <td><bean:write name="address" property="phone" filter="true"/></td>
        <td>
          <logic:equal name="address" property="default" value="true"><bean:message key="label.yes"/></logic:equal>
          <logic:notEqual name="address" property="default" value="true">-</logic:notEqual>
        </td>
      </tr>
    </logic:iterate>
  </table>
</logic:notEmpty>
