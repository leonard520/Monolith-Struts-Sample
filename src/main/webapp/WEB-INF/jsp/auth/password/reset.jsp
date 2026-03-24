<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="skishop" uri="http://skishop.com/tags" %>
<h2>パスワードリセット</h2>
<form action="<c:url value='/password/reset'/>" method="post">
  <table>
    <tr><th>トークン</th><td><input type="text" name="token" size="40"/></td></tr>
    <tr><th>新パスワード</th><td><input type="password" name="password" size="30"/></td></tr>
    <tr><th>パスワード(確認)</th><td><input type="password" name="passwordConfirm" size="30"/></td></tr>
  </table>
  <skishop:csrfToken/>
  <button type="submit">更新</button>
</form>
