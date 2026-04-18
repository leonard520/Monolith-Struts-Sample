<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<h2 class="page-title">商品列表</h2>
<div class="card">
<html:form action="/products.do" method="get" styleClass="form-inline">
  <table>
    <tr>
      <th>关键词</th>
      <td><html:text property="keyword" size="20"/></td>
      <th>分类</th>
      <td>
        <html:select property="categoryId">
          <html:optionsCollection name="categoryOptions" label="label" value="value"/>
        </html:select>
      </td>
      <th>排序</th>
      <td>
        <html:select property="sort">
          <html:option value="">不指定</html:option>
          <html:option value="priceAsc">价格（升序）</html:option>
          <html:option value="priceDesc">价格（降序）</html:option>
          <html:option value="newest">最新</html:option>
        </html:select>
      </td>
    </tr>
  </table>
  <html:token/>
  <html:submit value="搜索"/>
  </table>
</html:form>
</div>

<logic:empty name="productList">
  <div class="card">未找到相关商品。</div>
</logic:empty>
<logic:notEmpty name="productList">
  <div class="card table-responsive">
  <table>
    <tr>
      <th>商品名称</th>
      <th>品牌</th>
      <th>价格</th>
      <th>购物车</th>
    </tr>
    <logic:iterate id="product" name="productList">
      <bean:define id="productId" name="product" property="id" type="java.lang.String"/>
      <tr>
        <td>
          <html:link page="/product.do" paramId="id" paramName="product" paramProperty="id">
            <bean:write name="product" property="name" filter="true"/>
          </html:link>
        </td>
        <td><bean:write name="product" property="brand" filter="true"/></td>
        <td><bean:write name="product" property="priceDisplay" filter="true"/></td>
        <td>
          <html:form action="/cart.do" method="post">
            <html:hidden property="productId" value="<%= org.apache.struts.util.ResponseUtils.filter(productId) %>"/>
            <html:text property="quantity" value="1" size="3"/>
            <html:token/>
            <html:submit value="加入"/>
          </html:form>
        </td>
      </tr>
    </logic:iterate>
  </table>
  </div>
</logic:notEmpty>
