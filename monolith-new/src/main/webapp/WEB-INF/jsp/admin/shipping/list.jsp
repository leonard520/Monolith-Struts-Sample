<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ja">
<head>
  <meta charset="UTF-8">
  <title>配送方式管理</title>
  <link rel="stylesheet" href="<c:url value='/assets/css/app.css'/>" />
</head>
<body>
  <div class="app-header">
    <jsp:include page="/WEB-INF/jsp/common/header.jsp"/>
  </div>
  <div class="site-container">
    <jsp:include page="/WEB-INF/jsp/common/messages.jsp"/>
    <h2>配送方式管理</h2>
    <p><a href="<c:url value='/admin/shipping/edit'/>">添加配送方式</a></p>
    <c:if test="${empty shippingMethods}">
      <p>暂无配送方式。</p>
    </c:if>
    <c:if test="${not empty shippingMethods}">
      <table border="1">
        <tr>
          <th>代码</th>
          <th>名称</th>
          <th>运费</th>
          <th>有效</th>
          <th>排序</th>
          <th>编辑</th>
        </tr>
        <c:forEach var="method" items="${shippingMethods}">
          <tr>
            <td><c:out value="${method.code}"/></td>
            <td><c:out value="${method.name}"/></td>
            <td><c:out value="${method.fee}"/></td>
            <td><c:out value="${method.active}"/></td>
            <td><c:out value="${method.sortOrder}"/></td>
            <td>
              <a href="<c:url value='/admin/shipping/edit'/>?code=<c:out value='${method.code}'/>">编辑</a>
            </td>
          </tr>
        </c:forEach>
      </table>
    </c:if>
  </div>
  <jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
</body>
</html>
