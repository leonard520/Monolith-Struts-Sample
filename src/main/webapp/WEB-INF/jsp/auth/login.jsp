<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="skishop" uri="http://skishop.com/tags" %>
<h2>ログイン</h2>
<form action="<c:url value='/login'/>" method="post">
    <table>
        <tr><th>メールアドレス</th><td><input type="text" name="email" size="30"/></td></tr>
        <tr><th>パスワード</th><td><input type="password" name="password" size="30"/></td></tr>
    </table>
    <skishop:csrfToken/>
    <button type="submit">ログイン</button>
</form>
<p><a href="<c:url value='/register'/>">会員登録</a> | <a href="<c:url value='/password/forgot'/>">パスワードを忘れた場合</a></p>
