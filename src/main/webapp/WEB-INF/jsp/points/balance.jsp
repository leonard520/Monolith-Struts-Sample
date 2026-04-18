<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<h2>积分余额</h2>
<logic:present name="pointBalance">
  <p>余额: <bean:write name="pointBalance" property="balance" filter="true"/></p>
  <p>累计获得: <bean:write name="pointBalance" property="lifetimeEarned" filter="true"/></p>
  <p>累计使用: <bean:write name="pointBalance" property="lifetimeRedeemed" filter="true"/></p>
</logic:present>
<logic:notPresent name="pointBalance">
  <p>无积分信息。</p>
</logic:notPresent>
