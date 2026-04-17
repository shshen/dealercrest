/* pages/service.js */
(function(){

function renderService(){
  const tabs=['All','In Progress','Waiting Parts','Ready','Completed','Scheduled'];
  const filtered = roTab==='All' ? SERVICE_ROS : SERVICE_ROS.filter(r=>r.status===roTab);
  const tabHtml = tabs.map(t=>`<div class="pill${roTab===t?' active':''}" onclick="roTab='${t}';render()">${t}${t==='All'?'':' ('+SERVICE_ROS.filter(r=>r.status===t).length+')'}</div>`).join('');
  const bays=[
    {name:'Bay 1',tech:'Mike Davis',ro:'RO-1042',car:'Tesla Model 3',status:'In Progress'},
    {name:'Bay 2',tech:'Tom Wilson',ro:'RO-1041',car:'BMW X3',status:'Waiting Parts'},
    {name:'Bay 3',tech:'Mike Davis',ro:'RO-1039',car:'Audi Q7',status:'Ready'},
    {name:'Bay 4',tech:'—',ro:'',car:'Available',status:'Open'},
  ];
  const bayCards=bays.map(b=>`
    <div style="background:var(--s2);border:1px solid ${b.status==='Open'?'var(--b1)':'var(--acc)22'};border-radius:10px;padding:14px">
      <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:8px">
        <span style="font-size:12px;font-weight:700;color:var(--tx)">${b.name}</span>
        <span style="font-size:10px;padding:2px 8px;border-radius:4px;background:${b.status==='Open'?'var(--b1)':'var(--acc)22'};color:${b.status==='Open'?'var(--tx3)':'var(--acc)'}">${b.status}</span>
      </div>
      <div style="font-size:13px;color:var(--tx2)">${b.car}</div>
      <div style="font-size:11px;color:var(--tx3);margin-top:4px">${b.tech}${b.ro?' · '+b.ro:''}</div>
    </div>`).join('');

  const roRows=filtered.map(ro=>{
    const col=RO_STATUS_COLOR[ro.status]||'var(--tx3)';
    const total=ro.jobs.reduce((s,j)=>s+j.labor+j.parts,0);
    return `<div class="cr" style="cursor:pointer" onclick="roDetail='${ro.id}';render()">
      <div style="flex:1;min-width:0">
        <div style="display:flex;align-items:center;gap:8px;margin-bottom:3px">
          <span style="font-size:12px;font-weight:700;color:var(--acc)">${ro.id}</span>
          <span style="font-size:10px;padding:2px 8px;border-radius:4px;background:${col}22;color:${col}">${ro.status}</span>
          <span style="font-size:10px;color:var(--tx3)">${ro.type}</span>
        </div>
        <div style="font-size:13px;font-weight:600;color:var(--tx)">${ro.customer} — ${ro.vehicle}</div>
        <div style="font-size:11px;color:var(--tx3);margin-top:2px">${ro.concern}</div>
      </div>
      <div style="text-align:right;flex-shrink:0">
        <div style="font-size:13px;font-weight:700;color:var(--tx)">$${total.toLocaleString()}</div>
        <div style="font-size:11px;color:var(--tx3)">Promise: ${ro.promise}</div>
        <div style="font-size:11px;color:var(--tx3)">${ro.advisor} / ${ro.tech}</div>
      </div>
    </div>`;
  }).join('');

  // Stats
  const totalOpen=SERVICE_ROS.filter(r=>!['Completed'].includes(r.status)).length;
  const totalRev=SERVICE_ROS.reduce((s,r)=>s+r.jobs.reduce((ss,j)=>ss+j.labor+j.parts,0),0);

  return `${bc('Service')}
  <div class="ph" style="display:flex;align-items:center;justify-content:space-between">
    <div><h1>Service Department</h1><p>Repair orders, bays and technician management</p></div>
    ${btn(svgIcon('plus',12)+' New RO','btn btn-p btn-sm','onclick="newRO()"')}
  </div>
  <div style="padding:0 20px">
    <div class="sr" style="grid-template-columns:repeat(4,1fr);margin-bottom:16px">
      ${statCard('Open ROs',totalOpen,'var(--ora)')}
      ${statCard('In Progress',SERVICE_ROS.filter(r=>r.status==='In Progress').length,'var(--acc)')}
      ${statCard('Ready for Pickup',SERVICE_ROS.filter(r=>r.status==='Ready').length,'var(--grn)')}
      ${statCard("Today's Revenue",'$'+totalRev.toLocaleString(),'var(--pur)')}
    </div>
    <div style="margin-bottom:16px">
      <div style="font-size:12px;font-weight:600;color:var(--tx2);margin-bottom:10px">SERVICE BAYS</div>
      <div style="display:grid;grid-template-columns:repeat(4,1fr);gap:10px">${bayCards}</div>
    </div>
    <div class="cs">
      <div class="cs-h">
        <div style="display:flex;align-items:center;gap:7px">${svgIcon('wrench',14)}<h3>Repair Orders</h3></div>
        <div class="pills">${tabHtml}</div>
      </div>
      ${roRows||'<div style="padding:20px;color:var(--tx3);text-align:center">No ROs match this filter.</div>'}
    </div>
  </div>`;
}
function newRO(){
  showToast('New RO form — coming soon');
}


/* ══════════════════════════════════════════
   REPORTS
══════════════════════════════════════════ */

/* ── Auto-init ── */
initService();

})();

