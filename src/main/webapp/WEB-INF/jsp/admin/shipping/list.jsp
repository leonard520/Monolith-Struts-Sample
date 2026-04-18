<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<h2>配送方式管理</h2>
<p><html:link page="/admin/shipping/edit.do">添加配送方式</html:link></p>
<logic:empty name="shippingMethods">
  <p>暂无配送方式。</p>
</logic:empty>
<logic:notEmpty name="shippingMethods">
  <table border="1">
    <tr>
      <th>代码</th>
      <th>名称</th>
      <th>运费</th>
      <th>有效</th>
      <th>排序</th>
      <th>编辑</th>
    </tr>
    <logic:iterate id="method" name="shippingMethods">
      <tr>
        <td><bean:write name="method" property="code" filter="true"/></td>
        <td><bean:write name="method" property="name" filter="true"/></td>
        <td><bean:write name="method" property="fee" filter="true"/></td>
        <td><bean:write name="method" property="active" filter="true"/></td>
        <td><bean:write name="method" property="sortOrder" filter="true"/></td>
        <td>
          <html:link page="/admin/shipping/edit.do" paramId="code" paramName="method" paramProperty="code">编辑</html:link>
        </td>
      </tr>
    </logic:iterate>
  </table>
</logic:notEmpty>
