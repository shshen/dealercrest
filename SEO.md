# Dealer website — SEO plan

Technical SEO strategy for a multi-dealer SaaS platform built on Java + Netty + PostgreSQL.

---

## Route strategy

Two-tier routing is used to separate concerns cleanly.

**Tier 1 — Explicit routes (registered first)**

All explicit routes are registered before the wildcard fallback. Netty matches the most specific route first, so these are never caught by the fallback.

- `/api/**` — REST endpoints (inventory query, leads, chat, etc.)
- `/assets/**` — Static assets (CSS, JS, images) served from classpath via `WebAppScanner`
- `/sitemap.xml` — Dynamically generated per dealer domain
- `/robots.txt` — Dynamically generated per dealer domain

**Tier 2 — Wildcard fallback (registered last)**

```
{path**}
```

Catches all dealer page requests. Resolves:

```
domain → dealer_id → page record → template render → HTML response
```

Path is normalized on entry: lowercased and trailing slash stripped before the DB lookup.
Query params are accepted and passed into the template engine for SSR on initial load
(e.g. `/suvs?year=2024&maxPrice=40000`).

---

## Page rendering and indexing rules

| URL pattern | Render method | Google indexes | Notes |
|---|---|---|---|
| `/suvs` | SSR | Yes | Category landing page. Establishes dealer sells SUVs. Page 1 results only. |
| `/suvs?year=2024` | SSR | Yes | Ad campaign landing pages. Query params SSR'd on first load. |
| `/suvs?page=2` | AJAX only | Blocked | No SEO value. Blocked via `Disallow: /*?page=` in robots.txt. |
| `/inventory/{vin}` | SSR | Yes | Vehicle detail page (VDP). One per vehicle. Core SEO value. Full meta tags. |
| `/about`, `/contact` | SSR | Yes | Dealer info pages. Important for local SEO and Google Business Profile. |
| `/admin/**` | Admin UI | Blocked | Blocked via `Disallow: /admin/` in robots.txt. |
| `/api/**` | JSON REST | No | Returns `application/json` — Google does not index JSON responses. No robots.txt block needed. Security is handled by auth middleware, not robots.txt. |

---

## Pagination and filtering behavior

**Pagination — AJAX only**

Page 1 is SSR'd on initial load. Pages 2 and beyond are loaded via AJAX — JavaScript
calls `/api/inventory?page=N` and replaces the inventory grid in place. The URL does
not change on pagination. Paginated pages are blocked in `robots.txt` because they
have no SEO value: all individual vehicles are already covered by VDP pages and the
sitemap.

**Filtering — AJAX with URL pushState**

Two filter layers are always merged before calling `/api/inventory`:

- **Base filter** — set by the dealer in the page record (e.g. `bodyStyle=SUV`). Baked
  into the SSR HTML as a `data-base-filters` attribute on the inventory root element.
- **Customer filter** — applied by the customer via UI controls, or arriving via query
  params on the initial URL.

When a customer applies a filter, JavaScript fetches new results via AJAX and updates
the URL via `window.history.pushState` so the filtered view is shareable and bookmarkable.
If a customer shares or bookmarks a filtered URL (e.g. `/suvs?year=2024&maxPrice=40000`),
the fallback handler SSR's that filtered view on load — query params are passed into the
template engine and the page is fully rendered server-side.

Example HTML embedding both filter layers at SSR time:

```html
<div id="inventory-root"
     data-base-filters='{"bodyStyle":"SUV"}'
     data-customer-filters='{"year":"2024","maxPrice":"40000"}'>
</div>
```

JavaScript merges these on load, pre-fills the filter UI, and makes the initial API call.

---

## VDP pages — vehicle detail pages

VDP pages are the primary source of organic search traffic. Every active vehicle gets its
own fully SSR'd page at `/inventory/{vin}`. These are submitted to Google via the sitemap
and capture specific long-tail searches like `"2024 Toyota Camry 12000 miles Salt Lake City"`.

**Required meta tags (baked in at SSR time)**

```html
<!-- Title: specific, includes year/make/model/trim + dealer city -->
<title>2024 Toyota Camry LE | Smith Auto — Salt Lake City</title>

<!-- Meta description: price, mileage, location, call to action -->
<meta name="description"
      content="2024 Toyota Camry LE, 12,000 miles, $28,500. Available now at
               Smith Auto in Salt Lake City. Schedule a test drive today.">

<!-- Canonical: prevents duplicate content if vehicle is accessible via multiple paths -->
<link rel="canonical" href="https://smithauto.com/inventory/vin-1HGBH41J">

<!-- Open Graph: for social sharing and ad campaigns -->
<meta property="og:title"   content="2024 Toyota Camry LE — $28,500">
<meta property="og:image"   content="https://cdn.smithauto.com/vehicles/vin-1HGBH41J/photo-1.jpg">
<meta property="og:description" content="2024 Toyota Camry LE, 12,000 miles. Available at Smith Auto.">
```

---

## Sitemap — `/sitemap.xml`