/* ── DOM init ── */
function initService() {
  const ROS = window.SERVICE_ROS || [
    { id:'RO-1041', customer:'Sarah M.', vehicle:'2022 BMW X3',  tech:'Tom W.', status:'In Progress', type:'Customer Pay', jobs:'Oil change, Tire rotation', promised:'Today 4pm',  bay:1, hours:2.5, total:280 },
    { id:'RO-1042', customer:'James R.', vehicle:'2023 Tesla S',  tech:'',      status:'Waiting',     type:'Warranty',     jobs:'Software update, HVAC diag', promised:'Tomorrow', bay:2, hours:0,   total:0   },
    { id:'RO-1039', customer:'Marcus J.',vehicle:'2022 Audi Q7',  tech:'Lisa C.',status:'Ready',       type:'Customer Pay', jobs:'Brake pads + rotors',        promised:'Ready now',bay:3, hours:3.0, total:620 },
    { id:'RO-1040', customer:'Walk-in',  vehicle:'2019 Honda CR-V',tech:'Tom W.',status:'In Progress', type:'Internal',     jobs:'Detail, inspect',            promised:'EOD',      bay:4, hours:1.0, total:0   },
  ];

  document.getElementById('svc-stat-open').textContent  = ROS.filter(r => r.status !== 'Ready').length;
  document.getElementById('svc-stat-prog').textContent  = ROS.filter(r => r.status === 'In Progress').length;
  document.getElementById('svc-stat-ready').textContent = ROS.filter(r => r.status === 'Ready').length;
  document.getElementById('svc-stat-rev').textContent   = '$' + ROS.reduce((s,r)=>s+r.total,0).toLocaleString();

  const STATUS_COL = { 'In Progress':'var(--ora)', 'Waiting':'var(--blu)', 'Ready':'var(--grn)' };
  const bayEl = document.getElementById('bay-map');
  if (bayEl) bayEl.innerHTML = ROS.map(r => `
    <div style="background:var(--s2);border:1px solid var(--b1);border-radius:10px;padding:14px">
      <div style="font-size:10px;color:var(--tx3);font-weight:700;text-transform:uppercase;letter-spacing:.5px;margin-bottom:6px">Bay ${r.bay}</div>
      <div style="font-size:13px;font-weight:600;margin-bottom:4px">${r.vehicle}</div>
      <div style="font-size:11px;color:var(--tx2);margin-bottom:8px">${r.tech || 'Unassigned'}</div>
      <span style="font-size:10px;padding:2px 9px;border-radius:10px;background:${STATUS_COL[r.status]}22;color:${STATUS_COL[r.status]};font-weight:600">● ${r.status}</span>
    </div>`).join('');

  renderROList(ROS);

  window.setROFilter = function(f) {
    document.querySelectorAll('[data-ro-filter]').forEach(el => el.classList.toggle('on', el.dataset.roFilter === f));
    renderROList(f === 'All' ? ROS : ROS.filter(r => r.status === f));
  };

  function renderROList(list) {
    const el = document.getElementById('ro-list');
    if (!el) return;
    el.innerHTML = list.map(r => `
      <div class="cr">
        <div style="flex:1">
          <div style="display:flex;align-items:center;gap:8px;margin-bottom:3px">
            <span style="font-weight:700;font-family:'Syne',sans-serif">${r.id}</span>
            <span style="font-size:10px;padding:2px 8px;border-radius:4px;background:${STATUS_COL[r.status]}22;color:${STATUS_COL[r.status]}">${r.status}</span>
            <span style="font-size:10px;color:var(--tx3)">${r.type}</span>
          </div>
          <div style="font-size:13px;font-weight:500">${r.customer} — ${r.vehicle}</div>
          <div style="font-size:11px;color:var(--tx3);margin-top:3px">${r.jobs}</div>
        </div>
        <div style="text-align:right;flex-shrink:0">
          <div style="font-size:12px;color:var(--tx2)">Promise: ${r.promised}</div>
          <div style="font-size:13px;font-weight:600;color:var(--grn);margin-top:3px">${r.total ? '$'+r.total.toLocaleString() : '—'}</div>
        </div>
      </div>`).join('');
  }

  window.newROModal = function() { showToast('New RO form coming soon'); };
}
