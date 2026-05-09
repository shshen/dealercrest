/* pages/finance.js */
(function(){

function renderFinance(){
  const cars = CARS.map(c=>`<option value="${c.id}">${c.year} ${c.make} ${c.model}</option>`).join('');
  const leads= LEADS_DATA.map(l=>`<option value="${l.id}">${l.name}</option>`).join('');
  const calc = finCalc();
  const row=(label,val,cls='')=>`<div style="display:flex;justify-content:space-between;padding:7px 0;border-bottom:1px solid var(--b1);font-size:13px"><span style="color:var(--tx2)">${label}</span><span style="color:var(--tx);font-weight:600;${cls}">${val}</span></div>`;
  const fi=(label,key,val,min,max,step)=>`
    <div style="margin-bottom:10px">
      <div style="font-size:11px;color:var(--tx3);margin-bottom:4px">${label}</div>
      <input class="inp" type="number" min="${min}" max="${max}" step="${step}" value="${val}"
        oninput="FIN['${key}']=parseFloat(this.value)||0;document.getElementById('fin-out').innerHTML=finRenderOut()" style="width:100%"/>
    </div>`;
  const tog=(label,key,val)=>`
    <div style="display:flex;align-items:center;justify-content:space-between;padding:8px 0;border-bottom:1px solid var(--b1)">
      <div><div style="font-size:13px;color:var(--tx)">${label}</div><div style="font-size:11px;color:var(--tx3)">$${val.toLocaleString()}</div></div>
      <div class="tog${FIN[key]>0?' on':''}" onclick="FIN['${key}']=FIN['${key}']>0?0:${val};document.getElementById('fin-out').innerHTML=finRenderOut()"></div>
    </div>`;

  return `${bc('Finance & Desking')}
  <div class="ph"><h1>Finance & Desking</h1><p>Deal worksheet, payment calculator and F&I products</p></div>
  <div style="display:grid;grid-template-columns:340px 1fr;gap:14px;padding:0 20px 20px">
    <!-- LEFT: inputs -->
    <div>
      <div class="cs" style="margin-bottom:14px">
        <div class="cs-h"><div style="display:flex;align-items:center;gap:7px">${svgIcon('leads',14)}<h3>Customer & Vehicle</h3></div></div>
        <div style="padding:14px">
          <div style="margin-bottom:10px">
            <div style="font-size:11px;color:var(--tx3);margin-bottom:4px">Customer</div>
            <select class="inp" style="width:100%" onchange="FIN.leadId=this.value"><option value="">— Select lead —</option>${leads}</select>
          </div>
          <div style="margin-bottom:0">
            <div style="font-size:11px;color:var(--tx3);margin-bottom:4px">Vehicle</div>
            <select class="inp" style="width:100%" onchange="FIN.carId=this.value;const car=CARS.find(c=>c.id==this.value);if(car){FIN.salePrice=car.price;FIN.msrp=car.msrp||car.price;document.getElementById('fin-out').innerHTML=finRenderOut()}"><option value="">— Select vehicle —</option>${cars}</select>
          </div>
        </div>
      </div>
      <div class="cs" style="margin-bottom:14px">
        <div class="cs-h"><div style="display:flex;align-items:center;gap:7px">${svgIcon('dollar',14)}<h3>Deal Structure</h3></div></div>
        <div style="padding:14px">
          ${fi('Sale Price','salePrice',FIN.salePrice,0,999999,100)}
          ${fi('Trade-In Value','tradeIn',FIN.tradeIn,0,99999,100)}
          ${fi('Trade-In Owed','tradeOwed',FIN.tradeOwed,0,99999,100)}
          ${fi('Cash Down','down',FIN.down,0,99999,500)}
          ${fi('Interest Rate (%)','rate',FIN.rate,0,30,0.1)}
          ${fi('Term (months)','term',FIN.term,12,96,12)}
          ${fi('Tax Rate (%)','taxRate',FIN.taxRate,0,15,0.25)}
        </div>
      </div>
      <div class="cs">
        <div class="cs-h"><div style="display:flex;align-items:center;gap:7px">${svgIcon('star',14)}<h3>F&I Products</h3></div></div>
        <div style="padding:14px">
          ${tog('GAP Insurance','gapIns',895)}
          ${tog('Extended Warranty','extWarranty',2200)}
          ${tog('Paint Protection','paintProtect',799)}
        </div>
      </div>
    </div>
    <!-- RIGHT: output -->
    <div id="fin-out">${finRenderOut()}</div>
  </div>`;
}
function finCalc(){
  const fi = FIN;
  const tradeNet = fi.tradeIn - fi.tradeOwed;
  const fiProducts = fi.gapIns + fi.extWarranty + fi.paintProtect;
  const taxable = fi.salePrice - Math.max(0,tradeNet);
  const tax = taxable * (fi.taxRate/100);
  const totalFinanced = fi.salePrice + fiProducts + tax - Math.max(0,tradeNet) - fi.down;
  const monthlyRate = fi.rate / 100 / 12;
  const n = fi.term;
  const monthly = totalFinanced <= 0 ? 0 :
    monthlyRate === 0 ? totalFinanced/n :
    totalFinanced * monthlyRate * Math.pow(1+monthlyRate,n) / (Math.pow(1+monthlyRate,n)-1);
  const totalCost = monthly*n + fi.down;
  const grossProfit = fi.salePrice - (fi.msrp * 0.88); // rough estimate
  return {tradeNet,fiProducts,tax,totalFinanced,monthly,totalCost,grossProfit,fiProducts};
}
function finRenderOut(){
  const calc = finCalc();
  const fmt = v=>'$'+Math.abs(v).toLocaleString(undefined,{minimumFractionDigits:2,maximumFractionDigits:2});
  const fmtr = v=>'$'+Math.round(v).toLocaleString();
  const row=(label,val,bold,color)=>`<div style="display:flex;justify-content:space-between;padding:8px 0;border-bottom:1px solid var(--b1);font-size:13px"><span style="color:var(--tx2)">${label}</span><span style="font-weight:${bold?700:500};color:${color||'var(--tx)'};">${val}</span></div>`;
  // Payment terms grid
  const terms=[24,36,48,60,72,84].map(t=>{
    const mRate=FIN.rate/100/12;
    const tf=Math.max(0,FIN.salePrice+(FIN.gapIns+FIN.extWarranty+FIN.paintProtect)+(FIN.salePrice-Math.max(0,FIN.tradeIn-FIN.tradeOwed))*FIN.taxRate/100-Math.max(0,FIN.tradeIn-FIN.tradeOwed)-FIN.down);
    const pmt=mRate===0?tf/t:tf*mRate*Math.pow(1+mRate,t)/(Math.pow(1+mRate,t)-1);
    const active=t===FIN.term;
    return `<div onclick="FIN.term=${t};document.getElementById('fin-out').innerHTML=finRenderOut()" style="padding:12px 8px;border-radius:8px;background:${active?'var(--acc)':'var(--s2)'};color:${active?'#fff':'var(--tx2)'};cursor:pointer;text-align:center;border:1px solid ${active?'var(--acc)':'var(--b1)'}">
      <div style="font-size:10px;margin-bottom:4px;opacity:.8">${t} mo</div>
      <div style="font-size:15px;font-weight:700">$${Math.round(pmt).toLocaleString()}</div>
    </div>`;
  }).join('');

  return `
    <div class="cs" style="margin-bottom:14px">
      <div class="cs-h"><div style="display:flex;align-items:center;gap:7px">${svgIcon('dollar',14)}<h3>Payment Calculator</h3></div></div>
      <div style="padding:20px">
        <div style="text-align:center;margin-bottom:20px">
          <div style="font-size:13px;color:var(--tx3);margin-bottom:4px">Est. Monthly Payment</div>
          <div style="font-size:48px;font-weight:800;font-family:'Syne',sans-serif;color:var(--acc)">$${Math.round(calc.monthly).toLocaleString()}<span style="font-size:18px;font-weight:500;color:var(--tx3)">/mo</span></div>
          <div style="font-size:12px;color:var(--tx3);margin-top:4px">${FIN.term} months @ ${FIN.rate}% APR</div>
        </div>
        <div style="display:grid;grid-template-columns:repeat(6,1fr);gap:8px;margin-bottom:20px">${terms}</div>
      </div>
    </div>
    <div style="display:grid;grid-template-columns:1fr 1fr;gap:14px">
      <div class="cs">
        <div class="cs-h"><div style="display:flex;align-items:center;gap:7px">${svgIcon('report',14)}<h3>Deal Summary</h3></div></div>
        <div style="padding:14px">
          ${row('Sale Price',fmt(FIN.salePrice))}
          ${row('Trade-In Net','<span style="color:var(--grn)">'+fmt(calc.tradeNet)+'</span>')}
          ${row('Cash Down','<span style="color:var(--grn)">'+fmt(FIN.down)+'</span>')}
          ${row('F&I Products',fmt(calc.fiProducts))}
          ${row('Sales Tax',fmt(calc.tax))}
          ${row('Amount Financed',fmt(Math.max(0,calc.totalFinanced)),true)}
          ${row('Total Cost',fmt(calc.totalCost))}
        </div>
      </div>
      <div class="cs">
        <div class="cs-h"><div style="display:flex;align-items:center;gap:7px">${svgIcon('trendUp',14)}<h3>Gross Profit</h3></div></div>
        <div style="padding:14px">
          ${row('Front-End Gross',fmt(Math.max(0,calc.grossProfit)),'',calc.grossProfit>0?'var(--grn)':'var(--red)')}
          ${row('F&I Gross',fmt(calc.fiProducts),'','var(--grn)')}
          ${row('Total Gross',fmt(Math.max(0,calc.grossProfit)+calc.fiProducts),true,'var(--grn)')}
        </div>
        <div style="padding:14px;padding-top:0">
          ${btn('Print Deal Sheet','btn btn-s btn-sm','onclick="showToast(\'Printing…\')"')}
          ${btn('Send to RouteOne','btn btn-p btn-sm','onclick="showToast(\'Sent to RouteOne\')"')}
        </div>
      </div>
    </div>`;
}


/* ══════════════════════════════════════════
   SERVICE DEPARTMENT
══════════════════════════════════════════ */
let SERVICE_ROS = [
  {id:'RO-1042',customer:'Sarah Mitchell', phone:'+1 555-0182',vehicle:'2022 Tesla Model 3',vin:'5YJ3E1EA2NF123456',advisor:'Alex Thompson',tech:'Mike Davis',status:'In Progress',type:'Customer Pay',mileage:22400,in:'9:00 AM',promise:'2:00 PM',concern:'Oil change + tire rotation',jobs:[{desc:'Oil Change - Full Synthetic 5W-30',labor:89,parts:45,status:'Done'},{desc:'Tire Rotation & Balance',labor:49,parts:0,status:'Done'},{desc:'Cabin Air Filter Replacement',labor:25,parts:38,status:'Pending'}]},
  {id:'RO-1041',customer:'Marcus Johnson', phone:'+1 555-0128',vehicle:'2021 BMW X3',vin:'WBA7U2C51LF123789',advisor:'Maria Garcia',tech:'Tom Wilson',status:'Waiting Parts',type:'Warranty',mileage:31200,in:'8:30 AM',promise:'5:00 PM',concern:'Check engine light - rough idle',jobs:[{desc:'Diagnose MIL / Engine Misfire',labor:150,parts:0,status:'Done'},{desc:'Ignition Coil Pack x4',labor:220,parts:380,status:'Waiting Parts'}]},
  {id:'RO-1040',customer:'Emily Chen',     phone:'+1 555-0391',vehicle:'2020 Mercedes GLE',vin:'4JGFB5KB5LA123012',advisor:'Alex Thompson',tech:'Mike Davis',status:'Completed',type:'Customer Pay',mileage:41800,in:'Yesterday',promise:'Yesterday 4PM',concern:'Brake inspection & service',jobs:[{desc:'Front Brake Pads & Rotors',labor:180,parts:420,status:'Done'},{desc:'Rear Brake Fluid Flush',labor:95,parts:35,status:'Done'}]},
  {id:'RO-1039',customer:'James Rodriguez',phone:'+1 555-0247',vehicle:'2019 Audi Q7',vin:'WA1BXAFY8KD123345',advisor:'David Kim',tech:'Tom Wilson',status:'Ready',type:'Internal',mileage:58900,in:'8:00 AM',promise:'12:00 PM',concern:'Pre-sale inspection & detail',jobs:[{desc:'Multi-Point Safety Inspection',labor:99,parts:0,status:'Done'},{desc:'Full Detail & Reconditioning',labor:250,parts:80,status:'Done'}]},
];
const RO_STATUS_COLOR={Scheduled:'var(--blu)','In Progress':'var(--ora)','Waiting Parts':'var(--pur)',Ready:'var(--grn)',Completed:'var(--tx3)'};
let roTab='All', roDetail=null;


/* ── DOM writers ── */
function finCalc() {
  const gn  = id => parseFloat(document.getElementById(id)?.value) || 0;
  const sale = gn('fin-sale'), msrp = gn('fin-msrp'), trade = gn('fin-trade'),
        owed = gn('fin-owed'), down = gn('fin-down'),  tax   = gn('fin-tax'),
        rate = gn('fin-rate'), term = gn('fin-term');

  const FI_PRODUCTS = [
    { label:'GAP Insurance',      price:895  },
    { label:'Extended Warranty',  price:2200 },
    { label:'Paint Protection',   price:799  },
  ];

  // F&I products checkboxes
  const prodEl = document.getElementById('fin-products');
  if (prodEl && prodEl.innerHTML === '') {
    prodEl.innerHTML = FI_PRODUCTS.map((p, i) => `
      <div class="cr" style="justify-content:space-between" id="fi-row-${i}">
        <div>
          <div style="font-size:13px;font-weight:500">${p.label}</div>
          <div style="font-size:12px;color:var(--tx3)">$${p.price.toLocaleString()}</div>
        </div>
        <div class="tog" id="fi-tog-${i}" onclick="this.classList.toggle('on');finCalc()"></div>
      </div>`).join('');
  }

  const fiTotal = FI_PRODUCTS.reduce((s, p, i) =>
    s + (document.getElementById('fi-tog-' + i)?.classList.contains('on') ? p.price : 0), 0);

  const taxAmt   = sale * (tax / 100);
  const financed = Math.max(0, sale + taxAmt + fiTotal - trade + owed - down);
  const mo       = r => {
    if (r <= 0) return financed / term;
    const mr = r / 100 / 12;
    return financed * mr / (1 - Math.pow(1 + mr, -term));
  };
  const monthly  = mo(rate);

  // Payment grid
  const gridEl = document.getElementById('fin-grid');
  if (gridEl) {
    gridEl.innerHTML = [24,36,48,60,72,84].map(t => {
      const pmt = financed > 0 ? (rate > 0
        ? financed * (rate/100/12) / (1 - Math.pow(1 + rate/100/12, -t))
        : financed / t) : 0;
      return `<div onclick="document.getElementById('fin-term').value=${t};finCalc()"
                   style="background:${t==term?'var(--acc)':'var(--s2)'};border-radius:9px;padding:14px;text-align:center;cursor:pointer;transition:background .15s;border:1px solid ${t==term?'var(--acc)':'var(--b1)'}">
        <div style="font-size:11px;color:${t==term?'rgba(255,255,255,.7)':'var(--tx3)'};margin-bottom:4px">${t} mo</div>
        <div style="font-size:18px;font-weight:700;font-family:'Syne',sans-serif;color:${t==term?'#fff':'var(--tx)'}">$${Math.round(pmt).toLocaleString()}</div>
      </div>`;
    }).join('');
  }

  // Summary
  const sumEl = document.getElementById('fin-summary');
  if (sumEl) {
    const row = (l, v, bold, col) =>
      `<div style="display:flex;justify-content:space-between;padding:9px 0;border-bottom:1px solid var(--b1)">
        <span style="font-size:13px;color:var(--tx2)">${l}</span>
        <span style="font-size:13px;font-weight:${bold?700:500};color:${col||'var(--tx)'}">${v}</span>
      </div>`;
    const fmt = n => '$' + Math.round(n).toLocaleString();
    sumEl.innerHTML =
      row('Sale Price',      fmt(sale)) +
      row('Trade-in',        fmt(-trade), false, trade ? 'var(--grn)' : '') +
      row('Trade-in Owed',   fmt(owed),   false, owed  ? 'var(--red)' : '') +
      row('Down Payment',    fmt(-down),  false, 'var(--grn)') +
      row('Tax (' + tax + '%)', fmt(taxAmt)) +
      row('F&I Products',    fmt(fiTotal)) +
      `<div style="display:flex;justify-content:space-between;padding:12px 0;margin-top:4px">
        <span style="font-size:14px;font-weight:700">Amount Financed</span>
        <span style="font-size:18px;font-weight:800;font-family:'Syne',sans-serif;color:var(--acc)">${fmt(financed)}</span>
      </div>` +
      `<div style="background:var(--acc);border-radius:10px;padding:16px;text-align:center;margin-top:8px">
        <div style="font-size:12px;color:rgba(255,255,255,.7);margin-bottom:4px">${term} months @ ${rate}% APR</div>
        <div style="font-size:28px;font-weight:800;font-family:'Syne',sans-serif;color:#fff">$${Math.round(monthly).toLocaleString()}<span style="font-size:14px">/mo</span></div>
      </div>`;
  }
}

/* ── Auto-init ── */
finCalc(); // run initial calc

})();
