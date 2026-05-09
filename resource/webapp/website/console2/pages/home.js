/* pages/home.js — populates the home dashboard after home.html loads */

(function () {

  const FEATURES = [
    { id:'inventory',   label:'Inventory',     desc:'Vehicle stock, pricing and listings',                   icon:'car',    color:'#7c6eff', bg:'rgba(124,110,255,.12)', badge:'',          bc:'' },
    { id:'leads',       label:'Leads & CRM',   desc:'Prospects, pipeline and follow-ups',                   icon:'leads',  color:'#1fd6a0', bg:'rgba(31,214,160,.12)',  badge:'12 new',    bc:'bg-grn' },
    { id:'tasks',       label:'Tasks',         desc:'Daily to-dos, follow-ups and team assignments',         icon:'task',   color:'#f87171', bg:'rgba(248,113,113,.12)', badge:'',          bc:'bg-red' },
    { id:'website',     label:'Website',       desc:'Web pages, content and builder',                       icon:'globe',  color:'#42c4f7', bg:'rgba(66,196,247,.12)',  badge:'8 pages',   bc:'bg-blu' },
    { id:'finance',     label:'Finance',       desc:'Deal worksheets, F&I and payment calc',                icon:'dollar', color:'#ffab4c', bg:'rgba(255,171,76,.12)',  badge:'',          bc:'' },
    { id:'service',     label:'Service',       desc:'Repair orders, bays and technicians',                  icon:'wrench', color:'#42c4f7', bg:'rgba(66,196,247,.12)',  badge:'4 open',    bc:'bg-blu' },
    { id:'listings',    label:'Listings',      desc:'Push inventory to AutoTrader, Cars.com, KSL and more', icon:'cast',   color:'#1fd6a0', bg:'rgba(31,214,160,.12)',  badge:'3 portals', bc:'bg-grn' },
    { id:'analytics',   label:'Analytics',     desc:'Traffic, clicks and conversions',                      icon:'chart',  color:'#f87171', bg:'rgba(248,113,113,.12)', badge:'Live',      bc:'bg-grn' },
    { id:'reports',     label:'Reports',       desc:'Sales leaderboard and aged inventory',                 icon:'report', color:'#c084fc', bg:'rgba(192,132,252,.12)', badge:'',          bc:'' },
    { id:'advertising', label:'Advertising',   desc:'Paid ad campaigns and ROI tracking',                   icon:'zap',    color:'#ffab4c', bg:'rgba(255,171,76,.12)',  badge:'3 active',  bc:'bg-ora' },
    { id:'marketing',   label:'Marketing',     desc:'Email campaigns and automation flows',                 icon:'mail',   color:'#ff5272', bg:'rgba(255,82,114,.12)',  badge:'2 running', bc:'bg-red' },
    { id:'social',      label:'Social',        desc:'Posts, comments and social inbox',                     icon:'share',  color:'#1fd6a0', bg:'rgba(31,214,160,.12)',  badge:'7 pending', bc:'bg-ora' },
    { id:'config',      label:'Configuration', desc:'Users, domains and system settings',                   icon:'cog',    color:'#c084fc', bg:'rgba(192,132,252,.12)', badge:'',          bc:'' },
  ];

  // ── KPI stats ──
  document.getElementById('stat-vehicles').textContent = (window.CARS || []).length;
  document.getElementById('stat-leads').textContent    = (window.LEADS_DATA || []).filter(l => !l.done).length;

  // ── Feature cards ──
  const cardGrid = document.getElementById('feature-cards');
  if (cardGrid) {
    cardGrid.innerHTML = FEATURES.map(f => `
      <div class="fc" onclick="navigate('${f.id}')">
        <div class="fc-ic" style="background:${f.bg};color:${f.color}">${svgIcon(f.icon, 19)}</div>
        <div class="fc-t">${f.label}</div>
        <div class="fc-d">${f.desc}</div>
        <div class="fc-m">
          ${f.badge ? `<span class="bdg ${f.bc}">${f.badge}</span>` : '<span></span>'}
          <span style="color:var(--acc);font-size:11px">Open &#8594;</span>
        </div>
      </div>`).join('');
  }

  // ── Task icon ──
  const iconEl = document.getElementById('task-widget-icon');
  if (iconEl) iconEl.innerHTML = svgIcon('task', 14);

  // ── Today's tasks widget ──
  const tasks      = window.TASKS_DATA || [];
  const overdue    = tasks.filter(t => !t.done && t.dueTs < 0);
  const todayTasks = tasks.filter(t => !t.done && t.dueTs === 0);
  const urgent     = [...overdue, ...todayTasks];

  const overdueEl = document.getElementById('overdue-badge');
  const todayEl   = document.getElementById('today-badge');
  if (overdue.length) {
    overdueEl.style.display = '';
    overdueEl.textContent   = overdue.length + ' overdue';
  }
  if (todayTasks.length) {
    todayEl.style.display = '';
    todayEl.textContent   = todayTasks.length + ' due today';
  }

  const prioColor = { High: 'var(--red)', Medium: 'var(--ora)', Low: 'var(--tx3)' };
  const body = document.getElementById('task-widget-body');
  if (!body) return;

  if (urgent.length === 0) {
    body.innerHTML = `
      <div style="padding:28px;text-align:center;color:var(--tx3);font-size:13px">
        ${svgIcon('check', 20)}<br><br>All caught up — no tasks due today!
      </div>`;
    return;
  }

  body.innerHTML = urgent.slice(0, 6).map(t => {
    const lead = t.leadId ? (window.LEADS_DATA || []).find(l => l.id === t.leadId) : null;
    const pc   = prioColor[t.priority] || 'var(--tx3)';
    return `
      <div style="display:flex;align-items:center;gap:12px;padding:11px 16px;border-bottom:1px solid var(--b1)">
        <div onclick="toggleTask(${t.id})"
             style="width:18px;height:18px;border-radius:4px;border:2px solid var(--b2);cursor:pointer;flex-shrink:0"></div>
        <div style="flex:1;min-width:0">
          <div style="font-size:13px;color:var(--tx);white-space:nowrap;overflow:hidden;text-overflow:ellipsis">${t.text}</div>
          <div style="font-size:11px;color:var(--tx3);margin-top:2px">
            ${lead ? `<span style="color:var(--acc)">${lead.name}</span> · ` : ''}${t.assignee}
          </div>
        </div>
        <span style="font-size:10px;padding:2px 7px;border-radius:4px;background:${pc}22;color:${pc};font-weight:600;flex-shrink:0">${t.priority}</span>
        ${t.dueTs < 0 ? `<span style="font-size:11px;color:var(--red);font-weight:600;flex-shrink:0">Overdue</span>` : ''}
      </div>`;
  }).join('') +
  (urgent.length > 6 ? `
    <div style="padding:10px 16px;font-size:12px;color:var(--acc);cursor:pointer" onclick="navigate('tasks')">
      + ${urgent.length - 6} more tasks →
    </div>` : '');

})();
