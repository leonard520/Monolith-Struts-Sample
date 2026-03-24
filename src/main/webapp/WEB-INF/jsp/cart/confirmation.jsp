<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<h2>注文確認</h2>
<c:if test="${not empty order}">
  <p>注文番号: <c:out value="${order.orderNumber}"/></p>
  <p>ステータス: <c:out value="${order.status}"/></p>
  <p>合計金額: <c:out value="${order.totalAmount}"/></p>
</c:if>
<c:if test="${empty order}"><p>注文情報がありません。</p></c:if>
<p><a href="<c:url value='/orders'/>">注文履歴へ</a></p>
