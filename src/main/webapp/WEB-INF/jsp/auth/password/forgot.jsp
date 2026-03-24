<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="skishop" uri="http://skishop.com/tags" %>
<h2>パスワード再発行</h2>
<form action="<c:url value='/password/forgot'/>" method="post">
  <table>
    <tr><th>メールアドレス</th><td><input type="text" name="email" size="40"/></td></tr>
  </table>
  <skishop:csrfToken/>
  <button type="submit">送信</button>
</form>
<p><a href="<c:url value='/login'/>">ログインへ戻る</a></p>
