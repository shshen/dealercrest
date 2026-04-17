/* pages/vdp.js */
(function(){

function openVDP(carId){
  vdpCarId = carId;
  navigate('vdp');
}
function renderVDP(){
  const car = CARS.find(c=>c.id===vdpCarId) || CARS[0];
  if(!car) return '<div style="padding:40px;color:var(--tx3)">Vehicle not found.</div>';
  const photos = (car.photos||[]).filter(Boolean);
  const mainPhoto = photos[0] || '';
  const thumbs = photos.slice(1,5).map(p=>`<div style="width:80px;height:60px;border-radius:6px;background:var(--s2);border:1px solid var(--b1);overflow:hidden;flex-shrink:0"><img src="${p}" style="width:100%;height:100%;object-fit:cover" onerror="this.style.display='none'"/></div>`).join('');

  const spec=(label,val)=>val?`<div style="display:flex;justify-content:space-between;padding:7px 0;border-bottom:1px solid var(--b1);font-size:13px"><span style="color:var(--tx3)">${label}</span><span style="color:var(--tx);font-weight:500">${val}</span></div>`:'';

  const featureTags=(car.features||[]).map(f=>`<span style="background:var(--s2);border:1px solid var(--b1);color:var(--tx2);padding:4px 10px;border-radius:6px;font-size:11px">${f}</span>`).join('');

  // Payment estimate
  const price=car.price||0;
  const downEst=price*0.1;
  const financed=price-downEst;
  const r=6.9/100/12, n=60;
  const pmt=financed*r*Math.pow(1+r,n)/(Math.pow(1+r,n)-1);

  return `${bc('Inventory','inventory')}
  <div class="ph" style="display:flex;align-items:center;justify-content:space-between;flex-wrap:wrap;gap:10px">
    <div><h1>${car.year} ${car.make} ${car.model}${car.trim?' '+car.trim:''}</h1>
      <p>${car.body||''} · ${car.color||''} · ${car.mileage===0?'New':car.mileage.toLocaleString()+' miles'}</p>
    </div>
    <div style="text-align:right">
      <div style="font-size:28px;font-weight:800;font-family:'Syne',sans-serif;color:var(--acc)">$${price.toLocaleString()}</div>
      ${car.msrp&&car.msrp>price?`<div style="font-size:12px;color:var(--tx3)">MSRP $${car.msrp.toLocaleString()} · Save $${(car.msrp-price).toLocaleString()}</div>`:''}
    </div>
  </div>
  <div style="display:grid;grid-template-columns:1fr 320px;gap:16px;padding:0 20px 20px">
    <!-- LEFT -->
    <div>
      <!-- Photo gallery -->
      <div style="background:var(--s2);border:1px solid var(--b1);border-radius:12px;overflow:hidden;margin-bottom:14px">
        <div style="height:320px;display:flex;align-items:center;justify-content:center;background:var(--s1)">
          ${mainPhoto
            ? `<img src="${mainPhoto}" style="max-height:100%;max-width:100%;object-fit:contain" onerror="this.parentElement.innerHTML='<div style=color:var(--tx3);font-size:13px>No photo</div>'">`
            : `<div style="display:flex;flex-direction:column;align-items:center;gap:10px;color:var(--tx3)">${svgIcon('image',40)}<span style="font-size:13px">No photos uploaded</span></div>`}
        </div>
        ${thumbs?`<div style="display:flex;gap:8px;padding:12px;overflow-x:auto">${thumbs}</div>`:''}
      </div>
      <!-- Specs -->
      <div class="cs" style="margin-bottom:14px">
        <div class="cs-h"><div style="display:flex;align-items:center;gap:7px">${svgIcon('clip',14)}<h3>Vehicle Specifications</h3></div></div>
        <div style="padding:0 16px">
          ${spec('VIN',car.vin)}
          ${spec('Stock #',car.stock)}
          ${spec('Condition',car.condition)}
          ${spec('Exterior Color',car.color)}
          ${spec('Interior Color',car.interior)}
          ${spec('Engine',car.engine)}
          ${spec('Transmission',car.transmission)}
          ${spec('Drivetrain',car.drive)}
          ${spec('Fuel Type',car.fuel)}
          ${car.mpgCity?spec('Fuel Economy',car.mpgCity+' city / '+car.mpgHwy+' hwy mpg'):''}
          ${spec('Doors',car.doors)}
          ${spec('Seats',car.seats)}
        </div>
      </div>
      <!-- Features -->
      ${featureTags?`<div class="cs" style="margin-bottom:14px">
        <div class="cs-h"><div style="display:flex;align-items:center;gap:7px">${svgIcon('star',14)}<h3>Features & Options</h3></div></div>
        <div style="padding:14px;display:flex;flex-wrap:wrap;gap:6px">${featureTags}</div>
      </div>`:''}
      <!-- Description -->
      ${car.desc?`<div class="cs">
        <div class="cs-h"><div style="display:flex;align-items:center;gap:7px">${svgIcon('type',14)}<h3>Description</h3></div></div>
        <div style="padding:16px;font-size:13px;color:var(--tx2);line-height:1.8">${car.desc}</div>
      </div>`:''}
    </div>
    <!-- RIGHT sidebar -->
    <div>
      <!-- Payment estimator -->
      <div class="cs" style="margin-bottom:14px">
        <div class="cs-h"><div style="display:flex;align-items:center;gap:7px">${svgIcon('dollar',14)}<h3>Est. Payment</h3></div></div>
        <div style="padding:16px;text-align:center">
          <div style="font-size:36px;font-weight:800;font-family:'Syne',sans-serif;color:var(--acc)">$${Math.round(pmt).toLocaleString()}<span style="font-size:14px;font-weight:400;color:var(--tx3)">/mo</span></div>
          <div style="font-size:11px;color:var(--tx3);margin-bottom:14px">60 mo @ 6.9% APR, 10% down — est only</div>
          ${btn('Full Payment Calculator','btn btn-p btn-sm','style="width:100%;margin-bottom:8px" onclick="navigate(\'finance\')"')}
          ${btn('Apply for Financing','btn btn-s btn-sm','style="width:100%" onclick="showToast(\'Credit application opened\')"')}
        </div>
      </div>
      <!-- Lead capture -->
      <div class="cs" style="margin-bottom:14px">
        <div class="cs-h"><div style="display:flex;align-items:center;gap:7px">${svgIcon('mail',14)}<h3>Interested?</h3></div></div>
        <div style="padding:14px;display:flex;flex-direction:column;gap:8px">
          <input class="inp" placeholder="Your name" id="vdp-name"/>
          <input class="inp" placeholder="Email address" id="vdp-email" type="email"/>
          <input class="inp" placeholder="Phone number" id="vdp-phone" type="tel"/>
          <select class="inp"><option>I want to test drive this vehicle</option><option>Send me more info</option><option>I have a trade-in</option><option>Check availability</option></select>
          ${btn('Send Inquiry','btn btn-p btn-sm','style="width:100%" onclick="vdpSendLead()"')}
        </div>
      </div>
      <!-- Trade-in CTA -->
      <div class="cs" style="margin-bottom:14px">
        <div class="cs-h"><div style="display:flex;align-items:center;gap:7px">${svgIcon('repeat',14)}<h3>Trade-In Value</h3></div></div>
        <div style="padding:14px;text-align:center">
          <div style="font-size:13px;color:var(--tx2);margin-bottom:10px">Get an instant estimate for your current vehicle</div>
          ${btn('Get Trade-In Offer','btn btn-s btn-sm','style="width:100%" onclick="showToast(\'Trade-in valuation tool opening…\')"')}
        </div>
      </div>
      <!-- Carfax -->
      <div class="cs">
        <div class="cs-h"><div style="display:flex;align-items:center;gap:7px">${svgIcon('report',14)}<h3>Vehicle History</h3></div></div>
        <div style="padding:14px">
          <div style="display:flex;align-items:center;gap:10px;background:var(--s2);border:1px solid var(--b1);border-radius:8px;padding:12px">
            <div style="width:36px;height:36px;background:#d62b27;border-radius:6px;display:flex;align-items:center;justify-content:center;color:#fff;font-size:9px;font-weight:800;flex-shrink:0">CARFAX</div>
            <div style="flex:1">
              <div style="font-size:12px;font-weight:600;color:var(--tx)">1-Owner Vehicle</div>
              <div style="font-size:11px;color:var(--grn)">No accidents reported</div>
            </div>
            ${btn('View Report','btn btn-s btn-sm','onclick="showToast(\'Carfax report loading…\')"')}
          </div>
        </div>
      </div>
    </div>
  </div>`;
}
function vdpSendLead(){
  const name=document.getElementById('vdp-name')?.value;
  if(!name){ showToast('Please enter your name','var(--red)'); return; }
  showToast('Inquiry sent! We\'ll be in touch soon.');
}

/* ══════════════════════════════════════════
   LEAD DETAIL
══════════════════════════════════════════ */
let currentLeadId = null;

/* ── Auto-init ── */
initVDP();

})();

