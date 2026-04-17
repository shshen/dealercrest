function svgIcon(name, size=16) {
  const d = IC[name] || '';
  const parts = d.split('|');
  const inner = parts.map(p => {
    if (p.startsWith('C')) {
      const [cx,cy,r] = p.slice(1).split(',');
      return `<circle cx="${cx}" cy="${cy}" r="${r}"/>`;
    }
    if (p.startsWith('R')) {
      const [x,y,w,h,rx] = p.slice(1).split(',');
      return `<rect x="${x}" y="${y}" width="${w}" height="${h}" rx="${rx}"/>`;
    }
    return `<path d="${p}"/>`;
  }).join('');
  return `<svg width="${size}" height="${size}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">${inner}</svg>`;
}

const SOCIAL_SVG = {
  twitter:  `<svg viewBox="0 0 24 24" fill="currentColor"><path d="M18.244 2.25h3.308l-7.227 8.26 8.502 11.24H16.17l-5.214-6.817L4.99 21.75H1.68l7.73-8.835L1.254 2.25H8.08l4.713 6.231zm-1.161 17.52h1.833L7.084 4.126H5.117z"/></svg>`,
  linkedin: `<svg viewBox="0 0 24 24" fill="currentColor"><path d="M16 8a6 6 0 016 6v7h-4v-7a2 2 0 00-2-2 2 2 0 00-2 2v7h-4v-7a6 6 0 016-6zM2 9h4v12H2z"/><circle cx="4" cy="4" r="2"/></svg>`,
  facebook: `<svg viewBox="0 0 24 24" fill="currentColor"><path d="M18 2h-3a5 5 0 00-5 5v3H7v4h3v8h4v-8h3l1-4h-4V7a1 1 0 011-1h3z"/></svg>`,
  instagram:`<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"><rect x="2" y="2" width="20" height="20" rx="5"/><circle cx="12" cy="12" r="4"/><circle cx="17.5" cy="6.5" r="0.5" fill="currentColor"/></svg>`,
};
function socialSvg(p,s=14){ return (SOCIAL_SVG[p]||SOCIAL_SVG.instagram).replace('<svg ',`<svg width="${s}" height="${s}" `); }

