<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="skishop" uri="http://skishop.com/tags" %>
<h2>商品詳細</h2>
<c:if test="${not empty product}">
    <h3><c:out value="${product.name}"/></h3>
    <p>ブランド: <c:out value="${product.brand}"/></p>
    <p>SKU: <c:out value="${product.sku}"/></p>
    <p>カテゴリ: <c:out value="${product.categoryId}"/></p>
    <p>説明: <c:out value="${product.description}"/></p>
    <form action="<c:url value='/cart'/>" method="post">
        <input type="hidden" name="productId" value="${product.id}"/>
        <input type="text" name="quantity" value="1" size="3"/>
        <skishop:csrfToken/>
        <button type="submit">カートへ追加</button>
    </form>
</c:if>
<c:if test="${empty product}"><p>商品情報が取得できませんでした。</p></c:if>
