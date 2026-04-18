<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ja">
<head>
  <meta charset="UTF-8">
  <title>住所編集</title>
  <link rel="stylesheet" href="<c:url value='/assets/css/app.css'/>" />
</head>
<body>
  <div class="app-header">
    <jsp:include page="/WEB-INF/jsp/common/header.jsp"/>
  </div>
  <div class="site-container">
    <jsp:include page="/WEB-INF/jsp/common/messages.jsp"/>
    <h2 class="page-title">住所編集</h2>
    <div class="card">
      <form action="<c:url value='/account/addresses/save'/>" method="post">
        <input type="hidden" name="id" value="<c:out value='${addressForm.id}'/>"/>
        <table>
          <tr>
            <th>ラベル</th>
            <td><input type="text" name="label" size="20" value="<c:out value='${addressForm.label}'/>"/></td>
          </tr>
          <tr>
            <th>宛名</th>
            <td><input type="text" name="recipientName" size="30" value="<c:out value='${addressForm.recipientName}'/>"/></td>
          </tr>
          <tr>
            <th>郵便番号</th>
            <td><input type="text" name="postalCode" size="10" value="<c:out value='${addressForm.postalCode}'/>"/></td>
          </tr>
          <tr>
            <th>都道府県</th>
            <td><input type="text" name="prefecture" size="20" value="<c:out value='${addressForm.prefecture}'/>"/></td>
          </tr>
          <tr>
            <th>住所1</th>
            <td><input type="text" name="address1" size="40" value="<c:out value='${addressForm.address1}'/>"/></td>
          </tr>
          <tr>
            <th>住所2</th>
            <td><input type="text" name="address2" size="40" value="<c:out value='${addressForm.address2}'/>"/></td>
          </tr>
          <tr>
            <th>電話番号</th>
            <td><input type="text" name="phone" size="20" value="<c:out value='${addressForm.phone}'/>"/></td>
          </tr>
          <tr>
            <th>デフォルト</th>
            <td><input type="checkbox" name="isDefault" value="true" <c:if test="${addressForm.isDefault}">checked</c:if>/></td>
          </tr>
        </table>
        <input type="hidden" name="_csrfToken" value="${_csrf.token}"/>
        <button type="submit" class="btn">保存</button>
      </form>
    </div>
    <p><a href="<c:url value='/account/addresses'/>">住所帳へ戻る</a></p>
  </div>
  <jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
</body>
</html>
