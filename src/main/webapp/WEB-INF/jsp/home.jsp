<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<div class="hero">
    <h1>Azure SkiShop へようこそ</h1>
    <p>最高品質のスキー・スノーボード用品で、あなたの冬のアドベンチャーを始めよう。</p>
    <div class="hero-actions"><a href="<c:url value='/products'/>" class="btn">商品を見る</a></div>
</div>
<h2 class="page-title">おすすめスキー用品</h2>
<c:if test="${not empty featuredProducts}">
    <div class="products-grid">
        <c:forEach items="${featuredProducts}" var="product">
            <div class="product-card">
                <div class="name"><a href="<c:url value='/product'/>?id=${product.id}"><c:out value="${product.name}"/></a></div>
                <div class="price">¥<c:out value="${product.price}"/></div>
                <c:if test="${not empty product.brand}"><div class="tags"><span class="tag"><c:out value="${product.brand}"/></span></div></c:if>
                <div><a href="<c:url value='/product'/>?id=${product.id}" class="btn">詳細を見る</a></div>
            </div>
        </c:forEach>
    </div>
</c:if>
<c:if test="${empty featuredProducts}"><p>おすすめ商品を準備中です。</p></c:if>
