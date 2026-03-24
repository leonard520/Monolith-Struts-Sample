<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="skishop" uri="http://skishop.com/tags" %>
<h2 class="page-title">商品一覧</h2>
<div class="card">
<form action="<c:url value='/products'/>" method="get" class="form-inline">
    <table><tr>
        <th>キーワード</th><td><input type="text" name="keyword" size="20"/></td>
        <th>カテゴリ</th><td><select name="categoryId"><c:forEach items="${categoryOptions}" var="opt"><option value="${opt[1]}">${opt[0]}</option></c:forEach></select></td>
        <th>並び替え</th><td><select name="sort"><option value="">指定なし</option><option value="priceAsc">価格(昇順)</option><option value="priceDesc">価格(降順)</option><option value="newest">新着</option></select></td>
    </tr></table>
    <button type="submit">検索</button>
</form>
</div>
<c:if test="${empty productList}"><div class="card">商品が見つかりませんでした。</div></c:if>
<c:if test="${not empty productList}">
<div class="card table-responsive"><table>
    <tr><th>商品名</th><th>ブランド</th><th>価格</th><th>カート</th></tr>
    <c:forEach items="${productList}" var="product">
    <tr>
        <td><a href="<c:url value='/product'/>?id=${product.id}"><c:out value="${product.name}"/></a></td>
        <td><c:out value="${product.brand}"/></td>
        <td><c:out value="${product.priceDisplay}"/></td>
        <td><form action="<c:url value='/cart'/>" method="post"><input type="hidden" name="productId" value="${product.id}"/><input type="text" name="quantity" value="1" size="3"/><skishop:csrfToken/><button type="submit">追加</button></form></td>
    </tr>
    </c:forEach>
</table></div>
</c:if>