/* ══════════════════════════════════════════
   DATA
══════════════════════════════════════════ */
let CARS = [
  {id:1,make:'Tesla',   model:'Model 3',      year:2023,price:42500,status:'Available',mileage:0,    color:'Silver',vin:'5YJ3E1EA8PF001234'},
  {id:2,make:'BMW',     model:'X5 xDrive40i', year:2022,price:68000,status:'Reserved', mileage:12400,color:'White', vin:'5UXCR6C04N9J12345'},
  {id:3,make:'Mercedes',model:'GLE 350',      year:2023,price:59900,status:'Available',mileage:5200, color:'Black', vin:'4JGFB4KB0PA012345'},
  {id:4,make:'Audi',    model:'Q7 Premium',   year:2022,price:55700,status:'Sold',     mileage:8800, color:'Gray',  vin:'WA1VXAF79MD012345'},
  {id:5,make:'Porsche', model:'Cayenne',      year:2023,price:89000,status:'Available',mileage:1200, color:'Black', vin:'WP1AA2AY4PDA12345'},
  {id:6,make:'Ford',    model:'F-150 Lariat', year:2022,price:52000,status:'Available',mileage:18000,color:'Red',   vin:'1FTFW1E52NFA12345'},
];
const LEADS_DATA = [
  {id:1,name:'Sarah Mitchell',  email:'sarah@gmail.com',     phone:'+1 555-0182',interest:'Tesla Model 3',   status:'Hot', score:92,channels:['chat','phone'],       last:'2h ago', source:'Website'},
  {id:2,name:'James Rodriguez', email:'jrodriguez@mail.com', phone:'+1 555-0247',interest:'BMW X5',          status:'Warm',score:67,channels:['mail'],               last:'1d ago', source:'Facebook'},
  {id:3,name:'Emily Chen',      email:'emily@work.com',      phone:'+1 555-0391',interest:'Mercedes GLE',    status:'Cold',score:34,channels:['chat'],               last:'3d ago', source:'Google Ad'},
  {id:4,name:'Marcus Johnson',  email:'mjohnson@corp.com',   phone:'+1 555-0128',interest:'Porsche Cayenne', status:'Hot', score:88,channels:['phone','chat','mail'], last:'30m ago',source:'Referral'},
  {id:5,name:'Priya Patel',     email:'priya@startup.io',    phone:'+1 555-0476',interest:'Audi Q7',         status:'Warm',score:71,channels:['mail','chat'],         last:'5h ago', source:'LinkedIn'},
];
let PAGES_DATA = [
  {id:1,name:'Home',          url:'/',            status:'Published',views:12400,edited:'2h ago'},
  {id:2,name:'Inventory',     url:'/inventory',   status:'Published',views:8900, edited:'1d ago'},
  {id:3,name:'About Us',      url:'/about',       status:'Published',views:3200, edited:'1w ago'},
  {id:4,name:'Contact',       url:'/contact',     status:'Published',views:2800, edited:'3d ago'},
  {id:5,name:'Finance',       url:'/finance',     status:'Draft',    views:0,    edited:'2d ago'},
  {id:6,name:'Special Offers',url:'/offers',      status:'Published',views:5600, edited:'4h ago'},
  {id:7,name:'Blog',          url:'/blog',        status:'Draft',    views:0,    edited:'1w ago'},
  {id:8,name:'Testimonials',  url:'/testimonials',status:'Published',views:1900, edited:'5d ago'},
];
const POSTS = [
  {id:1,platform:'twitter',  acct:'@premiummotors',    text:'New Tesla Model 3 just arrived! Starting at $42,500. Zero emissions, zero compromises. Book a test drive today! #Tesla #EV',likes:248, comments:34, shares:67, status:'Published',time:'2h ago'},
  {id:2,platform:'linkedin', acct:'Premium Motors LLC',text:'Proud to announce our partnership with Tesla Motors, making us the premier Tesla-certified dealership in the region.',       likes:512, comments:89, shares:143,status:'Published',time:'1d ago'},
  {id:3,platform:'facebook', acct:'Premium Motors',    text:'Weekend Special! Take $2,000 off any 2022 certified pre-owned vehicle this Saturday and Sunday only. Limited time!',         likes:892, comments:156,shares:234,status:'Published',time:'2d ago'},
  {id:4,platform:'twitter',  acct:'@premiummotors',    text:'Did you know? We offer 0% financing for the first 12 months on select vehicles. Call us to learn more! #CarDeals',          likes:0,   comments:0,  shares:0,  status:'Scheduled',time:'Tomorrow 9AM'},
  {id:5,platform:'instagram',acct:'@premiummotors_ig', text:'Behind the scenes of our latest photoshoot. Our BMW X5 looking stunning under studio lights. Which color would you choose?', likes:1240,comments:203,shares:89, status:'Published',time:'3d ago'},
];
const ADS = [
  {id:1,name:'Tesla Model 3 – Google Search',platform:'Google Ads',status:'Active',budget:2500,spend:1840,imp:48200, clicks:1240,ctr:'2.57%',roas:4.2},
  {id:2,name:'SUV Collection – Facebook',    platform:'Meta Ads',  status:'Active',budget:1800,spend:1200,imp:134000,clicks:2890,ctr:'2.16%',roas:3.8},
  {id:3,name:'Finance Offer – Instagram',    platform:'Meta Ads',  status:'Paused',budget:1200,spend:890, imp:89000, clicks:1560,ctr:'1.75%',roas:2.9},
];
const CAMPAIGNS = [
  {id:1,name:'Spring EV Promotion',    type:'Email',status:'Active',   sent:12400,opened:4340,clicks:892, rev:48200,pct:68},
  {id:2,name:'End of Quarter Push',    type:'SMS',  status:'Active',   sent:8900, opened:7120,clicks:1240,rev:32100,pct:45},
  {id:3,name:'New Arrivals Newsletter',type:'Email',status:'Completed',sent:21000,opened:9450,clicks:2100,rev:89400,pct:100},
  {id:4,name:'Loyalty Reward Drive',   type:'Email',status:'Draft',    sent:0,    opened:0,   clicks:0,   rev:0,    pct:0},
];
const MONTHS=['Oct','Nov','Dec','Jan','Feb','Mar'];
const VISITORS=[8400,9200,11800,10200,13400,14890];
const LEADS_CNT=[124,148,210,189,267,312];
const FEATURES=[
  {id:'inventory',  label:'Inventory',    desc:'Vehicle stock, pricing and listings', icon:'car',   color:'#7c6eff',bg:'rgba(124,110,255,.12)',badge:'324 cars', bc:'bg-blu'},
  {id:'leads',      label:'Leads',        desc:'Prospects via chat, phone and email',  icon:'leads', color:'#1fd6a0',bg:'rgba(31,214,160,.12)', badge:'12 new',  bc:'bg-grn'},
  {id:'config',     label:'Configuration',desc:'Users, domains and system settings',   icon:'cog',   color:'#c084fc',bg:'rgba(192,132,252,.12)',badge:'',        bc:''},
  {id:'website',    label:'Website',      desc:'Web pages, content and builder',       icon:'globe', color:'#42c4f7',bg:'rgba(66,196,247,.12)', badge:'8 pages', bc:'bg-blu'},
  {id:'analytics',  label:'Analytics',    desc:'Traffic, clicks and conversions',      icon:'chart', color:'#f87171',bg:'rgba(248,113,113,.12)',badge:'Live',    bc:'bg-grn'},
  {id:'advertising',label:'Advertising',  desc:'Paid ad campaigns and ROI tracking',   icon:'zap',   color:'#ffab4c',bg:'rgba(255,171,76,.12)', badge:'3 active',bc:'bg-ora'},
  {id:'marketing',  label:'Marketing',    desc:'Email campaigns and automation flows', icon:'mail',  color:'#ff5272',bg:'rgba(255,82,114,.12)', badge:'2 running',bc:'bg-red'},
  {id:'social',     label:'Social',       desc:'Posts, comments and social inbox',     icon:'share', color:'#1fd6a0',bg:'rgba(31,214,160,.12)', badge:'7 pending',bc:'bg-ora'},
  {id:'finance',    label:'Finance',      desc:'Deal worksheets, F&I and payment calc', icon:'dollar',color:'#ffab4c',bg:'rgba(255,171,76,.12)', badge:'',         bc:''},
  {id:'service',    label:'Service',      desc:'Repair orders, bays and technicians',   icon:'wrench',color:'#42c4f7',bg:'rgba(66,196,247,.12)', badge:'4 open',   bc:'bg-blu'},
  {id:'reports',    label:'Reports',      desc:'Sales leaderboard and aged inventory',  icon:'report',color:'#c084fc',bg:'rgba(192,132,252,.12)',badge:'',         bc:''},
  {id:'tasks',      label:'Tasks',        desc:'Daily to-dos, follow-ups and team assignments', icon:'task', color:'#f87171',bg:'rgba(248,113,113,.12)',badge:'3 due today',bc:'bg-red'},
  {id:'listings',   label:'Listings',     desc:'Push inventory to Autotrader, Cars.com, KSL and more', icon:'cast',color:'#1fd6a0',bg:'rgba(31,214,160,.12)', badge:'5 portals', bc:'bg-grn'},
];
const NAV_ITEMS=[
  {id:'home',       icon:'home',  label:'Home'},
  {id:'inventory',  icon:'car',   label:'Inventory'},
  {id:'leads',      icon:'leads', label:'Leads'},
  {id:'website',    icon:'globe', label:'Website'},
  {id:'analytics',  icon:'chart', label:'Analytics'},
  {id:'advertising',icon:'zap',   label:'Advertising'},
  {id:'marketing',  icon:'mail',  label:'Marketing'},
  {id:'social',     icon:'share', label:'Social'},
  {id:'finance',    icon:'dollar',label:'Finance'},
  {id:'service',    icon:'wrench',label:'Service'},
  {id:'reports',    icon:'report',label:'Reports'},
  {id:'tasks',      icon:'task',  label:'Tasks'},
  {id:'listings',   icon:'cast',  label:'Listings'},
  {id:'config',     icon:'cog',   label:'Config'},
];
const PAGE_TITLES={home:'Dashboard',inventory:'Inventory','vehicle-editor':'Vehicle',leads:'Leads','lead-detail':'Lead Detail',website:'Website',analytics:'Analytics',advertising:'Advertising',marketing:'Marketing',social:'Social',finance:'Finance & Desking',service:'Service',reports:'Reports',tasks:'Tasks',listings:'Listings',config:'Configuration',vdp:'Vehicle Detail'};
let TOGGLES={notif:true,tfa:false,analytics:true,maintenance:false,backup:true};
let INV_FEED={url:'',token:'',format:'JSON',status:''}; // inventory feed config

