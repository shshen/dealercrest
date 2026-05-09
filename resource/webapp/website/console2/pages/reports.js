/* pages/reports.js */
(function(){

function renderReports(){
  // Aged inventory
  const agedData=[[0,30,'var(--grn)'],[31,60,'var(--ora)'],[61,90,'var(--red)'],[91,999,'#ff2244']];
  const aged=CARS.map((car,i)=>({...car,days:[5,18,44,67,92,12][i%6]}));
  const agedRows=aged.map(car=>{
    const col=car.days<31?'var(--grn)':car.days<61?'var(--ora)':car.days<91?'var(--red)':'#ff2244';
    return `<div class="cr">
      <div style="flex:1"><div style="font-size:13px;font-weight:500">${car.year} ${car.make} ${car.model}</div><div style="font-size:11px;color:var(--tx3)">${car.stock||car.vin||''}</div></div>
      <div style="text-align:right">
        <div style="font-weight:700;color:${col}">${car.days} days</div>
        <div style="font-size:11px;color:var(--tx3)">$${car.price.toLocaleString()}</div>
      </div>
    </div>`;
  }).join('');

  // Salesperson leaderboard
  const team=[
    {name:'Alex Thompson', deals:8, gross:62400, leads:24},
    {name:'David Kim',     deals:6, gross:41800, leads:19},
    {name:'Maria Garcia',  deals:5, gross:38200, leads:16},
    {name:'Lisa Chen',     deals:3, gross:22100, leads:12},
  ];
  const maxDeals=team[0].deals;
  const salesRows=team.map((s,i)=>`
    <div class="cr">
      <div style="width:22px;font-size:12px;font-weight:700;color:${i===0?'var(--ora)':'var(--tx3)'}">#${i+1}</div>
      <div style="flex:1">
        <div style="font-size:13px;font-weight:500;margin-bottom:4px">${s.name}</div>
        <div style="height:5px;background:var(--b1);border-radius:3px;width:100%"><div style="height:100%;background:var(--acc);border-radius:3px;width:${Math.round(s.deals/maxDeals*100)}%"></div></div>
      </div>
      <div style="text-align:right;min-width:100px">
        <div style="font-size:13px;font-weight:700;color:var(--tx)">${s.deals} deals</div>
        <div style="font-size:11px;color:var(--grn)">$${s.gross.toLocaleString()} gross</div>
      </div>
    </div>`).join('');

  // Conversion funnel
  const funnel=[
    {label:'Website Visitors', val:4820, pct:100, col:'var(--acc)'},
    {label:'Leads Generated',  val:312,  pct:6.5, col:'var(--blu)'},
    {label:'Appointments Set', val:89,   pct:1.8, col:'var(--pur)'},
    {label:'Test Drives',      val:54,   pct:1.1, col:'var(--ora)'},
    {label:'Deals Closed',     val:22,   pct:0.46,col:'var(--grn)'},
  ];
  const funnelHtml=funnel.map(f=>`
    <div style="margin-bottom:12px">
      <div style="display:flex;justify-content:space-between;margin-bottom:4px">
        <span style="font-size:12px;color:var(--tx2)">${f.label}</span>
        <span style="font-size:12px;font-weight:700;color:var(--tx)">${f.val.toLocaleString()} <span style="color:var(--tx3);font-weight:400">(${f.pct}%)</span></span>
      </div>
      <div style="height:8px;background:var(--b1);border-radius:4px"><div style="height:100%;background:${f.col};border-radius:4px;width:${f.pct===100?100:Math.max(3,f.pct*6)}%"></div></div>
    </div>`).join('');

  // Source breakdown
  const sources=[
    {src:'Website',leads:98,pct:31},
    {src:'AutoTrader',leads:72,pct:23},
    {src:'Cars.com',leads:58,pct:19},
    {src:'Facebook',leads:49,pct:16},
    {src:'Google Ads',leads:35,pct:11},
  ];
  const sourceRows=sources.map(s=>`
    <div class="cr">
      <div style="flex:1">
        <div style="font-size:13px;font-weight:500;margin-bottom:4px">${s.src}</div>
        <div style="height:5px;background:var(--b1);border-radius:3px"><div style="height:100%;background:var(--acc);border-radius:3px;width:${s.pct}%"></div></div>
      </div>
      <div style="text-align:right;min-width:80px">
        <div style="font-size:13px;font-weight:700">${s.leads}</div>
        <div style="font-size:11px;color:var(--tx3)">${s.pct}%</div>
      </div>
    </div>`).join('');

  return `${bc('Reports')}
  <div class="ph"><h1>Reports & Analytics</h1><p>Sales performance, inventory health and lead attribution</p></div>
  <div style="padding:0 20px 20px">
    <div class="sr" style="grid-template-columns:repeat(4,1fr);margin-bottom:16px">
      ${statCard('MTD Sales','22','var(--grn)')}
      ${statCard('MTD Gross','$164,500','var(--acc)')}
      ${statCard('Avg Days to Sale','31','var(--ora)')}
      ${statCard('Conversion Rate','0.46%','var(--pur)')}
    </div>
    <div style="display:grid;grid-template-columns:1fr 1fr;gap:14px;margin-bottom:14px">
      <div class="cs">
        <div class="cs-h"><div style="display:flex;align-items:center;gap:7px">${svgIcon('star',14)}<h3>Sales Leaderboard — This Month</h3></div></div>
        <div style="padding:4px 0">${salesRows}</div>
      </div>
      <div class="cs">
        <div class="cs-h"><div style="display:flex;align-items:center;gap:7px">${svgIcon('trendUp',14)}<h3>Lead-to-Sale Funnel</h3></div></div>
        <div style="padding:16px">${funnelHtml}</div>
      </div>
    </div>
    <div style="display:grid;grid-template-columns:1fr 1fr;gap:14px">
      <div class="cs">
        <div class="cs-h"><div style="display:flex;align-items:center;gap:7px">${svgIcon('car',14)}<h3>Aged Inventory Report</h3></div>
          <div style="display:flex;gap:8px;font-size:10px">
            <span style="color:var(--grn)">● 0–30</span>
            <span style="color:var(--ora)">● 31–60</span>
            <span style="color:var(--red)">● 61–90</span>
            <span style="color:#ff2244">● 90+</span>
          </div>
        </div>
        <div>${agedRows}</div>
      </div>
      <div class="cs">
        <div class="cs-h"><div style="display:flex;align-items:center;gap:7px">${svgIcon('globe',14)}<h3>Lead Source Attribution</h3></div></div>
        <div style="padding:4px 0">${sourceRows}</div>
      </div>
    </div>
  </div>`;
}


/* ══════════════════════════════════════════
   VDP — Vehicle Detail Page (public-facing preview)
══════════════════════════════════════════ */
let vdpCarId = null;

/* ── Auto-init ── */
initReports();

})();

