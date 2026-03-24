## Phase 2: Cross-Cutting Infrastructure

**Batch**: 2
**Goal**: Implement authentication, CSRF, error handling, layout, and filter infrastructure before any controller.
**Exit Criteria**: Auth + CSRF interceptors functional. Layout template renders. Error handler catches exceptions. `mvn -B compile` passes.

---

### 2.1 AuthInterceptor

- [ ] T200 [Plan:2.1] Create `AuthInterceptor` implementing `HandlerInterceptor` in `src/main/java/com/skishop/web/interceptor/AuthInterceptor.java`. In `preHandle`: check `HttpSession` for `loginUser` attribute; if null on a protected path, redirect to `/login` and return `false`. If user present but role insufficient, send 403. Define URL→role mapping derived from struts-config.xml: `/cart/**`, `/checkout/**`, `/orders/**`, `/points/**`, `/coupons/apply`, `/account/**` require role `USER` or `ADMIN`; `/admin/**` requires role `ADMIN`. Public paths (`/home`, `/login`, `/register`, `/products`, `/product`, `/logout`, `/password/**`, `/coupons` (GET list)) must pass through without auth check.
- [ ] T201 [P] [Plan:2.1] Create unit test `src/test/java/com/skishop/web/interceptor/AuthInterceptorTest.java`. Test scenarios: (a) unauthenticated request to protected path → redirect to `/login`; (b) authenticated USER accessing USER path → allowed; (c) authenticated USER accessing ADMIN path → 403; (d) authenticated ADMIN accessing ADMIN path → allowed; (e) request to public path (no session) → allowed.

### 2.2 CsrfInterceptor

- [ ] T202 [Plan:2.2] Create `CsrfInterceptor` implementing `HandlerInterceptor` in `src/main/java/com/skishop/web/interceptor/CsrfInterceptor.java`. In `preHandle`: on non-POST requests, generate a UUID-based CSRF token and store in `HttpSession` under key `_csrfToken` if not already present. On POST requests: read `_csrfToken` parameter from request; compare against session token; if missing or mismatched, send 403 and return `false`; on match, reset (remove) the session token so it is regenerated on next GET. This matches the existing `AuthRequestProcessor` + `TokenProcessor` behavior.
- [ ] T203 [P] [Plan:2.2] Create unit test `src/test/java/com/skishop/web/interceptor/CsrfInterceptorTest.java`. Test scenarios: (a) GET request generates token in session when missing; (b) GET request preserves existing token; (c) POST with valid token → allowed, token reset; (d) POST with invalid token → 403; (e) POST with no token parameter → 403.

### 2.3 CsrfTokenTag (Custom JSP Tag)

- [ ] T204 [Plan:2.3] Create `CsrfTokenTag` JSP tag class in `src/main/java/com/skishop/web/tag/CsrfTokenTag.java`. Extends `TagSupport`. In `doStartTag`: read `_csrfToken` from `HttpSession`; write `<input type="hidden" name="_csrfToken" value="{token}" />` to `JspWriter`. This replaces the existing Struts-dependent `TokenTag` that uses `Globals.TRANSACTION_TOKEN_KEY` and `Constants.TOKEN_KEY`. Remove the old `TokenTag.java` file (or overwrite it).
- [ ] T205 [Plan:2.3] Create TLD file `src/main/webapp/WEB-INF/tags/skishop.tld`. Register `CsrfTokenTag` with tag name `csrfToken` under URI `http://skishop.com/tags` (or similar). JSPs will use `<%@ taglib uri="http://skishop.com/tags" prefix="skishop" %>` and `<skishop:csrfToken/>` to output the hidden field.

### 2.4 WebMvcConfig

- [ ] T206 [Plan:2.4] Create `WebMvcConfig` `@Configuration` class in `src/main/java/com/skishop/web/config/WebMvcConfig.java` implementing `WebMvcConfigurer`. Override `addInterceptors(InterceptorRegistry)`: register `CsrfInterceptor` for `/**` (all paths); register `AuthInterceptor` for `/**` excluding static resources (`/assets/**`). Both interceptors should be Spring-managed beans (use `@Component` or `@Bean` factory methods). Configure interceptor ordering: CsrfInterceptor before AuthInterceptor.

### 2.5 GlobalExceptionHandler

