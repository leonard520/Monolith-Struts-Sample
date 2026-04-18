<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<h2>编辑配送方式</h2>
<html:form action="/admin/shipping/edit.do" method="post">
  <html:hidden property="id"/>
  <table>
    <tr>
      <th><bean:message key="label.shippingCode"/></th>
      <td><html:text property="code" size="12"/></td>
    </tr>
    <tr>
      <th><bean:message key="label.shippingName"/></th>
      <td><html:text property="name" size="20"/></td>
    </tr>
    <tr>
      <th><bean:message key="label.shippingFee"/></th>
      <td><html:text property="fee" size="10"/></td>
    </tr>
    <tr>
      <th><bean:message key="label.shippingActive"/></th>
      <td><html:checkbox property="active"/></td>
    </tr>
    <tr>
      <th><bean:message key="label.shippingSort"/></th>
      <td><html:text property="sortOrder" size="4"/></td>
    </tr>
  </table>
  <html:token/>
  <html:submit value="更新"/>
</html:form>
