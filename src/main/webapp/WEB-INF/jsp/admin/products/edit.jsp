<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="skishop" uri="http://skishop.com/tags" %>
<h2>商品編集</h2>
<form action="<c:url value='/admin/product/edit'/>" method="post">
  <input type="hidden" name="id" value="${adminProductForm.id}"/>
  <table>
    <tr><th>ID</th><td><c:out value="${adminProductForm.id}"/></td></tr>
    <tr><th>商品名</th><td><input type="text" name="name" value="${adminProductForm.name}" size="40"/></td></tr>
    <tr><th>ブランド</th><td><input type="text" name="brand" value="${adminProductForm.brand}" size="30"/></td></tr>
    <tr><th>説明</th><td><textarea name="description" cols="40" rows="4">${adminProductForm.description}</textarea></td></tr>
    <tr><th>カテゴリ</th><td><input type="text" name="categoryId" value="${adminProductForm.categoryId}" size="20"/></td></tr>
    <tr><th>価格</th><td><input type="text" name="price" value="${adminProductForm.price}" size="10"/></td></tr>
    <tr><th>状態</th><td><input type="text" name="status" value="${adminProductForm.status}" size="12"/></td></tr>
    <tr><th>在庫数</th><td><input type="text" name="inventoryQty" value="${adminProductForm.inventoryQty}" size="6"/></td></tr>
  </table>
  <skishop:csrfToken/>
  <button type="submit">更新</button>
</form>
<c:if test="${not empty updatedAt}">
  <p>更新日時: <c:out value="${updatedAt}"/></p>
</c:if>
