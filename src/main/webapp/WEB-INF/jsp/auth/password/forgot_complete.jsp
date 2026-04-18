<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<h2>密码找回已提交</h2>
<p>重置信息已发送至您的邮筱。</p>
<logic:present name="resetToken">
  <p>开发用令牌: <bean:write name="resetToken" filter="true"/></p>
</logic:present>
<p><html:link page="/password/reset.do">前往重置密码</html:link></p>
