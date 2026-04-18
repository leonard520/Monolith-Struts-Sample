# t15 — Created JSP Files

## Files Created (7)

| File | View Name | Controller | Migration Notes |
|------|-----------|-----------|-----------------|
| `monolith-new/src/main/webapp/WEB-INF/jsp/cart/checkout.jsp` | `cart/checkout` | CheckoutController GET/POST `/checkout` | `<html:form>` → plain `<form>`, `<html:text>` → `<input>`, `<html:select>` → `<select>`, `<html:token>` → `<input type="hidden" name="_csrfToken">`, `<html:hidden>` → `<input type="hidden">`. Cart items table + payment form. |
| `monolith-new/src/main/webapp/WEB-INF/jsp/cart/confirmation.jsp` | `cart/confirmation` | CheckoutController POST `/checkout` (success) | `<bean:write>` → `<c:out>`, `<logic:present>` → `<c:if test="${not empty}">`, `<html:link page="/orders.do">` → `<a href="<c:url value='/orders'/>">`. |
| `monolith-new/src/main/webapp/WEB-INF/jsp/orders/history.jsp` | `orders/history` | OrderController GET `/orders` | `<logic:iterate>` → `<c:forEach>`, `<bean:write>` → `<c:out>`, `<html:link paramId>` → `<a href="<c:url>">`, cancel/return forms use `_csrfToken` hidden field, action URLs use `/orders/${order.id}/cancel` and `/orders/${order.id}/return` path-variable pattern. |
| `monolith-new/src/main/webapp/WEB-INF/jsp/orders/detail.jsp` | `orders/detail` | OrderController GET `/orders/detail?id=` | `<logic:present>` → `<c:if>`, `<bean:write>` → `<c:out>`, order items table with `<c:forEach>`. |
| `monolith-new/src/main/webapp/WEB-INF/jsp/account/addresses.jsp` | `account/addresses` | AddressController GET `/account/addresses` | `<logic:empty>` → `<c:if test="${empty}">`, `<logic:iterate>` → `<c:forEach>`, `<logic:equal property="default" value="true">` → `<c:if test="${address.default}">`, `<html:link page="/addresses/save.do">` → `<a href="<c:url value='/account/addresses/edit'/>">`. |
| `monolith-new/src/main/webapp/WEB-INF/jsp/account/address_edit.jsp` | `account/address_edit` | AddressController GET `/account/addresses/edit`, POST `/account/addresses/save` | `<html:form action="/addresses/save.do">` → `<form action="<c:url value='/account/addresses/save'/>">`, `<html:text>` → `<input>`, `<html:checkbox>` → `<input type="checkbox">`, `<html:token>` → `_csrfToken` hidden field. Form fields: id, label, recipientName, postalCode, prefecture, address1, address2, phone, isDefault. |
| `monolith-new/src/main/webapp/WEB-INF/jsp/points/balance.jsp` | `points/balance` | PointController GET `/points` | `<logic:present>` → `<c:if test="${not empty}">`, `<bean:write>` → `<c:out>`. Displays balance, lifetimeEarned, lifetimeRedeemed. |

## Struts → JSTL Tag Migration Summary

| Struts Tag | JSTL/Spring Replacement |
|-----------|------------------------|
| `<html:form action="...">` | `<form action="<c:url value='...'/>" method="post">` |
| `<html:text property="x">` | `<input type="text" name="x" value="<c:out value='${form.x}'/>"/>` |
| `<html:password property="x">` | `<input type="password" name="x"/>` |
| `<html:hidden property="x" value="...">` | `<input type="hidden" name="x" value="<c:out value='...'/>"/>` |
| `<html:select property="x">` | `<select name="x">` |
| `<html:option value="v">` | `<option value="v">` |
| `<html:checkbox property="x">` | `<input type="checkbox" name="x" value="true">` |
| `<html:submit value="...">` | `<button type="submit" class="btn">...</button>` |
| `<html:token/>` | `<input type="hidden" name="_csrfToken" value="${_csrf.token}"/>` |
| `<html:link page="/x.do">` | `<a href="<c:url value='/x'/>">` |
| `<bean:write name="o" property="p" filter="true"/>` | `<c:out value="${o.p}"/>` |
| `<bean:message key="k"/>` | Hardcoded Japanese label (matching source) |
| `<logic:present name="x">` | `<c:if test="${not empty x}">` |
| `<logic:notPresent name="x">` | `<c:if test="${empty x}">` |
| `<logic:empty name="x">` | `<c:if test="${empty x}">` |
| `<logic:notEmpty name="x">` | `<c:if test="${not empty x}">` |
| `<logic:iterate id="i" name="x">` | `<c:forEach var="i" items="${x}">` |
| `<logic:equal name="x" property="p" value="v">` | `<c:if test="${x.p == v}">` or `<c:if test="${x.p}">` for booleans |

## Layout Pattern

All 7 JSPs follow the t6 layout pattern:
- Full HTML document (`<!DOCTYPE html>`, `<html>`, `<head>`, `<body>`)
- `<jsp:include page="/WEB-INF/jsp/common/header.jsp"/>` in `.app-header` div
- `<jsp:include page="/WEB-INF/jsp/common/messages.jsp"/>` at top of `.site-container`
- Page content in `.site-container` with `.card` and `.table-responsive` wrappers
- `<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>` at end of body
- CSRF tokens as `<input type="hidden" name="_csrfToken" value="${_csrf.token}"/>`

## Model Attribute Contract

| JSP | Required Model Attributes | Set By |
|-----|--------------------------|--------|
| cart/checkout.jsp | `checkoutForm`, `cartItems`, `cartSubtotal`, `cartId` | CheckoutController |
| cart/confirmation.jsp | `order` | CheckoutController (success path) |
| orders/history.jsp | `orders` (List\<Order>) | OrderController |
| orders/detail.jsp | `order`, `orderItems` (List\<OrderItem>) | OrderController |
| account/addresses.jsp | `addresses` (List\<Address>) | AddressController |
| account/address_edit.jsp | `addressForm` | AddressController |
| points/balance.jsp | `pointBalance` (PointAccount) | PointController |

## Build Status
`mvn -f monolith-new/pom.xml clean package -DskipTests -q` — **PASS** (zero errors, all 7 JSPs in JAR under `META-INF/resources/`)
