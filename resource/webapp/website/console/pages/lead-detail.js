/* pages/lead-detail.js */
(function(){

function openLeadDetail(id){
  currentLeadId = id;
  currentPage = 'lead-detail';
  navigate('lead-detail');
}

function renderLeadDetail(){
  const lead = LEADS_DATA.find(l=>l.id===currentLeadId);
  if(!lead) return bc('Leads','leads')+'<div class="ph"><p>Lead not found.</p></div>';

  const stageBar = DEAL_STAGES.map(s=>{
    const active = s===lead.stage;
    const col = STAGE_COLORS[s]||'var(--tx3)';
    return `<div onclick="ldSetStage(${lead.id},'${s}')" style="flex:1;padding:7px 4px;text-align:center;font-size:10px;font-weight:${active?700:500};cursor:pointer;border-bottom:2px solid ${active?col:'transparent'};color:${active?col:'var(--tx3)'};transition:all .2s;white-space:nowrap">${s}</div>`;
  }).join('');

  const noteRows = (lead.notes||[]).map(n=>`
    <div style="display:flex;gap:10px;margin-bottom:14px">
      <div style="width:28px;height:28px;border-radius:50%;background:var(--acc)22;color:var(--acc);display:flex;align-items:center;justify-content:center;font-size:10px;font-weight:700;flex-shrink:0">${n.by[0]}</div>
      <div style="flex:1">
        <div style="font-size:12px;font-weight:600;color:var(--tx);margin-bottom:2px">${n.by} <span style="color:var(--tx3);font-weight:400">${n.ts}</span></div>
        <div style="font-size:13px;color:var(--tx2);line-height:1.6;background:var(--s2);padding:10px 12px;border-radius:8px">${n.text}</div>
      </div>
    </div>`).join('');

  const taskRows = (lead.tasks||[]).map((t,i)=>`
    <div style="display:flex;align-items:center;gap:10px;padding:10px 0;border-bottom:1px solid var(--b1)">
      <div onclick="ldToggleTask(${lead.id},${i})" style="width:18px;height:18px;border-radius:4px;border:1.5px solid ${t.done?'var(--grn)':'var(--b2)'};background:${t.done?'var(--grn)':'transparent'};cursor:pointer;display:flex;align-items:center;justify-content:center;flex-shrink:0">
        ${t.done?'<svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="3"><polyline points="20 6 9 17 4 12"/></svg>':''}
      </div>
      <div style="flex:1;font-size:13px;color:${t.done?'var(--tx3)':'var(--tx)'};text-decoration:${t.done?'line-through':'none'}">${t.text}</div>
      <div style="font-size:11px;color:var(--tx3)">${t.due}</div>
    </div>`).join('');

  const tradeHtml = lead.tradeIn
    ? `<div style="background:var(--s2);border:1px solid var(--b1);border-radius:8px;padding:14px">
        <div style="font-size:12px;color:var(--tx3);margin-bottom:6px">Trade-In Vehicle</div>
        <div style="font-size:14px;font-weight:600;color:var(--tx)">${lead.tradeIn.year} ${lead.tradeIn.make} ${lead.tradeIn.model}</div>
        <div style="font-size:18px;font-weight:700;color:var(--grn);margin-top:4px">Est. $${lead.tradeIn.est.toLocaleString()}</div>
      </div>`
    : `<div style="background:var(--s2);border:1px solid var(--b1);border-radius:8px;padding:14px;text-align:center;color:var(--tx3);font-size:13px">No trade-in</div>`;

  const sBg={Hot:'rgba(255,82,114,.12)',Warm:'rgba(255,171,76,.12)',Cold:'rgba(66,196,247,.12)'};
  const sTx={Hot:'var(--red)',Warm:'var(--ora)',Cold:'var(--blu)'};

  return bc('Leads','leads')
  +`<div class="ph" style="display:flex;align-items:center;justify-content:space-between;flex-wrap:wrap;gap:10px">
      <div style="display:flex;align-items:center;gap:14px">
        <div style="width:48px;height:48px;border-radius:50%;background:var(--acc)22;color:var(--acc);display:flex;align-items:center;justify-content:center;font-family:'Syne',sans-serif;font-weight:800;font-size:18px">${lead.name.split(' ').map(w=>w[0]).join('')}</div>
        <div><h1 style="margin-bottom:2px">${lead.name}</h1><p style="margin:0">${lead.interest} &middot; from ${lead.source}</p></div>
      </div>
      <div style="display:flex;gap:7px">
        ${btn(svgIcon('phone',12)+' Call','btn btn-s btn-sm','')}
        ${btn(svgIcon('mail',12)+' Email','btn btn-s btn-sm','')}
        ${btn(svgIcon('chat',12)+' Chat','btn btn-p btn-sm',`onclick="openChatWith(${lead.id})"`)}
      </div>
    </div>
    <!-- Stage pipeline bar -->
    <div style="background:var(--s1);border:1px solid var(--b1);border-radius:10px;display:flex;margin:0 20px 16px;overflow-x:auto">
      ${stageBar}
    </div>
    <div style="display:grid;grid-template-columns:1fr 300px;gap:14px;padding:0 20px 20px">
      <!-- LEFT -->
      <div>
        <!-- Notes -->
        <div class="cs" style="margin-bottom:14px">
          <div class="cs-h"><div style="display:flex;align-items:center;gap:7px">${svgIcon('msg',14)}<h3>Activity & Notes</h3></div></div>
          <div style="padding:16px">
            ${noteRows||'<div style="color:var(--tx3);font-size:13px">No notes yet.</div>'}
            <div style="display:flex;gap:8px;margin-top:12px">
              <input class="inp" id="ld-note-inp" placeholder="Add a note..." style="flex:1"/>
              ${btn('Add Note','btn btn-p btn-sm',`onclick="ldAddNote(${lead.id})"`)}
            </div>
          </div>
        </div>
        <!-- Tasks -->
        <div class="cs">
          <div class="cs-h"><div style="display:flex;align-items:center;gap:7px">${svgIcon('check',14)}<h3>Tasks</h3></div>${btn(svgIcon('plus',12)+' Add','btn btn-s btn-sm',`onclick="ldAddTask(${lead.id})"`)}</div>
          <div style="padding:0 16px">
            ${taskRows||'<div style="color:var(--tx3);font-size:13px;padding:14px 0">No tasks.</div>'}
          </div>
        </div>
      </div>
      <!-- RIGHT sidebar -->
      <div>
        <!-- Contact info -->
        <div class="cs" style="margin-bottom:14px">
          <div class="cs-h"><div style="display:flex;align-items:center;gap:7px">${svgIcon('leads',14)}<h3>Contact Info</h3></div></div>
          <div style="padding:14px;display:flex;flex-direction:column;gap:10px">
            <div><div style="font-size:10px;color:var(--tx3);text-transform:uppercase;margin-bottom:3px">Email</div><div style="font-size:13px;color:var(--tx)">${lead.email}</div></div>
            <div><div style="font-size:10px;color:var(--tx3);text-transform:uppercase;margin-bottom:3px">Phone</div><div style="font-size:13px;color:var(--tx)">${lead.phone}</div></div>
            <div><div style="font-size:10px;color:var(--tx3);text-transform:uppercase;margin-bottom:3px">Lead Score</div>
              <div style="display:flex;align-items:center;gap:8px">
                <div style="flex:1;height:6px;background:var(--b1);border-radius:3px"><div style="width:${lead.score}%;height:100%;background:${sTx[lead.status]};border-radius:3px"></div></div>
                <span style="font-size:12px;font-weight:700;color:${sTx[lead.status]}">${lead.score}</span>
              </div>
            </div>
            <div><div style="font-size:10px;color:var(--tx3);text-transform:uppercase;margin-bottom:3px">Assigned To</div><div style="font-size:13px;color:var(--tx)">${lead.assigned||'Unassigned'}</div></div>
            <div><div style="font-size:10px;color:var(--tx3);text-transform:uppercase;margin-bottom:3px">Source</div><div style="font-size:13px;color:var(--tx)">${lead.source}</div></div>
            <div><div style="font-size:10px;color:var(--tx3);text-transform:uppercase;margin-bottom:3px">Status</div>
              <span style="background:${sBg[lead.status]};color:${sTx[lead.status]};padding:3px 10px;border-radius:4px;font-size:11px;font-weight:700">${lead.status}</span>
            </div>
          </div>
        </div>
        <!-- Trade-in -->
        <div class="cs" style="margin-bottom:14px">
          <div class="cs-h"><div style="display:flex;align-items:center;gap:7px">${svgIcon('repeat',14)}<h3>Trade-In</h3></div></div>
          <div style="padding:14px">${tradeHtml}</div>
        </div>
        <!-- Quick actions -->
        <div class="cs">
          <div class="cs-h"><div style="display:flex;align-items:center;gap:7px">${svgIcon('zap',14)}<h3>Quick Actions</h3></div></div>
          <div style="padding:14px;display:flex;flex-direction:column;gap:8px">
            ${btn(svgIcon('dollar',12)+' Open Deal Worksheet','btn btn-p btn-sm','style="width:100%" onclick="navigate(\'finance\')"')}
            ${btn(svgIcon('car',12)+' View Matching Inventory','btn btn-s btn-sm','style="width:100%" onclick="navigate(\'inventory\')"')}
            ${btn(svgIcon('report',12)+' Run Credit App','btn btn-s btn-sm','style="width:100%"')}
          </div>
        </div>
      </div>
    </div>`;
}


function addLeadModal(){
  openModal('Add Lead',`
    <div style="display:flex;flex-direction:column;gap:10px">
      ${formField('Name','text','al-name','Full name')}
      ${formField('Email','email','al-email','email@example.com')}
      ${formField('Phone','text','al-phone','+1 555-0000')}
      ${formField('Interested In','text','al-interest','e.g. BMW X5 2024')}
      <div>
        <label style="font-size:11px;color:var(--tx3);display:block;margin-bottom:5px">Source</label>
        <select class="inp" id="al-source">
          <option>Website</option><option>AutoTrader</option><option>Cars.com</option>
          <option>Facebook</option><option>Google Ad</option><option>Walk-in</option><option>Referral</option>
        </select>
      </div>
      <div>
        <label style="font-size:11px;color:var(--tx3);display:block;margin-bottom:5px">Status</label>
        <select class="inp" id="al-status">
          <option>Hot</option><option>Warm</option><option>Cold</option>
        </select>
      </div>
    </div>`,
    ()=>{
      const name=document.getElementById('al-name')?.value?.trim();
      if(!name){showToast('Name required','var(--red)');return;}
      const newId=Math.max(...LEADS_DATA.map(l=>l.id))+1;
      LEADS_DATA.push({
        id:newId,
        name,
        email:document.getElementById('al-email')?.value||'',
        phone:document.getElementById('al-phone')?.value||'',
        interest:document.getElementById('al-interest')?.value||'',
        source:document.getElementById('al-source')?.value||'Website',
        status:document.getElementById('al-status')?.value||'Warm',
        score:50,channels:['chat'],last:'Just now',stage:'New',
        assigned:'',notes:[],tasks:[],tradeIn:null
      });
      closeModal();render();showToast('Lead added');
    },'Add Lead');
}

function ldSetStage(id, stage){
  const lead = LEADS_DATA.find(l=>l.id===id); if(!lead) return;
  lead.stage = stage;
  render();
}
function ldAddNote(id){
  const inp = document.getElementById('ld-note-inp');
  const txt = inp ? inp.value.trim() : '';
  if(!txt) return;
  const lead = LEADS_DATA.find(l=>l.id===id); if(!lead) return;
  lead.notes = lead.notes||[];
  lead.notes.unshift({by:'You', text:txt, ts:'Just now'});
  inp.value='';
  render();
}
function ldToggleTask(id, idx){
  const lead = LEADS_DATA.find(l=>l.id===id); if(!lead) return;
  lead.tasks[idx].done = !lead.tasks[idx].done;
  render();
}
function ldAddTask(id){
  const txt = prompt('Task description:');
  if(!txt) return;
  const due = prompt('Due date/time:','Tomorrow') || 'Soon';
  const lead = LEADS_DATA.find(l=>l.id===id); if(!lead) return;
  lead.tasks = lead.tasks||[];
  lead.tasks.push({text:txt, done:false, due});
  render();
}


/* ══════════════════════════════════════════
   LISTINGS — THIRD-PARTY PORTALS
══════════════════════════════════════════ */
let PORTALS = [
  {
    id:'autotrader', name:'AutoTrader', region:'National',
    logo:'AT', logoColor:'#f26b21', logoBg:'#fff3eb',
    url:'', token:'', format:'ADF/XML',
    enabled:true, status:'Active', lastSync:'2 min ago',
    leads:47, views:1840, listed:CARS ? CARS.length : 6,
    syncInterval:'15 min', errors:[],
  },
  {
    id:'carsdotcom', name:'Cars.com', region:'National',
    logo:'C.', logoColor:'#e4002b', logoBg:'#fff0f1',
    url:'', token:'', format:'JSON',
    enabled:true, status:'Active', lastSync:'8 min ago',
    leads:31, views:1220, listed:CARS ? CARS.length : 6,
    syncInterval:'30 min', errors:[],
  },
  {
    id:'ksl', name:'KSL Classifieds', region:'Utah / Mountain West',
    logo:'KSL', logoColor:'#0057a8', logoBg:'#eef4ff',
    url:'', token:'', format:'XML',
    enabled:true, status:'Active', lastSync:'12 min ago',
    leads:28, views:980, listed:CARS ? CARS.length : 6,
    syncInterval:'30 min', errors:[],
  },
  {
    id:'cargurus', name:'CarGurus', region:'National',
    logo:'CG', logoColor:'#00a090', logoBg:'#e8faf8',
    url:'', token:'', format:'JSON',
    enabled:false, status:'Disconnected', lastSync:'—',
    leads:0, views:0, listed:0,
    syncInterval:'60 min', errors:[],
  },
  {
    id:'facebook', name:'Facebook Marketplace', region:'National / Local',
    logo:'fb', logoColor:'#1877f2', logoBg:'#eef4ff',
    url:'', token:'', format:'CSV',
    enabled:false, status:'Disconnected', lastSync:'—',
    leads:0, views:0, listed:0,
    syncInterval:'60 min', errors:[],
  },
];

let listingPortalDetail = null; // id of portal being configured

/* ── Auto-init ── */
initLeadDetail();

})();

