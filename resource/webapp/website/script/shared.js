/* ============================================================
   shared.js — Icons, utility helpers, modal, toast
   Loaded once in index.html. All page scripts rely on these.
   ============================================================ */

const IC = {
  home:   'M3 9l9-7 9 7v11a2 2 0 01-2 2H5a2 2 0 01-2-2z|M9 22V12h6v10',
  car:    'M5 17H3a2 2 0 01-2-2V9a2 2 0 012-2h1l2-3h10l2 3h1a2 2 0 012 2v6a2 2 0 01-2 2h-2|C7,17,2|C17,17,2',
  leads:  'M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2|C9,7,4|M23 21v-2a4 4 0 00-3-3.87|M16 3.13a4 4 0 010 7.75',
  cog:    'C12,12,3|M19.07 4.93l-1.41 1.41|M4.93 4.93l1.41 1.41|M12 2v2|M12 20v2|M2 12h2|M20 12h2|M19.07 19.07l-1.41-1.41|M4.93 19.07l1.41-1.41',
  globe:  'C12,12,10|M2 12h20|M12 2a15.3 15.3 0 014 10 15.3 15.3 0 01-4 10 15.3 15.3 0 01-4-10 15.3 15.3 0 014-10z',
  chart:  'M18 20V10|M12 20V4|M6 20v-6',
  zap:    'M13 2L3 14h9l-1 8 10-12h-9l1-8z',
  mail:   'M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z|M22 6l-10 7-10-7',
  share:  'M4 12v8a2 2 0 002 2h12a2 2 0 002-2v-8|M16 6l-4-4-4 4|M12 2v13',
  plus:   'M12 5v14|M5 12h14',
  edit:   'M11 4H4a2 2 0 00-2 2v14a2 2 0 002 2h14a2 2 0 002-2v-7|M18.5 2.5a2.121 2.121 0 013 3L12 15l-4 1 1-4 9.5-9.5z',
  trash:  'M3 6h18|M8 6V4a1 1 0 011-1h6a1 1 0 011 1v2|M19 6l-1 14a2 2 0 01-2 2H8a2 2 0 01-2-2L5 6|M10 11v6|M14 11v6',
  search: 'C11,11,8|M21 21l-4.35-4.35',
  bell:   'M18 8A6 6 0 006 8c0 7-3 9-3 9h18s-3-2-3-9|M13.73 21a2 2 0 01-3.46 0',
  close:  'M18 6L6 18|M6 6l12 12',
  check:  'M20 6L9 17l-5-5',
  eye:    'M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z|C12,12,3',
  phone:  'M22 16.92v3a2 2 0 01-2.18 2 19.79 19.79 0 01-8.63-3.07A19.5 19.5 0 013.07 9.81a19.79 19.79 0 01-3.07-8.67A2 2 0 012 1h3a2 2 0 012 1.72c.127.96.361 1.903.7 2.81a2 2 0 01-.45 2.11L6.91 8.09a16 16 0 006 6l1.27-1.27a2 2 0 012.11-.45c.907.339 1.85.573 2.81.7A2 2 0 0122 15v1.92z',
  chat:   'M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2z',
  trendUp:'M23 6L13.5 15.5 8.5 10.5 1 18|M17 6h6v6',
  send:   'M22 2L11 13|M22 2l-7 20-4-9-9-4 20-7z',
  logout: 'M9 21H5a2 2 0 01-2-2V5a2 2 0 012-2h4|M16 17l5-5-5-5|M21 12H9',
  layout: 'R3,3,18,18,2|M3 9h18|M9 21V9',
  type:   'M4 7V4h16v3|M9 20h6|M12 4v16',
  image:  'R3,3,18,18,2|C8.5,8.5,1.5|M21 15l-5-5L5 21',
  form:   'M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z|M8 13h8|M8 17h8|M8 9h2',
  grid:   'R3,3,7,7,0|R14,3,7,7,0|R14,14,7,7,0|R3,14,7,7,0',
  minus:  'M5 12h14',
  arrowU: 'M12 19V5|M5 12l7-7 7 7',
  arrowD: 'M12 5v14|M19 12l-7 7-7-7',
  arrowL: 'M19 12H5|M12 19l-7-7 7-7',
  arrowR: 'M5 12h14|M12 5l7 7-7 7',
  save:   'M19 21H5a2 2 0 01-2-2V5a2 2 0 012-2h11l5 5v11a2 2 0 01-2 2z|M17 21V13H7v8|M7 3v5h8',
  panels: 'R3,3,18,18,2|M9 3v18',
  msg:    'M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2z',
  dollar: 'M12 1v22|M17 5H9.5a3.5 3.5 0 000 7h5a3.5 3.5 0 010 7H6',
  wrench: 'M14.7 6.3a1 1 0 000 1.4l1.6 1.6a1 1 0 001.4 0l3.77-3.77a6 6 0 01-7.94 7.94l-6.91 6.91a2.12 2.12 0 01-3-3l6.91-6.91a6 6 0 017.94-7.94l-3.76 3.76z',
  clip:   'M16 4h2a2 2 0 012 2v14a2 2 0 01-2 2H6a2 2 0 01-2-2V6a2 2 0 012-2h2|R8,2,8,4,1',
  truck:  'M1 3h15v13H1z|M16 8h4l3 3v5h-7V8z|C5.5,19,1.5|C18.5,19,1.5',
  repeat: 'M17 1l4 4-4 4|M3 11V9a4 4 0 014-4h14|M7 23l-4-4 4-4|M21 13v2a4 4 0 01-4 4H3',
  report: 'M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8z|M14 2v6h6|M16 13H8|M16 17H8|M10 9H8',
  star:   'M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z',
  key:    'C7.5,16.5,5|M21 2l-2 2m-7.61 7.61a5.5 5.5 0 01-7.778 7.778 5.5 5.5 0 017.777-7.777zm0 0L15.5 7.5m0 0l3 3L22 7l-3-3m-3.5 3.5L19 4',
  pipe:   'M22 12H2|M5 12l7-7 7 7|M5 12l7 7 7-7',
  cast:   'M2 16.1A5 5 0 015.9 20M2 12.05A9 9 0 019.95 20M2 8V6a2 2 0 012-2h16a2 2 0 012 2v12a2 2 0 01-2 2h-6|C22,19,1',
  wifi:   'M5 12.55a11 11 0 0114.08 0|M1.42 9a16 16 0 0121.16 0|M8.53 16.11a6 6 0 016.95 0|C12,21,1',
  clock:  'C12,12,10|M12 6v6l4 2',
  flag:   'M4 15s1-1 4-1 5 2 8 2 4-1 4-1V3s-1 1-4 1-5-2-8-2-4 1-4 1z|M4 22v-7',
  task:   'M9 11l3 3L22 4|M21 12v7a2 2 0 01-2 2H5a2 2 0 01-2-2V5a2 2 0 012-2h11',
};

