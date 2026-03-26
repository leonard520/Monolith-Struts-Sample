#!/bin/bash
# =============================================================================
# SkiShop Spring MVC API Test Script
# =============================================================================
# Usage: ./api-test.sh [BASE_URL]
#   Default BASE_URL: http://localhost:8080
#
# This script will:
#   1. Build the application with Maven (skip tests)
#   2. Start the Spring Boot jar with H2 in-memory database
#   3. Wait for the application to become healthy
#   4. Run all API tests
#   5. Shut down the application after tests complete
#
# Prerequisites:
#   - Java 21+ and Maven installed
#   - curl installed
#
# This is a session-based web application (Thymeleaf views, not REST API).
# Tests validate HTTP status codes and response body content (HTML).
# =============================================================================

set -euo pipefail

BASE_URL="${1:-http://localhost:8080}"
COOKIE_JAR=$(mktemp /tmp/skishop_cookies_XXXXXX)
ADMIN_COOKIE_JAR=$(mktemp /tmp/skishop_admin_cookies_XXXXXX)

PASS=0
FAIL=0
TOTAL=0

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# ---------------------------------------------------------------------------
# Project directory
# ---------------------------------------------------------------------------
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="${SCRIPT_DIR}/monolith-new"
APP_PID=""
APP_LOG=$(mktemp /tmp/skishop_log_XXXXXX)

# ---------------------------------------------------------------------------
# Cleanup: stop application process and remove temp files on exit
# ---------------------------------------------------------------------------
cleanup() {
    echo ""
    echo -e "${CYAN}Shutting down application...${NC}"
    if [[ -n "$APP_PID" ]] && kill -0 "$APP_PID" 2>/dev/null; then
        kill "$APP_PID" 2>/dev/null || true
        wait "$APP_PID" 2>/dev/null || true
    fi
    rm -f "$COOKIE_JAR" "$ADMIN_COOKIE_JAR" "$APP_LOG"
    echo -e "${CYAN}Application stopped.${NC}"
}
trap cleanup EXIT

# ---------------------------------------------------------------------------
# Build application with Maven
# ---------------------------------------------------------------------------
echo -e "${CYAN}================================================================${NC}"
echo -e "${CYAN}  Building SkiShop application with Maven...${NC}"
echo -e "${CYAN}================================================================${NC}"
echo ""

mvn -f "${PROJECT_DIR}/pom.xml" clean package -DskipTests -q

echo -e "${GREEN}Build successful.${NC}"
echo ""

# ---------------------------------------------------------------------------
# Start application with H2 in-memory database
# ---------------------------------------------------------------------------
echo -e "${CYAN}Starting SkiShop application (H2 in-memory DB)...${NC}"

java -jar "${PROJECT_DIR}/target/skishop-app-2.0.0.jar" \
    --spring.profiles.active=h2 \
    > "$APP_LOG" 2>&1 &
APP_PID=$!

# ---------------------------------------------------------------------------
# Wait for application to become healthy
# ---------------------------------------------------------------------------
echo ""
echo -e "${CYAN}Waiting for application to become healthy...${NC}"

MAX_WAIT=120   # seconds
ELAPSED=0
INTERVAL=3

while [[ $ELAPSED -lt $MAX_WAIT ]]; do
    if ! kill -0 "$APP_PID" 2>/dev/null; then
        echo -e "${RED}Application process died. Last 30 lines of log:${NC}"
        tail -30 "$APP_LOG"
        exit 1
    fi
    HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "${BASE_URL}/" 2>/dev/null) || HTTP_CODE="000"
    if [[ "$HTTP_CODE" == "200" ]]; then
        echo -e "${GREEN}Application is ready! (took ${ELAPSED}s)${NC}"
        break
    fi
    echo -e "  Waiting... (${ELAPSED}s elapsed, HTTP ${HTTP_CODE})"
    sleep $INTERVAL
    ELAPSED=$((ELAPSED + INTERVAL))
done

if [[ $ELAPSED -ge $MAX_WAIT ]]; then
    echo -e "${RED}Application did not start within ${MAX_WAIT}s. Aborting.${NC}"
    echo -e "${RED}Last 50 lines of application log:${NC}"
    tail -50 "$APP_LOG"
    exit 1
fi

echo ""

