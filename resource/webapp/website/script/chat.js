/* chat.js — floating chat drawer and conversations */

function renderChatBubble(){
  const el=document.getElementById('chat-bubble');
  if(!el)return;
  el.innerHTML=`${svgIcon('chat',20)}${totalUnread>0?`<div class="cb-badge">${totalUnread}</div>`:''}`;
  el.classList.toggle('open-state',chatOpen);
}

function openChatDrawer(){
  chatOpen=true;
  document.getElementById('chat-drawer').classList.add('open');
  renderChatBubble();
  renderConvList();
  renderChatWindow();
  // Clear unread for active conv
  markRead(activeConvId);
}
function closeChatDrawer(){
  chatOpen=false;
  document.getElementById('chat-drawer').classList.remove('open');
  renderChatBubble();
}
function toggleChat(){
  if(chatOpen) closeChatDrawer(); else openChatDrawer();
}

function markRead(convId){
  const conv=CHAT_CONVS.find(c=>c.id===convId);
  if(conv&&conv.unread>0){
    totalUnread=Math.max(0,totalUnread-conv.unread);
    conv.unread=0;
    renderChatBubble();
    renderConvList();
  }
}

function renderConvList(){
  const el=document.getElementById('conv-list');
  if(!el)return;
  el.innerHTML=CHAT_CONVS.map(c=>`
    <div class="conv-item${activeConvId===c.id?' active':''}" onclick="selectConv(${c.id})">
      <div style="display:flex;align-items:center;gap:6px;margin-bottom:3px">
        <div style="width:22px;height:22px;border-radius:50%;background:${c.color}22;color:${c.color};display:flex;align-items:center;justify-content:center;font-size:8px;font-weight:700;flex-shrink:0">${c.initials}</div>
        <div class="conv-name">${c.name}</div>
      </div>
      <div class="conv-preview">${c.messages[c.messages.length-1]?.text||'...'}</div>
      <div style="display:flex;justify-content:space-between;align-items:center;margin-top:2px">
        <div class="conv-time">${c.messages[c.messages.length-1]?.time||''}</div>
        ${c.unread>0?`<div class="conv-unread">${c.unread}</div>`:''}
      </div>
    </div>`).join('');
}

function renderChatWindow(){
  const win=document.getElementById('chat-win-content');
  if(!win)return;
  const conv=CHAT_CONVS.find(c=>c.id===activeConvId);
  if(!conv){win.innerHTML='<div class="chat-empty">'+svgIcon('chat',28)+'<span>Select a conversation</span></div>';return;}
  const msgData=convMessages.find(c=>c.id===conv.id);
  const msgs=(msgData?.msgs||[]).map(m=>`
    <div class="msg ${m.from==='me'?'mine':'theirs'}">
      <div class="msg-bubble">${m.text}</div>
      <div class="msg-time">${m.time}</div>
    </div>`).join('');
  const dot=conv.status==='online'?'var(--grn)':conv.status==='away'?'var(--ora)':'var(--tx3)';
  win.innerHTML=`
    <div class="chat-win-h">
      <div class="chat-win-av" style="background:${conv.color}22;color:${conv.color}">${conv.initials}</div>
      <div>
        <div class="chat-win-name">${conv.name}</div>
        <div style="display:flex;align-items:center;gap:4px"><div style="width:6px;height:6px;border-radius:50%;background:${dot}"></div><span class="chat-win-status" style="color:${dot}">${conv.status}</span></div>
      </div>
      <div class="ab" style="margin-left:auto">
        ${btn(svgIcon('phone',13),'btn btn-s btn-sm','')}
        ${btn(svgIcon('eye',13)+' Profile','btn btn-s btn-sm','')}
      </div>
    </div>
    <div class="chat-msgs" id="chat-msgs">${msgs}</div>
    <div class="chat-input-row">
      <textarea id="chat-input" placeholder="Type a message…" rows="1" onkeydown="chatKeydown(event)"></textarea>
      <button class="chat-send" onclick="sendMessage()">${svgIcon('send',14)}</button>
    </div>`;
  setTimeout(()=>{const m=document.getElementById('chat-msgs');if(m)m.scrollTop=m.scrollHeight;},50);
}

function selectConv(id){
  activeConvId=id;
  markRead(id);
  renderConvList();
  renderChatWindow();
}

function sendMessage(){
  const inp=document.getElementById('chat-input');
  if(!inp)return;
  const text=inp.value.trim();
  if(!text)return;
  inp.value='';
  const now=new Date();
  const time=now.getHours()+':'+(now.getMinutes()<10?'0':'')+now.getMinutes()+' '+(now.getHours()<12?'AM':'PM');
  const msgData=convMessages.find(c=>c.id===activeConvId);
  if(msgData) msgData.msgs.push({from:'me',text,time});
  renderChatWindow();
  renderConvList();
  // Simulate reply after 1.5s
  setTimeout(()=>{
    const replies=['Got it, thanks!','Sure, sounds good.','Let me check and get back to you.','That works for me!','Can we do a bit later?','Perfect, see you then!'];
    const reply=replies[Math.floor(Math.random()*replies.length)];
    const md=convMessages.find(c=>c.id===activeConvId);
    const conv=CHAT_CONVS.find(c=>c.id===activeConvId);
    if(md&&conv) md.msgs.push({from:'them',text:reply,time:time});
    if(activeConvId===activeConvId) renderChatWindow(); // still open
  },1500);
}
function chatKeydown(e){if(e.key==='Enter'&&!e.shiftKey){e.preventDefault();sendMessage();}}

/* ══════════════════════════════════════════
   PAGE BUILDER ENGINE
══════════════════════════════════════════ */
let builderActive=false;
let builderPageId=null;
let canvasEls=[];       // [{id,type,variantIdx,props}]
let selectedElId=null;
let leftPanelOpen=true;
let rightPanelOpen=true;
let dragBlockType=null;
let tmplMode=null;      // null | block type string (showing templates)

/* ─ Block + variant catalog ─ */
const BLOCK_CATALOG=[
  {group:'Structure',blocks:[
    {type:'navbar',      label:'Navigation Bar',     icon:'layout', variants:['Dark Logo','Light Bar','Mega Menu']},
    {type:'hero',        label:'Hero Banner',         icon:'layout', variants:['Full Bleed','Split + Car Image','Video Style']},
    {type:'section',     label:'Text Section',        icon:'type',   variants:['Left Align','Centered','Two Column']},
    {type:'cta',         label:'CTA Banner',          icon:'zap',    variants:['Gradient','Dark','Inline']},
    {type:'footer',      label:'Footer',              icon:'layout', variants:['Simple','Full Columns']},
    {type:'divider',     label:'Divider',             icon:'minus',  variants:['Line']},
  ]},
  {group:'Inventory',blocks:[
    {type:'inv_grid',    label:'Inventory Grid',      icon:'grid',   variants:['3-Col Cards','2-Col Large','List View']},
    {type:'inv_featured',label:'Featured Vehicles',   icon:'car',    variants:['Spotlight 3','Hero + Side','Carousel Style']},
    {type:'inv_search',  label:'Search & Filter',     icon:'search', variants:['Full Bar','Compact Inline']},
    {type:'car_detail',  label:'Car Detail Block',    icon:'car',    variants:['Full Detail','Summary Card']},
    {type:'compare',     label:'Compare Vehicles',    icon:'grid',   variants:['Side by Side']},
  ]},
  {group:'Promotions',blocks:[
    {type:'promo_banner',label:'Promo Banner',        icon:'zap',    variants:['Holiday','End of Month','Flash Sale']},
    {type:'promo_cards', label:'Offer Cards',         icon:'grid',   variants:['3 Offers','2 Offers']},
    {type:'financing',   label:'Financing Banner',    icon:'dollar', variants:['0% APR','Monthly Payment','Trade-In']},
    {type:'countdown',   label:'Countdown Timer',     icon:'zap',    variants:['Sale Ends','Event Countdown']},
  ]},
  {group:'Service',blocks:[
    {type:'service_dept',label:'Service Department',  icon:'cog',    variants:['Full Section','Split Layout']},
    {type:'schedule_svc',label:'Schedule Service',    icon:'form',   variants:['Full Form','Quick Form']},
    {type:'svc_specials',label:'Service Specials',    icon:'zap',    variants:['Card Grid','List Style']},
    {type:'svc_brands',  label:'Certified Brands',    icon:'check',  variants:['Logo Row','Brand Cards']},
  ]},
  {group:'Trust',blocks:[
    {type:'testimonials',label:'Testimonials',        icon:'chat',   variants:['3-Up Grid','Single Quote','Carousel']},
    {type:'why_us',      label:'Why Choose Us',       icon:'check',  variants:['Icon Grid','Split Image']},
    {type:'awards',      label:'Awards & Badges',     icon:'check',  variants:['Logo Strip','Card Row']},
    {type:'team',        label:'Meet the Team',       icon:'leads',  variants:['Grid','List']},
  ]},
  {group:'Content',blocks:[
    {type:'text',        label:'Text Block',          icon:'type',   variants:['Paragraph','Pull Quote']},
    {type:'image',       label:'Image',               icon:'image',  variants:['Contained','Full Width']},
    {type:'form',        label:'Contact / Lead Form', icon:'form',   variants:['Full Form','Quick Lead Capture']},
    {type:'map_hours',   label:'Location & Hours',    icon:'globe',  variants:['Map + Hours','Hours Only']},
  ]},
];