The sitemap is dynamically generated from the inventory DB per dealer domain. It is served
as an explicit Tier 1 route so it is never caught by the wildcard fallback. Every time
Google hits `/sitemap.xml` it gets a fresh list reflecting current inventory.

**What goes in the sitemap**

- One entry per active vehicle VDP (`/inventory/{vin}`)
- Category landing pages (`/suvs`, `/used-trucks`, `/specials`)
- Static info pages (`/about`, `/contact`)
- Filtered ad landing pages, if the dealer has configured them

**What stays out of the sitemap**

- Paginated listing pages (`?page=2`, `?page=3`, etc.)
- API endpoints (`/api/**`)
- Admin pages (`/admin/**`)
- Sold or inactive vehicles — removed immediately on status change

**Scale**

Google supports up to 50,000 URLs per sitemap file. A typical dealer with 100–500 vehicles
fits easily in one file. At larger scale (dealer groups with 10,000+ vehicles), split into
multiple files with a sitemap index:

```xml
<!-- /sitemap.xml -->
<sitemapindex>
  <sitemap><loc>https://smithauto.com/sitemap-inventory.xml</loc></sitemap>
  <sitemap><loc>https://smithauto.com/sitemap-pages.xml</loc></sitemap>
</sitemapindex>
```

**Competitive advantage**

Incumbent platforms (e.g. Dealer.com / Cox Automotive) commonly have stale sitemaps —
sold vehicles remain listed and new inventory is slow to appear. Dynamic generation means
Google finds new vehicles immediately and sold vehicles disappear the same day. This is a
concrete, demonstrable SEO advantage and a core feature of the SEO Autopilot module.

**At scale: event-driven regeneration with cache**

At high inventory volume, generating the sitemap on every request becomes expensive.
The recommended approach:

1. On vehicle add or sold status change, mark sitemap as dirty.
2. A background job regenerates the sitemap and stores it in cache.
3. `/sitemap.xml` serves the cached version.

This keeps Google's view reasonably fresh while avoiding DB load on every crawl.

---

## robots.txt — `/robots.txt`

The `robots.txt` is dynamically generated because the `Sitemap:` directive must contain
the dealer's own domain, which varies per tenant. It is served as an explicit Tier 1 route.

**Important:** `robots.txt` is a crawling directive, not a security measure. It is ignored
by malicious crawlers entirely. `/api/**` is intentionally NOT blocked here — Google does
not index JSON responses, so no robots.txt block is needed. API security belongs in auth
middleware, not in `robots.txt`.

```
# Generated dynamically — dealer domain resolved from Host header.
# Sitemap URL is dealer-specific so this cannot be a static file.

User-agent: *
Allow: /

# Block admin UI — HTML pages that Google could index but should not.
Disallow: /admin/

# Block paginated listing pages — no SEO value, all vehicles covered by sitemap + VDPs.
Disallow: /*?page=

# Point Google to this dealer's sitemap.
Sitemap: https://{dealerDomain}/sitemap.xml
```

---

## Crawl budget allocation

Google gives each site a crawl budget. The goal is to concentrate that budget on pages
with actual search value and away from pages with no value.

| Page type | Priority | Reason |
|---|---|---|
| Vehicle detail pages (VDPs) | Highest | Primary organic traffic source. Updated frequently. |
| Category landing pages | High | Captures broad search terms. Establishes inventory categories. |
| Info pages (about, contact) | Medium | Local SEO value. Updated infrequently. |
| Filtered ad landing pages | Medium | Only those explicitly added to sitemap by the dealer. |
| Paginated listing pages | Blocked | Duplicate content risk. Waste of crawl budget. All vehicles covered by VDPs + sitemap. |

---

## 404 handling

The 404 response should be dealer-branded, not a generic platform error. When a path
resolves to no page record, the handler looks up the dealer's configured 404 page first
and only falls back to a platform-level default if none exists. This matters for ad
campaigns — if a dealer changes a URL, traffic hitting the old link should see their
branding, not a bare platform error.

---

## Implementation checklist

- [ ] Wildcard fallback `{path**}` registered last, accepts query params for SSR filtering
- [ ] Path normalized on entry (lowercase, trailing slash stripped) before DB lookup
- [ ] Page record stores base filter JSON; embedded as `data-base-filters` in SSR HTML
- [ ] Pagination via AJAX only — JS replaces grid, URL does not change, blocked in robots.txt
- [ ] Filter changes update URL via `window.history.pushState` for shareable links
- [ ] `/sitemap.xml` explicit route — dynamically generated from inventory DB per dealer domain
- [ ] Sold vehicles removed from sitemap immediately on status change
- [ ] `/robots.txt` explicit route — dynamically generated, blocks `/admin/` and `/*?page=` only
- [ ] `/api/**` NOT blocked in robots.txt — security handled by auth middleware
- [ ] VDP pages fully SSR with title, meta description, canonical, and Open Graph tags
- [ ] 404 resolves to dealer-branded page before platform fallback
- [ ] At scale: event-driven sitemap regeneration + cache (mark dirty on inventory change)