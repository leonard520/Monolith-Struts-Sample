# t16 — Admin JSPs

## Summary
8 admin JSP files created, migrating all Struts tags (`<html:link>`, `<html:form>`, `<bean:write>`, `<logic:iterate>`, `<logic:empty>`, `<logic:present>`, `<logic:equal>`, `<html:token>`, `<bean:message>`) to JSTL (`<c:url>`, `<c:out>`, `<c:forEach>`, `<c:if>`) with standard HTML forms. All pages follow the t6 layout pattern (full HTML document with header/messages/footer includes).

## Deliverables
- [t16-frontend-files.md](./t16-frontend-files.md) — file inventory with migration details

## Migration Mapping

| Source JSP | Target JSP | Controller | View Name |
|-----------|-----------|-----------|-----------|
| `admin/products/list.jsp` | `admin/products/list.jsp` | `AdminProductController#list()` | `admin/products/list` |
| `admin/products/edit.jsp` | `admin/products/edit.jsp` | `AdminProductController#showEdit()/save()` | `admin/products/edit` |
| `admin/orders/list.jsp` | `admin/orders/list.jsp` | `AdminOrderController#list()` | `admin/orders/list` |
| `admin/orders/detail.jsp` | `admin/orders/detail.jsp` | `AdminOrderController#detail()` | `admin/orders/detail` |
| `admin/coupons/list.jsp` | `admin/coupons/list.jsp` | `AdminCouponController#list()` | `admin/coupons/list` |
| `admin/coupons/edit.jsp` | `admin/coupons/edit.jsp` | `AdminCouponController#showEdit()/save()` | `admin/coupons/edit` |
| `admin/shipping/list.jsp` | `admin/shipping/list.jsp` | `AdminShippingController#list()` | `admin/shipping/list` |
| `admin/shipping/edit.jsp` | `admin/shipping/edit.jsp` | `AdminShippingController#showEdit()/save()` | `admin/shipping/edit` |

## Build Status
`mvn -f monolith-new/pom.xml clean package -DskipTests -q` — **PASS** (zero errors, all 8 JSPs in JAR under `META-INF/resources/`)