function svgIcon(name, size = 16) {
  const d = IC[name] || '';
  const parts = d.split('|');
  const inner = parts.map(p => {
    if (p.startsWith('C')) { const [cx,cy,r]=p.slice(1).split(','); return `<circle cx="${cx}" cy="${cy}" r="${r}"/>`; }
    if (p.startsWith('R')) { const [x,y,w,h,rx]=p.slice(1).split(','); return `<rect x="${x}" y="${y}" width="${w}" height="${h}" rx="${rx}"/>`; }
    return `<path d="${p}"/>`;
  }).join('');
  return `<svg width="${size}" height="${size}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">${inner}</svg>`;
}

/* ── Small HTML helpers ── */
function btn(label, cls = 'btn btn-s', attrs = '') {
  return `<button class="${cls}" ${attrs}>${label}</button>`;
}
function bdg(label, cls = '') {
  return `<span class="bdg ${cls}">${label}</span>`;
}
function statCard(label, val, color = 'var(--tx)', change = '') {
  return `<div class="sc"><div class="sc-l">${label}</div><div class="sc-v" style="color:${color}">${val}</div>${change ? `<div class="sc-c">${change}</div>` : ''}</div>`;
}
function bc(label, backPage = '') {
  return `<div class="bcd">${backPage ? `<span onclick="navigate('${backPage}')">${backPage}</span> /` : ''} <span>${label}</span></div>`;
}
function formField(label, type, id, placeholder = '') {
  return `<div><label style="font-size:11px;color:var(--tx3);display:block;margin-bottom:5px">${label}</label><input class="inp" type="${type}" id="${id}" placeholder="${placeholder}"/></div>`;
}
function fv(id) { return document.getElementById(id)?.value || ''; }
function sh(icon, title, actionHtml = '') {
  return `<div class="cs-h"><div style="display:flex;align-items:center;gap:7px">${svgIcon(icon, 14)}<h3>${title}</h3></div>${actionHtml}</div>`;
}

/* ── Modal ── */
let _modalSaveCb = null;
function openModal(title, bodyHtml, onSave, saveLabel = 'Save') {
  _modalSaveCb = onSave;
  document.getElementById('modal-title').textContent = title;
  document.getElementById('modal-body').innerHTML = bodyHtml;
  document.getElementById('modal-save-btn').textContent = saveLabel;
  document.getElementById('modal-overlay').style.display = 'flex';
}
function closeModal() {
  document.getElementById('modal-overlay').style.display = 'none';
  _modalSaveCb = null;
}
function modalSave() { if (_modalSaveCb) _modalSaveCb(); }

/* ── Toast ── */
function showToast(msg, color = 'var(--grn)') {
  const t = document.getElementById('toast');
  t.textContent = msg;
  t.style.background = color;
  t.style.opacity = '1';
  t.style.transform = 'translateY(0)';
  clearTimeout(window._toastTimer);
  window._toastTimer = setTimeout(() => {
    t.style.opacity = '0';
    t.style.transform = 'translateY(8px)';
  }, 2800);
}