/* ── Chat data ── */
const CHAT_CONVS = [
  {id:1,name:'Sarah Mitchell', initials:'SM',color:'#7c6eff',status:'online', unread:3,
   messages:[
    {from:'them',text:'Hi! I saw the Tesla Model 3 listing on your website.',time:'10:22 AM'},
    {from:'them',text:'Is it still available? What colors do you have?',     time:'10:23 AM'},
    {from:'me',  text:'Hi Sarah! Yes it is still available. We have it in Silver, White, and Midnight Blue.',time:'10:31 AM'},
    {from:'them',text:'Perfect! Can I come in for a test drive this Saturday?',time:'10:33 AM'},
    {from:'them',text:'Also is 0% financing available on that model?',         time:'10:34 AM'},
  ]},
  {id:2,name:'James Rodriguez',initials:'JR',color:'#1fd6a0',status:'online', unread:1,
   messages:[
    {from:'them',text:'Hello, I am interested in the BMW X5 you have listed.',time:'9:15 AM'},
    {from:'me',  text:'Hi James! Great choice. The X5 xDrive40i is a fantastic vehicle. Have you seen it in person?',time:'9:20 AM'},
    {from:'them',text:'Not yet. Can you tell me more about the warranty?',     time:'9:45 AM'},
  ]},
  {id:3,name:'Marcus Johnson', initials:'MJ',color:'#f87171',status:'away',   unread:0,
   messages:[
    {from:'me',  text:'Hi Marcus, just following up on the Porsche Cayenne you were looking at last week.',time:'Yesterday'},
    {from:'them',text:'Yes I am still interested! My wife wants to see it too.',time:'Yesterday'},
    {from:'me',  text:'Perfect! We can arrange a family viewing. How does this weekend work?',time:'Yesterday'},
    {from:'them',text:'Saturday at 2pm works great for us.',time:'Yesterday'},
  ]},
  {id:4,name:'Priya Patel',    initials:'PP',color:'#ffab4c',status:'offline',unread:0,
   messages:[
    {from:'them',text:'Hi, do you offer trade-in valuations?',time:'Mon'},
    {from:'me',  text:'Absolutely! We offer free trade-in appraisals. What vehicle are you trading in?',time:'Mon'},
  ]},
];

