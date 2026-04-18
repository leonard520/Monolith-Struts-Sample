<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<h2>重置密码</h2>
<html:form action="/password/reset.do" method="post">
  <table>
    <tr>
      <th><bean:message key="label.token"/></th>
      <td><html:text property="token" size="40"/></td>
    </tr>
    <tr>
      <th><bean:message key="label.password"/></th>
      <td><html:password property="password" size="30"/></td>
    </tr>
    <tr>
      <th><bean:message key="label.passwordConfirm"/></th>
      <td><html:password property="passwordConfirm" size="30"/></td>
    </tr>
  </table>
  <html:token/>
  <html:submit value="确认重置"/>
</html:form>
