<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ja">
<head>
  <meta charset="UTF-8">
  <title>注文履歴</title>
  <link rel="stylesheet" href="<c:url value='/assets/css/app.css'/>" />
</head>
<body>
  <div class="app-header">
    <jsp:include page="/WEB-INF/jsp/common/header.jsp"/>
  </div>
  <div class="site-container">
    <jsp:include page="/WEB-INF/jsp/common/messages.jsp"/>
    <h2 class="page-title">注文履歴</h2>
    <c:if test="${empty orders}">
      <div class="card">注文履歴はありません。</div>
    </c:if>
    <c:if test="${not empty orders}">
      <div class="card table-responsive">
        <table>
          <tr>
            <th>注文番号</th>
            <th>ステータス</th>
            <th>合計</th>
            <th>操作</th>
          </tr>
          <c:forEach var="order" items="${orders}">
            <tr>
              <td><c:out value="${order.orderNumber}"/></td>
              <td><c:out value="${order.status}"/></td>
              <td><c:out value="${order.totalAmount}"/></td>
              <td>
                <a href="<c:url value='/orders/detail?id=${order.id}'/>">詳細</a>
                <form action="<c:url value='/orders/${order.id}/cancel'/>" method="post" style="display:inline;">
                  <input type="hidden" name="_csrfToken" value="${_csrf.token}"/>
                  <button type="submit" class="btn btn-small">キャンセル</button>
                </form>
                <form action="<c:url value='/orders/${order.id}/return'/>" method="post" style="display:inline;">
                  <input type="hidden" name="_csrfToken" value="${_csrf.token}"/>
                  <button type="submit" class="btn btn-small">返品</button>
                </form>
              </td>
            </tr>
          </c:forEach>
        </table>
      </div>
    </c:if>
  </div>
  <jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
</body>
</html>
