# t13 — Auth & Home JSPs: File Inventory

## Created Files

| File | Source | Migration Notes |
|------|--------|-----------------|
| `monolith-new/src/main/webapp/WEB-INF/jsp/home.jsp` | `src/.../jsp/home.jsp` | `<html:link>` → `<a href="<c:url>">`, `<logic:iterate>` → `<c:forEach>`, `<logic:notEmpty/empty>` → `<c:if>`, `<bean:write>` → `<c:out>`. Hero text matches screenshot (Japanese). |
| `monolith-new/src/main/webapp/WEB-INF/jsp/auth/login.jsp` | `src/.../jsp/auth/login.jsp` | `<html:form>` → plain `<form>`, `<html:text/password>` → `<input>`, `<html:token/>` → `<input type="hidden" name="_csrfToken" value="${_csrf.token}"/>`, `<html:submit>` → `<button>`. Form model attribute: `loginForm`. |
| `monolith-new/src/main/webapp/WEB-INF/jsp/auth/register.jsp` | `src/.../jsp/auth/register.jsp` | Same tag conversions as login. Form model attribute: `registerForm`. Fields: email, username, password, passwordConfirm. |
| `monolith-new/src/main/webapp/WEB-INF/jsp/auth/password/forgot.jsp` | `src/.../jsp/auth/password/forgot.jsp` | Form model attribute: `passwordResetRequestForm`. Single email field. |
| `monolith-new/src/main/webapp/WEB-INF/jsp/auth/password/forgot_complete.jsp` | `src/.../jsp/auth/password/forgot_complete.jsp` | `<logic:present>` → `<c:if test="${not empty resetToken}">`, `<bean:write>` → `<c:out>`. No form (display-only page). |
| `monolith-new/src/main/webapp/WEB-INF/jsp/auth/password/reset.jsp` | `src/.../jsp/auth/password/reset.jsp` | Form model attribute: `passwordResetForm`. Pre-fills token from `${passwordResetForm.token}` (supports ?token= query param via controller). |

## Tag Conversion Summary

| Struts Tag | JSTL/HTML Replacement |
|------------|----------------------|
| `<html:form action="/x.do">` | `<form action="<c:url value='/x'/>" method="post">` |
| `<html:text property="f"/>` | `<input type="text" name="f" value="<c:out value='${form.f}'/>"/>` |
| `<html:password property="f"/>` | `<input type="password" name="f"/>` |
| `<html:token/>` | `<input type="hidden" name="_csrfToken" value="${_csrf.token}"/>` |
| `<html:submit value="X"/>` | `<button type="submit">X</button>` |
| `<html:link page="/x.do">` | `<a href="<c:url value='/x'/>">` |
| `<bean:message key="k"/>` | Hardcoded label text (matching source messages.properties) |
| `<bean:write name="x" property="p"/>` | `<c:out value="${x.p}"/>` |
| `<logic:present name="x">` | `<c:if test="${not empty x}">` |
| `<logic:notEmpty name="x">` | `<c:if test="${not empty x}">` |
| `<logic:empty name="x">` | `<c:if test="${empty x}">` |
| `<logic:iterate id="i" name="list">` | `<c:forEach var="i" items="${list}">` |

## CSRF Token Contract

All POST forms include:
```html
<input type="hidden" name="_csrfToken" value="${_csrf.token}"/>
```
The `extract_csrf` function in api-test.sh parses `name="_csrfToken"` with regex — attribute order (name before value or value before name) is handled by the script.

## Layout Pattern

All pages follow the t6 layout template: full HTML document with `<jsp:include>` for header, messages, and footer fragments.

## Build Verification
- `mvn -f monolith-new/pom.xml clean package -DskipTests -q` → **SUCCESS**
- All 6 JSPs present in JAR under `META-INF/resources/WEB-INF/jsp/`
