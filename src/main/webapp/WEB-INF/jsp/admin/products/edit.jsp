<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<h2>商品编辑</h2>
<html:form action="/admin/product/edit.do" method="post">
  <html:hidden property="id"/>
  <table>
    <tr>
      <th>ID</th>
      <td><bean:write name="adminProductForm" property="id" filter="true"/></td>
    </tr>
    <tr>
      <th><bean:message key="label.productName"/></th>
      <td><html:text property="name" size="40"/></td>
    </tr>
    <tr>
      <th><bean:message key="label.brand"/></th>
      <td><html:text property="brand" size="30"/></td>
    </tr>
    <tr>
      <th><bean:message key="label.description"/></th>
      <td><html:textarea property="description" cols="40" rows="4"/></td>
    </tr>
    <tr>
      <th><bean:message key="label.category"/></th>
      <td><html:text property="categoryId" size="20"/></td>
    </tr>
    <tr>
      <th><bean:message key="label.productPrice"/></th>
      <td><html:text property="price" size="10"/></td>
    </tr>
    <tr>
      <th><bean:message key="label.productStatus"/></th>
      <td><html:text property="status" size="12"/></td>
    </tr>
    <tr>
      <th><bean:message key="label.inventoryQty"/></th>
      <td><html:text property="inventoryQty" size="6"/></td>
    </tr>
  </table>
  <html:token/>
  <html:submit value="更新"/>
</html:form>
<logic:present name="updatedAt">
  <p>更新时间: <bean:write name="updatedAt" filter="true"/></p>
</logic:present>
