<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<h2>ポイント残高</h2>
<c:if test="${not empty pointBalance}">
  <p>残高: <c:out value="${pointBalance.balance}"/></p>
  <p>累計獲得: <c:out value="${pointBalance.lifetimeEarned}"/></p>
  <p>累計利用: <c:out value="${pointBalance.lifetimeRedeemed}"/></p>
</c:if>
<c:if test="${empty pointBalance}"><p>ポイント情報がありません。</p></c:if>
