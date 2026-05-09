/* ============================================================
   router.js
   Hash router: fetches pages/{page}.html into #main-ct,
   then loads pages/{page}.js to wire up behavior.
   ============================================================ */

const NAV_ITEMS = [
  { id: 'home',          icon: 'home',   label: 'Home' },
  { id: 'inventory',     icon: 'car',    label: 'Inventory' },
  { id: 'leads',         icon: 'leads',  label: 'Leads' },
  { id: 'website',       icon: 'globe',  label: 'Website' },
  { id: 'analytics',     icon: 'chart',  label: 'Analytics' },
  { id: 'advertising',   icon: 'zap',    label: 'Advertising' },
  { id: 'marketing',     icon: 'mail',   label: 'Marketing' },
  { id: 'social',        icon: 'share',  label: 'Social' },
  { id: 'finance',       icon: 'dollar', label: 'Finance' },
  { id: 'service',       icon: 'wrench', label: 'Service' },
  { id: 'reports',       icon: 'report', label: 'Reports' },
  { id: 'tasks',         icon: 'task',   label: 'Tasks' },
  { id: 'listings',      icon: 'cast',   label: 'Listings' },
  { id: 'config',        icon: 'cog',    label: 'Config' },
];

const SUB_PAGE_PARENT = {
  'vehicle-editor': 'inventory',
  'lead-detail':    'leads',
  'vdp':            'inventory',
};

const PAGE_TITLES = {
  home: 'Dashboard', inventory: 'Inventory', 'vehicle-editor': 'Vehicle Editor',
  leads: 'Leads & CRM', 'lead-detail': 'Lead Detail', website: 'Website',
  analytics: 'Analytics', advertising: 'Advertising', marketing: 'Marketing',
  social: 'Social', finance: 'Finance & Desking', service: 'Service',
  reports: 'Reports', tasks: 'Tasks', listings: 'Listings',
  config: 'Configuration', vdp: 'Vehicle Detail',
};

const ALL_PAGES = [...NAV_ITEMS.map(n => n.id), ...Object.keys(SUB_PAGE_PARENT)];

// Cache fetched HTML (JS is never cached — always re-runs on navigation)
const _htmlCache = {};

/* ── Public navigate ── */
function navigate(pageId) {
  window.location.hash = pageId;
}

/* ── Hash change → load page ── */
async function onHashChange() {
  const hash = window.location.hash.slice(1) || 'home';
  const page = ALL_PAGES.includes(hash) ? hash : 'home';

  updateSidebarActive(page);
  await loadPage(page);
}

/* ── Load HTML + JS for a page ── */
async function loadPage(page) {
  const ct = document.getElementById('main-ct');
  ct.innerHTML = `<div style="padding:40px;color:var(--tx3);text-align:center">
    ${svgIcon('clock', 20)}<br><br>Loading…
  </div>`;

  try {
    // 1. Fetch HTML (cached)
    if (!_htmlCache[page]) {
      const res = await fetch(`pages/${page}`);
      if (!res.ok) throw new Error(`pages/${page} — HTTP ${res.status}`);
      _htmlCache[page] = await res.text();
    }
    ct.innerHTML = _htmlCache[page];
    runEmbeddedScripts(ct);

    // 2. Load the companion JS (always fresh — busts cache so re-init runs)
    await loadScript(`pages/${page}.js`);

  } catch (err) {
    ct.innerHTML = `
      <div style="padding:60px;text-align:center;color:var(--tx3)">
        <div style="font-size:32px;margin-bottom:16px">⚠️</div>
        <div style="font-size:15px;color:var(--tx);margin-bottom:8px">
          Could not load <code style="color:var(--acc)">${page}</code>
        </div>
        <div style="font-size:12px;margin-bottom:20px;color:var(--red)">${err.message}</div>
        <div style="font-size:12px;max-width:440px;margin:0 auto;line-height:2;
                    background:var(--s2);padding:20px;border-radius:12px">
          A local server is required to fetch HTML files.<br>
          Open a terminal in the <code>dealer-app/</code> folder and run:<br><br>
          <code style="color:var(--acc);font-size:13px">npx serve .</code>
          &nbsp;&nbsp;or&nbsp;&nbsp;
          <code style="color:var(--acc);font-size:13px">python3 -m http.server 8080</code><br><br>
          Then open <code>http://localhost:3000</code> in your browser.
        </div>
      </div>`;
  }
}

/* ── Execute scripts from fetched HTML fragments ── */
function runEmbeddedScripts(root) {
  root.querySelectorAll('script').forEach(oldScript => {
    const script = document.createElement('script');
    for (const attr of oldScript.attributes) {
      script.setAttribute(attr.name, attr.value);
    }
    script.textContent = oldScript.textContent;
    oldScript.replaceWith(script);
  });
}

/* ── Dynamically load a script (returns promise) ── */
function loadScript(src) {
  return new Promise((resolve) => {
    // Remove old script tag for this page if it exists
    const old = document.querySelector(`script[data-page-script="${src}"]`);
    if (old) old.remove();

    const s = document.createElement('script');
    s.src = src + '?v=' + Date.now();   // cache-bust so init re-runs every navigation
    s.dataset.pageScript = src;
    s.onload  = resolve;
    s.onerror = resolve;                // don't break if .js doesn't exist (optional pages)
    document.body.appendChild(s);
  });
}

/* ── Update sidebar active item ── */
function updateSidebarActive(page) {
  const activeNav = SUB_PAGE_PARENT[page] || page;
  document.querySelectorAll('[data-nav-id]').forEach(el => {
    el.classList.toggle('on', el.dataset.navId === activeNav);
  });
  const titleEl = document.getElementById('page-title');
  if (titleEl) titleEl.textContent = PAGE_TITLES[page] || page;
}

/* ── Build sidebar nav ── */
function buildSidebar() {
  const nav = document.getElementById('sidebar-nav');
  if (!nav) return;
  nav.innerHTML = NAV_ITEMS.map(item => `
    <button class="sb-btn" type="button" data-nav-id="${item.id}" onclick="navigate('${item.id}')" aria-label="${item.label}">
      ${svgIcon(item.icon, 16)}
      <span class="sb-tip">${item.label}</span>
    </button>`).join('');
}

/* ── Init ── */
function init() {
  buildSidebar();
  window.addEventListener('hashchange', onHashChange);
  onHashChange();
}

document.addEventListener('DOMContentLoaded', init);
