<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<h2>编辑优惠券</h2>
<html:form action="/admin/coupon/edit.do" method="post">
  <html:hidden property="id"/>
  <table>
    <tr>
      <th><bean:message key="label.couponCode"/></th>
      <td><html:text property="code" size="20"/></td>
    </tr>
    <tr>
      <th><bean:message key="label.campaignId"/></th>
      <td><html:text property="campaignId" size="20"/></td>
    </tr>
    <tr>
      <th><bean:message key="label.couponType"/></th>
      <td><html:text property="couponType" size="12"/></td>
    </tr>
    <tr>
      <th><bean:message key="label.discountType"/></th>
      <td><html:text property="discountType" size="12"/></td>
    </tr>
    <tr>
      <th><bean:message key="label.discountValue"/></th>
      <td><html:text property="discountValue" size="10"/></td>
    </tr>
    <tr>
      <th><bean:message key="label.minimumAmount"/></th>
      <td><html:text property="minimumAmount" size="10"/></td>
    </tr>
    <tr>
      <th><bean:message key="label.maximumDiscount"/></th>
      <td><html:text property="maximumDiscount" size="10"/></td>
    </tr>
    <tr>
      <th><bean:message key="label.usageLimit"/></th>
      <td><html:text property="usageLimit" size="6"/></td>
    </tr>
    <tr>
      <th><bean:message key="label.couponActive"/></th>
      <td><html:checkbox property="active"/></td>
    </tr>
    <tr>
      <th><bean:message key="label.expiresAt"/></th>
      <td><html:text property="expiresAt" size="12"/></td>
    </tr>
  </table>
  <html:token/>
  <html:submit value="更新"/>
</html:form>