/* ══════════════════════════════════════════
   STATE
══════════════════════════════════════════ */
let currentPage = 'home';
let invFilter = 'All', invSearch = '';

/* ══════════════════════════════════════════
   GLOBAL TASKS DATA
══════════════════════════════════════════ */
let TASKS_DATA = [
  {id:1, text:'Call Sarah Mitchell to confirm Saturday test drive', done:false, due:'Today',    dueTs:0,  priority:'High',   category:'Lead Follow-up',  leadId:1,  assignee:'Alex Thompson',  createdBy:'Alex Thompson'},
  {id:2, text:'Send financing options to Sarah Mitchell',           done:true,  due:'Yesterday',dueTs:-1, priority:'Medium', category:'Lead Follow-up',  leadId:1,  assignee:'Alex Thompson',  createdBy:'Maria Garcia'},
  {id:3, text:'Counter offer call with Marcus Johnson at 3pm',      done:false, due:'Today',    dueTs:0,  priority:'High',   category:'Lead Follow-up',  leadId:4,  assignee:'Alex Thompson',  createdBy:'Alex Thompson'},
  {id:4, text:'Send Q7 vs GLE comparison to Lisa Park',             done:false, due:'Today',    dueTs:0,  priority:'Medium', category:'Lead Follow-up',  leadId:5,  assignee:'David Kim',      createdBy:'David Kim'},
  {id:5, text:'Schedule showroom visit with James Rodriguez',        done:false, due:'Friday',   dueTs:4,  priority:'Medium', category:'Lead Follow-up',  leadId:2,  assignee:'David Kim',      createdBy:'David Kim'},
  {id:6, text:'RO-1041 BMW X3 — follow up on parts arrival',        done:false, due:'Today',    dueTs:0,  priority:'High',   category:'Service',         leadId:null,assignee:'Maria Garcia', createdBy:'Maria Garcia'},
  {id:7, text:'Post new Porsche Cayenne to Facebook Marketplace',    done:false, due:'Tomorrow', dueTs:1,  priority:'Low',    category:'Marketing',       leadId:null,assignee:'Lisa Chen',   createdBy:'Lisa Chen'},
  {id:8, text:'Review and approve Q4 ad budget',                    done:false, due:'Friday',   dueTs:4,  priority:'Medium', category:'Admin',            leadId:null,assignee:'Alex Thompson',createdBy:'Alex Thompson'},
  {id:9, text:'Order floor mats for RO-1039 Audi Q7',               done:false, due:'Tomorrow', dueTs:1,  priority:'Low',    category:'Service',         leadId:null,assignee:'Maria Garcia', createdBy:'Tom Wilson'},
  {id:10,text:'Update website hero banner for November promotion',   done:false, due:'Next Week',dueTs:7,  priority:'Low',    category:'Marketing',       leadId:null,assignee:'Lisa Chen',   createdBy:'Lisa Chen'},
];
let nextTaskId = 11;
let taskFilter = 'Today';   // Today | All | Done | My Tasks
let taskCategory = 'All';

const DEAL_STAGES = ['New','Contacted','Test Drive','Negotiating','F&I','Closed Won','Closed Lost'];
const STAGE_COLORS = {
  'New':         'var(--tx3)',
  'Contacted':   'var(--blu)',
  'Test Drive':  'var(--ora)',
  'Negotiating': 'var(--pur)',
  'F&I':         'var(--acc)',
  'Closed Won':  'var(--grn)',
  'Closed Lost': 'var(--red)',
};
let leadTab = 'All', leadSearch = '', leadView = 'list';
let socPlatform = 'All';
let chatOpen = false;
let activeConvId = 1;
let convMessages = CHAT_CONVS.map(c=>({id:c.id,msgs:[...c.messages]}));
let totalUnread = CHAT_CONVS.reduce((a,c)=>a+c.unread,0);

/* ══════════════════════════════════════════
   HASH ROUTING
══════════════════════════════════════════ */
function initRouter(){