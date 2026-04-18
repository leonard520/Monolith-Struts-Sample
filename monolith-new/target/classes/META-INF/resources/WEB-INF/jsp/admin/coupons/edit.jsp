<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ja">
<head>
  <meta charset="UTF-8">
  <title>编辑优惠券</title>
  <link rel="stylesheet" href="<c:url value='/assets/css/app.css'/>" />
</head>
<body>
  <div class="app-header">
    <jsp:include page="/WEB-INF/jsp/common/header.jsp"/>
  </div>
  <div class="site-container">
    <jsp:include page="/WEB-INF/jsp/common/messages.jsp"/>
    <h2>编辑优惠券</h2>
    <form action="<c:url value='/admin/coupon/edit'/>" method="post">
      <input type="hidden" name="id" value="<c:out value='${couponForm.id}'/>"/>
      <table>
        <tr>
          <th>优惠券代码</th>
          <td><input type="text" name="code" value="<c:out value='${couponForm.code}'/>" size="20"/></td>
        </tr>
        <tr>
          <th>活动编号</th>
          <td><input type="text" name="campaignId" value="<c:out value='${couponForm.campaignId}'/>" size="20"/></td>
        </tr>
        <tr>
          <th>优惠券类型</th>
          <td><input type="text" name="couponType" value="<c:out value='${couponForm.couponType}'/>" size="12"/></td>
        </tr>
        <tr>
          <th>折扣类型</th>
          <td><input type="text" name="discountType" value="<c:out value='${couponForm.discountType}'/>" size="12"/></td>
        </tr>
        <tr>
          <th>折扣值</th>
          <td><input type="text" name="discountValue" value="<c:out value='${couponForm.discountValue}'/>" size="10"/></td>
        </tr>
        <tr>
          <th>最低金额</th>
          <td><input type="text" name="minimumAmount" value="<c:out value='${couponForm.minimumAmount}'/>" size="10"/></td>
        </tr>
        <tr>
          <th>最大折扣</th>
          <td><input type="text" name="maximumDiscount" value="<c:out value='${couponForm.maximumDiscount}'/>" size="10"/></td>
        </tr>
        <tr>
          <th>使用上限</th>
          <td><input type="text" name="usageLimit" value="<c:out value='${couponForm.usageLimit}'/>" size="6"/></td>
        </tr>
        <tr>
          <th>有效</th>
          <td><input type="checkbox" name="active" value="true" <c:if test="${couponForm.active}">checked="checked"</c:if>/></td>
        </tr>
        <tr>
          <th>有效期至（YYYY-MM-DD）</th>
          <td><input type="text" name="expiresAt" value="<c:out value='${couponForm.expiresAt}'/>" size="12"/></td>
        </tr>
      </table>
      <input type="hidden" name="_csrfToken" value="${_csrf.token}"/>
      <button type="submit">更新</button>
    </form>
  </div>
  <jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
</body>
</html>
