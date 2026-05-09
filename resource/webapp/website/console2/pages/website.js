function renderWebsite(){
  const cards=PAGES_DATA.map(page=>`
    <div class="pgc">
      <div class="pgp"><div class="pl h"></div><div style="height:3px"></div><div class="pl"></div><div class="pl m"></div><div class="pl sm"></div><div style="height:5px"></div><div class="pl"></div><div class="pl m"></div></div>
      <div class="pgi"><div class="pgn">${page.name}<span class="bdg ${page.status==='Published'?'bg-grn':'bg-gry'}" style="font-size:10px">${page.status}</span></div>
      <div class="pgu">${page.url}</div>
      <div style="display:flex;justify-content:space-between;margin-top:5px;font-size:11px;color:var(--tx3)"><span>${page.views>0?page.views.toLocaleString()+' views':'No views'}</span><span>${page.edited}</span></div>
      <div style="display:flex;gap:5px;margin-top:8px">${btn(svgIcon('edit',12)+' Edit','btn btn-p btn-sm',`onclick="openBuilder(${page.id})"`)}</div>
      <div style="display:flex;gap:5px;margin-top:5px">${btn(svgIcon('eye',12),'btn btn-s btn-sm',`onclick=\"\"`)}${btn(svgIcon('trash',12),'btn btn-d btn-sm',`onclick="deletePage(${page.id})"`)}</div>
      </div></div>`).join('');
  return `${bc('Website')}<div class="ph"><h1>Website Pages</h1><p>Manage your website content and pages</p></div>
    <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px">
    <div style="display:flex;align-items:center;gap:9px"><span style="font-size:13px;color:var(--tx2)">Domain:</span><span style="font-size:13px;font-weight:500">premiummotors.com</span>${bdg('Live','bg-grn')}</div>
    ${btn(svgIcon('plus',13)+' New Page','btn btn-p btn-sm','onclick="openNewPageModal()"')}</div>
    <div class="pg">${cards}</div>`;
}
function bindWebsite(){}
function refreshWebsitePage(){
  const ct=document.getElementById('main-ct');
  if(ct) ct.innerHTML=renderWebsite();
}
function deletePage(id){
  const page=PAGES_DATA.find(p=>p.id===id);
  if(!page) return;
  openModal('Delete Page',
    `<div style="text-align:center;padding:10px 0">
      <div style="width:48px;height:48px;border-radius:50%;background:rgba(255,82,114,.12);color:var(--red);display:flex;align-items:center;justify-content:center;margin:0 auto 14px">${svgIcon('trash',22)}</div>
      <div style="font-size:15px;font-weight:600;margin-bottom:8px">Delete "${page.name}"?</div>
      <div style="font-size:13px;color:var(--tx2)">This will permanently remove the page at <strong>${page.url}</strong>. This action cannot be undone.</div>
    </div>`,
    ()=>{ PAGES_DATA=PAGES_DATA.filter(p=>p.id!==id); closeModal(); refreshWebsitePage(); showToast('Page deleted.','var(--red)'); },
    'Delete Page'
  );
  // Make delete button red
  setTimeout(()=>{
    const f=document.getElementById('modal-footer');
    if(f){ const b=f.querySelector('.btn-p'); if(b){b.style.background='var(--red)';b.style.boxShadow='0 0 16px rgba(255,82,114,.3)';}}
  },30);
}
function openNewPageModal(){
  openModal('New Page',`<div style="display:flex;flex-direction:column;gap:11px"><div class="fc2"><div class="fl">Page Name</div><input class="inp" id="f-pname" placeholder="e.g. About Us"/></div><div class="fc2"><div class="fl">URL Slug</div><input class="inp" id="f-purl" placeholder="/about"/></div><div class="fc2"><div class="fl">Status</div><select class="inp" id="f-pstatus"><option>Draft</option><option>Published</option></select></div></div>`,
    ()=>{const np={id:Date.now(),name:document.getElementById('f-pname').value||'Untitled',url:document.getElementById('f-purl').value||'/',status:document.getElementById('f-pstatus').value,views:0,edited:'Just now'};PAGES_DATA.push(np);closeModal();refreshWebsitePage();setTimeout(()=>openBuilder(np.id),80);},'Create & Edit');
}

refreshWebsitePage();

/* ══════════════════════════════════════════
   ANALYTICS
══════════════════════════════════════════ */
