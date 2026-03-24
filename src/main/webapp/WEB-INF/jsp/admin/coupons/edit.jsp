<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="skishop" uri="http://skishop.com/tags" %>
<h2>クーポン編集</h2>
<form action="<c:url value='/admin/coupon/edit'/>" method="post">
  <input type="hidden" name="id" value="${adminCouponForm.id}"/>
  <table>
    <tr><th>コード</th><td><input type="text" name="code" value="${adminCouponForm.code}" size="20"/></td></tr>
    <tr><th>キャンペーンID</th><td><input type="text" name="campaignId" value="${adminCouponForm.campaignId}" size="20"/></td></tr>
    <tr><th>クーポン種別</th><td><input type="text" name="couponType" value="${adminCouponForm.couponType}" size="12"/></td></tr>
    <tr><th>割引種別</th><td><input type="text" name="discountType" value="${adminCouponForm.discountType}" size="12"/></td></tr>
    <tr><th>割引値</th><td><input type="text" name="discountValue" value="${adminCouponForm.discountValue}" size="10"/></td></tr>
    <tr><th>最低金額</th><td><input type="text" name="minimumAmount" value="${adminCouponForm.minimumAmount}" size="10"/></td></tr>
    <tr><th>最大割引</th><td><input type="text" name="maximumDiscount" value="${adminCouponForm.maximumDiscount}" size="10"/></td></tr>
    <tr><th>利用上限</th><td><input type="text" name="usageLimit" value="${adminCouponForm.usageLimit}" size="6"/></td></tr>
    <tr><th>有効</th><td><input type="checkbox" name="active" value="true" ${adminCouponForm.active ? 'checked' : ''}/></td></tr>
    <tr><th>期限</th><td><input type="text" name="expiresAt" value="${adminCouponForm.expiresAt}" size="12"/></td></tr>
  </table>
  <skishop:csrfToken/>
  <button type="submit">更新</button>
</form>
