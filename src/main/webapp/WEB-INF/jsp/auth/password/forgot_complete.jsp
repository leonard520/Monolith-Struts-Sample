<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<h2>パスワード再発行完了</h2>
<p>メールにリセット情報を送信しました。</p>
<c:if test="${not empty resetToken}">
  <p>開発用トークン: <c:out value="${resetToken}"/></p>
</c:if>
<p><a href="<c:url value='/password/reset'/>">パスワードリセットへ</a></p>
