<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<h2>配送方法管理</h2>
<p><a href="<c:url value='/admin/shipping/edit'/>">配送方法を追加</a></p>
<c:if test="${empty shippingMethods}"><p>配送方法がありません。</p></c:if>
<c:if test="${not empty shippingMethods}">
  <table border="1">
    <tr><th>コード</th><th>名称</th><th>送料</th><th>有効</th><th>並び順</th><th>編集</th></tr>
    <c:forEach items="${shippingMethods}" var="method">
      <tr>
        <td><c:out value="${method.code}"/></td>
        <td><c:out value="${method.name}"/></td>
        <td><c:out value="${method.fee}"/></td>
        <td><c:out value="${method.active}"/></td>
        <td><c:out value="${method.sortOrder}"/></td>
        <td><a href="<c:url value='/admin/shipping/edit'/>?code=${method.code}">編集</a></td>
      </tr>
    </c:forEach>
  </table>
</c:if>
