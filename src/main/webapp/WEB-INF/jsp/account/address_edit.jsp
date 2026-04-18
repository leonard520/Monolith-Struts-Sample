<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<h2>编辑地址</h2>
<html:form action="/addresses/save.do" method="post">
  <html:hidden property="id"/>
  <table>
    <tr>
      <th><bean:message key="label.addressLabel"/></th>
      <td><html:text property="label" size="20"/></td>
    </tr>
    <tr>
      <th><bean:message key="label.recipientName"/></th>
      <td><html:text property="recipientName" size="30"/></td>
    </tr>
    <tr>
      <th><bean:message key="label.postalCode"/></th>
      <td><html:text property="postalCode" size="10"/></td>
    </tr>
    <tr>
      <th><bean:message key="label.prefecture"/></th>
      <td><html:text property="prefecture" size="20"/></td>
    </tr>
    <tr>
      <th><bean:message key="label.address1"/></th>
      <td><html:text property="address1" size="40"/></td>
    </tr>
    <tr>
      <th><bean:message key="label.address2"/></th>
      <td><html:text property="address2" size="40"/></td>
    </tr>
    <tr>
      <th><bean:message key="label.phone"/></th>
      <td><html:text property="phone" size="20"/></td>
    </tr>
    <tr>
      <th><bean:message key="label.isDefault"/></th>
      <td><html:checkbox property="isDefault"/></td>
    </tr>
  </table>
  <html:token/>
  <html:submit value="保存"/>
</html:form>
