<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ja">
<head>
  <meta charset="UTF-8">
  <title>ログイン</title>
  <link rel="stylesheet" href="<c:url value='/assets/css/app.css'/>" />
</head>
<body>
  <div class="app-header">
    <jsp:include page="/WEB-INF/jsp/common/header.jsp"/>
  </div>
  <div class="site-container">
    <jsp:include page="/WEB-INF/jsp/common/messages.jsp"/>
    <h2>登录</h2>
    <form action="<c:url value='/login'/>" method="post">
      <table>
        <tr>
          <th>邮箱</th>
          <td><input type="text" name="email" value="<c:out value='${loginForm.email}'/>" size="30"/></td>
        </tr>
        <tr>
          <th>密码</th>
          <td><input type="password" name="password" size="30"/></td>
        </tr>
      </table>
      <input type="hidden" name="_csrfToken" value="${_csrf.token}"/>
      <button type="submit">登录</button>
    </form>
    <p>
      <a href="<c:url value='/register'/>">注册会员</a>
      |
      <a href="<c:url value='/password/forgot'/>">忘记密码</a>
    </p>
  </div>
  <jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
</body>
</html>
