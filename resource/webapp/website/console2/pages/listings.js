/* pages/listings.js */
(function(){

function renderListings(){
  const totalLeads  = PORTALS.filter(p=>p.enabled).reduce((s,p)=>s+p.leads,0);
  const totalViews  = PORTALS.filter(p=>p.enabled).reduce((s,p)=>s+p.views,0);
  const totalListed = CARS ? CARS.length : 6;
  const activeCount = PORTALS.filter(p=>p.enabled).length;

  const stats = `<div class="sr" style="grid-template-columns:repeat(4,1fr);margin-bottom:16px">
    ${statCard('Active Portals', activeCount+' / '+PORTALS.length, 'var(--grn)')}
    ${statCard('Total Listings', totalListed, 'var(--acc)')}
    ${statCard('Monthly Views', totalViews.toLocaleString(), 'var(--blu)')}
    ${statCard('Portal Leads (30d)', totalLeads, 'var(--pur)')}
  </div>`;

  // Per-portal cards
  const portalCards = PORTALS.map(p => {
    const statusCol = p.enabled ? 'var(--grn)' : 'var(--tx3)';
    const statusBg  = p.enabled ? 'rgba(31,214,160,.12)' : 'var(--s2)';

    // Mini bar: leads proportion
    const maxLeads = Math.max(...PORTALS.map(x=>x.leads), 1);
    const barW = p.leads ? Math.round(p.leads/maxLeads*100) : 0;

    return `
    <div class="cs" style="margin-bottom:0">
      <div style="padding:16px">
        <!-- Header row -->
        <div style="display:flex;align-items:center;gap:12px;margin-bottom:14px">
          <div style="width:44px;height:44px;border-radius:10px;background:${p.logoBg};color:${p.logoColor};display:flex;align-items:center;justify-content:center;font-weight:800;font-size:${p.logo.length>2?11:14}px;flex-shrink:0;border:1px solid ${p.logoColor}33">${p.logo}</div>
          <div style="flex:1">
            <div style="font-size:14px;font-weight:700;color:var(--tx)">${p.name}</div>
            <div style="font-size:11px;color:var(--tx3)">${p.region}</div>
          </div>
          <div class="tog${p.enabled?' on':''}" onclick="lstTogglePortal('${p.id}')"></div>
        </div>

        <!-- Status + last sync -->
        <div style="display:flex;align-items:center;gap:8px;margin-bottom:12px">
          <span style="font-size:10px;padding:2px 9px;border-radius:10px;background:${statusBg};color:${statusCol};font-weight:600">● ${p.status}</span>
          ${p.enabled ? `<span style="font-size:11px;color:var(--tx3)">Synced ${p.lastSync}</span>` : ''}
        </div>

        <!-- Stats (only if connected) -->
        ${p.enabled ? `
        <div style="display:grid;grid-template-columns:1fr 1fr 1fr;gap:8px;margin-bottom:12px">
          <div style="background:var(--s2);border-radius:7px;padding:10px;text-align:center">
            <div style="font-size:18px;font-weight:700;font-family:'Syne',sans-serif;color:var(--tx)">${p.listed}</div>
            <div style="font-size:10px;color:var(--tx3)">Listed</div>
          </div>
          <div style="background:var(--s2);border-radius:7px;padding:10px;text-align:center">
            <div style="font-size:18px;font-weight:700;font-family:'Syne',sans-serif;color:var(--blu)">${p.views.toLocaleString()}</div>
            <div style="font-size:10px;color:var(--tx3)">Views</div>
          </div>
          <div style="background:var(--s2);border-radius:7px;padding:10px;text-align:center">
            <div style="font-size:18px;font-weight:700;font-family:'Syne',sans-serif;color:var(--pur)">${p.leads}</div>
            <div style="font-size:10px;color:var(--tx3)">Leads</div>
          </div>
        </div>
        <!-- Lead share bar -->
        <div style="margin-bottom:12px">
          <div style="font-size:10px;color:var(--tx3);margin-bottom:4px">Lead share vs other portals</div>
          <div style="height:5px;background:var(--b1);border-radius:3px">
            <div style="height:100%;width:${barW}%;background:var(--acc);border-radius:3px;transition:width .4s"></div>
          </div>
        </div>` : `
        <div style="padding:16px 0;text-align:center;color:var(--tx3);font-size:13px">
          Connect this portal to start pushing listings
        </div>`}

        <!-- Actions -->
        <div style="display:flex;gap:7px;flex-wrap:wrap">
          ${btn(svgIcon('cog',12)+' Configure','btn btn-s btn-sm',`onclick="lstConfigure('${p.id}')"`)}
          ${p.enabled ? btn(svgIcon('repeat',12)+' Sync Now','btn btn-s btn-sm',`onclick="lstSync('${p.id}')"`) : btn(svgIcon('zap',12)+' Connect','btn btn-p btn-sm',`onclick="lstConfigure('${p.id}')"`) }
          ${p.enabled ? btn(svgIcon('eye',12)+' View Live','btn btn-s btn-sm',`onclick="showToast('Opening ${p.name} dealer page…')"`) : ''}
        </div>
      </div>
    </div>`;
  }).join('');

  // Inventory feed health table
  const feedRows = PORTALS.filter(p=>p.enabled).map(p=>`
    <div class="cr">
      <div style="width:32px;height:32px;border-radius:8px;background:${p.logoBg};color:${p.logoColor};display:flex;align-items:center;justify-content:center;font-weight:800;font-size:10px;flex-shrink:0">${p.logo}</div>
      <div style="flex:1">
        <div style="font-size:13px;font-weight:600">${p.name}</div>
        <div style="font-size:11px;color:var(--tx3)">${p.format} · every ${p.syncInterval}</div>
      </div>
      <div style="text-align:right">
        <div style="font-size:12px;color:var(--grn);font-weight:600">● Healthy</div>
        <div style="font-size:11px;color:var(--tx3)">Last: ${p.lastSync}</div>
      </div>
      ${btn('Sync','btn btn-s btn-sm',`onclick="lstSync('${p.id}')"`)}
    </div>`).join('');

  // Modal for configuration (rendered inline, opened by lstConfigure)
  return `${bc('Listings')}
  <div class="ph" style="display:flex;align-items:center;justify-content:space-between">
    <div>
      <h1>Listings & Portal Syndication</h1>
      <p>Push your inventory to AutoTrader, Cars.com, KSL, CarGurus and Facebook Marketplace</p>
    </div>
    ${btn(svgIcon('repeat',12)+' Sync All Active','btn btn-p btn-sm','onclick="lstSyncAll()"')}
  </div>
  <div style="padding:0 20px 20px">
    ${stats}
    <!-- Portal grid -->
    <div style="display:grid;grid-template-columns:repeat(auto-fill,minmax(280px,1fr));gap:14px;margin-bottom:20px">
      ${portalCards}
    </div>
    <!-- Feed health -->
    <div class="cs">
      <div class="cs-h">
        <div style="display:flex;align-items:center;gap:7px">${svgIcon('wifi',14)}<h3>Feed Health & Sync Log</h3></div>
        <span style="font-size:11px;color:var(--grn)">● All feeds healthy</span>
      </div>
      ${feedRows}
    </div>
  </div>`;
}

function lstTogglePortal(id){
  const p = PORTALS.find(x=>x.id===id);
  if(!p) return;
  if(!p.enabled && !p.token){
    lstConfigure(id); return;
  }
  p.enabled = !p.enabled;
  p.status  = p.enabled ? 'Active' : 'Disconnected';
  if(p.enabled) p.lastSync = 'Just now';
  render();
  showToast(p.enabled ? p.name+' enabled' : p.name+' disabled');
}

function lstConfigure(id){
  const p = PORTALS.find(x=>x.id===id);
  if(!p) return;
  const intervals = ['5 min','15 min','30 min','60 min','6 hours','Daily'].map(v=>
    `<option${p.syncInterval===v?' selected':''}>${v}</option>`).join('');
  const formats = ['JSON','ADF/XML','XML','CSV'].map(v=>
    `<option${p.format===v?' selected':''}>${v}</option>`).join('');

  openModal(`Configure — ${p.name}`,
    `<div style="display:flex;flex-direction:column;gap:12px">
      <div style="background:var(--s2);border-radius:8px;padding:10px 14px;font-size:12px;color:var(--tx2);line-height:1.7">
        <strong style="color:var(--tx)">${p.name} Integration</strong><br>
        ${p.id==='autotrader'   ? 'Obtain your Feed URL and API key from your AutoTrader Dealer Center account under <em>Integrations → Inventory Feed</em>.' : ''}
        ${p.id==='carsdotcom'   ? 'Get your Feed URL from Cars.com Dealer Dashboard under <em>Tools → Inventory Management → API Access</em>.' : ''}
        ${p.id==='ksl'          ? 'Request KSL Classifieds dealer feed access at <em>dealers.ksl.com</em>. KSL uses an XML pull feed — provide them your feed URL or use the push URL below.' : ''}
        ${p.id==='cargurus'     ? 'Get your CarGurus API token from <em>Dealer Dashboard → Account → API Access</em>.' : ''}
        ${p.id==='facebook'     ? 'Connect via Facebook Business Manager. Go to <em>Commerce Manager → Catalog → Data Sources → Add Items → Use a Data Feed</em> and paste your feed URL.' : ''}
      </div>
      <div>
        <label style="font-size:11px;color:var(--tx3);display:block;margin-bottom:5px">Feed URL <span style="color:var(--tx3)">(your inventory feed endpoint)</span></label>
        <input class="inp" id="lc-url" value="${p.url}" placeholder="https://feed.${p.id}.com/dealer/12345/inventory" style="font-family:monospace;font-size:12px"/>
      </div>
      <div>
        <label style="font-size:11px;color:var(--tx3);display:block;margin-bottom:5px">API Token / Key</label>
        <input class="inp" id="lc-token" type="password" value="${p.token}" placeholder="Paste your API key or bearer token"/>
      </div>
      <div style="display:flex;gap:10px">
        <div style="flex:1">
          <label style="font-size:11px;color:var(--tx3);display:block;margin-bottom:5px">Feed Format</label>
          <select class="inp" id="lc-format">${formats}</select>
        </div>
        <div style="flex:1">
          <label style="font-size:11px;color:var(--tx3);display:block;margin-bottom:5px">Sync Interval</label>
          <select class="inp" id="lc-interval">${intervals}</select>
        </div>
      </div>
    </div>`,
    ()=>{
      p.url          = document.getElementById('lc-url')?.value   || '';
      p.token        = document.getElementById('lc-token')?.value || '';
      const fmt      = document.getElementById('lc-format');
      const ivl      = document.getElementById('lc-interval');
      p.format       = fmt ? fmt.options[fmt.selectedIndex].text : p.format;
      p.syncInterval = ivl ? ivl.options[ivl.selectedIndex].text : p.syncInterval;
      if(p.url || p.token){
        p.enabled  = true;
        p.status   = 'Active';
        p.lastSync = 'Just now';
      }
      closeModal(); render();
      showToast(p.name+' settings saved');
    },
    'Save & Connect'
  );
}

function lstSync(id){
  const p = PORTALS.find(x=>x.id===id);
  if(!p) return;
  p.lastSync = 'Syncing…';
  render();
  setTimeout(()=>{
    p.lastSync = 'Just now';
    p.listed   = CARS ? CARS.length : 6;
    render();
    showToast(p.name+' synced — '+p.listed+' listings pushed');
  }, 1400);
}

function lstSyncAll(){
  PORTALS.filter(p=>p.enabled).forEach(p=>{ p.lastSync='Syncing…'; });
  render();
  setTimeout(()=>{
    PORTALS.filter(p=>p.enabled).forEach(p=>{
      p.lastSync = 'Just now';
      p.listed   = CARS ? CARS.length : 6;
    });
    render();
    showToast('All portals synced');
  }, 1800);
}


/* ══════════════════════════════════════════
   TASKS PAGE
══════════════════════════════════════════ */

/* ── Auto-init ── */
initListings();

})();