- [ ] T207 [Plan:2.5] Create `GlobalExceptionHandler` with `@ControllerAdvice` in `src/main/java/com/skishop/web/handler/GlobalExceptionHandler.java`. Add `@ExceptionHandler(Exception.class)` method: log the exception, set `errorMessage` model attribute, return view name `error/general`. This replicates the Struts `global-exceptions` entry that maps `java.lang.Exception` to `/error.jsp`.
- [ ] T208 [Plan:2.5] Create `src/main/webapp/WEB-INF/jsp/error/general.jsp`. Include base layout via `<jsp:include>`. Display `${errorMessage}` in the page body. Set page title to error label from messages.properties.
- [ ] T209 [P] [Plan:2.5] Create unit test `src/test/java/com/skishop/web/handler/GlobalExceptionHandlerTest.java`. Test: (a) unhandled `RuntimeException` → resolves to `error/general` view with `errorMessage` attribute; (b) `IllegalArgumentException` → same error view.

### 2.6 JSP Layout Template

- [ ] T210 [Plan:2.6] Rewrite `src/main/webapp/WEB-INF/jsp/layouts/base.jsp`. Remove all Tiles taglibs (`tiles:getAsString`, `tiles:insert`) and Struts taglibs (`html:rewrite`). Replace with: `<title>${pageTitle}</title>` (set by controllers as request attribute); `<jsp:include page="/WEB-INF/jsp/common/header.jsp"/>` for header; `<jsp:include page="/WEB-INF/jsp/common/messages.jsp"/>` for messages; `<jsp:include page="${bodyContent}"/>` for body (controllers set `bodyContent` request attribute); `<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>` for footer. Use `<c:url value='/assets/css/app.css'/>` for static resource link.
- [ ] T211 [P] [Plan:2.6] Rewrite `src/main/webapp/WEB-INF/jsp/common/header.jsp`. Remove Struts taglibs. Use JSTL: `<c:url>` for navigation links, `<c:if test="${not empty sessionScope.loginUser}">` for user-specific nav (logout, account), `<c:if test="${sessionScope.loginUser.role == 'ADMIN'}">` for admin links.
- [ ] T212 [P] [Plan:2.6] Rewrite `src/main/webapp/WEB-INF/jsp/common/footer.jsp`. Remove any Struts tag references. Use plain HTML and JSTL if needed.
- [ ] T213 [P] [Plan:2.6] Rewrite `src/main/webapp/WEB-INF/jsp/common/messages.jsp`. Replace `<html:errors/>` and `<logic:messagesPresent>` with JSTL: use `<c:if test="${not empty errors}"><div class="messages"><c:forEach items="${errors}" var="msg">...</c:forEach></div></c:if>`. Support both error and success message display from model attributes set by controllers.

### 2.7 RequestIdFilter Conversion

- [ ] T214 [Plan:2.7] Rewrite `src/main/java/com/skishop/web/filter/RequestIdFilter.java` as Spring `@Component` implementing `jakarta.servlet.Filter`. Update all imports from `javax.servlet.*` to `jakarta.servlet.*`. Replace `org.apache.log4j.MDC` with `org.slf4j.MDC`. Preserve existing behavior: read `X-Request-Id` header, fall back to UUID, set MDC key `reqId`, set response header, clean up MDC in `finally` block.
- [ ] T215 [P] [Plan:2.7] Add UTF-8 encoding configuration to `src/main/resources/application.properties`: `server.servlet.encoding.charset=UTF-8`, `server.servlet.encoding.enabled=true`, `server.servlet.encoding.force=true`. This replaces the `CharacterEncodingFilter` from `web.xml`.

### 2.8 MessageSource Configuration

- [ ] T216 [Plan:2.8] Configure `MessageSource` bean. Add a `@Bean` method in `WebMvcConfig` (or a dedicated `MessageConfig` class) returning `ReloadableResourceBundleMessageSource` with basename `classpath:messages` and default encoding `UTF-8`. This enables `<spring:message>` tag in JSPs and `MessageSource` injection in controllers for resolving keys from `messages.properties`.

### 2.9 PasswordHasher Preservation

- [ ] T217 [Plan:2.9] Verify and update `src/main/java/com/skishop/common/util/PasswordHasher.java` for JDK 21 compilation. Replace the `"UTF-8"` string in `getBytes("UTF-8")` with `java.nio.charset.StandardCharsets.UTF_8` to eliminate `UnsupportedEncodingException`. Update the `hash()` method signature if needed (remove checked exception). Preserve all field values (`HASH_ITERATIONS = 1000`), algorithm (`SHA-256`), salt generation, matching logic, and constant-time comparison exactly.

### 2.10 Compile + Unit Test Gate

- [ ] T218 [Plan:2.10] Run `mvn -B compile` — verify all Phase 2 source code (interceptors, tags, config, handler, filter, layout JSPs) compiles with zero errors.
- [ ] T219 [Plan:2.10] Run `mvn -B test -pl . -Dtest="AuthInterceptorTest,CsrfInterceptorTest,GlobalExceptionHandlerTest"` — verify all Phase 2 unit tests pass. Fix any failures before proceeding to Phase 3.
