<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<h2>住所帳</h2>
<p><a href="<c:url value='/addresses/edit'/>">新しい住所を追加</a></p>
<c:if test="${empty addresses}"><p>登録済み住所がありません。</p></c:if>
<c:if test="${not empty addresses}">
  <table border="1">
    <tr><th>ラベル</th><th>宛名</th><th>住所</th><th>電話</th><th>既定</th></tr>
    <c:forEach items="${addresses}" var="address">
      <tr>
        <td><c:out value="${address.label}"/></td>
        <td><c:out value="${address.recipientName}"/></td>
        <td>
          <c:out value="${address.postalCode}"/>
          <c:out value="${address.prefecture}"/>
          <c:out value="${address.address1}"/>
          <c:out value="${address.address2}"/>
        </td>
        <td><c:out value="${address.phone}"/></td>
        <td><c:if test="${address['default']}">はい</c:if><c:if test="${not address['default']}">-</c:if></td>
      </tr>
    </c:forEach>
  </table>
</c:if>