/* ── DOM init ── */
function initListings() {
  const PORTALS = window.PORTALS || [
    { id:'autotrader', name:'AutoTrader',          region:'National',              logo:'AT',  logoColor:'#f26b21', logoBg:'#fff3eb', enabled:true,  status:'Active',       lastSync:'2 min ago', leads:47, views:1840 },
    { id:'carsdotcom', name:'Cars.com',             region:'National',              logo:'C.',  logoColor:'#e4002b', logoBg:'#fff0f1', enabled:true,  status:'Active',       lastSync:'8 min ago', leads:31, views:1220 },
    { id:'ksl',        name:'KSL Classifieds',      region:'Utah / Mountain West',  logo:'KSL', logoColor:'#0057a8', logoBg:'#eef4ff', enabled:true,  status:'Active',       lastSync:'12 min ago',leads:28, views:980  },
    { id:'cargurus',   name:'CarGurus',             region:'National',              logo:'CG',  logoColor:'#00a090', logoBg:'#e8faf8', enabled:false, status:'Disconnected', lastSync:'—',         leads:0,  views:0    },
    { id:'facebook',   name:'Facebook Marketplace', region:'National / Local',      logo:'fb',  logoColor:'#1877f2', logoBg:'#eef4ff', enabled:false, status:'Disconnected', lastSync:'—',         leads:0,  views:0    },
  ];
  if (!window.PORTALS) window.PORTALS = PORTALS;

  const listed = (window.CARS || []).length;
  const active = PORTALS.filter(p => p.enabled);

  document.getElementById('lst-stat-active').textContent = active.length + ' / ' + PORTALS.length;
  document.getElementById('lst-stat-listed').textContent = listed;
  document.getElementById('lst-stat-views').textContent  = active.reduce((s,p)=>s+p.views,0).toLocaleString();
  document.getElementById('lst-stat-leads').textContent  = active.reduce((s,p)=>s+p.leads,0);

  const maxLeads = Math.max(...PORTALS.map(p=>p.leads), 1);
  const grid = document.getElementById('portal-grid');
  if (grid) grid.innerHTML = PORTALS.map(p => `
    <div class="cs" style="margin:0">
      <div style="padding:16px">
        <div style="display:flex;align-items:center;gap:12px;margin-bottom:14px">
          <div style="width:44px;height:44px;border-radius:10px;background:${p.logoBg};color:${p.logoColor};display:flex;align-items:center;justify-content:center;font-weight:800;font-size:${p.logo.length>2?11:14}px;flex-shrink:0;border:1px solid ${p.logoColor}33">${p.logo}</div>
          <div style="flex:1">
            <div style="font-size:14px;font-weight:700">${p.name}</div>
            <div style="font-size:11px;color:var(--tx3)">${p.region}</div>
          </div>
          <div class="tog${p.enabled?' on':''}" id="tog-${p.id}" onclick="togglePortal('${p.id}')"></div>
        </div>
        <div style="margin-bottom:12px">
          <span style="font-size:10px;padding:2px 9px;border-radius:10px;background:${p.enabled?'rgba(31,214,160,.12)':'var(--s2)'};color:${p.enabled?'var(--grn)':'var(--tx3)'};font-weight:600">● ${p.status}</span>
          ${p.enabled ? `<span style="font-size:11px;color:var(--tx3);margin-left:8px">Synced ${p.lastSync}</span>` : ''}
        </div>
        ${p.enabled ? `
          <div style="display:grid;grid-template-columns:1fr 1fr 1fr;gap:8px;margin-bottom:12px">
            <div style="background:var(--s2);border-radius:7px;padding:10px;text-align:center">
              <div style="font-size:17px;font-weight:700;font-family:'Syne',sans-serif">${listed}</div>
              <div style="font-size:10px;color:var(--tx3)">Listed</div>
            </div>
            <div style="background:var(--s2);border-radius:7px;padding:10px;text-align:center">
              <div style="font-size:17px;font-weight:700;font-family:'Syne',sans-serif;color:var(--blu)">${p.views.toLocaleString()}</div>
              <div style="font-size:10px;color:var(--tx3)">Views</div>
            </div>
            <div style="background:var(--s2);border-radius:7px;padding:10px;text-align:center">
              <div style="font-size:17px;font-weight:700;font-family:'Syne',sans-serif;color:var(--pur)">${p.leads}</div>
              <div style="font-size:10px;color:var(--tx3)">Leads</div>
            </div>
          </div>
          <div style="margin-bottom:12px">
            <div style="font-size:10px;color:var(--tx3);margin-bottom:4px">Lead share</div>
            <div style="height:5px;background:var(--b1);border-radius:3px">
              <div style="height:100%;width:${Math.round(p.leads/maxLeads*100)}%;background:var(--acc);border-radius:3px"></div>
            </div>
          </div>` : `<div style="padding:16px 0;text-align:center;color:var(--tx3);font-size:13px">Connect to start syncing</div>`}
        <div style="display:flex;gap:7px;flex-wrap:wrap">
          <button class="btn btn-s btn-sm" onclick="configurePortal('${p.id}')">Configure</button>
          ${p.enabled ? `<button class="btn btn-s btn-sm" onclick="syncPortal('${p.id}')">Sync Now</button>` : `<button class="btn btn-p btn-sm" onclick="configurePortal('${p.id}')">Connect</button>`}
        </div>
      </div>
    </div>`).join('');

  // Feed health
  const feedEl = document.getElementById('feed-health-list');
  if (feedEl) feedEl.innerHTML = PORTALS.filter(p=>p.enabled).map(p=>`
    <div class="cr">
      <div style="width:32px;height:32px;border-radius:8px;background:${p.logoBg};color:${p.logoColor};display:flex;align-items:center;justify-content:center;font-weight:800;font-size:10px;flex-shrink:0">${p.logo}</div>
      <div style="flex:1">
        <div style="font-size:13px;font-weight:600">${p.name}</div>
        <div style="font-size:11px;color:var(--tx3)">Last: ${p.lastSync}</div>
      </div>
      <span style="font-size:12px;color:var(--grn);font-weight:600">● Healthy</span>
      <button class="btn btn-s btn-sm" onclick="syncPortal('${p.id}')">Sync</button>
    </div>`).join('');

  window.togglePortal = function(id) {
    const p = PORTALS.find(x=>x.id===id);
    if (!p) return;
    p.enabled = !p.enabled;
    p.status  = p.enabled ? 'Active' : 'Disconnected';
    initListings();
    showToast(p.name + (p.enabled ? ' enabled' : ' disabled'));
  };

  window.syncPortal = function(id) {
    showToast('Syncing ' + (PORTALS.find(x=>x.id===id)?.name || '') + '…');
    setTimeout(() => showToast('Sync complete ✓'), 1500);
  };

  window.syncAllPortals = function() {
    showToast('Syncing all active portals…');
    setTimeout(() => showToast('All portals synced ✓'), 1800);
  };

  window.configurePortal = function(id) {
    const p = PORTALS.find(x=>x.id===id);
    if (!p) return;
    openModal('Configure — ' + p.name, `
      <div style="display:flex;flex-direction:column;gap:12px">
        <div><label style="font-size:11px;color:var(--tx3);display:block;margin-bottom:5px">Feed URL</label>
          <input class="inp" id="pc-url" value="${p.url||''}" placeholder="https://feed.example.com/dealer/12345" style="font-family:monospace;font-size:12px"/></div>
        <div><label style="font-size:11px;color:var(--tx3);display:block;margin-bottom:5px">API Token</label>
          <input class="inp" id="pc-token" type="password" value="${p.token||''}" placeholder="Paste your API key"/></div>
      </div>`,
      () => {
        p.url    = document.getElementById('pc-url')?.value   || '';
        p.token  = document.getElementById('pc-token')?.value || '';
        if (p.url || p.token) { p.enabled = true; p.status = 'Active'; p.lastSync = 'Just now'; }
        closeModal(); initListings(); showToast(p.name + ' saved');
      }, 'Save & Connect');
  };
}
