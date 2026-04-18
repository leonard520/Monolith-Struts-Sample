# t19 — Visual Spot-Check Findings

Compared live rendered HTML at `http://localhost:8080` against reference screenshots in `screenshot/`.

---

## V1: Header Navigation Bar Renders Empty — BUG

**Severity:** BUG — visual mismatch on every page
**Affected:** All pages (home, products, login, etc.)

**Expected (from screenshot/home.png):**
- Nav bar with: "Ski Resort Shop" logo, ホーム, 商品, クーポン, ログイン, 会員登録
- Search input with placeholder "商品名やブランドで検索"
- Cart button (🛒 カート)

**Actual:**
```html
<div class="app-header">
    <!-- empty — no nav bar content rendered -->
</div>
```

**Source code check:**
- `home.jsp:14` has `<jsp:include page="/WEB-INF/jsp/common/header.jsp"/>`
- `header.jsp` contains full nav bar markup with `<c:url>` tags
- The include executes but produces empty output in the rendered HTML

**Root cause hypothesis:** The `<c:url>` JSTL tag or `<c:if>` conditional may be failing silently, or the JSTL taglib URI resolution is not working for the included JSP. The `app-header` div is present (so the include runs) but the content inside is empty.

**Assigned to:** backend — investigate JSP include rendering mechanism

---

## V2: Footer Renders Empty — BUG

**Severity:** BUG — visual mismatch on every page
**Affected:** All pages

**Expected (from screenshot/home.png):**
```
SkiShop Online Store © 2026
```

**Actual:**
```html
<!-- footer include generates no visible output -->
</body>
```

**Source code check:**
- `home.jsp:55` has `<jsp:include page="/WEB-INF/jsp/common/footer.jsp"/>`
- `footer.jsp` contains `<div class="site-footer">...<p>SkiShop Online Store &copy; 2026</p>...</div>`
- Same behavior as header — include runs but produces empty content

**Assigned to:** backend — same root cause as V1

---

## V3: Chinese Text on Home Page — BUG

**Severity:** BUG — language inconsistency, visual mismatch with screenshots
**Affected:** `home.jsp` only (products.jsp correctly uses Japanese)

**Expected (from screenshot/product_list.png):**
- Section heading: "おすすめスキー用品"
- Product card button: "詳細を見る"

**Actual (from home.jsp):**
- Line 28: `<h2 class="page-title">推荐滑雪装备</h2>` (Chinese)
- Line 46: `<a href="..." class="btn">查看详情</a>` (Chinese)
- Line 52: `<p>推荐商品正在准备中。</p>` (Chinese)

**Fix required:**
| Line | Current (Chinese) | Should be (Japanese) |
|------|-------------------|---------------------|
| 28 | 推荐滑雪装备 | おすすめスキー用品 |
| 46 | 查看详情 | 詳細を見る |
| 52 | 推荐商品正在准备中。 | おすすめ商品は準備中です。 |

**Assigned to:** frontend — JSP text correction

---

## Pages Verified Visually

| Page | URL | Key Elements Checked | Match? |
|------|-----|---------------------|--------|
| Home | `/` | Hero text, recommended products grid, layout | **Partial** — hero text correct, products grid renders, but no nav/footer, Chinese text |
| Products | `/products` | Search form, category filter, product cards, pagination | **Partial** — content correct, no nav/footer |
| Product detail | `/product?id=PSK001` | Product name ("Atomic Redster"), price, brand | **PASS** (content matches) |
| Login | `/login` | Form fields (email, password, CSRF), submit button | **PASS** (content matches) |
| Register | `/register` | Form fields, validation | **PASS** (content matches) |

**Note:** The `product_list.png` screenshot shows a blue hero banner with "Azure SkiShop へようこそ" — this appears to be from a different version/deployment. The current implementation shows "Ski Resort Shop へようこそ" which matches `home.png`. This discrepancy in the screenshot itself is noted but not flagged as a bug.
