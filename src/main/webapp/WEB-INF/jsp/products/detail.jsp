<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<h2>商品详情</h2>
<logic:present name="product">
  <bean:define id="productId" name="product" property="id" type="java.lang.String"/>
  <h3><bean:write name="product" property="name" filter="true"/></h3>
  <p>品牌: <bean:write name="product" property="brand" filter="true"/></p>
  <p>SKU: <bean:write name="product" property="sku" filter="true"/></p>
  <p>分类: <bean:write name="product" property="categoryId" filter="true"/></p>
  <p>描述: <bean:write name="product" property="description" filter="true"/></p>
  <html:form action="/cart.do" method="post">
    <html:hidden property="productId" value="<%= org.apache.struts.util.ResponseUtils.filter(productId) %>"/>
    <html:text property="quantity" value="1" size="3"/>
    <html:token/>
    <html:submit value="加入购物车"/>
  </html:form>
</logic:present>
<logic:notPresent name="product">
  <p>无法获取商品信息。</p>
</logic:notPresent>