/* ── DOM init ── */
function initLeadDetail() {
  const id   = window._leadDetailId;
  const lead = (window.LEADS_DATA || []).find(l => l.id === id);
  const el   = document.getElementById('lead-detail-content');
  if (!el) return;
  if (!lead) {
    el.innerHTML = `<div style="padding:60px;text-align:center;color:var(--tx3)">Lead not found. <button class="btn btn-s btn-sm" onclick="navigate('leads')">Back to Leads</button></div>`;
    return;
  }
  const STAGES = ['New','Contacted','Test Drive','Negotiating','F&I','Closed Won','Closed Lost'];
  const STAGE_COL = { New:'var(--tx3)',Contacted:'var(--blu)','Test Drive':'var(--ora)',Negotiating:'var(--pur)','F&I':'var(--acc)','Closed Won':'var(--grn)','Closed Lost':'var(--red)' };
  const curStage = lead.stage || 'New';

  function renderDetail() {
    el.innerHTML = `
      <div class="bcd"><span onclick="navigate('leads')" style="cursor:pointer">Leads</span> / <span>${lead.name}</span></div>
      <div class="ph"><h1>${lead.name}</h1><p>${lead.interest} · ${lead.source || 'Unknown source'}</p></div>

      <!-- Stage pipeline bar -->
      <div class="cs" style="margin-bottom:14px">
        <div class="cs-h"><h3>Deal Pipeline</h3></div>
        <div style="display:flex;overflow-x:auto;padding:16px;gap:0">
          ${STAGES.map(s => {
            const col  = STAGE_COL[s];
            const active = s === curStage;
            return `<div onclick="setLeadStage(${lead.id},'${s}')"
              style="flex:1;min-width:80px;padding:10px 8px;text-align:center;cursor:pointer;border-bottom:3px solid ${active?col:'var(--b1)'};transition:border-color .2s">
              <div style="font-size:10px;font-weight:${active?700:400};color:${active?col:'var(--tx3)'};white-space:nowrap">${s}</div>
            </div>`;
          }).join('')}
        </div>
      </div>

      <div style="display:grid;grid-template-columns:1fr 300px;gap:16px;padding:0 20px 20px">
        <!-- Left: activity feed + tasks -->
        <div>
          <!-- Notes -->
          <div class="cs" style="margin-bottom:14px">
            <div class="cs-h"><h3>Notes & Activity</h3></div>
            <div id="ld-notes-list">
              ${(lead.notes||[]).map(n=>`<div class="cr"><div style="flex:1"><div style="font-size:13px">${n.text}</div><div style="font-size:11px;color:var(--tx3)">${n.by} · ${n.when}</div></div></div>`).join('') || '<div style="padding:16px;color:var(--tx3);text-align:center;font-size:13px">No notes yet</div>'}
            </div>
            <div style="padding:12px 16px;display:flex;gap:8px;border-top:1px solid var(--b1)">
              <input class="inp" id="ld-note-input" placeholder="Add a note…" style="flex:1" onkeydown="if(event.key==='Enter')addLeadNote(${lead.id})"/>
              <button class="btn btn-p btn-sm" onclick="addLeadNote(${lead.id})">Add</button>
            </div>
          </div>

          <!-- Tasks -->
          <div class="cs">
            <div class="cs-h" style="display:flex;align-items:center;justify-content:space-between">
              <h3>Tasks</h3>
              <button class="btn btn-s btn-sm" onclick="addLeadTask(${lead.id})">+ Task</button>
            </div>
            <div id="ld-tasks-list">
              ${(lead.tasks||[]).map((t,i)=>`
                <div class="cr">
                  <div onclick="toggleLeadTask(${lead.id},${i})"
                    style="width:18px;height:18px;border-radius:4px;border:2px solid ${t.done?'var(--grn)':'var(--b2)'};background:${t.done?'var(--grn)':'transparent'};cursor:pointer;flex-shrink:0;display:flex;align-items:center;justify-content:center">
                    ${t.done?'<svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="3.5"><polyline points="20 6 9 17 4 12"/></svg>':''}
                  </div>
                  <span style="font-size:13px;flex:1;text-decoration:${t.done?'line-through':'none'};color:${t.done?'var(--tx3)':'var(--tx)'}">${t.text}</span>
                  <span style="font-size:11px;color:var(--tx3)">${t.due||''}</span>
                </div>`).join('') || '<div style="padding:16px;color:var(--tx3);text-align:center;font-size:13px">No tasks</div>'}
            </div>
          </div>
        </div>

        <!-- Right: contact info -->
        <div>
          <div class="cs" style="margin-bottom:14px">
            <div class="cs-h"><h3>Contact Info</h3></div>
            <div style="padding:16px;display:flex;flex-direction:column;gap:10px">
              ${[
                ['Email', lead.email||'—'], ['Phone', lead.phone||'—'],
                ['Source', lead.source||'—'], ['Assigned', lead.assigned||'Unassigned'],
                ['Status', lead.status], ['Score', lead.score + ' / 100'],
              ].map(([l,v])=>`<div><div style="font-size:11px;color:var(--tx3);margin-bottom:2px">${l}</div><div style="font-size:13px;font-weight:500">${v}</div></div>`).join('')}
            </div>
          </div>
          <div class="cs">
            <div class="cs-h"><h3>Quick Actions</h3></div>
            <div style="padding:12px;display:flex;flex-direction:column;gap:8px">
              <button class="btn btn-p" onclick="navigate('finance')">Open Deal Worksheet</button>
              <button class="btn btn-s" onclick="navigate('inventory')">View Matching Inventory</button>
              <button class="btn btn-s" onclick="showToast('Credit app opened')">Run Credit App</button>
              <button class="btn btn-s" onclick="openChatWith(${lead.id})">Open Chat</button>
            </div>
          </div>
        </div>
      </div>`;

    window.setLeadStage = function(id, stage) {
      const l = (window.LEADS_DATA||[]).find(x=>x.id===id);
      if (l) { l.stage = stage; renderDetail(); }
    };
    window.addLeadNote = function(id) {
      const l = (window.LEADS_DATA||[]).find(x=>x.id===id);
      const inp = document.getElementById('ld-note-input');
      if (!l||!inp||!inp.value.trim()) return;
      l.notes = l.notes || [];
      l.notes.push({ text: inp.value.trim(), by: 'You', when: 'Just now' });
      inp.value = '';
      renderDetail();
    };
    window.toggleLeadTask = function(id, idx) {
      const l = (window.LEADS_DATA||[]).find(x=>x.id===id);
      if (l && l.tasks[idx]) { l.tasks[idx].done = !l.tasks[idx].done; renderDetail(); }
    };
    window.addLeadTask = function(id) {
      openModal('New Task', `<input class="inp" id="lt-text" placeholder="Task description…" style="margin-bottom:10px"/><input class="inp" id="lt-due" placeholder="Due date (e.g. Thursday)"/>`,
        () => {
          const l = (window.LEADS_DATA||[]).find(x=>x.id===id);
          if (l) {
            l.tasks = l.tasks || [];
            l.tasks.push({ text: document.getElementById('lt-text')?.value||'', due: document.getElementById('lt-due')?.value||'', done:false });
          }
          closeModal(); renderDetail();
        }, 'Add Task');
    };
  }

  renderDetail();
}
