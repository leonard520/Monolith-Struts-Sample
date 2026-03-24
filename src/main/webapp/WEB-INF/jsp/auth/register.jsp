<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="skishop" uri="http://skishop.com/tags" %>
<h2>会員登録</h2>
<form action="<c:url value='/register'/>" method="post">
    <table>
        <tr><th>メールアドレス</th><td><input type="text" name="email" size="30"/></td></tr>
        <tr><th>ユーザー名</th><td><input type="text" name="username" size="30"/></td></tr>
        <tr><th>パスワード</th><td><input type="password" name="password" size="30"/></td></tr>
        <tr><th>パスワード(確認)</th><td><input type="password" name="passwordConfirm" size="30"/></td></tr>
    </table>
    <skishop:csrfToken/>
    <button type="submit">登録</button>
</form>
<p><a href="<c:url value='/login'/>">ログインへ戻る</a></p>
