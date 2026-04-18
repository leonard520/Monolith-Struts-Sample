<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ja">
<head>
  <meta charset="UTF-8">
  <title>商品編集</title>
  <link rel="stylesheet" href="<c:url value='/assets/css/app.css'/>" />
</head>
<body>
  <div class="app-header">
    <jsp:include page="/WEB-INF/jsp/common/header.jsp"/>
  </div>
  <div class="site-container">
    <jsp:include page="/WEB-INF/jsp/common/messages.jsp"/>
    <h2>商品编辑</h2>
    <form action="<c:url value='/admin/product/edit'/>" method="post">
      <input type="hidden" name="id" value="<c:out value='${productForm.id}'/>"/>
      <table>
        <tr>
          <th>ID</th>
          <td><c:out value="${productForm.id}"/></td>
        </tr>
        <tr>
          <th>商品名称</th>
          <td><input type="text" name="name" value="<c:out value='${productForm.name}'/>" size="40"/></td>
        </tr>
        <tr>
          <th>品牌</th>
          <td><input type="text" name="brand" value="<c:out value='${productForm.brand}'/>" size="30"/></td>
        </tr>
        <tr>
          <th>描述</th>
          <td><textarea name="description" cols="40" rows="4"><c:out value="${productForm.description}"/></textarea></td>
        </tr>
        <tr>
          <th>分类</th>
          <td><input type="text" name="categoryId" value="<c:out value='${productForm.categoryId}'/>" size="20"/></td>
        </tr>
        <tr>
          <th>价格</th>
          <td><input type="text" name="price" value="<c:out value='${productForm.price}'/>" size="10"/></td>
        </tr>
        <tr>
          <th>状态</th>
          <td><input type="text" name="status" value="<c:out value='${productForm.status}'/>" size="12"/></td>
        </tr>
        <tr>
          <th>库存数量</th>
          <td><input type="text" name="inventoryQty" value="<c:out value='${productForm.inventoryQty}'/>" size="6"/></td>
        </tr>
      </table>
      <input type="hidden" name="_csrfToken" value="${_csrf.token}"/>
      <button type="submit">更新</button>
    </form>
    <c:if test="${not empty updatedAt}">
      <p>更新时间: <c:out value="${updatedAt}"/></p>
    </c:if>
  </div>
  <jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
</body>
</html>
