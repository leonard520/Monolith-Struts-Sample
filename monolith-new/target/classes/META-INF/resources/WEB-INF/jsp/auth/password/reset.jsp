<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ja">
<head>
  <meta charset="UTF-8">
  <title>重置密码</title>
  <link rel="stylesheet" href="<c:url value='/assets/css/app.css'/>" />
</head>
<body>
  <div class="app-header">
    <jsp:include page="/WEB-INF/jsp/common/header.jsp"/>
  </div>
  <div class="site-container">
    <jsp:include page="/WEB-INF/jsp/common/messages.jsp"/>
    <h2>重置密码</h2>
    <form action="<c:url value='/password/reset'/>" method="post">
      <table>
        <tr>
          <th>重置令牌</th>
          <td><input type="text" name="token" value="<c:out value='${passwordResetForm.token}'/>" size="40"/></td>
        </tr>
        <tr>
          <th>密码</th>
          <td><input type="password" name="password" size="30"/></td>
        </tr>
        <tr>
          <th>确认密码</th>
          <td><input type="password" name="passwordConfirm" size="30"/></td>
        </tr>
      </table>
      <input type="hidden" name="_csrfToken" value="${_csrf.token}"/>
      <button type="submit">确认重置</button>
    </form>
  </div>
  <jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
</body>
</html>
