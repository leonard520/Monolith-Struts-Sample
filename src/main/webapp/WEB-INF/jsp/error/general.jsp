<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ja">
<head>
    <meta charset="UTF-8">
    <title>エラー - Ski Resort Shop</title>
    <link rel="stylesheet" href="<c:url value='/assets/css/app.css'/>" />
</head>
<body>
<div class="app-header">
    <div class="inner">
        <div class="logo"><a href="<c:url value='/home'/>">Ski Resort Shop</a></div>
    </div>
</div>
<div class="site-container">
    <div class="error-page">
        <h2>エラーが発生しました</h2>
        <p><c:out value="${errorMessage}" default="不明なエラー"/></p>
        <a href="<c:url value='/home'/>">ホームに戻る</a>
    </div>
</div>
</body>
</html>
