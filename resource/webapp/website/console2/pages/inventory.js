/* pages/inventory.js — wires up the inventory table after inventory.html loads */

(function () {

  let currentFilter = 'All';

  // ── Populate KPI stats ──
  function updateStats() {
    const cars = window.CARS || [];
    document.getElementById('inv-stat-total').textContent = cars.length;
    document.getElementById('inv-stat-avail').textContent = cars.filter(c => c.status === 'Available').length;
    document.getElementById('inv-stat-res').textContent   = cars.filter(c => c.status === 'Reserved').length;
    document.getElementById('inv-stat-sold').textContent  = cars.filter(c => c.status === 'Sold').length;
  }

  // ── Build table rows ──
  function buildRows() {
    const cars   = window.CARS || [];
    const q      = (document.getElementById('inv-search')?.value || '').toLowerCase();
    const badge  = { Available: 'bg-grn', Reserved: 'bg-ora', Sold: 'bg-gry' };

    const filtered = cars.filter(c => {
      const matchFilter = currentFilter === 'All' || c.status === currentFilter;
      const matchSearch = !q || (c.make + ' ' + c.model + ' ' + c.year + ' ' + c.vin).toLowerCase().includes(q);
      return matchFilter && matchSearch;
    });

    const tbody = document.getElementById('inv-tbody');
    const empty = document.getElementById('inv-empty');
    if (!tbody) return;

    if (filtered.length === 0) {
      tbody.innerHTML = '';
      if (empty) empty.style.display = '';
      return;
    }
    if (empty) empty.style.display = 'none';

    tbody.innerHTML = filtered.map(car => `
      <tr>
        <td data-label="Vehicle">
          <div class="td-row">
            <div class="car-th">${svgIcon('car', 15)}</div>
            <div>
              <div style="font-weight:500">${car.make} ${car.model}</div>
              <div style="font-size:11px;color:var(--tx3);font-family:monospace">${car.vin}</div>
            </div>
          </div>
        </td>
        <td data-label="Year">${car.year}</td>
        <td data-label="Price" style="font-family:'Syne',sans-serif;font-weight:600">$${car.price.toLocaleString()}</td>
        <td data-label="Mileage">${car.mileage === 0 ? '<span style="color:var(--grn);font-weight:500">New</span>' : car.mileage.toLocaleString() + ' mi'}</td>
        <td data-label="Color">${car.color}</td>
        <td data-label="Status"><span class="bdg ${badge[car.status] || ''}">${car.status}</span></td>
        <td data-label="Actions">
          <div class="ab">
            <button class="btn btn-s btn-sm" onclick="editCar(${car.id})">${svgIcon('edit', 12)}</button>
            <button class="btn btn-s btn-sm" onclick="viewVDP(${car.id})">${svgIcon('eye', 12)}</button>
            <button class="btn btn-d btn-sm" onclick="deleteCar(${car.id})">${svgIcon('trash', 12)}</button>
          </div>
        </td>
      </tr>`).join('');
  }

  // ── Filter pill click ──
  window.setInvFilter = function (f) {
    currentFilter = f;
    document.querySelectorAll('#inv-filters .pill').forEach(el => {
      el.classList.toggle('on', el.dataset.filter === f);
    });
    buildRows();
    updateStats();
  };

  // ── Search input ──
  window.filterInventory = function () { buildRows(); };

  // ── Delete car ──
  window.deleteCar = function (id) {
    if (!confirm('Remove this vehicle from inventory?')) return;
    window.CARS = (window.CARS || []).filter(c => c.id !== id);
    buildRows();
    updateStats();
    showToast('Vehicle removed');
  };

  // ── Navigate to VDP ──
  window.viewVDP = function (id) {
    window._vdpCarId = id;
    navigate('vdp');
  };

  // ── Navigate to vehicle editor (edit) ──
  window.editCar = function (id) {
    window._editCarId = id;
    navigate('vehicle-editor');
  };

  // ── Navigate to vehicle editor (new) ──
  window.initNewCar = function () {
    window._editCarId = null;
    navigate('vehicle-editor');
  };

  // ── Init ──
  updateStats();
  buildRows();

})();
