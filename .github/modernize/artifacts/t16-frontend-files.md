# t16 — Admin JSP File Inventory

## Files Created (8)

| File | Source | Key Transformations |
|------|--------|-------------------|
| `monolith-new/src/main/webapp/WEB-INF/jsp/admin/products/list.jsp` | `src/main/webapp/WEB-INF/jsp/admin/products/list.jsp` | `<logic:iterate>` → `<c:forEach>`, `<bean:write>` → `<c:out>`, `<html:link>` → `<a href="<c:url>">`, `<html:form>` → plain `<form>`, `<html:token>` → `_csrfToken` hidden field, `<html:hidden>` → `<input type="hidden">`, `.do` suffix removed from URLs |
| `monolith-new/src/main/webapp/WEB-INF/jsp/admin/products/edit.jsp` | `src/main/webapp/WEB-INF/jsp/admin/products/edit.jsp` | `<html:form>` → `<form>`, `<html:text>` → `<input type="text">`, `<html:textarea>` → `<textarea>`, `<html:hidden>` → hidden input, `<bean:message key="...">` → literal Chinese labels, `<logic:present>` → `<c:if>`, form bean `adminProductForm` → `productForm` (matching controller) |
| `monolith-new/src/main/webapp/WEB-INF/jsp/admin/orders/list.jsp` | `src/main/webapp/WEB-INF/jsp/admin/orders/list.jsp` | `<bean:define>` variables eliminated (JSTL accesses properties directly), `<html:form>` inline update forms → plain `<form>`, `ResponseUtils.filter()` escaped values → `<c:out>` for XSS safety |
| `monolith-new/src/main/webapp/WEB-INF/jsp/admin/orders/detail.jsp` | `src/main/webapp/WEB-INF/jsp/admin/orders/detail.jsp` | `<logic:equal>` → `<c:if test="${order.status == 'DELIVERED'}">`, `<logic:present>/<logic:notPresent>` → `<c:if test="${not empty/empty}">`, refund form hidden field uses `name="id"` matching controller `@RequestParam("id")` |
| `monolith-new/src/main/webapp/WEB-INF/jsp/admin/coupons/list.jsp` | `src/main/webapp/WEB-INF/jsp/admin/coupons/list.jsp` | Same iterator/write/link patterns as products. Edit link passes `?code=` parameter matching controller |
| `monolith-new/src/main/webapp/WEB-INF/jsp/admin/coupons/edit.jsp` | `src/main/webapp/WEB-INF/jsp/admin/coupons/edit.jsp` | `<html:checkbox>` → `<input type="checkbox">` with `<c:if>` for checked state, form bean `adminCouponForm` → `couponForm` (matching controller `@ModelAttribute("couponForm")`) |
| `monolith-new/src/main/webapp/WEB-INF/jsp/admin/shipping/list.jsp` | `src/main/webapp/WEB-INF/jsp/admin/shipping/list.jsp` | Same patterns. Edit link passes `?code=` matching controller |
| `monolith-new/src/main/webapp/WEB-INF/jsp/admin/shipping/edit.jsp` | `src/main/webapp/WEB-INF/jsp/admin/shipping/edit.jsp` | Checkbox active field + form bean `shippingForm` matching controller `@ModelAttribute("shippingForm")` |

## Struts-to-JSTL Tag Mapping Applied

| Struts Tag | JSTL/HTML Replacement |
|-----------|----------------------|
| `<html:link page="..." paramId="..." paramName="..." paramProperty="...">` | `<a href="<c:url value='...'/>?param=<c:out value='${...}'/>">` |
| `<html:form action="..." method="post">` | `<form action="<c:url value='...'/>" method="post">` |
| `<html:text property="..." size="..."/>` | `<input type="text" name="..." value="<c:out value='${form.field}'/>" size="..."/>` |
| `<html:textarea property="..." cols="..." rows="...">` | `<textarea name="..." cols="..." rows="..."><c:out value="${form.field}"/></textarea>` |
| `<html:hidden property="..."/>` | `<input type="hidden" name="..." value="<c:out value='${form.field}'/>"/>` |
| `<html:checkbox property="..."/>` | `<input type="checkbox" name="..." value="true" <c:if test="${form.field}">checked="checked"</c:if>/>` |
| `<html:token/>` | `<input type="hidden" name="_csrfToken" value="${_csrfToken}"/>` |
| `<html:submit value="..."/>` | `<button type="submit">...</button>` |
| `<bean:write name="..." property="..." filter="true"/>` | `<c:out value="${obj.property}"/>` |
| `<bean:message key="..."/>` | Literal Chinese text (hardcoded, matching source display) |
| `<logic:iterate id="..." name="...">` | `<c:forEach var="..." items="${...}">` |
| `<logic:empty name="...">` | `<c:if test="${empty ...}">` |
| `<logic:notEmpty name="...">` | `<c:if test="${not empty ...}">` |
| `<logic:present name="...">` | `<c:if test="${not empty ...}">` |
| `<logic:notPresent name="...">` | `<c:if test="${empty ...}">` |
| `<logic:equal name="..." property="..." value="...">` | `<c:if test="${obj.property == 'value'}">` |
| `<bean:define id="..." name="..." property="..." type="..."/>` | Eliminated (JSTL accesses directly) |

## Model Attribute Contract (Controller → JSP)

| JSP | Model Attributes Used |
|-----|---------------------|
| `admin/products/list` | `products` (List<Product>) |
| `admin/products/edit` | `productForm` (AdminProductForm), `error` (String), `updatedAt` (String) |
| `admin/orders/list` | `orders` (List<Order>) |
| `admin/orders/detail` | `order` (Order), `orderItems` (List<OrderItem>) |
| `admin/coupons/list` | `coupons` (List<Coupon>) |
| `admin/coupons/edit` | `couponForm` (AdminCouponForm), `error` (String) |
| `admin/shipping/list` | `shippingMethods` (List<ShippingMethod>) |
| `admin/shipping/edit` | `shippingForm` (AdminShippingMethodForm), `error` (String) |
