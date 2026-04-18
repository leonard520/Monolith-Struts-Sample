<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<h2>登录</h2>
<html:form action="/login.do" method="post">
  <table>
    <tr>
      <th><bean:message key="label.email"/></th>
      <td><html:text property="email" size="30"/></td>
    </tr>
    <tr>
      <th><bean:message key="label.password"/></th>
      <td><html:password property="password" size="30"/></td>
    </tr>
  </table>
  <html:token/>
  <html:submit value="登录"/>
</html:form>
<p>
  <html:link page="/register.do">注册会员</html:link>
  |
  <html:link page="/password/forgot.do">忘记密码</html:link>
</p>
