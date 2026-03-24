<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="skishop" uri="http://skishop.com/tags" %>
<h2>配送方法編集</h2>
<form action="<c:url value='/admin/shipping/edit'/>" method="post">
  <input type="hidden" name="id" value="${adminShippingMethodForm.id}"/>
  <table>
    <tr><th>コード</th><td><input type="text" name="code" value="${adminShippingMethodForm.code}" size="12"/></td></tr>
    <tr><th>名称</th><td><input type="text" name="name" value="${adminShippingMethodForm.name}" size="20"/></td></tr>
    <tr><th>送料</th><td><input type="text" name="fee" value="${adminShippingMethodForm.fee}" size="10"/></td></tr>
    <tr><th>有効</th><td><input type="checkbox" name="active" value="true" ${adminShippingMethodForm.active ? 'checked' : ''}/></td></tr>
    <tr><th>並び順</th><td><input type="text" name="sortOrder" value="${adminShippingMethodForm.sortOrder}" size="4"/></td></tr>
  </table>
  <skishop:csrfToken/>
  <button type="submit">更新</button>
</form>