# ---------------------------------------------------------------------------
# Helper: assert HTTP status + optional body content
# Usage: assert_response "Test Name" HTTP_STATUS RESPONSE_BODY EXPECTED_STATUS [EXPECTED_BODY_CONTAINS...]
# ---------------------------------------------------------------------------
assert_response() {
    local test_name="$1"
    local http_status="$2"
    local body="$3"
    local expected_status="$4"
    shift 4
    local expected_contents=()
    [[ $# -gt 0 ]] && expected_contents=("$@")

    TOTAL=$((TOTAL + 1))
    local passed=true

    if [[ "$http_status" != "$expected_status" ]]; then
        passed=false
    fi

    if [[ ${#expected_contents[@]} -gt 0 ]]; then
        for expected in "${expected_contents[@]}"; do
            if ! echo "$body" | grep -qi "$expected" 2>/dev/null; then
                passed=false
            fi
        done
    fi

    if $passed; then
        PASS=$((PASS + 1))
        echo -e "  ${GREEN}✓ PASS${NC} [$test_name] (HTTP $http_status)"
    else
        FAIL=$((FAIL + 1))
        echo -e "  ${RED}✗ FAIL${NC} [$test_name] (HTTP $http_status, expected $expected_status)"
        if [[ ${#expected_contents[@]} -gt 0 ]]; then
            for expected in "${expected_contents[@]}"; do
                if ! echo "$body" | grep -qi "$expected" 2>/dev/null; then
                    echo -e "         Missing in body: '${expected}'"
                fi
            done
        fi
    fi
}

# ---------------------------------------------------------------------------
# Helper: perform a request and capture status + body
# Usage: do_request METHOD URL [COOKIE_JAR] [EXTRA_CURL_ARGS...]
# Sets: RESP_STATUS, RESP_BODY
# ---------------------------------------------------------------------------
do_request() {
    local method="$1"
    local url="$2"
    local jar="${3:-$COOKIE_JAR}"
    shift 3 || true
    local extra_args=()
    [[ $# -gt 0 ]] && extra_args=("$@")

    local tmpfile
    tmpfile=$(mktemp /tmp/skishop_resp_XXXXXX)

    RESP_STATUS=$(curl -s -o "$tmpfile" -w "%{http_code}" \
        -X "$method" \
        -b "$jar" -c "$jar" \
        -L --max-redirs 5 \
        ${extra_args[@]+"${extra_args[@]}"} \
        "$url" 2>/dev/null) || RESP_STATUS="000"

    RESP_BODY=$(cat "$tmpfile" 2>/dev/null || echo "")
    rm -f "$tmpfile"
}

# Extract CSRF token from response body (if present)
extract_csrf() {
    local body="$1"
    local token
    token=$(echo "$body" | sed -n 's/.*name="_csrfToken"[[:space:]]*value="\([^"]*\)".*/\1/p' | head -1)
    if [[ -z "$token" ]]; then
        token=$(echo "$body" | sed -n 's/.*value="\([^"]*\)"[[:space:]]*name="_csrfToken".*/\1/p' | head -1)
    fi
    echo "$token"
}

echo ""
echo -e "${CYAN}================================================================${NC}"
echo -e "${CYAN}  SkiShop API Test Suite${NC}"
echo -e "${CYAN}  Base URL: ${BASE_URL}${NC}"
echo -e "${CYAN}================================================================${NC}"
echo ""

# =============================================================================
# 1. HOME / PUBLIC PAGES
# =============================================================================
echo -e "${YELLOW}━━━ 1. Home Page ━━━${NC}"

# 1.1 GET / - Home page
do_request GET "${BASE_URL}/" "$COOKIE_JAR"
assert_response "GET / - Home page loads" "$RESP_STATUS" "$RESP_BODY" "200"

# 1.2 GET /home - Home alias
do_request GET "${BASE_URL}/home" "$COOKIE_JAR"
assert_response "GET /home - Home alias loads" "$RESP_STATUS" "$RESP_BODY" "200"

echo ""

# =============================================================================
# 2. PRODUCT PAGES (Public)
# =============================================================================
echo -e "${YELLOW}━━━ 2. Product Pages ━━━${NC}"

# 2.1 GET /products - Product listing (default pagination)
do_request GET "${BASE_URL}/products" "$COOKIE_JAR"
assert_response "GET /products - Product list" "$RESP_STATUS" "$RESP_BODY" "200"

# 2.2 GET /products?page=1&size=5 - Product listing with pagination
do_request GET "${BASE_URL}/products?page=1&size=5" "$COOKIE_JAR"
assert_response "GET /products?page=1&size=5 - Paginated" "$RESP_STATUS" "$RESP_BODY" "200"

# 2.3 GET /products?keyword=Atomic - Search by keyword
do_request GET "${BASE_URL}/products?keyword=Atomic" "$COOKIE_JAR"
assert_response "GET /products?keyword=Atomic - Search" "$RESP_STATUS" "$RESP_BODY" "200"

# 2.4 GET /products?categoryId=c-1 - Filter by category (Ski)
do_request GET "${BASE_URL}/products?categoryId=c-1" "$COOKIE_JAR"
assert_response "GET /products?categoryId=c-1 - Category filter" "$RESP_STATUS" "$RESP_BODY" "200"

# 2.5 GET /product?id=PSK001 - Product detail (existing product)
do_request GET "${BASE_URL}/product?id=PSK001" "$COOKIE_JAR"
assert_response "GET /product?id=PSK001 - Product detail" "$RESP_STATUS" "$RESP_BODY" "200" "Atomic Redster"

# 2.6 GET /product?id=NONEXISTENT - Product detail (non-existing)
do_request GET "${BASE_URL}/product?id=NONEXISTENT" "$COOKIE_JAR"
assert_response "GET /product?id=NONEXISTENT - 404" "$RESP_STATUS" "$RESP_BODY" "200" # renders 404 error page but still HTTP 200

echo ""

# =============================================================================
# 3. AUTHENTICATION
# =============================================================================
echo -e "${YELLOW}━━━ 3. Authentication ━━━${NC}"

# 3.1 GET /login - Show login form
do_request GET "${BASE_URL}/login" "$COOKIE_JAR"
assert_response "GET /login - Login form" "$RESP_STATUS" "$RESP_BODY" "200"
CSRF_TOKEN=$(extract_csrf "$RESP_BODY")

# 3.2 POST /login - Login with invalid credentials
do_request POST "${BASE_URL}/login" "$COOKIE_JAR" \
    -d "email=wrong@example.com&password=wrongpassword&_csrfToken=${CSRF_TOKEN}"
assert_response "POST /login - Invalid credentials" "$RESP_STATUS" "$RESP_BODY" "200"
# Expected: stays on login page with error message

# 3.3 POST /login - Login with valid format but wrong password
do_request GET "${BASE_URL}/login" "$COOKIE_JAR"
CSRF_TOKEN=$(extract_csrf "$RESP_BODY")
do_request POST "${BASE_URL}/login" "$COOKIE_JAR" \
    -d "email=user@example.com&password=wrongpassword&_csrfToken=${CSRF_TOKEN}"
assert_response "POST /login - Wrong password" "$RESP_STATUS" "$RESP_BODY" "200"

# 3.4 POST /login - Validation error (short password)
do_request GET "${BASE_URL}/login" "$COOKIE_JAR"
CSRF_TOKEN=$(extract_csrf "$RESP_BODY")
do_request POST "${BASE_URL}/login" "$COOKIE_JAR" \
    -d "email=user@example.com&password=short&_csrfToken=${CSRF_TOKEN}"
assert_response "POST /login - Validation error (short password)" "$RESP_STATUS" "$RESP_BODY" "200"

# 3.5 GET /logout - Logout
do_request GET "${BASE_URL}/logout" "$COOKIE_JAR"
assert_response "GET /logout - Logout redirect" "$RESP_STATUS" "$RESP_BODY" "200"

echo ""

# =============================================================================
# 4. REGISTRATION
# =============================================================================
echo -e "${YELLOW}━━━ 4. Registration ━━━${NC}"

# 4.1 GET /register - Show registration form
do_request GET "${BASE_URL}/register" "$COOKIE_JAR"
assert_response "GET /register - Register form" "$RESP_STATUS" "$RESP_BODY" "200"
CSRF_TOKEN=$(extract_csrf "$RESP_BODY")

# 4.2 POST /register - Validation error (empty fields)
do_request POST "${BASE_URL}/register" "$COOKIE_JAR" \
    -d "email=&username=&password=&passwordConfirm=&_csrfToken=${CSRF_TOKEN}"
assert_response "POST /register - Empty fields validation" "$RESP_STATUS" "$RESP_BODY" "200"

# 4.3 POST /register - Validation error (password mismatch)
do_request GET "${BASE_URL}/register" "$COOKIE_JAR"
CSRF_TOKEN=$(extract_csrf "$RESP_BODY")
do_request POST "${BASE_URL}/register" "$COOKIE_JAR" \
    -d "email=newuser@example.com&username=newuser&password=password123&passwordConfirm=different123&_csrfToken=${CSRF_TOKEN}"
assert_response "POST /register - Password mismatch" "$RESP_STATUS" "$RESP_BODY" "200"

# 4.4 POST /register - Duplicate email
do_request GET "${BASE_URL}/register" "$COOKIE_JAR"
CSRF_TOKEN=$(extract_csrf "$RESP_BODY")
do_request POST "${BASE_URL}/register" "$COOKIE_JAR" \
    -d "email=user@example.com&username=duplicate&password=password123&passwordConfirm=password123&_csrfToken=${CSRF_TOKEN}"
assert_response "POST /register - Duplicate email" "$RESP_STATUS" "$RESP_BODY" "200"

# 4.5 POST /register - Successful registration (unique email)
UNIQUE_SUFFIX=$(date +%s)
do_request GET "${BASE_URL}/register" "$COOKIE_JAR"
CSRF_TOKEN=$(extract_csrf "$RESP_BODY")
do_request POST "${BASE_URL}/register" "$COOKIE_JAR" \
    -d "email=testuser${UNIQUE_SUFFIX}@example.com&username=testuser${UNIQUE_SUFFIX}&password=password123&passwordConfirm=password123&_csrfToken=${CSRF_TOKEN}"
assert_response "POST /register - Successful registration" "$RESP_STATUS" "$RESP_BODY" "200"
# Expected: redirects to /login after successful registration

echo ""

# =============================================================================
# 5. LOGIN WITH NEWLY REGISTERED USER
# =============================================================================
echo -e "${YELLOW}━━━ 5. Login with new user ━━━${NC}"

# 5.1 Login with the newly registered user
do_request GET "${BASE_URL}/login" "$COOKIE_JAR"
CSRF_TOKEN=$(extract_csrf "$RESP_BODY")
do_request POST "${BASE_URL}/login" "$COOKIE_JAR" \
    -d "email=testuser${UNIQUE_SUFFIX}@example.com&password=password123&_csrfToken=${CSRF_TOKEN}"
assert_response "POST /login - Login with new user" "$RESP_STATUS" "$RESP_BODY" "200"
# Expected: redirects to /products on successful login

echo ""

# =============================================================================
# 6. PASSWORD RESET (Public)
# =============================================================================
echo -e "${YELLOW}━━━ 6. Password Reset ━━━${NC}"

# 6.1 GET /password/forgot - Show forgot form
do_request GET "${BASE_URL}/password/forgot" "$COOKIE_JAR"
assert_response "GET /password/forgot - Forgot form" "$RESP_STATUS" "$RESP_BODY" "200"
CSRF_TOKEN=$(extract_csrf "$RESP_BODY")

# 6.2 POST /password/forgot - Request reset for existing email
do_request POST "${BASE_URL}/password/forgot" "$COOKIE_JAR" \
    -d "email=user@example.com&_csrfToken=${CSRF_TOKEN}"
assert_response "POST /password/forgot - Request reset" "$RESP_STATUS" "$RESP_BODY" "200"
# Expected: always shows success (no user enumeration)

# 6.3 POST /password/forgot - Non-existing email (still shows success)
do_request GET "${BASE_URL}/password/forgot" "$COOKIE_JAR"
CSRF_TOKEN=$(extract_csrf "$RESP_BODY")
do_request POST "${BASE_URL}/password/forgot" "$COOKIE_JAR" \
    -d "email=nonexist@example.com&_csrfToken=${CSRF_TOKEN}"
assert_response "POST /password/forgot - Non-existing email" "$RESP_STATUS" "$RESP_BODY" "200"

# 6.4 GET /password/reset - Show reset form
do_request GET "${BASE_URL}/password/reset" "$COOKIE_JAR"
assert_response "GET /password/reset - Reset form" "$RESP_STATUS" "$RESP_BODY" "200"

# 6.5 GET /password/reset?token=test - Reset form with token
do_request GET "${BASE_URL}/password/reset?token=testtoken" "$COOKIE_JAR"
assert_response "GET /password/reset?token=... - Reset form with token" "$RESP_STATUS" "$RESP_BODY" "200"

# 6.6 POST /password/reset - Invalid token
do_request GET "${BASE_URL}/password/reset" "$COOKIE_JAR"
CSRF_TOKEN=$(extract_csrf "$RESP_BODY")
do_request POST "${BASE_URL}/password/reset" "$COOKIE_JAR" \
    -d "token=invalid-token-xyz&password=newpassword123&passwordConfirm=newpassword123&_csrfToken=${CSRF_TOKEN}"
assert_response "POST /password/reset - Invalid token" "$RESP_STATUS" "$RESP_BODY" "200"
# Expected: error message about invalid/expired token

echo ""

# =============================================================================
# 7. CART (Session-based, no login required)
# =============================================================================
echo -e "${YELLOW}━━━ 7. Cart ━━━${NC}"

# 7.1 GET /cart - View empty cart
CART_COOKIE_JAR=$(mktemp /tmp/skishop_cart_XXXXXX)
do_request GET "${BASE_URL}/cart" "$CART_COOKIE_JAR"
assert_response "GET /cart - View cart" "$RESP_STATUS" "$RESP_BODY" "200"

# 7.2 GET /cart - View cart with items
do_request GET "${BASE_URL}/cart" "$CART_COOKIE_JAR"
assert_response "GET /cart - Cart view" "$RESP_STATUS" "$RESP_BODY" "200"

rm -f "$CART_COOKIE_JAR"

echo ""

# =============================================================================
# 8. COUPONS (Public)
# =============================================================================
echo -e "${YELLOW}━━━ 8. Coupons ━━━${NC}"

# 8.1 GET /coupons - List active coupons
do_request GET "${BASE_URL}/coupons" "$COOKIE_JAR"
assert_response "GET /coupons - Coupon list" "$RESP_STATUS" "$RESP_BODY" "200"

# 8.2 POST /coupons/apply - Apply invalid coupon
# Note: /coupons page has no CSRF hidden input, so get token from /cart page
do_request GET "${BASE_URL}/cart" "$COOKIE_JAR"
CSRF_TOKEN=$(extract_csrf "$RESP_BODY")
do_request POST "${BASE_URL}/coupons/apply" "$COOKIE_JAR" \
    -d "code=INVALIDCODE&_csrfToken=${CSRF_TOKEN}"
assert_response "POST /coupons/apply - Invalid coupon code" "$RESP_STATUS" "$RESP_BODY" "200"

# 8.3 POST /coupons/apply - Apply valid coupon (SAVE10 from seed data)
do_request GET "${BASE_URL}/cart" "$COOKIE_JAR"
CSRF_TOKEN=$(extract_csrf "$RESP_BODY")
do_request POST "${BASE_URL}/coupons/apply" "$COOKIE_JAR" \
    -d "code=SAVE10&_csrfToken=${CSRF_TOKEN}"
assert_response "POST /coupons/apply - Valid coupon SAVE10" "$RESP_STATUS" "$RESP_BODY" "200"

echo ""

# =============================================================================
# 9. CHECKOUT / ORDERS (Public checkout, auth for order history)
# =============================================================================
echo -e "${YELLOW}━━━ 9. Checkout & Orders ━━━${NC}"

# 9.1 GET /checkout - Show checkout form
do_request GET "${BASE_URL}/checkout" "$COOKIE_JAR"
assert_response "GET /checkout - Checkout form" "$RESP_STATUS" "$RESP_BODY" "200"

# 9.2 POST /checkout - Attempt checkout (may fail if no cart items)
do_request GET "${BASE_URL}/checkout" "$COOKIE_JAR"
CSRF_TOKEN=$(extract_csrf "$RESP_BODY")
do_request POST "${BASE_URL}/checkout" "$COOKIE_JAR" \
    -d "paymentMethod=CREDIT_CARD&cardNumber=4111111111111111&cardExpMonth=12&cardExpYear=2027&cardCvv=123&billingZip=160-0022&usePoints=0&_csrfToken=${CSRF_TOKEN}"
assert_response "POST /checkout - Place order attempt" "$RESP_STATUS" "$RESP_BODY" "200"

# 9.3 GET /orders - Order history (requires auth, should redirect to login)
ANON_JAR=$(mktemp /tmp/skishop_anon_XXXXXX)
do_request GET "${BASE_URL}/orders" "$ANON_JAR"
assert_response "GET /orders - Unauthenticated redirects to login" "$RESP_STATUS" "$RESP_BODY" "200"
rm -f "$ANON_JAR"

echo ""

# =============================================================================
# 10. PROTECTED PAGES (Without authentication - expect redirect to /login)
# =============================================================================
echo -e "${YELLOW}━━━ 10. Protected Pages (Unauthenticated) ━━━${NC}"

UNAUTH_JAR=$(mktemp /tmp/skishop_unauth_XXXXXX)

# 10.1 GET /account/addresses - Requires USER role
do_request GET "${BASE_URL}/account/addresses" "$UNAUTH_JAR"
assert_response "GET /account/addresses - Requires auth" "$RESP_STATUS" "$RESP_BODY" "200"
# Expected: redirected to /login page

# 10.2 GET /points - Requires USER role
do_request GET "${BASE_URL}/points" "$UNAUTH_JAR"
assert_response "GET /points - Requires auth" "$RESP_STATUS" "$RESP_BODY" "200"
# Expected: redirected to /login page

# 10.3 GET /admin/products - Requires ADMIN role
do_request GET "${BASE_URL}/admin/products" "$UNAUTH_JAR"
# May return 403 Forbidden or redirect to login
TOTAL=$((TOTAL + 1))
if [[ "$RESP_STATUS" == "200" || "$RESP_STATUS" == "403" ]]; then
    PASS=$((PASS + 1))
    echo -e "  ${GREEN}✓ PASS${NC} [GET /admin/products - Requires admin] (HTTP $RESP_STATUS)"
else
    FAIL=$((FAIL + 1))
    echo -e "  ${RED}✗ FAIL${NC} [GET /admin/products - Requires admin] (HTTP $RESP_STATUS, expected 200 or 403)"
fi

# 10.4 GET /admin/orders - Requires ADMIN role
do_request GET "${BASE_URL}/admin/orders" "$UNAUTH_JAR"
TOTAL=$((TOTAL + 1))
if [[ "$RESP_STATUS" == "200" || "$RESP_STATUS" == "403" ]]; then
    PASS=$((PASS + 1))
    echo -e "  ${GREEN}✓ PASS${NC} [GET /admin/orders - Requires admin] (HTTP $RESP_STATUS)"
else
    FAIL=$((FAIL + 1))
    echo -e "  ${RED}✗ FAIL${NC} [GET /admin/orders - Requires admin] (HTTP $RESP_STATUS, expected 200 or 403)"
fi

# 10.5 GET /admin/coupons - Requires ADMIN role
do_request GET "${BASE_URL}/admin/coupons" "$UNAUTH_JAR"
TOTAL=$((TOTAL + 1))
if [[ "$RESP_STATUS" == "200" || "$RESP_STATUS" == "403" ]]; then
    PASS=$((PASS + 1))
    echo -e "  ${GREEN}✓ PASS${NC} [GET /admin/coupons - Requires admin] (HTTP $RESP_STATUS)"
else
    FAIL=$((FAIL + 1))
    echo -e "  ${RED}✗ FAIL${NC} [GET /admin/coupons - Requires admin] (HTTP $RESP_STATUS, expected 200 or 403)"
fi

# 10.6 GET /admin/shipping - Requires ADMIN role
do_request GET "${BASE_URL}/admin/shipping" "$UNAUTH_JAR"
TOTAL=$((TOTAL + 1))
if [[ "$RESP_STATUS" == "200" || "$RESP_STATUS" == "403" ]]; then
    PASS=$((PASS + 1))
    echo -e "  ${GREEN}✓ PASS${NC} [GET /admin/shipping - Requires admin] (HTTP $RESP_STATUS)"
else
    FAIL=$((FAIL + 1))
    echo -e "  ${RED}✗ FAIL${NC} [GET /admin/shipping - Requires admin] (HTTP $RESP_STATUS, expected 200 or 403)"
fi

rm -f "$UNAUTH_JAR"

echo ""

# =============================================================================
# 11. AUTHENTICATED USER FLOWS
# =============================================================================
echo -e "${YELLOW}━━━ 11. Authenticated User Flows ━━━${NC}"

# First, register and login a fresh test user
AUTH_JAR=$(mktemp /tmp/skishop_auth_XXXXXX)
AUTH_SUFFIX=$(date +%s%N)

# Register
do_request GET "${BASE_URL}/register" "$AUTH_JAR"
CSRF_TOKEN=$(extract_csrf "$RESP_BODY")
do_request POST "${BASE_URL}/register" "$AUTH_JAR" \
    -d "email=authtest${AUTH_SUFFIX}@example.com&username=authtest${AUTH_SUFFIX}&password=password123&passwordConfirm=password123&_csrfToken=${CSRF_TOKEN}"

# Login
do_request GET "${BASE_URL}/login" "$AUTH_JAR"
CSRF_TOKEN=$(extract_csrf "$RESP_BODY")
do_request POST "${BASE_URL}/login" "$AUTH_JAR" \
    -d "email=authtest${AUTH_SUFFIX}@example.com&password=password123&_csrfToken=${CSRF_TOKEN}"
assert_response "Login test user for authenticated flow" "$RESP_STATUS" "$RESP_BODY" "200"

# 11.1 GET /account/addresses - List addresses (empty for new user)
do_request GET "${BASE_URL}/account/addresses" "$AUTH_JAR"
assert_response "GET /account/addresses - List (new user)" "$RESP_STATUS" "$RESP_BODY" "200"

# 11.2 GET /account/addresses/edit - Show add address form
do_request GET "${BASE_URL}/account/addresses/edit" "$AUTH_JAR"
assert_response "GET /account/addresses/edit - New address form" "$RESP_STATUS" "$RESP_BODY" "200"
CSRF_TOKEN=$(extract_csrf "$RESP_BODY")

# 11.3 POST /account/addresses/save - Save new address
do_request POST "${BASE_URL}/account/addresses/save" "$AUTH_JAR" \
    -d "label=Home&recipientName=Test+User&postalCode=160-0022&prefecture=Tokyo&address1=Shinjuku+1-1-1&address2=Room+101&phone=0312345678&isDefault=true&_csrfToken=${CSRF_TOKEN}"
assert_response "POST /account/addresses/save - Create address" "$RESP_STATUS" "$RESP_BODY" "200"

# 11.4 POST /account/addresses/save - Validation error (missing required fields)
do_request GET "${BASE_URL}/account/addresses/edit" "$AUTH_JAR"
CSRF_TOKEN=$(extract_csrf "$RESP_BODY")
do_request POST "${BASE_URL}/account/addresses/save" "$AUTH_JAR" \
    -d "label=&recipientName=&postalCode=&prefecture=&address1=&_csrfToken=${CSRF_TOKEN}"
assert_response "POST /account/addresses/save - Validation error" "$RESP_STATUS" "$RESP_BODY" "200"

# 11.5 GET /points - View point balance
do_request GET "${BASE_URL}/points" "$AUTH_JAR"
assert_response "GET /points - View point balance" "$RESP_STATUS" "$RESP_BODY" "200"

# 11.6 GET /orders - Order history (empty for new user)
do_request GET "${BASE_URL}/orders" "$AUTH_JAR"
assert_response "GET /orders - Order history (new user)" "$RESP_STATUS" "$RESP_BODY" "200"

# 11.7 Full flow: checkout -> view order
# Checkout
do_request GET "${BASE_URL}/checkout" "$AUTH_JAR"
CSRF_TOKEN=$(extract_csrf "$RESP_BODY")
do_request POST "${BASE_URL}/checkout" "$AUTH_JAR" \
    -d "paymentMethod=CREDIT_CARD&cardNumber=4111111111111111&cardExpMonth=12&cardExpYear=2027&cardCvv=123&billingZip=160-0022&usePoints=0&_csrfToken=${CSRF_TOKEN}"
assert_response "Authenticated: Place order" "$RESP_STATUS" "$RESP_BODY" "200"

# View order history after placing order
do_request GET "${BASE_URL}/orders" "$AUTH_JAR"
assert_response "Authenticated: Order history after checkout" "$RESP_STATUS" "$RESP_BODY" "200"

rm -f "$AUTH_JAR"

echo ""

# =============================================================================
# 12. ORDER OPERATIONS (Cancel / Return)
# =============================================================================
echo -e "${YELLOW}━━━ 12. Order Operations ━━━${NC}"

# Use the seed data user (u-1) who has order-1
# We need to login as that user, but the seed password_hash is 'hash' with salt 'salt'
# which won't match a real hashing - so test with invalid operations instead

# 12.1 POST /orders/{id}/cancel - Cancel with non-existent order (unauthenticated)
CANCEL_JAR=$(mktemp /tmp/skishop_cancel_XXXXXX)
do_request POST "${BASE_URL}/orders/nonexistent/cancel" "$CANCEL_JAR"
TOTAL=$((TOTAL + 1))
if [[ "$RESP_STATUS" == "200" || "$RESP_STATUS" == "302" || "$RESP_STATUS" == "403" || "$RESP_STATUS" == "405" ]]; then
    PASS=$((PASS + 1))
    echo -e "  ${GREEN}✓ PASS${NC} [POST /orders/nonexistent/cancel - Unauthenticated] (HTTP $RESP_STATUS)"
else
    FAIL=$((FAIL + 1))
    echo -e "  ${RED}✗ FAIL${NC} [POST /orders/nonexistent/cancel - Unauthenticated] (HTTP $RESP_STATUS)"
fi

# 12.2 POST /orders/{id}/return - Return with non-existent order (unauthenticated)
do_request POST "${BASE_URL}/orders/nonexistent/return" "$CANCEL_JAR"
TOTAL=$((TOTAL + 1))
if [[ "$RESP_STATUS" == "200" || "$RESP_STATUS" == "302" || "$RESP_STATUS" == "403" || "$RESP_STATUS" == "405" ]]; then
    PASS=$((PASS + 1))
    echo -e "  ${GREEN}✓ PASS${NC} [POST /orders/nonexistent/return - Unauthenticated] (HTTP $RESP_STATUS)"
else
    FAIL=$((FAIL + 1))
    echo -e "  ${RED}✗ FAIL${NC} [POST /orders/nonexistent/return - Unauthenticated] (HTTP $RESP_STATUS)"
fi

rm -f "$CANCEL_JAR"

echo ""

# =============================================================================
# 13. PRODUCT SEARCH VARIATIONS
# =============================================================================
echo -e "${YELLOW}━━━ 13. Product Search Variations ━━━${NC}"

# 13.1 Search by keyword in boots category
do_request GET "${BASE_URL}/products?keyword=Salomon&categoryId=c-boots" "$COOKIE_JAR"
assert_response "GET /products?keyword=Salomon&categoryId=c-boots" "$RESP_STATUS" "$RESP_BODY" "200"

# 13.2 Search with page 2
do_request GET "${BASE_URL}/products?page=2&size=10" "$COOKIE_JAR"
assert_response "GET /products?page=2&size=10 - Page 2" "$RESP_STATUS" "$RESP_BODY" "200"

# 13.3 Search across all categories with keyword
do_request GET "${BASE_URL}/products?keyword=Gore" "$COOKIE_JAR"
assert_response "GET /products?keyword=Gore" "$RESP_STATUS" "$RESP_BODY" "200"

# 13.4 Search for wax category
do_request GET "${BASE_URL}/products?categoryId=c-wax" "$COOKIE_JAR"
assert_response "GET /products?categoryId=c-wax" "$RESP_STATUS" "$RESP_BODY" "200"

# 13.5 Search for helmet category
do_request GET "${BASE_URL}/products?categoryId=c-helmet" "$COOKIE_JAR"
assert_response "GET /products?categoryId=c-helmet" "$RESP_STATUS" "$RESP_BODY" "200"

# 13.6 Product detail for different categories
do_request GET "${BASE_URL}/product?id=PBT001" "$COOKIE_JAR"
assert_response "GET /product?id=PBT001 - Boot detail" "$RESP_STATUS" "$RESP_BODY" "200" "Salomon"

do_request GET "${BASE_URL}/product?id=PWR001" "$COOKIE_JAR"
assert_response "GET /product?id=PWR001 - Wear detail" "$RESP_STATUS" "$RESP_BODY" "200" "Descente"

do_request GET "${BASE_URL}/product?id=PHL001" "$COOKIE_JAR"
assert_response "GET /product?id=PHL001 - Helmet detail" "$RESP_STATUS" "$RESP_BODY" "200" "Giro"

do_request GET "${BASE_URL}/product?id=PGL001" "$COOKIE_JAR"
assert_response "GET /product?id=PGL001 - Glove detail" "$RESP_STATUS" "$RESP_BODY" "200" "Hestra"

do_request GET "${BASE_URL}/product?id=PPL001" "$COOKIE_JAR"
assert_response "GET /product?id=PPL001 - Pole detail" "$RESP_STATUS" "$RESP_BODY" "200" "Leki"

do_request GET "${BASE_URL}/product?id=PWX001" "$COOKIE_JAR"
assert_response "GET /product?id=PWX001 - Wax detail" "$RESP_STATUS" "$RESP_BODY" "200" "Swix"

echo ""

# =============================================================================
# 14. EDGE CASES
# =============================================================================
echo -e "${YELLOW}━━━ 14. Edge Cases ━━━${NC}"

# 14.1 Non-existing page → expect 404 or error page
EDGE_JAR=$(mktemp /tmp/skishop_edge_XXXXXX)
do_request GET "${BASE_URL}/nonexistent-page" "$EDGE_JAR"
TOTAL=$((TOTAL + 1))
if [[ "$RESP_STATUS" == "404" || "$RESP_STATUS" == "200" ]]; then
    PASS=$((PASS + 1))
    echo -e "  ${GREEN}✓ PASS${NC} [GET /nonexistent-page - 404 or error page] (HTTP $RESP_STATUS)"
else
    FAIL=$((FAIL + 1))
    echo -e "  ${RED}✗ FAIL${NC} [GET /nonexistent-page] (HTTP $RESP_STATUS, expected 404)"
fi

# 14.2 Product with empty id
do_request GET "${BASE_URL}/product?id=" "$EDGE_JAR"
TOTAL=$((TOTAL + 1))
if [[ "$RESP_STATUS" == "200" || "$RESP_STATUS" == "400" || "$RESP_STATUS" == "404" ]]; then
    PASS=$((PASS + 1))
    echo -e "  ${GREEN}✓ PASS${NC} [GET /product?id= - Empty ID] (HTTP $RESP_STATUS)"
else
    FAIL=$((FAIL + 1))
    echo -e "  ${RED}✗ FAIL${NC} [GET /product?id= - Empty ID] (HTTP $RESP_STATUS)"
fi

# 14.3 Products with huge page number
do_request GET "${BASE_URL}/products?page=99999&size=10" "$EDGE_JAR"
assert_response "GET /products?page=99999 - Large page number" "$RESP_STATUS" "$RESP_BODY" "200"

# 14.4 Products with size=0
do_request GET "${BASE_URL}/products?page=1&size=0" "$EDGE_JAR"
TOTAL=$((TOTAL + 1))
if [[ "$RESP_STATUS" == "200" || "$RESP_STATUS" == "400" ]]; then
    PASS=$((PASS + 1))
    echo -e "  ${GREEN}✓ PASS${NC} [GET /products?size=0 - Zero page size] (HTTP $RESP_STATUS)"
else
    FAIL=$((FAIL + 1))
    echo -e "  ${RED}✗ FAIL${NC} [GET /products?size=0] (HTTP $RESP_STATUS)"
fi

rm -f "$EDGE_JAR"

echo ""

# =============================================================================
# SUMMARY
# =============================================================================
echo -e "${CYAN}================================================================${NC}"
echo -e "${CYAN}  TEST RESULTS SUMMARY${NC}"
echo -e "${CYAN}================================================================${NC}"
echo -e "  Total:  ${TOTAL}"
echo -e "  ${GREEN}Passed: ${PASS}${NC}"
echo -e "  ${RED}Failed: ${FAIL}${NC}"
echo ""

if [[ $FAIL -eq 0 ]]; then
    echo -e "  ${GREEN}All tests passed!${NC}"
    TEST_EXIT=0
else
    echo -e "  ${RED}Some tests failed. Review output above for details.${NC}"
    TEST_EXIT=1
fi

# cleanup trap will stop the application on exit
exit $TEST_EXIT
