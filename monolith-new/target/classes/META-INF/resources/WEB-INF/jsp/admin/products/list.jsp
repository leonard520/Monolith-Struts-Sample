<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ja">
<head>
  <meta charset="UTF-8">
  <title>商品管理</title>
  <link rel="stylesheet" href="<c:url value='/assets/css/app.css'/>" />
</head>
<body>
  <div class="app-header">
    <jsp:include page="/WEB-INF/jsp/common/header.jsp"/>
  </div>
  <div class="site-container">
    <jsp:include page="/WEB-INF/jsp/common/messages.jsp"/>
    <h2>商品管理</h2>
    <p><a href="<c:url value='/admin/product/edit'/>">添加商品</a></p>
    <c:if test="${empty products}">
      <p>暂无商品。</p>
    </c:if>
    <c:if test="${not empty products}">
      <table border="1">
        <tr>
          <th>ID</th>
          <th>商品名称</th>
          <th>品牌</th>
          <th>状态</th>
          <th>编辑</th>
          <th>删除</th>
        </tr>
        <c:forEach var="product" items="${products}">
          <tr>
            <td><c:out value="${product.id}"/></td>
            <td><c:out value="${product.name}"/></td>
            <td><c:out value="${product.brand}"/></td>
            <td><c:out value="${product.status}"/></td>
            <td>
              <a href="<c:url value='/admin/product/edit'/>?id=<c:out value='${product.id}'/>">编辑</a>
            </td>
            <td>
              <form action="<c:url value='/admin/product/delete'/>" method="post">
                <input type="hidden" name="id" value="<c:out value='${product.id}'/>"/>
                <input type="hidden" name="_csrfToken" value="${_csrf.token}"/>
                <button type="submit">删除</button>
              </form>
            </td>
          </tr>
        </c:forEach>
      </table>
    </c:if>
  </div>
  <jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
</body>
</html>