const TMPL_PREVIEWS={
  navbar:[
    '<div style="background:#0f0f1a;padding:8px 12px;display:flex;align-items:center;justify-content:space-between"><div style="color:#7c6eff;font-weight:800;font-size:10px">PM MOTORS</div><div style="display:flex;gap:6px"><div style="width:18px;height:3px;background:rgba(255,255,255,.3);border-radius:2px"></div><div style="width:18px;height:3px;background:rgba(255,255,255,.3);border-radius:2px"></div><div style="width:28px;height:3px;background:rgba(255,255,255,.3);border-radius:2px"></div><div style="width:18px;height:3px;background:rgba(255,255,255,.3);border-radius:2px"></div></div><div style="background:#7c6eff;color:#fff;font-size:8px;padding:3px 7px;border-radius:3px">Get a Quote</div></div>',
    '<div style="background:#fff;padding:8px 12px;border-bottom:1px solid #eee;display:flex;align-items:center;justify-content:space-between"><div style="color:#1a1a2e;font-weight:800;font-size:10px">PM MOTORS</div><div style="display:flex;gap:6px"><div style="width:18px;height:3px;background:#ccc;border-radius:2px"></div><div style="width:18px;height:3px;background:#ccc;border-radius:2px"></div><div style="width:28px;height:3px;background:#ccc;border-radius:2px"></div></div><div style="background:#1a1a2e;color:#fff;font-size:8px;padding:3px 7px;border-radius:3px">Inventory</div></div>',
    '<div style="background:#0f0f1a;padding:7px 12px"><div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:6px"><div style="color:#7c6eff;font-weight:800;font-size:10px">PM MOTORS</div><div style="display:flex;gap:5px"><div style="background:#7c6eff;color:#fff;font-size:7px;padding:2px 6px;border-radius:3px">New Cars</div><div style="background:rgba(255,255,255,.1);color:#fff;font-size:7px;padding:2px 6px;border-radius:3px">Used</div><div style="background:rgba(255,255,255,.1);color:#fff;font-size:7px;padding:2px 6px;border-radius:3px">Service</div></div></div><div style="height:1px;background:rgba(255,255,255,.08)"></div></div>',
  ],
  hero:[
    '<div style="background:linear-gradient(135deg,#0d0d1a,#1a1a3e);padding:18px;text-align:center;position:relative"><div style="color:rgba(255,255,255,.4);font-size:7px;text-transform:uppercase;letter-spacing:2px;margin-bottom:3px">PREMIUM MOTORS</div><div style="color:#fff;font-weight:800;font-size:12px;margin-bottom:4px">Drive Your Dream Car</div><div style="color:rgba(255,255,255,.55);font-size:8px;margin-bottom:9px">500+ premium vehicles in stock</div><div style="display:flex;gap:5px;justify-content:center"><div style="background:#7c6eff;color:#fff;font-size:8px;padding:4px 10px;border-radius:4px">View Inventory</div><div style="background:rgba(255,255,255,.1);color:#fff;font-size:8px;padding:4px 10px;border-radius:4px">Book Test Drive</div></div></div>',
    '<div style="background:linear-gradient(135deg,#0d0d1a,#1a1a3e);padding:14px;display:flex;align-items:center;gap:10px"><div style="flex:1"><div style="color:rgba(255,255,255,.4);font-size:7px;text-transform:uppercase;margin-bottom:2px">NEW ARRIVALS</div><div style="color:#fff;font-weight:800;font-size:11px;margin-bottom:4px">2024 Models Here</div><div style="color:rgba(255,255,255,.5);font-size:8px;margin-bottom:7px">Starting from $28,900</div><div style="background:#7c6eff;color:#fff;font-size:8px;padding:3px 9px;border-radius:4px;display:inline-block">Shop Now</div></div><div style="width:52px;height:40px;background:rgba(255,255,255,.06);border-radius:6px;display:flex;align-items:center;justify-content:center;font-size:20px">🚗</div></div>',
    '<div style="background:#0d0d1a;padding:20px;text-align:center;min-height:68px;display:flex;flex-direction:column;align-items:center;justify-content:center;position:relative"><div style="position:absolute;inset:0;background:linear-gradient(135deg,rgba(124,110,255,.15),transparent)"></div><div style="color:#7c6eff;font-size:8px;text-transform:uppercase;letter-spacing:3px;margin-bottom:5px">EXPERIENCE THE DIFFERENCE</div><div style="color:#fff;font-weight:800;font-size:13px;margin-bottom:7px">Premium Motors</div><div style="display:flex;gap:5px;justify-content:center"><div style="background:#7c6eff;color:#fff;font-size:8px;padding:4px 12px;border-radius:4px">Explore</div></div></div>',
  ],
  section:[
    '<div style="background:#fff;padding:12px 14px"><div style="color:#1a1a2e;font-weight:700;font-size:10px;margin-bottom:4px">About Premium Motors</div><div style="height:3px;background:#eee;border-radius:2px;margin-bottom:3px"></div><div style="height:3px;background:#eee;border-radius:2px;width:80%;margin-bottom:3px"></div><div style="height:3px;background:#eee;border-radius:2px;width:60%"></div></div>',
    '<div style="background:#fff;padding:12px;text-align:center"><div style="color:#1a1a2e;font-weight:700;font-size:10px;margin-bottom:4px">Our Promise to You</div><div style="height:3px;background:#eee;border-radius:2px;margin-bottom:3px;width:90%;margin-left:auto;margin-right:auto"></div><div style="height:3px;background:#eee;border-radius:2px;width:70%;margin-left:auto;margin-right:auto"></div></div>',
    '<div style="background:#fff;padding:10px;display:grid;grid-template-columns:1fr 1fr;gap:10px"><div><div style="color:#1a1a2e;font-weight:700;font-size:9px;margin-bottom:3px">Our Story</div><div style="height:2px;background:#eee;border-radius:2px;margin-bottom:2px"></div><div style="height:2px;background:#eee;border-radius:2px;width:80%"></div></div><div><div style="color:#1a1a2e;font-weight:700;font-size:9px;margin-bottom:3px">Our Values</div><div style="height:2px;background:#eee;border-radius:2px;margin-bottom:2px"></div><div style="height:2px;background:#eee;border-radius:2px;width:70%"></div></div></div>',
  ],
  cta:[
    '<div style="background:linear-gradient(135deg,#7c6eff,#c084fc);padding:14px;text-align:center"><div style="color:#fff;font-weight:700;font-size:11px;margin-bottom:3px">Ready to Drive Home Today?</div><div style="color:rgba(255,255,255,.8);font-size:8px;margin-bottom:7px">Limited time offer — ends Sunday</div><div style="background:#fff;color:#7c6eff;font-size:8px;padding:3px 10px;border-radius:4px;display:inline-block;font-weight:700">Claim Offer</div></div>',
    '<div style="background:linear-gradient(135deg,#0f0f1a,#1a1a2e);padding:14px;text-align:center;border:1px solid rgba(124,110,255,.25)"><div style="color:#fff;font-weight:700;font-size:11px;margin-bottom:3px">0% Financing Available</div><div style="color:rgba(255,255,255,.5);font-size:8px;margin-bottom:7px">On select 2024 models this month</div><div style="background:#7c6eff;color:#fff;font-size:8px;padding:3px 10px;border-radius:4px;display:inline-block">Apply Now</div></div>',
    '<div style="background:#fff;padding:12px 14px;display:flex;align-items:center;justify-content:space-between;border:1px solid #eee"><div><div style="color:#1a1a2e;font-weight:700;font-size:10px;margin-bottom:2px">Trade In Your Vehicle</div><div style="color:#666;font-size:8px">Get an instant online estimate</div></div><div style="background:#7c6eff;color:#fff;font-size:8px;padding:4px 12px;border-radius:4px;font-weight:700">Get Value</div></div>',
  ],
  footer:[
    '<div style="background:#0a0a12;padding:10px 14px;display:flex;align-items:center;justify-content:space-between"><div style="color:#fff;font-weight:700;font-size:10px">PREMIUM MOTORS</div><div style="color:rgba(255,255,255,.35);font-size:8px">© 2024 All rights reserved</div></div>',
    '<div style="background:#0a0a12;padding:12px 14px"><div style="display:grid;grid-template-columns:2fr 1fr 1fr;gap:10px;margin-bottom:8px"><div><div style="color:#fff;font-size:9px;font-weight:700;margin-bottom:4px">Premium Motors</div><div style="height:2px;background:rgba(255,255,255,.15);border-radius:2px;margin-bottom:2px"></div><div style="height:2px;background:rgba(255,255,255,.15);border-radius:2px;width:80%"></div></div><div><div style="color:#fff;font-size:8px;font-weight:700;margin-bottom:4px">Vehicles</div><div style="height:2px;background:rgba(255,255,255,.12);border-radius:2px;margin-bottom:2px"></div><div style="height:2px;background:rgba(255,255,255,.12);border-radius:2px;margin-bottom:2px"></div></div><div><div style="color:#fff;font-size:8px;font-weight:700;margin-bottom:4px">Service</div><div style="height:2px;background:rgba(255,255,255,.12);border-radius:2px;margin-bottom:2px"></div><div style="height:2px;background:rgba(255,255,255,.12);border-radius:2px;margin-bottom:2px"></div></div></div><div style="border-top:1px solid rgba(255,255,255,.07);padding-top:7px;display:flex;justify-content:space-between"><div style="color:#fff;font-size:8px;font-weight:700">PREMIUM MOTORS</div><div style="color:rgba(255,255,255,.3);font-size:7px">© 2024</div></div></div>',
  ],
  divider:[
    '<div style="padding:6px 14px"><hr style="border:none;border-top:1px solid #e0e0f0"/></div>',
  ],
  inv_grid:[
    '<div style="background:#f8f9ff;padding:8px;display:grid;grid-template-columns:repeat(3,1fr);gap:4px"><div style="background:#fff;border:1px solid #eee;border-radius:5px;overflow:hidden"><div style="height:26px;background:linear-gradient(135deg,#1a1a2e,#2a2a4e);display:flex;align-items:center;justify-content:center;font-size:11px">🚗</div><div style="padding:4px"><div style="height:4px;background:#1a1a2e15;border-radius:2px;margin-bottom:2px"></div><div style="height:5px;background:#7c6eff44;border-radius:2px;width:70%"></div></div></div><div style="background:#fff;border:1px solid #eee;border-radius:5px;overflow:hidden"><div style="height:26px;background:linear-gradient(135deg,#1a1a2e,#2a2a4e);display:flex;align-items:center;justify-content:center;font-size:11px">🚙</div><div style="padding:4px"><div style="height:4px;background:#1a1a2e15;border-radius:2px;margin-bottom:2px"></div><div style="height:5px;background:#7c6eff44;border-radius:2px;width:70%"></div></div></div><div style="background:#fff;border:1px solid #eee;border-radius:5px;overflow:hidden"><div style="height:26px;background:linear-gradient(135deg,#1a1a2e,#2a2a4e);display:flex;align-items:center;justify-content:center;font-size:11px">🚕</div><div style="padding:4px"><div style="height:4px;background:#1a1a2e15;border-radius:2px;margin-bottom:2px"></div><div style="height:5px;background:#7c6eff44;border-radius:2px;width:70%"></div></div></div></div>',
    '<div style="background:#f8f9ff;padding:8px;display:grid;grid-template-columns:1fr 1fr;gap:5px"><div style="background:#fff;border:1px solid #eee;border-radius:5px;overflow:hidden"><div style="height:36px;background:linear-gradient(135deg,#1a1a2e,#2a2a4e);display:flex;align-items:center;justify-content:center;font-size:14px">🚗</div><div style="padding:5px"><div style="height:4px;background:#1a1a2e15;border-radius:2px;margin-bottom:2px"></div><div style="height:5px;background:#7c6eff44;border-radius:2px;width:70%"></div></div></div><div style="background:#fff;border:1px solid #eee;border-radius:5px;overflow:hidden"><div style="height:36px;background:linear-gradient(135deg,#1a1a2e,#2a2a4e);display:flex;align-items:center;justify-content:center;font-size:14px">🚙</div><div style="padding:5px"><div style="height:4px;background:#1a1a2e15;border-radius:2px;margin-bottom:2px"></div><div style="height:5px;background:#7c6eff44;border-radius:2px;width:70%"></div></div></div></div>',
    '<div style="background:#fff;padding:8px"><div style="display:flex;align-items:center;gap:8px;padding:5px;border:1px solid #eee;border-radius:5px;margin-bottom:4px"><div style="width:36px;height:24px;background:linear-gradient(135deg,#1a1a2e,#2a2a4e);border-radius:3px;display:flex;align-items:center;justify-content:center;font-size:10px;flex-shrink:0">🚗</div><div style="flex:1"><div style="height:4px;background:#eee;border-radius:2px;margin-bottom:2px"></div><div style="height:3px;background:#7c6eff33;border-radius:2px;width:60%"></div></div><div style="background:#7c6eff;color:#fff;font-size:7px;padding:2px 5px;border-radius:3px">View</div></div><div style="display:flex;align-items:center;gap:8px;padding:5px;border:1px solid #eee;border-radius:5px"><div style="width:36px;height:24px;background:linear-gradient(135deg,#1a1a2e,#2a2a4e);border-radius:3px;display:flex;align-items:center;justify-content:center;font-size:10px;flex-shrink:0">🚙</div><div style="flex:1"><div style="height:4px;background:#eee;border-radius:2px;margin-bottom:2px"></div><div style="height:3px;background:#7c6eff33;border-radius:2px;width:60%"></div></div><div style="background:#7c6eff;color:#fff;font-size:7px;padding:2px 5px;border-radius:3px">View</div></div></div>',
  ],
  inv_featured:[
    '<div style="background:#f8f9ff;padding:8px;display:grid;grid-template-columns:repeat(3,1fr);gap:4px"><div style="background:#fff;border:2px solid #7c6eff33;border-radius:6px;overflow:hidden"><div style="height:30px;background:linear-gradient(135deg,#1a1a2e,#2a2a4e);display:flex;align-items:center;justify-content:center;font-size:13px">⭐🚗</div><div style="padding:5px"><div style="height:4px;background:#1a1a2e15;border-radius:2px;margin-bottom:2px"></div><div style="height:5px;background:#7c6eff55;border-radius:2px;width:80%"></div></div></div><div style="background:#fff;border:1px solid #eee;border-radius:5px;overflow:hidden"><div style="height:30px;background:linear-gradient(135deg,#1a1a2e,#2a2a4e);display:flex;align-items:center;justify-content:center;font-size:11px">🚙</div><div style="padding:4px"><div style="height:3px;background:#1a1a2e15;border-radius:2px;margin-bottom:2px"></div><div style="height:4px;background:#7c6eff33;border-radius:2px;width:70%"></div></div></div><div style="background:#fff;border:1px solid #eee;border-radius:5px;overflow:hidden"><div style="height:30px;background:linear-gradient(135deg,#1a1a2e,#2a2a4e);display:flex;align-items:center;justify-content:center;font-size:11px">🚕</div><div style="padding:4px"><div style="height:3px;background:#1a1a2e15;border-radius:2px;margin-bottom:2px"></div><div style="height:4px;background:#7c6eff33;border-radius:2px;width:70%"></div></div></div></div>',
    '<div style="background:#0f0f1a;padding:10px;display:flex;gap:8px"><div style="flex:1"><div style="color:rgba(255,255,255,.4);font-size:7px;text-transform:uppercase;margin-bottom:3px">FEATURED</div><div style="color:#fff;font-weight:700;font-size:10px;margin-bottom:2px">2024 Tesla Model 3</div><div style="color:#7c6eff;font-size:9px;font-weight:700;margin-bottom:5px">$42,500</div><div style="background:#7c6eff;color:#fff;font-size:7px;padding:2px 7px;border-radius:3px;display:inline-block">View Details</div></div><div style="width:48px;height:46px;background:rgba(255,255,255,.05);border-radius:5px;display:flex;align-items:center;justify-content:center;font-size:20px">🚗</div></div>',
    '<div style="background:#f8f9ff;padding:8px"><div style="background:#fff;border:1px solid #eee;border-radius:6px;padding:6px;margin-bottom:4px;display:flex;align-items:center;justify-content:space-between"><div style="font-size:9px;font-weight:700;color:#1a1a2e">Featured Vehicles</div><div style="display:flex;gap:3px"><div style="width:14px;height:14px;background:#eee;border-radius:50%;display:flex;align-items:center;justify-content:center;font-size:7px">‹</div><div style="width:14px;height:14px;background:#7c6eff;border-radius:50%;display:flex;align-items:center;justify-content:center;font-size:7px;color:#fff">›</div></div></div><div style="display:flex;gap:4px"><div style="flex:1;height:32px;background:linear-gradient(135deg,#1a1a2e,#2a2a4e);border-radius:4px;display:flex;align-items:center;justify-content:center;font-size:14px">🚗</div><div style="width:24px;display:flex;flex-direction:column;gap:3px;justify-content:center"><div style="height:10px;background:#f0f0f8;border-radius:3px"></div><div style="height:10px;background:#f0f0f8;border-radius:3px"></div></div></div></div>',
  ],
  inv_search:[
    '<div style="background:#1a1a2e;padding:10px"><div style="color:#fff;font-weight:700;font-size:9px;margin-bottom:6px;text-align:center">Search Our Inventory</div><div style="display:grid;grid-template-columns:1fr 1fr 1fr 1fr;gap:4px;margin-bottom:5px"><div style="height:16px;background:rgba(255,255,255,.1);border-radius:4px;border:1px solid rgba(255,255,255,.15)"></div><div style="height:16px;background:rgba(255,255,255,.1);border-radius:4px;border:1px solid rgba(255,255,255,.15)"></div><div style="height:16px;background:rgba(255,255,255,.1);border-radius:4px;border:1px solid rgba(255,255,255,.15)"></div><div style="height:16px;background:rgba(255,255,255,.1);border-radius:4px;border:1px solid rgba(255,255,255,.15)"></div></div><div style="background:#7c6eff;color:#fff;font-size:8px;padding:4px;border-radius:4px;text-align:center">Search</div></div>',
    '<div style="background:#fff;padding:8px;border-bottom:2px solid #7c6eff"><div style="display:flex;gap:5px;align-items:center"><div style="flex:1;height:20px;background:#f8f8ff;border:1px solid #eee;border-radius:4px;display:flex;align-items:center;padding:0 6px"><div style="height:3px;background:#ccc;border-radius:2px;width:60%"></div></div><div style="display:flex;gap:3px"><div style="height:20px;width:28px;background:#f0f0f8;border:1px solid #eee;border-radius:4px"></div><div style="height:20px;width:28px;background:#f0f0f8;border:1px solid #eee;border-radius:4px"></div><div style="height:20px;width:36px;background:#7c6eff;border-radius:4px"></div></div></div></div>',
  ],
  car_detail:[
    '<div style="background:#fff;padding:8px"><div style="display:grid;grid-template-columns:1fr 1fr;gap:6px;margin-bottom:6px"><div style="height:48px;background:linear-gradient(135deg,#1a1a2e,#2a2a4e);border-radius:5px;display:flex;align-items:center;justify-content:center;font-size:20px">🚗</div><div><div style="height:5px;background:#1a1a2e22;border-radius:2px;margin-bottom:3px"></div><div style="height:8px;background:#7c6eff44;border-radius:2px;width:80%;margin-bottom:4px"></div><div style="display:grid;grid-template-columns:1fr 1fr;gap:2px"><div style="height:3px;background:#eee;border-radius:2px"></div><div style="height:3px;background:#eee;border-radius:2px"></div><div style="height:3px;background:#eee;border-radius:2px"></div><div style="height:3px;background:#eee;border-radius:2px"></div></div></div></div><div style="background:#7c6eff;color:#fff;font-size:8px;padding:4px;border-radius:4px;text-align:center">Schedule Test Drive</div></div>',
    '<div style="background:#fff;padding:8px;display:flex;gap:6px;align-items:center"><div style="width:40px;height:30px;background:linear-gradient(135deg,#1a1a2e,#2a2a4e);border-radius:4px;display:flex;align-items:center;justify-content:center;font-size:14px;flex-shrink:0">🚗</div><div style="flex:1"><div style="height:4px;background:#1a1a2e22;border-radius:2px;margin-bottom:2px"></div><div style="height:6px;background:#7c6eff44;border-radius:2px;width:70%"></div></div><div style="background:#7c6eff;color:#fff;font-size:7px;padding:3px 6px;border-radius:3px">Details</div></div>',
  ],
  compare:[
    '<div style="background:#f8f9ff;padding:8px"><div style="color:#1a1a2e;font-weight:700;font-size:9px;margin-bottom:5px;text-align:center">Compare Vehicles</div><div style="display:grid;grid-template-columns:1fr 1fr;gap:5px"><div style="background:#fff;border:2px solid #7c6eff33;border-radius:5px;padding:5px;text-align:center"><div style="font-size:14px;margin-bottom:2px">🚗</div><div style="height:3px;background:#eee;border-radius:2px;margin-bottom:2px"></div><div style="height:3px;background:#7c6eff33;border-radius:2px;width:80%;margin:0 auto"></div></div><div style="background:#fff;border:2px solid #7c6eff33;border-radius:5px;padding:5px;text-align:center"><div style="font-size:14px;margin-bottom:2px">🚙</div><div style="height:3px;background:#eee;border-radius:2px;margin-bottom:2px"></div><div style="height:3px;background:#7c6eff33;border-radius:2px;width:80%;margin:0 auto"></div></div></div></div>',
  ],
  promo_banner:[
    '<div style="background:linear-gradient(135deg,#c0392b,#e74c3c);padding:14px;text-align:center"><div style="color:rgba(255,255,255,.8);font-size:7px;text-transform:uppercase;letter-spacing:2px;margin-bottom:2px">HOLIDAY SALE</div><div style="color:#fff;font-weight:800;font-size:12px;margin-bottom:3px">Save Up to $5,000</div><div style="color:rgba(255,255,255,.8);font-size:8px;margin-bottom:7px">On select vehicles — ends Dec 31</div><div style="background:#fff;color:#c0392b;font-size:8px;padding:3px 10px;border-radius:4px;display:inline-block;font-weight:700">Shop Holiday Deals</div></div>',
    '<div style="background:linear-gradient(135deg,#1a1a2e,#0f0f1a);padding:14px;text-align:center;border:1px solid rgba(255,171,76,.3)"><div style="color:#ffab4c;font-size:7px;text-transform:uppercase;letter-spacing:2px;margin-bottom:2px">END OF MONTH</div><div style="color:#fff;font-weight:800;font-size:12px;margin-bottom:3px">Clear Out Event</div><div style="color:rgba(255,255,255,.6);font-size:8px;margin-bottom:7px">Best prices of the year on 2023 models</div><div style="background:#ffab4c;color:#1a1a2e;font-size:8px;padding:3px 10px;border-radius:4px;display:inline-block;font-weight:700">See All Deals</div></div>',
    '<div style="background:linear-gradient(135deg,#7c6eff,#ff5272);padding:14px;text-align:center"><div style="color:rgba(255,255,255,.9);font-size:8px;font-weight:700;background:rgba(255,255,255,.15);display:inline-block;padding:2px 8px;border-radius:10px;margin-bottom:5px">⚡ FLASH SALE — 24 HRS ONLY</div><div style="color:#fff;font-weight:800;font-size:12px;margin-bottom:3px">Extra $2,500 Off</div><div style="color:rgba(255,255,255,.8);font-size:8px;margin-bottom:7px">Any in-stock vehicle purchased today</div><div style="background:#fff;color:#7c6eff;font-size:8px;padding:3px 10px;border-radius:4px;display:inline-block;font-weight:700">Claim Now</div></div>',
  ],
  promo_cards:[
    '<div style="background:#f8f9ff;padding:8px;display:grid;grid-template-columns:repeat(3,1fr);gap:4px"><div style="background:#fff;border:1px solid #eee;border-radius:5px;padding:6px;text-align:center"><div style="font-size:12px;margin-bottom:3px">🎁</div><div style="height:4px;background:#7c6eff22;border-radius:2px;margin-bottom:2px"></div><div style="height:3px;background:#eee;border-radius:2px;width:80%;margin:0 auto"></div></div><div style="background:#fff;border:1px solid #eee;border-radius:5px;padding:6px;text-align:center"><div style="font-size:12px;margin-bottom:3px">💰</div><div style="height:4px;background:#7c6eff22;border-radius:2px;margin-bottom:2px"></div><div style="height:3px;background:#eee;border-radius:2px;width:80%;margin:0 auto"></div></div><div style="background:#fff;border:1px solid #eee;border-radius:5px;padding:6px;text-align:center"><div style="font-size:12px;margin-bottom:3px">🔑</div><div style="height:4px;background:#7c6eff22;border-radius:2px;margin-bottom:2px"></div><div style="height:3px;background:#eee;border-radius:2px;width:80%;margin:0 auto"></div></div></div>',
    '<div style="background:#f8f9ff;padding:8px;display:grid;grid-template-columns:1fr 1fr;gap:5px"><div style="background:#fff;border:1px solid #eee;border-radius:5px;padding:8px;text-align:center"><div style="font-size:14px;margin-bottom:4px">🎄</div><div style="height:5px;background:#7c6eff22;border-radius:2px;margin-bottom:2px"></div><div style="height:3px;background:#eee;border-radius:2px;width:80%;margin:0 auto"></div></div><div style="background:#fff;border:1px solid #eee;border-radius:5px;padding:8px;text-align:center"><div style="font-size:14px;margin-bottom:4px">💳</div><div style="height:5px;background:#7c6eff22;border-radius:2px;margin-bottom:2px"></div><div style="height:3px;background:#eee;border-radius:2px;width:80%;margin:0 auto"></div></div></div>',
  ],
  financing:[
    '<div style="background:linear-gradient(135deg,#0f3460,#0f0f1a);padding:14px;text-align:center"><div style="color:rgba(255,255,255,.6);font-size:7px;text-transform:uppercase;letter-spacing:2px;margin-bottom:3px">LIMITED TIME OFFER</div><div style="color:#fff;font-weight:800;font-size:14px;margin-bottom:2px">0% APR</div><div style="color:rgba(255,255,255,.6);font-size:8px;margin-bottom:7px">For 60 months on all 2024 models</div><div style="background:#7c6eff;color:#fff;font-size:8px;padding:3px 10px;border-radius:4px;display:inline-block">Pre-Qualify Online</div></div>',
    '<div style="background:#fff;padding:10px;border-top:3px solid #7c6eff"><div style="display:flex;justify-content:space-around;margin-bottom:7px"><div style="text-align:center"><div style="color:#7c6eff;font-weight:800;font-size:12px">$299</div><div style="color:#666;font-size:7px">/month</div></div><div style="text-align:center"><div style="color:#7c6eff;font-weight:800;font-size:12px">0%</div><div style="color:#666;font-size:7px">APR</div></div><div style="text-align:center"><div style="color:#7c6eff;font-weight:800;font-size:12px">$0</div><div style="color:#666;font-size:7px">Down</div></div></div><div style="background:#7c6eff;color:#fff;font-size:8px;padding:4px;border-radius:4px;text-align:center">Calculate Payment</div></div>',
    '<div style="background:#f8f9ff;padding:10px"><div style="display:flex;align-items:center;gap:8px"><div style="flex:1"><div style="color:#1a1a2e;font-weight:700;font-size:10px;margin-bottom:2px">Trade-In Value</div><div style="color:#666;font-size:8px;margin-bottom:5px">Get your car&#39;s worth in 60 seconds</div><div style="height:14px;background:#fff;border:1px solid #ddd;border-radius:4px"></div></div><div style="background:#1fd6a0;color:#fff;font-size:8px;padding:4px 8px;border-radius:4px;white-space:nowrap">Get Value</div></div></div>',
  ],
  countdown:[
    '<div style="background:linear-gradient(135deg,#c0392b,#e74c3c);padding:12px;text-align:center"><div style="color:rgba(255,255,255,.8);font-size:8px;margin-bottom:5px">SALE ENDS IN</div><div style="display:flex;gap:5px;justify-content:center"><div style="background:rgba(0,0,0,.3);color:#fff;font-size:11px;font-weight:800;padding:4px 7px;border-radius:4px">02</div><div style="color:#fff;font-size:11px;align-self:center">:</div><div style="background:rgba(0,0,0,.3);color:#fff;font-size:11px;font-weight:800;padding:4px 7px;border-radius:4px">14</div><div style="color:#fff;font-size:11px;align-self:center">:</div><div style="background:rgba(0,0,0,.3);color:#fff;font-size:11px;font-weight:800;padding:4px 7px;border-radius:4px">38</div></div></div>',
    '<div style="background:#0f0f1a;padding:12px;text-align:center"><div style="color:#7c6eff;font-size:7px;text-transform:uppercase;letter-spacing:2px;margin-bottom:3px">NEXT EVENT</div><div style="color:#fff;font-size:9px;font-weight:700;margin-bottom:6px">Spring Car Show — Apr 15</div><div style="display:flex;gap:5px;justify-content:center"><div style="background:rgba(124,110,255,.2);color:#7c6eff;font-size:10px;font-weight:800;padding:4px 7px;border-radius:4px">12</div><div style="color:#7c6eff;font-size:11px;align-self:center">:</div><div style="background:rgba(124,110,255,.2);color:#7c6eff;font-size:10px;font-weight:800;padding:4px 7px;border-radius:4px">04</div><div style="color:#7c6eff;font-size:11px;align-self:center">:</div><div style="background:rgba(124,110,255,.2);color:#7c6eff;font-size:10px;font-weight:800;padding:4px 7px;border-radius:4px">22</div></div></div>',
  ],
  service_dept:[
    '<div style="background:#0f0f1a;padding:12px"><div style="display:grid;grid-template-columns:1fr 1fr;gap:6px"><div><div style="color:#fff;font-weight:700;font-size:10px;margin-bottom:4px">Service Center</div><div style="height:3px;background:rgba(255,255,255,.2);border-radius:2px;margin-bottom:2px"></div><div style="height:3px;background:rgba(255,255,255,.15);border-radius:2px;width:80%;margin-bottom:4px"></div><div style="background:#7c6eff;color:#fff;font-size:7px;padding:2px 7px;border-radius:3px;display:inline-block">Schedule Now</div></div><div style="display:grid;grid-template-columns:1fr 1fr;gap:3px"><div style="background:rgba(255,255,255,.05);border-radius:4px;padding:4px;text-align:center"><div style="font-size:9px;margin-bottom:1px">🔧</div><div style="height:2px;background:rgba(255,255,255,.15);border-radius:2px"></div></div><div style="background:rgba(255,255,255,.05);border-radius:4px;padding:4px;text-align:center"><div style="font-size:9px;margin-bottom:1px">🛞</div><div style="height:2px;background:rgba(255,255,255,.15);border-radius:2px"></div></div><div style="background:rgba(255,255,255,.05);border-radius:4px;padding:4px;text-align:center"><div style="font-size:9px;margin-bottom:1px">🔋</div><div style="height:2px;background:rgba(255,255,255,.15);border-radius:2px"></div></div><div style="background:rgba(255,255,255,.05);border-radius:4px;padding:4px;text-align:center"><div style="font-size:9px;margin-bottom:1px">🚿</div><div style="height:2px;background:rgba(255,255,255,.15);border-radius:2px"></div></div></div></div></div>',
    '<div style="background:#fff;padding:10px;display:flex;gap:8px;align-items:center"><div style="flex:1"><div style="color:#1a1a2e;font-weight:700;font-size:10px;margin-bottom:3px">Certified Service Department</div><div style="height:3px;background:#eee;border-radius:2px;margin-bottom:2px"></div><div style="height:3px;background:#eee;border-radius:2px;width:70%;margin-bottom:5px"></div><div style="display:flex;gap:3px"><div style="background:#7c6eff;color:#fff;font-size:7px;padding:2px 6px;border-radius:3px">Schedule</div><div style="background:#f0f0f8;color:#555;font-size:7px;padding:2px 6px;border-radius:3px">Specials</div></div></div><div style="font-size:22px">🔧</div></div>',
  ],
  schedule_svc:[
    '<div style="background:#fff;padding:10px"><div style="color:#1a1a2e;font-weight:700;font-size:10px;margin-bottom:6px">Schedule Service</div><div style="height:14px;background:#f8f8ff;border:1px solid #eee;border-radius:4px;margin-bottom:4px"></div><div style="display:grid;grid-template-columns:1fr 1fr;gap:4px;margin-bottom:4px"><div style="height:14px;background:#f8f8ff;border:1px solid #eee;border-radius:4px"></div><div style="height:14px;background:#f8f8ff;border:1px solid #eee;border-radius:4px"></div></div><div style="height:14px;background:#f8f8ff;border:1px solid #eee;border-radius:4px;margin-bottom:5px"></div><div style="background:#7c6eff;height:14px;border-radius:4px;text-align:center;color:#fff;font-size:8px;line-height:14px">Book Appointment</div></div>',
    '<div style="background:#1a1a2e;padding:10px"><div style="color:#fff;font-weight:700;font-size:9px;margin-bottom:5px">Quick Service Request</div><div style="display:grid;grid-template-columns:1fr 1fr;gap:4px;margin-bottom:4px"><div style="height:16px;background:rgba(255,255,255,.08);border-radius:4px;border:1px solid rgba(255,255,255,.1)"></div><div style="height:16px;background:rgba(255,255,255,.08);border-radius:4px;border:1px solid rgba(255,255,255,.1)"></div></div><div style="background:#7c6eff;height:14px;border-radius:4px;text-align:center;color:#fff;font-size:8px;line-height:14px">Check Availability</div></div>',
  ],
  svc_specials:[
    '<div style="background:#f8f9ff;padding:8px;display:grid;grid-template-columns:repeat(3,1fr);gap:4px"><div style="background:#fff;border:1px solid #eee;border-radius:5px;padding:6px;text-align:center"><div style="font-size:12px;margin-bottom:2px">🛢️</div><div style="height:3px;background:#7c6eff22;border-radius:2px;margin-bottom:2px"></div><div style="height:3px;background:#eee;border-radius:2px;width:80%;margin:0 auto"></div></div><div style="background:#fff;border:1px solid #eee;border-radius:5px;padding:6px;text-align:center"><div style="font-size:12px;margin-bottom:2px">🛞</div><div style="height:3px;background:#7c6eff22;border-radius:2px;margin-bottom:2px"></div><div style="height:3px;background:#eee;border-radius:2px;width:80%;margin:0 auto"></div></div><div style="background:#fff;border:1px solid #eee;border-radius:5px;padding:6px;text-align:center"><div style="font-size:12px;margin-bottom:2px">🔋</div><div style="height:3px;background:#7c6eff22;border-radius:2px;margin-bottom:2px"></div><div style="height:3px;background:#eee;border-radius:2px;width:80%;margin:0 auto"></div></div></div>',
    '<div style="background:#fff;padding:8px"><div style="display:flex;align-items:center;justify-content:space-between;padding:5px;border:1px solid #eee;border-radius:5px;margin-bottom:4px"><div style="display:flex;align-items:center;gap:5px"><div style="font-size:10px">🛢️</div><div style="height:3px;background:#eee;border-radius:2px;width:50px"></div></div><div style="background:#1fd6a0;color:#fff;font-size:7px;padding:2px 5px;border-radius:3px">$29.99</div></div><div style="display:flex;align-items:center;justify-content:space-between;padding:5px;border:1px solid #eee;border-radius:5px"><div style="display:flex;align-items:center;gap:5px"><div style="font-size:10px">🛞</div><div style="height:3px;background:#eee;border-radius:2px;width:50px"></div></div><div style="background:#1fd6a0;color:#fff;font-size:7px;padding:2px 5px;border-radius:3px">$15.99</div></div></div>',
  ],
  svc_brands:[
    '<div style="background:#fff;padding:8px"><div style="color:#1a1a2e;font-weight:700;font-size:9px;margin-bottom:6px;text-align:center">Certified For</div><div style="display:flex;gap:6px;justify-content:center;flex-wrap:wrap"><div style="background:#f0f0f8;border-radius:4px;padding:4px 8px;font-size:8px;font-weight:700;color:#1a1a2e">Tesla</div><div style="background:#f0f0f8;border-radius:4px;padding:4px 8px;font-size:8px;font-weight:700;color:#1a1a2e">BMW</div><div style="background:#f0f0f8;border-radius:4px;padding:4px 8px;font-size:8px;font-weight:700;color:#1a1a2e">Audi</div><div style="background:#f0f0f8;border-radius:4px;padding:4px 8px;font-size:8px;font-weight:700;color:#1a1a2e">Porsche</div></div></div>',
    '<div style="background:#f8f9ff;padding:8px;display:grid;grid-template-columns:repeat(4,1fr);gap:4px"><div style="background:#fff;border:1px solid #eee;border-radius:5px;padding:5px;text-align:center"><div style="font-size:8px;font-weight:700;color:#1a1a2e">Tesla</div></div><div style="background:#fff;border:1px solid #eee;border-radius:5px;padding:5px;text-align:center"><div style="font-size:8px;font-weight:700;color:#1a1a2e">BMW</div></div><div style="background:#fff;border:1px solid #eee;border-radius:5px;padding:5px;text-align:center"><div style="font-size:8px;font-weight:700;color:#1a1a2e">Audi</div></div><div style="background:#fff;border:1px solid #eee;border-radius:5px;padding:5px;text-align:center"><div style="font-size:8px;font-weight:700;color:#1a1a2e">Merc</div></div></div>',
  ],
  testimonials:[
    '<div style="background:#f8f9ff;padding:8px;display:grid;grid-template-columns:repeat(3,1fr);gap:4px"><div style="background:#fff;border:1px solid #eee;border-radius:5px;padding:6px"><div style="color:#ffab4c;font-size:8px;margin-bottom:2px">★★★★★</div><div style="height:3px;background:#eee;border-radius:2px;margin-bottom:2px"></div><div style="height:3px;background:#eee;border-radius:2px;width:80%;margin-bottom:3px"></div><div style="height:3px;background:#1a1a2e22;border-radius:2px;width:50%"></div></div><div style="background:#fff;border:1px solid #eee;border-radius:5px;padding:6px"><div style="color:#ffab4c;font-size:8px;margin-bottom:2px">★★★★★</div><div style="height:3px;background:#eee;border-radius:2px;margin-bottom:2px"></div><div style="height:3px;background:#eee;border-radius:2px;width:80%;margin-bottom:3px"></div><div style="height:3px;background:#1a1a2e22;border-radius:2px;width:50%"></div></div><div style="background:#fff;border:1px solid #eee;border-radius:5px;padding:6px"><div style="color:#ffab4c;font-size:8px;margin-bottom:2px">★★★★★</div><div style="height:3px;background:#eee;border-radius:2px;margin-bottom:2px"></div><div style="height:3px;background:#eee;border-radius:2px;width:80%;margin-bottom:3px"></div><div style="height:3px;background:#1a1a2e22;border-radius:2px;width:50%"></div></div></div>',
    '<div style="background:#0f0f1a;padding:14px;text-align:center"><div style="color:rgba(255,255,255,.4);font-size:8px;margin-bottom:4px">WHAT OUR CUSTOMERS SAY</div><div style="color:#ffab4c;font-size:10px;margin-bottom:4px">★★★★★</div><div style="height:3px;background:rgba(255,255,255,.15);border-radius:2px;margin-bottom:3px;width:90%;margin-left:auto;margin-right:auto"></div><div style="height:3px;background:rgba(255,255,255,.12);border-radius:2px;width:70%;margin-left:auto;margin-right:auto;margin-bottom:5px"></div><div style="color:rgba(255,255,255,.5);font-size:8px">— Happy Customer</div></div>',
    '<div style="background:#f8f9ff;padding:8px"><div style="background:#fff;border:1px solid #eee;border-radius:6px;padding:7px;display:flex;align-items:flex-start;gap:5px"><div style="width:20px;height:20px;border-radius:50%;background:#7c6eff22;flex-shrink:0"></div><div><div style="height:3px;background:#eee;border-radius:2px;margin-bottom:2px"></div><div style="height:3px;background:#eee;border-radius:2px;width:80%;margin-bottom:3px"></div><div style="color:#ffab4c;font-size:8px">★★★★★</div></div></div><div style="display:flex;justify-content:center;gap:4px;margin-top:5px"><div style="width:6px;height:6px;border-radius:50%;background:#7c6eff"></div><div style="width:6px;height:6px;border-radius:50%;background:#eee"></div><div style="width:6px;height:6px;border-radius:50%;background:#eee"></div></div></div>',
  ],
  why_us:[
    '<div style="background:#fff;padding:8px"><div style="color:#1a1a2e;font-weight:700;font-size:10px;margin-bottom:6px;text-align:center">Why Choose Us</div><div style="display:grid;grid-template-columns:repeat(3,1fr);gap:5px"><div style="text-align:center;padding:5px"><div style="font-size:12px;margin-bottom:2px">🏆</div><div style="height:3px;background:#eee;border-radius:2px;margin:0 auto;width:80%"></div></div><div style="text-align:center;padding:5px"><div style="font-size:12px;margin-bottom:2px">💯</div><div style="height:3px;background:#eee;border-radius:2px;margin:0 auto;width:80%"></div></div><div style="text-align:center;padding:5px"><div style="font-size:12px;margin-bottom:2px">🤝</div><div style="height:3px;background:#eee;border-radius:2px;margin:0 auto;width:80%"></div></div></div></div>',
    '<div style="background:#f8f9ff;padding:10px;display:flex;gap:8px;align-items:center"><div style="width:44px;height:44px;background:linear-gradient(135deg,#7c6eff22,#7c6eff44);border-radius:8px;display:flex;align-items:center;justify-content:center;font-size:18px;flex-shrink:0">🏆</div><div><div style="color:#1a1a2e;font-weight:700;font-size:10px;margin-bottom:3px">Award-Winning Dealership</div><div style="height:3px;background:#ddd;border-radius:2px;margin-bottom:2px"></div><div style="height:3px;background:#ddd;border-radius:2px;width:75%"></div></div></div>',
  ],
  awards:[
    '<div style="background:#fff;padding:8px;border-top:2px solid #7c6eff"><div style="color:#1a1a2e;font-weight:700;font-size:9px;text-align:center;margin-bottom:5px">Awards & Recognition</div><div style="display:flex;justify-content:center;gap:8px"><div style="background:#f8f8ff;border:1px solid #eee;border-radius:4px;padding:4px 8px;font-size:7px;font-weight:700;color:#7c6eff">Best Dealer 2024</div><div style="background:#f8f8ff;border:1px solid #eee;border-radius:4px;padding:4px 8px;font-size:7px;font-weight:700;color:#7c6eff">5-Star</div><div style="background:#f8f8ff;border:1px solid #eee;border-radius:4px;padding:4px 8px;font-size:7px;font-weight:700;color:#7c6eff">JD Power</div></div></div>',
    '<div style="background:#0f0f1a;padding:8px"><div style="display:grid;grid-template-columns:repeat(4,1fr);gap:4px"><div style="background:rgba(255,255,255,.05);border-radius:5px;padding:5px;text-align:center"><div style="font-size:10px;margin-bottom:2px">🏆</div><div style="height:2px;background:rgba(255,255,255,.15);border-radius:2px"></div></div><div style="background:rgba(255,255,255,.05);border-radius:5px;padding:5px;text-align:center"><div style="font-size:10px;margin-bottom:2px">⭐</div><div style="height:2px;background:rgba(255,255,255,.15);border-radius:2px"></div></div><div style="background:rgba(255,255,255,.05);border-radius:5px;padding:5px;text-align:center"><div style="font-size:10px;margin-bottom:2px">🥇</div><div style="height:2px;background:rgba(255,255,255,.15);border-radius:2px"></div></div><div style="background:rgba(255,255,255,.05);border-radius:5px;padding:5px;text-align:center"><div style="font-size:10px;margin-bottom:2px">💎</div><div style="height:2px;background:rgba(255,255,255,.15);border-radius:2px"></div></div></div></div>',
  ],
  team:[
    '<div style="background:#f8f9ff;padding:8px;display:grid;grid-template-columns:repeat(3,1fr);gap:4px"><div style="background:#fff;border:1px solid #eee;border-radius:5px;padding:5px;text-align:center"><div style="width:18px;height:18px;background:#7c6eff22;border-radius:50%;margin:0 auto 3px"></div><div style="height:3px;background:#eee;border-radius:2px;margin:0 auto;width:80%"></div></div><div style="background:#fff;border:1px solid #eee;border-radius:5px;padding:5px;text-align:center"><div style="width:18px;height:18px;background:#1fd6a022;border-radius:50%;margin:0 auto 3px"></div><div style="height:3px;background:#eee;border-radius:2px;margin:0 auto;width:80%"></div></div><div style="background:#fff;border:1px solid #eee;border-radius:5px;padding:5px;text-align:center"><div style="width:18px;height:18px;background:#ffab4c22;border-radius:50%;margin:0 auto 3px"></div><div style="height:3px;background:#eee;border-radius:2px;margin:0 auto;width:80%"></div></div></div>',
    '<div style="background:#fff;padding:8px"><div style="display:flex;align-items:center;gap:7px;padding:5px;border:1px solid #eee;border-radius:5px;margin-bottom:3px"><div style="width:22px;height:22px;background:#7c6eff22;border-radius:50%;flex-shrink:0"></div><div><div style="height:4px;background:#1a1a2e22;border-radius:2px;margin-bottom:2px;width:60px"></div><div style="height:3px;background:#eee;border-radius:2px;width:45px"></div></div></div><div style="display:flex;align-items:center;gap:7px;padding:5px;border:1px solid #eee;border-radius:5px"><div style="width:22px;height:22px;background:#1fd6a022;border-radius:50%;flex-shrink:0"></div><div><div style="height:4px;background:#1a1a2e22;border-radius:2px;margin-bottom:2px;width:60px"></div><div style="height:3px;background:#eee;border-radius:2px;width:45px"></div></div></div></div>',
  ],
  text:[
    '<div style="background:#fff;padding:12px 14px"><div style="height:3px;background:#eee;border-radius:2px;margin-bottom:3px"></div><div style="height:3px;background:#eee;border-radius:2px;width:90%;margin-bottom:3px"></div><div style="height:3px;background:#eee;border-radius:2px;width:70%"></div></div>',
    '<div style="background:#f8f9ff;border-left:3px solid #7c6eff;padding:10px 14px;margin:4px"><div style="height:3px;background:#1a1a2e22;border-radius:2px;margin-bottom:3px;width:90%"></div><div style="height:3px;background:#1a1a2e22;border-radius:2px;width:70%"></div></div>',
  ],
  image:[
    '<div style="background:#f8f9ff;padding:10px;text-align:center"><div style="height:48px;background:linear-gradient(135deg,#e8e8f8,#d8d8f0);border-radius:5px;display:flex;align-items:center;justify-content:center;color:#9090c0;font-size:18px">🖼</div></div>',
    '<div style="height:64px;background:linear-gradient(135deg,#e8e8f8,#d8d8f0);display:flex;align-items:center;justify-content:center;color:#9090c0;font-size:22px">🖼</div>',
  ],
  form:[
    '<div style="background:#fff;padding:10px"><div style="height:13px;background:#f0f0f8;border:1px solid #e8e8f0;border-radius:3px;margin-bottom:4px"></div><div style="height:13px;background:#f0f0f8;border:1px solid #e8e8f0;border-radius:3px;margin-bottom:4px"></div><div style="height:13px;background:#f0f0f8;border:1px solid #e8e8f0;border-radius:3px;margin-bottom:4px"></div><div style="background:#7c6eff;height:13px;border-radius:3px;margin-top:4px"></div></div>',
    '<div style="background:#1a1a2e;padding:10px"><div style="color:#fff;font-size:9px;font-weight:700;margin-bottom:5px">Get More Info</div><div style="height:14px;background:rgba(255,255,255,.08);border:1px solid rgba(255,255,255,.12);border-radius:4px;margin-bottom:4px"></div><div style="height:14px;background:rgba(255,255,255,.08);border:1px solid rgba(255,255,255,.12);border-radius:4px;margin-bottom:4px"></div><div style="background:#7c6eff;height:13px;border-radius:4px;text-align:center;color:#fff;font-size:8px;line-height:13px">Send</div></div>',
  ],
  map_hours:[
    '<div style="background:#fff;padding:8px;display:grid;grid-template-columns:1fr 1fr;gap:6px"><div style="background:#e8e8f8;border-radius:5px;height:44px;display:flex;align-items:center;justify-content:center;color:#9090c0;font-size:16px">📍</div><div><div style="color:#1a1a2e;font-weight:700;font-size:9px;margin-bottom:3px">Hours</div><div style="height:2px;background:#eee;border-radius:2px;margin-bottom:2px"></div><div style="height:2px;background:#eee;border-radius:2px;margin-bottom:2px;width:80%"></div><div style="height:2px;background:#eee;border-radius:2px;width:60%"></div></div></div>',
    '<div style="background:#fff;padding:10px;text-align:center"><div style="color:#1a1a2e;font-weight:700;font-size:10px;margin-bottom:5px">Business Hours</div><div style="display:grid;grid-template-columns:1fr 1fr;gap:3px"><div style="height:3px;background:#eee;border-radius:2px"></div><div style="height:3px;background:#7c6eff33;border-radius:2px"></div><div style="height:3px;background:#eee;border-radius:2px"></div><div style="height:3px;background:#7c6eff33;border-radius:2px"></div><div style="height:3px;background:#eee;border-radius:2px"></div><div style="height:3px;background:#7c6eff33;border-radius:2px"></div></div></div>',
  ],
};

