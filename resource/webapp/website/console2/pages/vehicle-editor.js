/* pages/vehicle-editor.js */
(function(){

function renderVehicleEditor(){
  const car = veInitData || {};
  const isNew = !veCarId;
  const title = isNew ? 'Add Vehicle' : (car.year+' '+car.make+' '+car.model);

  function sel(id,val,opts){
    return '<select class="ve-select" id="'+id+'">'+opts.map(function(o){
      return '<option'+(o===String(val)?' selected':'')+'>'+o+'</option>';
    }).join('')+'</select>';
  }
  function inp(id,val,ph,type){
    return '<input class="ve-input" id="'+id+'" type="'+(type||'text')+'" value="'+(val||'')+'" placeholder="'+(ph||'')+'"/>';
  }
  function field(label,content){
    return '<div class="ve-field"><label class="ve-label">'+label+'</label>'+content+'</div>';
  }

  const quickChips = ['Sunroof','Navigation','Heated Seats','Backup Camera',
    'Bluetooth','Apple CarPlay','Android Auto','Leather Seats',
    'Third Row','Tow Package','Remote Start','Keyless Entry'].map(function(f){
    return '<span style="background:var(--b1);border:1px solid var(--b2);border-radius:4px;padding:3px 9px;font-size:10px;color:var(--tx3);cursor:pointer" onclick="veQuickAdd(\''+f+'\')" >+ '+f+'</span>';
  }).join('');

  const tagsHtml = veTags.map(function(t,i){
    return '<span class="ve-tag">'+t+'<span class="ve-tag-x" onclick="veRemoveTag('+i+')">×</span></span>';
  }).join('');

  const galleryHtml = vePhotos.map(function(url,i){
    const filled = !!url;
    return '<div class="ve-media-slot'+(filled?' filled':'')+(i===0&&filled?' primary':'')+'" onclick="veSetPhoto('+i+')" style="cursor:pointer">'
      +(filled
        ? '<div style="position:absolute;inset:0;display:flex;align-items:center;justify-content:center;font-size:28px;background:var(--s3)">&#x1F5BC;</div>'
          +(i===0?'<div class="ve-slot-badge">Primary</div>':'')
          +'<div class="ve-slot-actions"><button style="background:var(--red);border:none;color:#fff;font-size:9px;padding:2px 5px;border-radius:3px;cursor:pointer" onclick="event.stopPropagation();veRemovePhoto('+i+')">✕</button></div>'
        : '<div class="ve-slot-emoji">'+(i===0?'&#x1F4F7;':'&#x2795;')+'</div>'
          +'<div class="ve-slot-label">'+(i===0?'Primary<br>Photo':'Photo '+(i+1))+'</div>')
      +'</div>';
  }).join('');

  const videoLabel = veVideoUrl ? veVideoUrl.slice(0,34)+'…' : 'Add Video URL';

  return bc('Inventory','vehicle-editor')
    +'<div class="ph" style="display:flex;align-items:center;justify-content:space-between;flex-wrap:wrap;gap:10px">'
    +'<div><h1>'+title+'</h1><p>'+(isNew?'Fill in the details below to add a new vehicle':'Edit vehicle details and save')+'</p></div>'
    +'<div style="display:flex;gap:8px;align-items:center">'
    +sel('ve-status',car.status||'Available',['Available','Reserved','Sold'])
    +'<button class="btn btn-s btn-sm" onclick="closeVehicleEditor()">Discard</button>'
    +'<button class="btn btn-p btn-sm" onclick="saveVehicleEditor()">'
    +'<svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" style="margin-right:4px"><path d="M19 21H5a2 2 0 01-2-2V5a2 2 0 012-2h11l5 5v11a2 2 0 01-2 2z"/><polyline points="17 21 17 13 7 13 7 21"/><polyline points="7 3 7 8 15 8"/></svg>'
    +'Save Vehicle</button>'
    +'</div></div>'
    +'<div class="ve-body">'

    // LEFT
    +'<div class="ve-main">'
    +'<div class="ve-card"><div class="ve-card-title">Basic Information</div>'
    +'<div class="ve-grid3" style="margin-bottom:12px">'
    +field('Year',inp('ve-year',car.year,'2024','number'))
    +field('Make',inp('ve-make',car.make,'e.g. Toyota'))
    +field('Model',inp('ve-model',car.model,'e.g. Camry'))
    +'</div><div class="ve-grid2" style="margin-bottom:12px">'
    +field('Trim',inp('ve-trim',car.trim,'e.g. XSE V6'))
    +field('Body Style',sel('ve-body-style',car.body||'Sedan',['Sedan','SUV','Truck','Coupe','Convertible','Van','Wagon','Hatchback']))
    +field('Exterior Color',inp('ve-color',car.color,'e.g. Midnight Blue'))
    +field('Interior Color',inp('ve-interior',car.interior,'e.g. Black Leather'))
    +'</div>'
    +field('VIN','<input class="ve-input" id="ve-vin" value="'+(car.vin||'')+'" placeholder="17-character VIN" style="font-family:monospace;letter-spacing:.05em"/>')
    +'</div>'

    +'<div class="ve-card"><div class="ve-card-title">Pricing &amp; Odometer</div>'
    +'<div class="ve-grid3">'
    +field('Asking Price ($)',inp('ve-price',car.price,'0','number'))
    +field('MSRP ($)',inp('ve-msrp',car.msrp,'0','number'))
    +field('Mileage',inp('ve-mileage',car.mileage,'0 = New','number'))
    +'</div></div>'

    +'<div class="ve-card"><div class="ve-card-title">Powertrain &amp; Specs</div>'
    +'<div class="ve-grid2">'
    +field('Engine',inp('ve-engine',car.engine,'e.g. 3.5L V6'))
    +field('Transmission',sel('ve-transmission',car.transmission||'Automatic',['Automatic','Manual','CVT','Dual-Clutch','Electric']))
    +field('Drivetrain',sel('ve-drive',car.drive||'FWD',['FWD','RWD','AWD','4WD']))
    +field('Fuel Type',sel('ve-fuel',car.fuel||'Gasoline',['Gasoline','Diesel','Hybrid','Plug-in Hybrid','Electric']))
    +field('MPG City',inp('ve-mpg-city',car.mpgCity,'0','number'))
    +field('MPG Highway',inp('ve-mpg-hwy',car.mpgHwy,'0','number'))
    +'</div></div>'

    +'<div class="ve-card"><div class="ve-card-title">Description</div>'
    +'<textarea class="ve-textarea" id="ve-desc" rows="5" placeholder="Describe the vehicle...">'+(car.desc||'')+'</textarea>'
    +'</div>'

    +'<div class="ve-card"><div class="ve-card-title">Features &amp; Options</div>'
    +'<div id="ve-tags-wrap" class="ve-tags-wrap">'+tagsHtml+'</div>'
    +'<div style="display:flex;gap:8px">'
    +'<input class="ve-input" id="ve-tag-input" placeholder="e.g. Sunroof, Heated Seats..." style="flex:1" onkeydown="if(event.key===\'Enter\'){event.preventDefault();veAddTag()}"/>'
    +'<button class="btn btn-s btn-sm" onclick="veAddTag()">Add</button>'
    +'</div>'
    +'<div style="margin-top:10px;display:flex;flex-wrap:wrap;gap:5px">'+quickChips+'</div>'
    +'</div>'
    +'</div>' // end ve-main

    // RIGHT
    +'<div class="ve-aside">'
    +'<div><div class="ve-card-title">Photos</div>'
    +'<div class="ve-gallery" id="ve-gallery">'+galleryHtml+'</div>'
    +'<div style="margin-top:8px;font-size:10px;color:var(--tx3)">Click a slot to set photo URL. First = primary.</div>'
    +'</div>'
    +'<div class="ve-divider"></div>'
    +'<div><div class="ve-card-title">Video</div>'
    +'<div class="ve-video-slot" onclick="veSetVideo()" style="cursor:pointer">'
    +'<div class="ve-video-slot-icon"><svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="var(--acc)" stroke-width="2" stroke-linecap="round"><polygon points="5 3 19 12 5 21 5 3"/></svg></div>'
    +'<div><div style="font-size:12px;font-weight:600;color:var(--tx)" id="ve-video-label">'+videoLabel+'</div>'
    +'<div style="font-size:10px;color:var(--tx3)">YouTube, Vimeo, or direct link</div>'
    +'</div></div></div>'
    +'<div class="ve-divider"></div>'
    +'<div><div class="ve-card-title">Condition</div>'
    +sel('ve-condition',car.condition||'New',['New','Certified Pre-Owned','Excellent','Good','Fair'])
    +'</div>'
    +'<div><div class="ve-card-title">Stock Number</div>'
    +inp('ve-stock',car.stock,'e.g. STK-0042')
    +'</div>'
    +'<div><div class="ve-card-title">Capacity</div>'
    +'<div class="ve-grid2">'
    +field('Doors',sel('ve-doors',String(car.doors||4),['2','3','4','5']))
    +field('Seats',sel('ve-seats',String(car.seats||5),['2','4','5','6','7','8']))
    +'</div></div>'
    +'<div class="ve-divider"></div>'
    +'<div><div class="ve-card-title">Internal Notes</div>'
    +'<textarea class="ve-textarea" id="ve-notes" rows="3" placeholder="Private notes for staff only...">'+(car.notes||'')+'</textarea>'
    +'</div>'
    +'</div>' // end ve-aside
    +'</div>'; // end ve-body
}
function saveVehicleEditor(){
  const gv=id=>{ const el=document.getElementById(id); return el?el.value.trim():''; };
  const gsel=id=>{ const el=document.getElementById(id); return el?el.options[el.selectedIndex].text:''; };
  const car = {
    id:        veCarId || Date.now(),
    make:      gv('ve-make'),
    model:     gv('ve-model'),
    year:      +gv('ve-year') || new Date().getFullYear(),
    trim:      gv('ve-trim'),
    body:      gsel('ve-body'),
    color:     gv('ve-color'),
    interior:  gv('ve-interior'),
    vin:       gv('ve-vin'),
    price:     +gv('ve-price') || 0,
    msrp:      +gv('ve-msrp') || 0,
    mileage:   +gv('ve-mileage') || 0,
    engine:    gv('ve-engine'),
    transmission: gsel('ve-transmission'),
    drive:     gsel('ve-drive'),
    fuel:      gsel('ve-fuel'),
    mpgCity:   +gv('ve-mpg-city') || 0,
    mpgHwy:    +gv('ve-mpg-hwy') || 0,
    desc:      gv('ve-desc'),
    condition: gsel('ve-condition'),
    stock:     gv('ve-stock'),
    doors:     +gsel('ve-doors') || 4,
    seats:     +gsel('ve-seats') || 5,
    notes:     gv('ve-notes'),
    status:    gsel('ve-status'),
    photos:    [...vePhotos],
    videoUrl:  veVideoUrl,
    features:  [...veTags],
  };
  if(veCarId){
    const idx = CARS.findIndex(c=>c.id===veCarId);
    if(idx>=0) CARS[idx]=car;
  } else {
    CARS.push(car);
  }
  closeVehicleEditor();
  showToast(veCarId?'Vehicle updated':'Vehicle added');
}



/* ══════════════════════════════════════════
   FINANCE & DESKING
══════════════════════════════════════════ */
let FIN = {
  salePrice:45000, msrp:47500, tradeIn:0, tradeOwed:0,
  down:5000, rate:6.9, term:60, taxRate:7.25,
  gapIns:895, extWarranty:2200, paintProtect:799,
  leadId:null, carId:null
};

/* ── Auto-init ── */
initVehicleEditor();

})();

