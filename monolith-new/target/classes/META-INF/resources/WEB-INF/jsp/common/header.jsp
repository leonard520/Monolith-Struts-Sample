<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<div class="inner">
  <div class="logo"><a href="<c:url value='/home'/>">Ski Resort Shop</a></div>
  <ul class="app-nav">
    <li><a href="<c:url value='/home'/>">ホーム</a></li>
    <li><a href="<c:url value='/products'/>">商品</a></li>
    <li><a href="<c:url value='/coupons'/>">クーポン</a></li>
    <c:if test="${not empty sessionScope.loginUser}">
      <li><a href="<c:url value='/orders'/>">注文履歴</a></li>
      <li><a href="<c:url value='/points'/>">ポイント</a></li>
      <li><a href="<c:url value='/account/addresses'/>">住所帳</a></li>
      <c:if test="${sessionScope.loginUser.role == 'ADMIN'}">
        <li><a href="<c:url value='/admin/products'/>">管理：商品</a></li>
        <li><a href="<c:url value='/admin/orders'/>">管理：注文</a></li>
        <li><a href="<c:url value='/admin/coupons'/>">管理：クーポン</a></li>
        <li><a href="<c:url value='/admin/shipping'/>">管理：配送</a></li>
      </c:if>
    </c:if>
    <c:if test="${empty sessionScope.loginUser}">
      <li><a href="<c:url value='/login'/>">ログイン</a></li>
      <li><a href="<c:url value='/register'/>">会員登録</a></li>
    </c:if>
  </ul>
  <div class="actions">
    <form action="<c:url value='/products'/>" method="get" class="header-search">
      <input type="text" name="keyword" value="" placeholder="商品名やブランドで検索" />
      <button type="submit">🔍</button>
    </form>
    <a href="<c:url value='/cart'/>" class="btn">🛒 カート</a>
    <c:if test="${not empty sessionScope.loginUser}">
      <span class="user-name">こんにちは、<c:out value="${sessionScope.loginUser.username}"/></span>
      <a href="<c:url value='/logout'/>">ログアウト</a>
    </c:if>
    <c:if test="${empty sessionScope.loginUser}">
      <a href="<c:url value='/login'/>">ログイン</a>
    </c:if>
  </div>
</div>