const BLOCK_DEFS={
  navbar:[
    {logoText:'Premium Motors',links:'New Cars,Used Cars,Finance,Service,Contact',ctaText:'Get a Quote'},
    {logoText:'Premium Motors',links:'Inventory,Finance,Service,About,Contact',ctaText:'Book Test Drive'},
    {logoText:'PREMIUM MOTORS',links:'New,Used,Certified,Finance,Service,Parts,Contact',ctaText:''},
  ],
  hero:[
    {heading:'Drive Your Dream Car Today',subtext:'500+ premium vehicles in stock. New arrivals weekly.',btnText:'Browse Inventory',btn2Text:'Book Test Drive',bgColor:'#0d0d1a'},
    {heading:'2024 Models Now Arriving',subtext:'Be the first to experience the latest lineup.',btnText:'Shop Now',bgColor:'#0d0d1a'},
    {heading:'Welcome to Premium Motors',subtext:'Experience the difference with every visit.',btnText:'Explore',bgColor:'#0d0d1a'},
  ],
  section:[
    {heading:'About Premium Motors',body:'Family-owned and operated since 1998, Premium Motors has served thousands of happy customers. We offer transparent pricing, a no-pressure environment, and a commitment to finding you the perfect vehicle.',bgColor:'#ffffff'},
    {heading:'Our Promise to You',body:'Every vehicle is inspected, every price is fair, and every customer is treated like family. That is the Premium Motors guarantee.',bgColor:'#ffffff'},
    {heading:'Our Story',colLeft:'Founded in 1998, Premium Motors began with a simple idea — sell great cars at honest prices.',colRight:'Today we stock 500+ vehicles and serve customers across the entire region.',bgColor:'#ffffff'},
  ],
  cta:[
    {heading:'Ready to Drive Home Today?',subtext:'Limited time offer — special pricing ends Sunday.',btnText:'Claim Offer'},
    {heading:'0% Financing Available',subtext:'On select 2024 models this month only.',btnText:'Pre-Qualify Online'},
    {heading:"What's Your Trade Worth?",subtext:'Get an instant online estimate in 60 seconds.',btnText:'Get My Value'},
  ],
  footer:[
    {logoText:'Premium Motors',copyright:'© 2024 Premium Motors. All rights reserved.'},
    {logoText:'PREMIUM MOTORS',col1:'Your trusted dealership since 1998. Serving the community with integrity and great vehicles.',col2Links:'New Cars,Used Cars,Certified Pre-Owned,Finance,Special Offers',col3Links:'Service Department,Schedule Service,Parts & Accessories,Contact Us,Careers',copyright:'© 2024 Premium Motors LLC. All rights reserved.'},
  ],
  divider:[{}],
  inv_grid:[
    {heading:'Browse Our Inventory',filterBar:true,count:6,cols:3},
    {heading:'Our Vehicles',filterBar:false,count:4,cols:2},
    {heading:'Available Now',filterBar:true,count:6,cols:1},
  ],
  inv_featured:[
    {heading:'Featured Vehicles',subheading:'Hand-picked by our experts',count:3},
    {heading:'Vehicle of the Month',make:'Tesla',model:'Model 3',year:2024,price:42500},
    {heading:'New Arrivals',subheading:'Fresh off the transport'},
  ],
  inv_search:[
    {heading:'Find Your Perfect Vehicle',placeholder:'Search by make, model, year...'},
    {placeholder:'Make, Model or Keyword...'},
  ],
  car_detail:[
    {make:'Tesla',model:'Model 3',year:2024,price:42500,mileage:'New',color:'Midnight Silver',engine:'Dual Motor AWD',mpg:'358mi range',vin:'5YJ3E1EA8PF001234'},
    {make:'BMW',model:'X5 xDrive40i',year:2023,price:68000,mileage:'12,400 mi',color:'Alpine White'},
  ],
  compare:[
    {heading:'Compare Vehicles',car1:'Select Vehicle 1',car2:'Select Vehicle 2'},
  ],
  promo_banner:[
    {tag:'HOLIDAY SALE',heading:'Save Up to $5,000',subtext:'On select vehicles — ends December 31st',btnText:'Shop Holiday Deals',bgColor:'#c0392b'},
    {tag:'END OF MONTH',heading:'Clearance Event',subtext:'Best prices of the year on remaining 2023 models',btnText:'See All Deals',bgColor:'#0f0f1a'},
    {tag:'⚡ FLASH SALE — 24 HRS ONLY',heading:'Extra $2,500 Off',subtext:'Any in-stock vehicle purchased today',btnText:'Claim Now',bgColor:'gradient'},
  ],
  promo_cards:[
    {card1Icon:'🎁',card1Title:'Holiday Special',card1Body:'$1,500 off any certified pre-owned vehicle.',card2Icon:'💰',card2Title:'0% Financing',card2Body:'60-month special financing available.',card3Icon:'🔑',card3Title:'Free First Service',card3Body:'Complimentary first oil change on us.'},
    {card1Icon:'🎄',card1Title:'Season&#39;s Savings',card1Body:'Up to $3,000 off select models.',card2Icon:'💳',card2Title:'No Payments 90 Days',card2Body:'Drive now, pay later this holiday.'},
  ],
  financing:[
    {rate:'0%',term:'60',heading:'Special APR Offer',subtext:'On all 2024 models through December 31st',btnText:'Pre-Qualify Online'},
    {monthly:299,apr:'2.9%',down:0,heading:'Low Monthly Payments',btnText:'Calculate My Payment'},
    {heading:"What's Your Trade Worth?",subtext:'Instant online trade-in estimate',btnText:'Get My Value'},
  ],
  countdown:[
    {heading:'Sale Ends In',event:'End of Month Clearance'},
    {heading:'Next Event',event:'Spring Car Show — April 15'},
  ],
  service_dept:[
    {heading:'Our Service Center',subtext:'Factory-certified technicians, genuine parts, and state-of-the-art equipment.',services:'Oil Change,Tire Rotation,Brake Service,Battery Check,Detailing,Multi-Point Inspection'},
    {heading:'Certified Service Department',subtext:'Keeping your vehicle in peak condition with every visit.'},
  ],
  schedule_svc:[
    {heading:'Schedule Your Service',fields:'Vehicle,Service Type,Preferred Date,Preferred Time,Name,Phone',btnText:'Book Appointment'},
    {heading:'Quick Service Request',fields:'Vehicle,Service Type,Date',btnText:'Check Availability'},
  ],
  svc_specials:[
    {special1Icon:'🛢️',special1Title:'Oil Change Special',special1Price:'$29.99',special2Icon:'🛞',special2Title:'Tire Rotation',special2Price:'$15.99',special3Icon:'🔋',special3Title:'Battery Check',special3Price:'FREE'},
    {special1Icon:'🛢️',special1Title:'Full Synthetic Oil Change',special1Price:'$49.99',special2Icon:'🚿',special2Title:'Detail Package',special2Price:'$149'},
  ],
  svc_brands:[
    {heading:'Certified For',brands:'Tesla,BMW,Mercedes,Audi,Porsche,Ford'},
    {heading:'Factory Authorized',brands:'Tesla,BMW,Mercedes,Audi,Porsche,Lexus,Cadillac,Ford'},
  ],
  testimonials:[
    {heading:'What Our Customers Say',reviews:[{name:'Jennifer M.',rating:5,text:'Incredible experience from start to finish. Alex was transparent and patient throughout the entire process.'},{name:'Robert T.',rating:5,text:'Best car buying experience of my life. No pressure, fair price, and the car is perfect.'},{name:'Amy K.',rating:5,text:'The service department is outstanding. Quick, honest, and always on time.'}]},
    {heading:'',quote:'Best dealership experience I have ever had. They found exactly what I was looking for at a price I could afford.',author:'Michael S., verified buyer'},
    {heading:'Customer Reviews',reviews:[{name:'Sarah L.',rating:5,text:'Absolutely wonderful staff and amazing selection of vehicles!'}]},
  ],
  why_us:[
    {heading:'Why 10,000+ Customers Choose Us',items:'No Hidden Fees|Transparent pricing always,500+ Vehicles|New and pre-owned in stock,5-Star Service|Award-winning team,Easy Financing|Rates from all top lenders'},
    {heading:'The Premium Motors Difference',image:'showroom',text:'We believe buying a car should be exciting, not stressful. No pressure. No gimmicks. Just great cars at honest prices.'},
  ],
  awards:[
    {heading:'Awards & Recognition',awards:'Best Dealership 2024,5-Star Google Rating,JD Power Certified,DealerRater Top Performer'},
    {heading:'',awards:'Best Dealer 2024,5-Star Service,JD Power,Top Performer 2023'},
  ],
  team:[
    {heading:'Meet Our Team',members:'Alex Thompson|Sales Manager,Maria Garcia|Finance Director,David Kim|Lead Salesperson,Lisa Chen|Service Advisor'},
    {heading:'Your Sales Team',members:'Alex Thompson|Owner,Maria Garcia|General Manager'},
  ],
  text:[
    {content:'Premium Motors has been serving the community since 1998. Our commitment to transparent pricing and exceptional service has earned us thousands of loyal customers and multiple industry awards.'},
    {content:'"The experience at Premium Motors was unlike any other dealership I have visited. Professional, honest, and genuinely focused on finding the right car for me." — Jennifer M., Verified Buyer'},
  ],
  image:[{alt:'Dealership showroom',caption:''},{alt:'Vehicle photo',caption:''}],
  form:[
    {heading:'Contact Our Sales Team'},
    {heading:'Request More Information'},
  ],
  map_hours:[
    {heading:'Visit Us',address:'1234 Motor Mile Blvd, Springfield, IL 62701',phone:'(555) 234-5678',hours:'Mon–Fri: 8am–8pm|Sat: 9am–7pm|Sun: 11am–5pm'},
    {heading:'Hours of Operation',hours:'Mon–Fri: 8am–8pm|Sat: 9am–7pm|Sun: 11am–5pm'},
  ],
};