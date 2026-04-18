<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ja">
<head>
  <meta charset="UTF-8">
  <title>会員登録</title>
  <link rel="stylesheet" href="<c:url value='/assets/css/app.css'/>" />
</head>
<body>
  <div class="app-header">
    <jsp:include page="/WEB-INF/jsp/common/header.jsp"/>
  </div>
  <div class="site-container">
    <jsp:include page="/WEB-INF/jsp/common/messages.jsp"/>
    <h2>注册会员</h2>
    <form action="<c:url value='/register'/>" method="post">
      <table>
        <tr>
          <th>邮箱</th>
          <td><input type="text" name="email" value="<c:out value='${registerForm.email}'/>" size="30"/></td>
        </tr>
        <tr>
          <th>用户名</th>
          <td><input type="text" name="username" value="<c:out value='${registerForm.username}'/>" size="30"/></td>
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
      <button type="submit">注册</button>
    </form>
    <p><a href="<c:url value='/login'/>">返回登录</a></p>
  </div>
  <jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
</body>
</html>