/* ── DOM init ── */
function initReports() {
  const SALESPEOPLE = [
    { name:'Alex Thompson', sales:14, revenue:612000, target:600000 },
    { name:'David Kim',     sales:11, revenue:487000, target:500000 },
    { name:'Maria Garcia',  sales:9,  revenue:398000, target:400000 },
    { name:'Lisa Chen',     sales:7,  revenue:301000, target:350000 },
  ];
  const FUNNEL = [
    { stage:'New Leads',   count:142, pct:100 },
    { stage:'Contacted',   count:98,  pct:69  },
    { stage:'Test Drive',  count:54,  pct:38  },
    { stage:'Negotiating', count:31,  pct:22  },
    { stage:'Closed Won',  count:18,  pct:13  },
  ];
  const SOURCES = [
    { name:'AutoTrader', leads:47, pct:34 },
    { name:'Cars.com',   leads:31, pct:22 },
    { name:'KSL',        leads:28, pct:20 },
    { name:'Website',    leads:19, pct:14 },
    { name:'Walk-in',    leads:15, pct:11 },
  ];

  // Leaderboard
  const lbEl = document.getElementById('leaderboard-list');
  if (lbEl) lbEl.innerHTML = SALESPEOPLE.map((s, i) => {
    const pct = Math.round(s.revenue / s.target * 100);
    return `<div class="cr" style="display:block;padding:12px 16px">
      <div style="display:flex;align-items:center;gap:10px;margin-bottom:8px">
        <span style="font-size:18px;font-weight:800;font-family:'Syne',sans-serif;color:${i===0?'var(--ora)':'var(--tx3)'};width:24px">${i+1}</span>
        <div class="av" style="font-size:11px">${s.name.split(' ').map(n=>n[0]).join('')}</div>
        <div style="flex:1">
          <div style="font-size:13px;font-weight:600">${s.name}</div>
          <div style="font-size:11px;color:var(--tx3)">${s.sales} sales · $${s.revenue.toLocaleString()}</div>
        </div>
        <span style="font-size:12px;font-weight:700;color:${pct>=100?'var(--grn)':'var(--ora)'}">${pct}%</span>
      </div>
      <div style="height:4px;background:var(--b1);border-radius:2px">
        <div style="height:100%;width:${Math.min(pct,100)}%;background:${pct>=100?'var(--grn)':'var(--acc)'};border-radius:2px;transition:width .6s"></div>
      </div>
    </div>`;
  }).join('');

  // Funnel
  const funEl = document.getElementById('funnel-list');
  if (funEl) funEl.innerHTML = FUNNEL.map(f => `
    <div style="margin-bottom:12px">
      <div style="display:flex;justify-content:space-between;margin-bottom:4px">
        <span style="font-size:13px">${f.stage}</span>
        <span style="font-size:13px;font-weight:700">${f.count} <span style="font-size:11px;color:var(--tx3)">(${f.pct}%)</span></span>
      </div>
      <div style="height:8px;background:var(--b1);border-radius:4px">
        <div style="height:100%;width:${f.pct}%;background:var(--acc);border-radius:4px;transition:width .6s"></div>
      </div>
    </div>`).join('');

  // Sources
  const srcEl = document.getElementById('source-list');
  if (srcEl) srcEl.innerHTML = SOURCES.map(s => `
    <div style="display:flex;align-items:center;gap:10px;margin-bottom:10px">
      <span style="font-size:13px;width:90px;flex-shrink:0">${s.name}</span>
      <div style="flex:1;height:6px;background:var(--b1);border-radius:3px">
        <div style="height:100%;width:${s.pct}%;background:var(--pur);border-radius:3px;transition:width .6s"></div>
      </div>
      <span style="font-size:12px;font-weight:600;width:30px;text-align:right">${s.leads}</span>
    </div>`).join('');

  // Aged inventory
  const cars   = window.CARS || [];
  const agedEl = document.getElementById('aged-list');
  if (agedEl) {
    const aged = cars.map((c, i) => ({ ...c, days: 15 + i * 18 })).sort((a,b) => b.days-a.days);
    agedEl.innerHTML = aged.map(c => {
      const col = c.days>90?'var(--red)':c.days>60?'var(--ora)':c.days>30?'var(--acc)':'var(--grn)';
      return `<div class="cr">
        <div style="flex:1">
          <div style="font-size:13px;font-weight:500">${c.year} ${c.make} ${c.model}</div>
          <div style="font-size:11px;color:var(--tx3)">$${c.price.toLocaleString()} · ${c.status}</div>
        </div>
        <span style="font-size:12px;font-weight:700;color:${col}">${c.days} days</span>
      </div>`;
    }).join('');
  }
}
