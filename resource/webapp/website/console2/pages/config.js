/* pages/config.js */
(function () {
  const TEAM = [
    { name: 'Alex Thompson', role: 'Sales Manager',   email: 'alex@dealer.io',  initials: 'AT' },
    { name: 'David Kim',     role: 'Sales Associate', email: 'david@dealer.io', initials: 'DK' },
    { name: 'Maria Garcia',  role: 'Service Manager', email: 'maria@dealer.io', initials: 'MG' },
    { name: 'Lisa Chen',     role: 'Marketing',       email: 'lisa@dealer.io',  initials: 'LC' },
  ];

  const TOGGLES = {
    'Auto-assign Leads': true, 'Email Notifications': true,
    'SMS Alerts': false, 'Dark Mode': true, 'Show Pricing': true, 'Live Chat': true,
  };

  const INTEGRATIONS = [
    { name: 'Google Analytics', connected: true,  icon: '📊' },
    { name: 'Mailchimp',        connected: true,  icon: '📧' },
    { name: 'Twilio SMS',       connected: false, icon: '💬' },
    { name: 'Stripe',           connected: false, icon: '💳' },
  ];

  // Team list
  const teamEl = document.getElementById('team-list');
  if (teamEl) teamEl.innerHTML = TEAM.map(m => `
    <div class="cr">
      <div class="av" style="font-size:11px;flex-shrink:0">${m.initials}</div>
      <div style="flex:1">
        <div style="font-size:13px;font-weight:600">${m.name}</div>
        <div style="font-size:11px;color:var(--tx3)">${m.role} · ${m.email}</div>
      </div>
      <button class="btn btn-s btn-sm">Edit</button>
    </div>`).join('');

  // Toggles
  const togEl = document.getElementById('toggles-list');
  if (togEl) togEl.innerHTML = Object.entries(TOGGLES).map(([label, on]) => `
    <div class="cr" style="justify-content:space-between">
      <span style="font-size:13px">${label}</span>
      <div class="tog${on ? ' on' : ''}" onclick="this.classList.toggle('on');showToast('${label} '+(this.classList.contains('on')?'enabled':'disabled'))"></div>
    </div>`).join('');

  // Integrations
  const intEl = document.getElementById('integrations-list');
  if (intEl) intEl.innerHTML = INTEGRATIONS.map(i => `
    <div class="cr" style="justify-content:space-between">
      <div style="display:flex;align-items:center;gap:10px">
        <span style="font-size:18px">${i.icon}</span>
        <div>
          <div style="font-size:13px;font-weight:500">${i.name}</div>
          <div style="font-size:11px;color:${i.connected ? 'var(--grn)' : 'var(--tx3)'}">
            ${i.connected ? '● Connected' : '○ Not connected'}
          </div>
        </div>
      </div>
      <button class="btn btn-s btn-sm" onclick="showToast('${i.connected ? 'Disconnecting' : 'Connecting'} ${i.name}…')">
        ${i.connected ? 'Disconnect' : 'Connect'}
      </button>
    </div>`).join('');

  window.saveDomain = function () { showToast('Domain settings saved'); };

  // Ad campaigns stub
  const adEl = document.getElementById('ad-campaigns-list');
  if (adEl) adEl.innerHTML = `
    ${['Google Search — "Buy BMW Utah"', 'Facebook — Inventory Carousel', 'KSL Display Ads'].map((name, i) => `
    <div class="cr">
      <div style="flex:1">
        <div style="font-size:13px;font-weight:500">${name}</div>
        <div style="font-size:11px;color:var(--tx3)">$${[2800,3100,2500][i].toLocaleString()}/mo · ${[18,23,12][i]} leads</div>
      </div>
      <span class="bdg bg-grn">Active</span>
    </div>`).join('')}`;
})();
