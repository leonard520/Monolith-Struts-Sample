<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="skishop" uri="http://skishop.com/tags" %>
<h2>住所編集</h2>
<form action="<c:url value='/addresses/save'/>" method="post">
  <input type="hidden" name="id" value="${addressForm.id}"/>
  <table>
    <tr><th>ラベル</th><td><input type="text" name="label" value="${addressForm.label}" size="20"/></td></tr>
    <tr><th>宛名</th><td><input type="text" name="recipientName" value="${addressForm.recipientName}" size="30"/></td></tr>
    <tr><th>郵便番号</th><td><input type="text" name="postalCode" value="${addressForm.postalCode}" size="10"/></td></tr>
    <tr><th>都道府県</th><td><input type="text" name="prefecture" value="${addressForm.prefecture}" size="20"/></td></tr>
    <tr><th>住所1</th><td><input type="text" name="address1" value="${addressForm.address1}" size="40"/></td></tr>
    <tr><th>住所2</th><td><input type="text" name="address2" value="${addressForm.address2}" size="40"/></td></tr>
    <tr><th>電話番号</th><td><input type="text" name="phone" value="${addressForm.phone}" size="20"/></td></tr>
    <tr><th>既定にする</th><td><input type="checkbox" name="isDefault" value="true" ${addressForm.isDefault ? 'checked' : ''}/></td></tr>
  </table>
  <skishop:csrfToken/>
  <button type="submit">保存</button>
</form>