/* ── DOM init ── */
function initVehicleEditor() {
  const id  = window._editCarId;
  const car = id ? (window.CARS||[]).find(c=>c.id===id) : null;
  const el  = document.getElementById('vehicle-editor-content');
  if (!el) return;

  el.innerHTML = `
    <div class="bcd"><span onclick="navigate('inventory')" style="cursor:pointer">Inventory</span> / <span>${car?car.year+' '+car.make+' '+car.model:'New Vehicle'}</span></div>
    <div class="ph" style="display:flex;align-items:center;justify-content:space-between;flex-wrap:wrap;gap:10px">
      <div><h1>${car?'Edit Vehicle':'Add Vehicle'}</h1><p>${car?'Update vehicle details and save':'Fill in details to add a new vehicle'}</p></div>
      <div style="display:flex;gap:8px">
        <button class="btn btn-s btn-sm" onclick="navigate('inventory')">Discard</button>
        <button class="btn btn-p btn-sm" onclick="saveVehicle()">Save Vehicle</button>
      </div>
    </div>
    <div style="display:grid;grid-template-columns:1fr 280px;gap:16px;padding:0 20px 20px">
      <div>
        <div class="cs" style="margin-bottom:14px">
          <div class="cs-h"><h3>Basic Information</h3></div>
          <div style="padding:16px;display:grid;grid-template-columns:1fr 1fr 1fr;gap:12px">
            ${[['Year','ve-year',car?.year||new Date().getFullYear(),'number'],['Make','ve-make',car?.make||'','text'],['Model','ve-model',car?.model||'','text']].map(([l,id,v,t])=>`<div><label style="font-size:11px;color:var(--tx3);display:block;margin-bottom:5px">${l}</label><input class="inp" id="${id}" type="${t}" value="${v}"/></div>`).join('')}
            ${[['Color','ve-color',car?.color||''],['VIN','ve-vin',car?.vin||'']].map(([l,id,v])=>`<div style="grid-column:span 1"><label style="font-size:11px;color:var(--tx3);display:block;margin-bottom:5px">${l}</label><input class="inp" id="${id}" value="${v}"/></div>`).join('')}
          </div>
        </div>
        <div class="cs" style="margin-bottom:14px">
          <div class="cs-h"><h3>Pricing</h3></div>
          <div style="padding:16px;display:grid;grid-template-columns:1fr 1fr 1fr;gap:12px">
            ${[['Price ($)','ve-price',car?.price||0],['MSRP ($)','ve-msrp',car?.msrp||0],['Mileage','ve-mileage',car?.mileage||0]].map(([l,id,v])=>`<div><label style="font-size:11px;color:var(--tx3);display:block;margin-bottom:5px">${l}</label><input class="inp" id="${id}" type="number" value="${v}"/></div>`).join('')}
          </div>
        </div>
        <div class="cs">
          <div class="cs-h"><h3>Description</h3></div>
          <div style="padding:16px"><textarea class="inp" id="ve-desc" rows="4" placeholder="Describe this vehicle…" style="resize:none">${car?.desc||''}</textarea></div>
        </div>
      </div>
      <div>
        <div class="cs" style="margin-bottom:14px">
          <div class="cs-h"><h3>Status</h3></div>
          <div style="padding:16px">
            <select class="inp" id="ve-status">
              ${['Available','Reserved','Sold'].map(s=>`<option${car?.status===s?' selected':''}>${s}</option>`).join('')}
            </select>
          </div>
        </div>
        <div class="cs">
          <div class="cs-h"><h3>Internal Notes</h3></div>
          <div style="padding:16px"><textarea class="inp" id="ve-notes" rows="4" placeholder="Staff-only notes…" style="resize:none">${car?.notes||''}</textarea></div>
        </div>
      </div>
    </div>`;

  window.saveVehicle = function() {
    const g = id => document.getElementById(id)?.value?.trim() || '';
    const updated = {
      id:     car?.id || Date.now(),
      make:   g('ve-make'), model:   g('ve-model'),
      year:   +g('ve-year') || new Date().getFullYear(),
      color:  g('ve-color'), vin:    g('ve-vin'),
      price:  +g('ve-price')||0, msrp: +g('ve-msrp')||0,
      mileage:+g('ve-mileage')||0, desc:  g('ve-desc'),
      notes:  g('ve-notes'),
      status: document.getElementById('ve-status')?.value || 'Available',
      photos:[], features:[], channels:['chat'],
    };
    if (car) {
      const idx = (window.CARS||[]).findIndex(c=>c.id===car.id);
      if (idx>=0) window.CARS[idx] = updated;
    } else {
      (window.CARS = window.CARS||[]).push(updated);
    }
    navigate('inventory');
    showToast(car ? 'Vehicle updated' : 'Vehicle added');
  };
}