/* ── DOM init ── */
function initVDP() {
  const carId = window._vdpCarId;
  const car   = (window.CARS || []).find(c => c.id === carId);
  const el    = document.getElementById('vdp-content');
  if (!el) return;
  if (!car) {
    el.innerHTML = `<div style="padding:60px;text-align:center;color:var(--tx3)">Vehicle not found. <button class="btn btn-s btn-sm" onclick="navigate('inventory')">Back to Inventory</button></div>`;
    return;
  }
  const monthly = Math.round(car.price * 0.9 * (6.9/100/12) / (1 - Math.pow(1+6.9/100/12,-60)));
  el.innerHTML = `
    <div class="bcd"><span onclick="navigate('inventory')" style="cursor:pointer">Inventory</span> / <span>${car.year} ${car.make} ${car.model}</span></div>
    <div class="ph" style="display:flex;align-items:center;justify-content:space-between;flex-wrap:wrap;gap:10px">
      <div>
        <h1>${car.year} ${car.make} ${car.model}</h1>
        <p>${car.color} · ${car.mileage === 0 ? 'New' : car.mileage.toLocaleString() + ' mi'} · ${car.vin}</p>
      </div>
      <div style="text-align:right">
        <div style="font-size:28px;font-weight:800;font-family:'Syne',sans-serif;color:var(--acc)">$${car.price.toLocaleString()}</div>
        <div style="font-size:13px;color:var(--tx3)">Est. $${monthly}/mo · 60mo @ 6.9% APR</div>
      </div>
    </div>
    <div style="display:grid;grid-template-columns:1fr 340px;gap:16px;padding:0 20px 20px">
      <div>
        <!-- Photo placeholder -->
        <div style="background:var(--s2);border-radius:12px;height:260px;display:flex;align-items:center;justify-content:center;font-size:64px;margin-bottom:16px;border:1px solid var(--b1)">🚗</div>
        <!-- Specs -->
        <div class="cs" style="margin-bottom:14px">
          <div class="cs-h"><h3>Vehicle Specs</h3></div>
          <div style="display:grid;grid-template-columns:1fr 1fr 1fr;gap:0">
            ${[
              ['Make', car.make], ['Model', car.model], ['Year', car.year],
              ['Color', car.color], ['Mileage', car.mileage===0?'New':car.mileage.toLocaleString()+' mi'], ['Status', car.status],
              ['VIN', car.vin], ['Stock', car.stock||'—'], ['Condition', car.condition||'—'],
            ].map(([l,v])=>`<div style="padding:12px 16px;border-bottom:1px solid var(--b1)"><div style="font-size:11px;color:var(--tx3);margin-bottom:2px">${l}</div><div style="font-size:13px;font-weight:500">${v}</div></div>`).join('')}
          </div>
        </div>
        ${car.features?.length ? `
        <div class="cs">
          <div class="cs-h"><h3>Features & Options</h3></div>
          <div style="padding:14px;display:flex;flex-wrap:wrap;gap:8px">
            ${car.features.map(f=>`<span class="bdg">${f}</span>`).join('')}
          </div>
        </div>` : ''}
      </div>
      <!-- Sidebar -->
      <div>
        <div class="cs" style="margin-bottom:14px">
          <div class="cs-h"><h3>Request Info</h3></div>
          <div style="padding:16px;display:flex;flex-direction:column;gap:10px">
            <input class="inp" id="vdp-name" placeholder="Your name"/>
            <input class="inp" id="vdp-email" type="email" placeholder="Email address"/>
            <input class="inp" id="vdp-phone" placeholder="Phone number"/>
            <textarea class="inp" rows="3" placeholder="Questions about this vehicle…" style="resize:none"></textarea>
            <button class="btn btn-p" onclick="vdpSubmitLead(${car.id})">Send Request</button>
          </div>
        </div>
        <div class="cs">
          <div class="cs-h"><h3>Payment Estimator</h3></div>
          <div style="padding:16px;text-align:center">
            <div style="font-size:13px;color:var(--tx3);margin-bottom:8px">60 months @ 6.9% APR · 10% down</div>
            <div style="font-size:32px;font-weight:800;font-family:'Syne',sans-serif;color:var(--acc)">$${monthly}<span style="font-size:16px">/mo</span></div>
            <button class="btn btn-p btn-sm" style="margin-top:12px;width:100%" onclick="navigate('finance')">Open Deal Worksheet</button>
          </div>
        </div>
      </div>
    </div>`;

  window.vdpSubmitLead = function(carId) {
    const name = document.getElementById('vdp-name')?.value?.trim();
    if (!name) { showToast('Please enter your name', 'var(--red)'); return; }
    showToast('Request sent! We\'ll be in touch soon.');
  };
}
