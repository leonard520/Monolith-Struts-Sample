<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="skishop" uri="http://skishop.com/tags" %>
<h2>商品管理</h2>
<p><a href="<c:url value='/admin/product/edit'/>">商品を追加</a></p>
<c:if test="${empty products}"><p>商品がありません。</p></c:if>
<c:if test="${not empty products}">
  <table border="1">
    <tr><th>ID</th><th>商品名</th><th>ブランド</th><th>状態</th><th>編集</th><th>削除</th></tr>
    <c:forEach items="${products}" var="product">
      <tr>
        <td><c:out value="${product.id}"/></td>
        <td><c:out value="${product.name}"/></td>
        <td><c:out value="${product.brand}"/></td>
        <td><c:out value="${product.status}"/></td>
        <td><a href="<c:url value='/admin/product/edit'/>?id=${product.id}">編集</a></td>
        <td>
          <form action="<c:url value='/admin/product/delete'/>" method="post">
            <input type="hidden" name="id" value="${product.id}"/>
            <skishop:csrfToken/>
            <button type="submit">削除</button>
          </form>
        </td>
      </tr>
    </c:forEach>
  </table>
</c:if>
