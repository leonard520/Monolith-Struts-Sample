<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ja">
<head>
  <meta charset="UTF-8">
  <title>编辑配送方式</title>
  <link rel="stylesheet" href="<c:url value='/assets/css/app.css'/>" />
</head>
<body>
  <div class="app-header">
    <jsp:include page="/WEB-INF/jsp/common/header.jsp"/>
  </div>
  <div class="site-container">
    <jsp:include page="/WEB-INF/jsp/common/messages.jsp"/>
    <h2>编辑配送方式</h2>
    <form action="<c:url value='/admin/shipping/edit'/>" method="post">
      <input type="hidden" name="id" value="<c:out value='${shippingForm.id}'/>"/>
      <table>
        <tr>
          <th>配送代码</th>
          <td><input type="text" name="code" value="<c:out value='${shippingForm.code}'/>" size="12"/></td>
        </tr>
        <tr>
          <th>配送名称</th>
          <td><input type="text" name="name" value="<c:out value='${shippingForm.name}'/>" size="20"/></td>
        </tr>
        <tr>
          <th>运费</th>
          <td><input type="text" name="fee" value="<c:out value='${shippingForm.fee}'/>" size="10"/></td>
        </tr>
        <tr>
          <th>有效</th>
          <td><input type="checkbox" name="active" value="true" <c:if test="${shippingForm.active}">checked="checked"</c:if>/></td>
        </tr>
        <tr>
          <th>排序</th>
          <td><input type="text" name="sortOrder" value="<c:out value='${shippingForm.sortOrder}'/>" size="4"/></td>
        </tr>
      </table>
      <input type="hidden" name="_csrfToken" value="${_csrf.token}"/>
      <button type="submit">更新</button>
    </form>
  </div>
  <jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
</body>
</html>
