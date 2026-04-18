# t6 — Created & Modified Files

## Layout & Common Fragments

| File | Purpose | Migration Notes |
|------|---------|-----------------|
| `monolith-new/src/main/webapp/WEB-INF/jsp/layouts/base.jsp` | Layout template showing the HTML document structure all pages must follow | Replaces Tiles `baseLayout` definition. Each content page JSP is a complete HTML document that `<jsp:include>`s header, messages, footer. |
| `monolith-new/src/main/webapp/WEB-INF/jsp/common/header.jsp` | Navigation header fragment | Struts `<html:link>` → `<a href="<c:url>">`, `<logic:present>` → `<c:if>`, `<bean:write>` → `<c:out>`. Japanese labels matching screenshots. |
| `monolith-new/src/main/webapp/WEB-INF/jsp/common/footer.jsp` | Footer fragment | `<bean:message>` → hardcoded "SkiShop Online Store © 2026" per screenshot. |
| `monolith-new/src/main/webapp/WEB-INF/jsp/common/messages.jsp` | Error/success message display | Replaces `<html:errors>` / `<logic:messagesPresent>`. Supports `errors` (list), `error` (string), `successMessage` model attributes. |

## Standalone Pages

| File | Purpose | Migration Notes |
|------|---------|-----------------|
| `monolith-new/src/main/webapp/WEB-INF/jsp/error.jsp` | Error page (view resolver target for `GlobalExceptionHandler`) | Returns "error" view name → `/WEB-INF/jsp/error.jsp`. Full HTML page with header/footer includes. |
| `monolith-new/src/main/webapp/error.jsp` | Servlet-level error page (webapp root) | Fallback for container-level errors. |
| `monolith-new/src/main/webapp/index.jsp` | Welcome file redirect | `response.sendRedirect("/home")` — changed from `/home.do` to `/home`. |

## Static Assets

| File | Purpose |
|------|---------|
| `monolith-new/src/main/webapp/assets/css/app.css` | Main CSS — copied verbatim from source |

## Build Configuration Change

| File | Change |
|------|--------|
| `monolith-new/pom.xml` | Added `<resource>` entry to copy `src/main/webapp` to `META-INF/resources` targetPath, enabling JSP/asset packaging in JAR. Without this, `src/main/webapp` is excluded from JAR packaging (only processed by WAR plugin). |

## Layout Pattern for Downstream Tasks (t13-t16)

Each content page JSP must follow this structure:

```jsp
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ja">
<head>
  <meta charset="UTF-8">
  <title>Page Title</title>
  <link rel="stylesheet" href="<c:url value='/assets/css/app.css'/>" />
</head>
<body>
  <div class="app-header">
    <jsp:include page="/WEB-INF/jsp/common/header.jsp"/>
  </div>
  <div class="site-container">
    <jsp:include page="/WEB-INF/jsp/common/messages.jsp"/>
    <!-- Page-specific content here -->
  </div>
  <jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>
</body>
</html>
```

## Header Navigation Structure

The header shows different links based on login state:
- **Not logged in**: ホーム, 商品, クーポン, ログイン, 会員登録 + search + カート
- **Logged in (USER)**: ホーム, 商品, クーポン, 注文履歴, ポイント, 住所帳 + search + カート + ログアウト
- **Logged in (ADMIN)**: Above + 管理：商品, 管理：注文, 管理：クーポン, 管理：配送

Login state checked via `${sessionScope.loginUser}` (set by AuthController in session).

## Messages Fragment Contract

Controllers should set these model attributes:
- `errors` — `List<String>` for multiple error messages
- `error` — `String` for a single error message
- `successMessage` — `String` for success feedback

## Verification
- `mvn clean package -DskipTests -q` → SUCCESS
- JAR contains all JSP and CSS files under `META-INF/resources/`
- CSS served at `/assets/css/app.css` → HTTP 200
- `index.jsp` redirects to `/home` (302)
