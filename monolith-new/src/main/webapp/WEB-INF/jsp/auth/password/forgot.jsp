<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ja">
<head>
  <meta charset="UTF-8">
  <title>找回密码</title>
  <link rel="stylesheet" href="<c:url value='/assets/css/app.css'/>" />
</head>
<body>
  <div class="app-header">
    <jsp:include page="/WEB-INF/jsp/common/header.jsp"/>
  </div>
  <div class="site-container">
    <jsp:include page="/WEB-INF/jsp/common/messages.jsp"/>
    <h2>找回密码</h2>
    <form action="<c:url value='/password/forgot'/>" method="post">
      <table>
        <tr>
          <th>邮箱</th>
          <td><input type="text" name="email" value="<c:out value='${passwordResetRequestForm.email}'/>" size="40"/></td>
        </tr>
      </table>
      <input type="hidden" name="_csrfToken" value="${_csrf.token}"/>
      <button type="submit">发送</button>
    </form>
    <p><a href="<c:url value='/login'/>">返回登录</a></p>
  </div>
  <jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
</body>
</html>
