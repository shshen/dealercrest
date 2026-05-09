/* pages/leads.js — wires up leads list and pipeline */
(function () {
  let activeView   = 'list';
  let activeStatus = 'All';

  const STATUS_BG  = { Hot: 'rgba(255,82,114,.15)',  Warm: 'rgba(255,171,76,.15)', Cold: 'rgba(66,196,247,.15)' };
  const STATUS_TX  = { Hot: 'var(--red)',             Warm: 'var(--ora)',            Cold: 'var(--blu)' };
  const STATUS_BDG = { Hot: 'bg-red',                 Warm: 'bg-ora',               Cold: 'bg-blu' };
  const STAGE_COL  = {
    New:'var(--tx3)', Contacted:'var(--blu)', 'Test Drive':'var(--ora)',
    Negotiating:'var(--pur)', 'F&I':'var(--acc)', 'Closed Won':'var(--grn)', 'Closed Lost':'var(--red)'
  };

  function getFiltered() {
    const q = (document.getElementById('lead-search')?.value || '').toLowerCase();
    return (window.LEADS_DATA || []).filter(l =>
      (activeStatus === 'All' || l.status === activeStatus) &&
      (!q || (l.name + l.interest + (l.source||'')).toLowerCase().includes(q))
    );
  }

  function updateStats() {
    const leads = window.LEADS_DATA || [];
    document.getElementById('lead-stat-total').textContent = leads.length;
    document.getElementById('lead-stat-hot').textContent   = leads.filter(l => l.status === 'Hot').length;
    document.getElementById('lead-stat-warm').textContent  = leads.filter(l => l.status === 'Warm').length;
    document.getElementById('lead-stat-cold').textContent  = leads.filter(l => l.status === 'Cold').length;
    document.getElementById('lead-stat-tasks').textContent =
      leads.reduce((s, l) => s + (l.tasks || []).filter(t => !t.done).length, 0);
  }

  function renderList(filtered) {
    if (!filtered.length) return `<div style="padding:40px;text-align:center;color:var(--tx3)">No leads match this filter.</div>`;
    return filtered.map(l => {
      const initials  = l.name.split(' ').map(n => n[0]).join('');
      const sc        = l.score > 80 ? 'var(--grn)' : l.score > 50 ? 'var(--ora)' : 'var(--tx2)';
      const stageCol  = STAGE_COL[l.stage] || 'var(--tx3)';
      const tasksDue  = (l.tasks || []).filter(t => !t.done).length;
      return `
        <div class="lead-card" onclick="openLeadDetail(${l.id})">
          <div class="lead-avatar" style="background:${STATUS_BG[l.status]};color:${STATUS_TX[l.status]}">${initials}</div>
          <div style="flex:1;min-width:0">
            <div style="display:flex;align-items:center;gap:8px;margin-bottom:3px;flex-wrap:wrap">
              <span style="font-weight:600;font-size:14px">${l.name}</span>
              <span class="bdg ${STATUS_BDG[l.status]}">${l.status}</span>
              <span style="font-size:10px;padding:2px 8px;border-radius:4px;background:${stageCol}22;color:${stageCol}">${l.stage || 'New'}</span>
              ${tasksDue ? `<span style="font-size:10px;color:var(--pur)">● ${tasksDue} task${tasksDue > 1 ? 's' : ''}</span>` : ''}
            </div>
            <div style="font-size:12px;color:var(--tx2);margin-bottom:3px">${l.interest}</div>
            <div style="font-size:11px;color:var(--tx3)">
              ${svgIcon('globe', 9)} ${l.source || '—'} &nbsp;·&nbsp;
              ${svgIcon('leads', 9)} ${l.assigned || 'Unassigned'} &nbsp;·&nbsp;
              ${l.last}
            </div>
          </div>
          <div style="text-align:right;flex-shrink:0">
            <div style="font-size:20px;font-weight:700;font-family:'Syne',sans-serif;color:${sc}">${l.score}</div>
            <div style="font-size:10px;color:var(--tx3)">score</div>
          </div>
          <div style="display:flex;flex-direction:column;gap:5px;flex-shrink:0" onclick="event.stopPropagation()">
            <button class="btn btn-p btn-sm" onclick="openLeadDetail(${l.id})">${svgIcon('edit', 12)} Detail</button>
            <button class="btn btn-s btn-sm" onclick="openChatWith(${l.id})">${svgIcon('chat', 12)} Chat</button>
          </div>
        </div>`;
    }).join('');
  }

  function renderPipeline() {
    const stages = ['New','Contacted','Test Drive','Negotiating','F&I','Closed Won'];
    return `<div style="display:flex;gap:12px;overflow-x:auto;align-items:flex-start;padding-bottom:8px">` +
      stages.map(stage => {
        const stageLeads = (window.LEADS_DATA || []).filter(l =>
          (l.stage === stage) || (stage === 'New' && !l.stage)
        );
        const col = STAGE_COL[stage] || 'var(--tx3)';
        const cards = stageLeads.map(l => {
          const initials = l.name.split(' ').map(n => n[0]).join('');
          const tasksDue = (l.tasks || []).filter(t => !t.done).length;
          return `
            <div class="pipeline-card" onclick="openLeadDetail(${l.id})">
              <div style="display:flex;align-items:center;gap:8px;margin-bottom:6px">
                <div style="width:26px;height:26px;border-radius:50%;background:${STATUS_BG[l.status]||'var(--b1)'};color:${STATUS_TX[l.status]||'var(--tx)'};display:flex;align-items:center;justify-content:center;font-size:10px;font-weight:700">${initials}</div>
                <span style="font-size:12px;font-weight:600;flex:1">${l.name}</span>
                <span class="bdg ${STATUS_BDG[l.status]||''}" style="font-size:9px">${l.status}</span>
              </div>
              <div style="font-size:11px;color:var(--tx2);margin-bottom:6px">${l.interest}</div>
              <div style="display:flex;justify-content:space-between;align-items:center">
                <span style="font-size:10px;color:var(--tx3)">${svgIcon('globe', 9)} ${l.source || '—'}</span>
                ${tasksDue ? `<span style="font-size:10px;color:var(--pur)">● ${tasksDue}</span>` : `<span style="font-size:10px;color:var(--tx3)">${l.last}</span>`}
              </div>
            </div>`;
        }).join('') || `<div style="font-size:11px;color:var(--tx3);text-align:center;padding:20px 0">Empty</div>`;
        return `
          <div class="pipeline-col">
            <div style="display:flex;align-items:center;gap:6px;margin-bottom:10px;padding-bottom:8px;border-bottom:2px solid ${col}">
              <span style="font-size:11px;font-weight:700;color:${col};text-transform:uppercase;letter-spacing:.5px">${stage}</span>
              <span style="font-size:10px;background:${col}22;color:${col};padding:1px 6px;border-radius:10px">${stageLeads.length}</span>
            </div>
            ${cards}
          </div>`;
      }).join('') + '</div>';
  }

  function render() {
    updateStats();
    const ct = document.getElementById('leads-content');
    if (!ct) return;
    const filtered = getFiltered();
    ct.innerHTML = activeView === 'pipeline' ? renderPipeline() : `<div class="cs">${renderList(filtered)}</div>`;
  }

  window.setLeadsView = function (v) {
    activeView = v;
    document.querySelectorAll('[data-view]').forEach(el => el.classList.toggle('on', el.dataset.view === v));
    render();
  };

  window.setLeadsStatus = function (s) {
    activeStatus = s;
    document.querySelectorAll('[data-status]').forEach(el => el.classList.toggle('on', el.dataset.status === s));
    render();
  };

  window.filterLeads = render;

  window.openLeadDetail = function (id) {
    window._leadDetailId = id;
    navigate('lead-detail');
  };

  window.openAddLeadModal = function () {
    openModal('Add Lead', `
      <div style="display:flex;flex-direction:column;gap:10px">
        <div><label style="font-size:11px;color:var(--tx3);display:block;margin-bottom:5px">Name</label>
          <input class="inp" id="al-name" placeholder="Full name"/></div>
        <div style="display:flex;gap:10px">
          <div style="flex:1"><label style="font-size:11px;color:var(--tx3);display:block;margin-bottom:5px">Email</label>
            <input class="inp" id="al-email" type="email" placeholder="email@example.com"/></div>
          <div style="flex:1"><label style="font-size:11px;color:var(--tx3);display:block;margin-bottom:5px">Phone</label>
            <input class="inp" id="al-phone" placeholder="+1 555-0000"/></div>
        </div>
        <div><label style="font-size:11px;color:var(--tx3);display:block;margin-bottom:5px">Interested In</label>
          <input class="inp" id="al-interest" placeholder="e.g. BMW X5 2024"/></div>
        <div style="display:flex;gap:10px">
          <div style="flex:1"><label style="font-size:11px;color:var(--tx3);display:block;margin-bottom:5px">Source</label>
            <select class="inp" id="al-source">
              <option>Website</option><option>AutoTrader</option><option>Cars.com</option>
              <option>KSL</option><option>Facebook</option><option>Google Ad</option>
              <option>Walk-in</option><option>Referral</option>
            </select></div>
          <div style="flex:1"><label style="font-size:11px;color:var(--tx3);display:block;margin-bottom:5px">Status</label>
            <select class="inp" id="al-status"><option>Hot</option><option>Warm</option><option>Cold</option></select></div>
        </div>
      </div>`,
      () => {
        const name = document.getElementById('al-name')?.value?.trim();
        if (!name) { showToast('Name required', 'var(--red)'); return; }
        const newId = Math.max(...(window.LEADS_DATA || []).map(l => l.id), 0) + 1;
        (window.LEADS_DATA = window.LEADS_DATA || []).push({
          id: newId, name,
          email:    document.getElementById('al-email')?.value || '',
          phone:    document.getElementById('al-phone')?.value || '',
          interest: document.getElementById('al-interest')?.value || '',
          source:   document.getElementById('al-source')?.value || 'Website',
          status:   document.getElementById('al-status')?.value || 'Warm',
          score: 50, channels: ['chat'], last: 'Just now', stage: 'New',
          assigned: '', notes: [], tasks: [], tradeIn: null,
        });
        closeModal(); render(); showToast('Lead added');
      }, 'Add Lead');
  };

  render();
})();
