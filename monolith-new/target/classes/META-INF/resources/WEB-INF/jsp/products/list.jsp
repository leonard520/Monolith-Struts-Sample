<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="ja">
<head>
  <meta charset="UTF-8">
  <title>商品一覧</title>
  <link rel="stylesheet" href="<c:url value='/assets/css/app.css'/>" />
</head>
<body>
  <div class="app-header">
    <jsp:include page="/WEB-INF/jsp/common/header.jsp"/>
  </div>
  <div class="site-container">
    <jsp:include page="/WEB-INF/jsp/common/messages.jsp"/>
    <h2 class="page-title">商品一覧</h2>
    <div class="card">
      <form action="<c:url value='/products'/>" method="get" class="form-inline">
        <table>
          <tr>
            <th>キーワード</th>
            <td><input type="text" name="keyword" value="<c:out value='${param.keyword}'/>" size="20"/></td>
            <th>カテゴリ</th>
            <td>
              <select name="categoryId">
                <c:forEach var="opt" items="${categoryOptions}">
                  <option value="<c:out value='${opt.value}'/>"
                    <c:if test="${param.categoryId == opt.value}">selected</c:if>
                  ><c:out value="${opt.label}"/></option>
                </c:forEach>
              </select>
            </td>
            <th>並び順</th>
            <td>
              <select name="sort">
                <option value="" <c:if test="${empty param.sort}">selected</c:if>>指定なし</option>
                <option value="priceAsc" <c:if test="${param.sort == 'priceAsc'}">selected</c:if>>価格（昇順）</option>
                <option value="priceDesc" <c:if test="${param.sort == 'priceDesc'}">selected</c:if>>価格（降順）</option>
                <option value="newest" <c:if test="${param.sort == 'newest'}">selected</c:if>>最新</option>
              </select>
            </td>
          </tr>
        </table>
        <button type="submit" class="btn">検索</button>
      </form>
    </div>

    <c:if test="${empty productList}">
      <div class="card">商品が見つかりませんでした。</div>
    </c:if>
    <c:if test="${not empty productList}">
      <div class="products-grid">
        <c:forEach var="product" items="${productList}">
          <div class="product-card">
            <div class="name">
              <a href="<c:url value='/product'/>?id=<c:out value='${product.id}'/>">
                <c:out value="${product.name}"/>
              </a>
            </div>
            <div class="brand"><c:out value="${product.brand}"/></div>
            <div class="tags">
              <c:if test="${not empty product.status}"><span class="tag"><c:out value="${product.status}"/></span></c:if>
            </div>
            <div>
              <a href="<c:url value='/product'/>?id=<c:out value='${product.id}'/>" class="btn">詳細を見る</a>
            </div>
          </div>
        </c:forEach>
      </div>

      <div class="pagination">
        <c:if test="${page > 1}">
          <a href="<c:url value='/products'/>?page=${page - 1}&size=${size}<c:if test='${not empty param.keyword}'>&keyword=<c:out value='${param.keyword}'/></c:if><c:if test='${not empty param.categoryId}'>&categoryId=<c:out value='${param.categoryId}'/></c:if><c:if test='${not empty param.sort}'>&sort=<c:out value='${param.sort}'/></c:if>" class="btn">前へ</a>
        </c:if>
        <span>ページ ${page}</span>
        <c:if test="${not empty productList && productList.size() >= size}">
          <a href="<c:url value='/products'/>?page=${page + 1}&size=${size}<c:if test='${not empty param.keyword}'>&keyword=<c:out value='${param.keyword}'/></c:if><c:if test='${not empty param.categoryId}'>&categoryId=<c:out value='${param.categoryId}'/></c:if><c:if test='${not empty param.sort}'>&sort=<c:out value='${param.sort}'/></c:if>" class="btn">次へ</a>
        </c:if>
      </div>
    </c:if>
  </div>
  <jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
</body>
</html>
