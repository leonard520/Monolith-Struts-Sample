<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<h2>商品管理</h2>
<p><html:link page="/admin/product/edit.do">添加商品</html:link></p>
<logic:empty name="products">
  <p>暂无商品。</p>
</logic:empty>
<logic:notEmpty name="products">
  <table border="1">
    <tr>
      <th>ID</th>
      <th>商品名称</th>
      <th>品牌</th>
      <th>状态</th>
      <th>编辑</th>
      <th>删除</th>
    </tr>
    <logic:iterate id="product" name="products">
      <tr>
        <td><bean:write name="product" property="id" filter="true"/></td>
        <td><bean:write name="product" property="name" filter="true"/></td>
        <td><bean:write name="product" property="brand" filter="true"/></td>
        <td><bean:write name="product" property="status" filter="true"/></td>
        <td>
          <html:link page="/admin/product/edit.do" paramId="id" paramName="product" paramProperty="id">编辑</html:link>
        </td>
        <td>
          <html:form action="/admin/product/delete.do" method="post">
            <html:hidden name="product" property="id"/>
            <html:token/>
            <html:submit value="删除"/>
          </html:form>
        </td>
      </tr>
    </logic:iterate>
  </table>
</logic:notEmpty>
