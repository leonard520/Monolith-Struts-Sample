<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ja">
<head>
  <meta charset="UTF-8">
  <title>住所帳</title>
  <link rel="stylesheet" href="<c:url value='/assets/css/app.css'/>" />
</head>
<body>
  <div class="app-header">
    <jsp:include page="/WEB-INF/jsp/common/header.jsp"/>
  </div>
  <div class="site-container">
    <jsp:include page="/WEB-INF/jsp/common/messages.jsp"/>
    <h2 class="page-title">住所帳</h2>
    <p><a href="<c:url value='/account/addresses/edit'/>" class="btn">新しい住所を追加</a></p>
    <c:if test="${empty addresses}">
      <div class="card">保存された住所はありません。</div>
    </c:if>
    <c:if test="${not empty addresses}">
      <div class="card table-responsive">
        <table>
          <tr>
            <th>ラベル</th>
            <th>宛名</th>
            <th>住所</th>
            <th>電話番号</th>
            <th>デフォルト</th>
          </tr>
          <c:forEach var="address" items="${addresses}">
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
              <td>
                <c:if test="${address.isDefault}">はい</c:if>
                <c:if test="${not address.isDefault}">-</c:if>
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
