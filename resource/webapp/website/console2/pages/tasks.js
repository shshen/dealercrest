/* pages/tasks.js — populates the tasks page after tasks.html loads */

(function () {

  let activeFilter = 'Today';
  let activeCat    = 'All';

  const PRIO_COLOR = { High: 'var(--red)', Medium: 'var(--ora)', Low: 'var(--tx3)' };
  const CAT_COLOR  = { 'Lead Follow-up': 'var(--acc)', Service: 'var(--blu)', Marketing: 'var(--pur)', Admin: 'var(--tx3)' };

  // ── Build one task row as HTML string ──
  function taskRow(t) {
    const lead = t.leadId ? (window.LEADS_DATA || []).find(l => l.id === t.leadId) : null;
    const pc   = PRIO_COLOR[t.priority] || 'var(--tx3)';
    const cc   = CAT_COLOR[t.category]  || 'var(--tx3)';
    const chk  = `<svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="3.5"><polyline points="20 6 9 17 4 12"/></svg>`;
    return `
      <div class="task-row${t.done ? ' done' : ''}" id="task-row-${t.id}">
        <div class="task-checkbox${t.done ? ' checked' : ''}" onclick="toggleTask(${t.id})">
          ${t.done ? chk : ''}
        </div>
        <div style="flex:1;min-width:0">
          <div style="font-size:13px;font-weight:500;color:${t.done ? 'var(--tx3)' : 'var(--tx)'};text-decoration:${t.done ? 'line-through' : 'none'};margin-bottom:5px">
            ${t.text}
          </div>
          <div style="display:flex;align-items:center;gap:8px;flex-wrap:wrap">
            <span style="font-size:10px;padding:2px 7px;border-radius:4px;background:${pc}22;color:${pc};font-weight:600">${t.priority}</span>
            <span style="font-size:10px;padding:2px 7px;border-radius:4px;background:${cc}22;color:${cc}">${t.category}</span>
            ${lead ? `<span style="font-size:10px;color:var(--acc);cursor:pointer" onclick="navigate('lead-detail');window._leadDetailId=${lead.id}">&nearr; ${lead.name}</span>` : ''}
            <span style="font-size:10px;color:var(--tx3)">${svgIcon('leads', 9)} ${t.assignee}</span>
          </div>
        </div>
        <div style="display:flex;flex-direction:column;align-items:flex-end;gap:6px;flex-shrink:0">
          <span style="font-size:11px;font-weight:600;color:${t.dueTs < 0 ? 'var(--red)' : t.dueTs === 0 ? 'var(--ora)' : 'var(--tx3)'}">
            ${t.dueTs < 0 ? '⚠ OVERDUE' : t.due}
          </span>
          <div style="display:flex;gap:5px">
            <button class="btn btn-s btn-sm" onclick="openEditTaskModal(${t.id})">${svgIcon('edit', 11)}</button>
            <button class="btn btn-d btn-sm" onclick="removeTask(${t.id})">${svgIcon('trash', 11)}</button>
          </div>
        </div>
      </div>`;
  }

  // ── Apply filters and render all sections ──
  function render() {
    const tasks = window.TASKS_DATA || [];

    // Stats (always off full dataset)
    document.getElementById('task-stat-today').textContent    = tasks.filter(t => !t.done && t.dueTs === 0).length;
    document.getElementById('task-stat-overdue').textContent  = tasks.filter(t => !t.done && t.dueTs < 0).length;
    document.getElementById('task-stat-upcoming').textContent = tasks.filter(t => !t.done && t.dueTs > 0).length;
    document.getElementById('task-stat-done').textContent     = tasks.filter(t => t.done).length;

    // Apply main filter
    let visible = tasks;
    if (activeFilter === 'Today')    visible = tasks.filter(t => t.dueTs <= 0);
    if (activeFilter === 'My Tasks') visible = tasks.filter(t => t.assignee === 'Alex Thompson');
    if (activeFilter === 'Done')     visible = tasks.filter(t => t.done);

    // Apply category filter
    if (activeCat !== 'All') visible = visible.filter(t => t.category === activeCat);

    const overdue  = visible.filter(t => !t.done && t.dueTs < 0);
    const today    = visible.filter(t => !t.done && t.dueTs === 0);
    const upcoming = visible.filter(t => !t.done && t.dueTs > 0);
    const done     = visible.filter(t => t.done);

    function showSection(sectionId, listId, countId, items) {
      const sec   = document.getElementById(sectionId);
      const list  = document.getElementById(listId);
      const count = document.getElementById(countId);
      if (!sec || !list) return;
      if (items.length === 0) { sec.style.display = 'none'; return; }
      sec.style.display  = '';
      list.innerHTML     = items.map(taskRow).join('');
      if (count) count.textContent = items.length;
    }

    showSection('section-overdue',  'list-overdue',  'count-overdue',  overdue);
    showSection('section-today',    'list-today',    'count-today',    today);
    showSection('section-upcoming', 'list-upcoming', 'count-upcoming', upcoming);
    showSection('section-done',     'list-done',     'count-done',     done);

    const empty = document.getElementById('tasks-empty');
    const anyVisible = overdue.length + today.length + upcoming.length + done.length > 0;
    if (empty) empty.style.display = anyVisible ? 'none' : '';
  }

  // ── Toggle task done ──
  window.toggleTask = function (id) {
    const t = (window.TASKS_DATA || []).find(x => x.id === id);
    if (!t) return;
    t.done = !t.done;
    render();
    showToast(t.done ? 'Task completed ✓' : 'Task reopened');
  };

  // ── Delete task ──
  window.removeTask = function (id) {
    window.TASKS_DATA = (window.TASKS_DATA || []).filter(x => x.id !== id);
    render();
    showToast('Task deleted');
  };

  // ── Filter tab click ──
  window.setTaskFilter = function (f) {
    activeFilter = f;
    document.querySelectorAll('[data-filter]').forEach(el => {
      el.classList.toggle('on', el.dataset.filter === f);
    });
    render();
  };

  // ── Category pill click ──
  window.setTaskCat = function (cat) {
    activeCat = cat;
    document.querySelectorAll('[data-cat]').forEach(el => {
      el.classList.toggle('on', el.dataset.cat === cat);
    });
    render();
  };

  // ── Add task modal ──
  window.openAddTaskModal = function () {
    const leads     = [{ id: '', name: '— None —' }, ...(window.LEADS_DATA || [])];
    const assignees = ['Alex Thompson', 'David Kim', 'Maria Garcia', 'Lisa Chen'];
    openModal('New Task', `
      <div style="display:flex;flex-direction:column;gap:10px">
        <div>
          <label style="font-size:11px;color:var(--tx3);display:block;margin-bottom:5px">Task Description</label>
          <input class="inp" id="nt-text" placeholder="What needs to be done?"/>
        </div>
        <div style="display:flex;gap:10px">
          <div style="flex:1">
            <label style="font-size:11px;color:var(--tx3);display:block;margin-bottom:5px">Priority</label>
            <select class="inp" id="nt-prio"><option>High</option><option selected>Medium</option><option>Low</option></select>
          </div>
          <div style="flex:1">
            <label style="font-size:11px;color:var(--tx3);display:block;margin-bottom:5px">Category</label>
            <select class="inp" id="nt-cat">
              <option>Lead Follow-up</option><option>Service</option><option>Marketing</option><option>Admin</option>
            </select>
          </div>
        </div>
        <div style="display:flex;gap:10px">
          <div style="flex:1">
            <label style="font-size:11px;color:var(--tx3);display:block;margin-bottom:5px">Due</label>
            <select class="inp" id="nt-due">
              <option>Today</option><option>Tomorrow</option><option>Friday</option><option>Next Week</option>
            </select>
          </div>
          <div style="flex:1">
            <label style="font-size:11px;color:var(--tx3);display:block;margin-bottom:5px">Assign To</label>
            <select class="inp" id="nt-assignee">
              ${assignees.map(a => `<option>${a}</option>`).join('')}
            </select>
          </div>
        </div>
        <div>
          <label style="font-size:11px;color:var(--tx3);display:block;margin-bottom:5px">Link to Lead (optional)</label>
          <select class="inp" id="nt-lead">
            ${leads.map(l => `<option value="${l.id}">${l.name}</option>`).join('')}
          </select>
        </div>
      </div>`,
      () => {
        const text = document.getElementById('nt-text')?.value?.trim();
        if (!text) { showToast('Please enter a task description', 'var(--red)'); return; }
        const due  = document.getElementById('nt-due')?.value || 'Today';
        const dueTs = { Today: 0, Tomorrow: 1, Friday: 4, 'Next Week': 7 }[due] ?? 0;
        const leadVal = document.getElementById('nt-lead')?.value;
        window.TASKS_DATA = window.TASKS_DATA || [];
        window.TASKS_DATA.push({
          id:       Date.now(),
          text,
          priority: document.getElementById('nt-prio')?.value || 'Medium',
          category: document.getElementById('nt-cat')?.value  || 'Admin',
          due, dueTs,
          assignee: document.getElementById('nt-assignee')?.value || 'Alex Thompson',
          leadId:   leadVal ? parseInt(leadVal) : null,
          done:     false,
        });
        closeModal();
        render();
        showToast('Task added');
      }, 'Add Task');
  };

  // ── Edit task modal ──
  window.openEditTaskModal = function (id) {
    const t = (window.TASKS_DATA || []).find(x => x.id === id);
    if (!t) return;
    openModal('Edit Task', `
      <div style="display:flex;flex-direction:column;gap:10px">
        <div>
          <label style="font-size:11px;color:var(--tx3);display:block;margin-bottom:5px">Task</label>
          <input class="inp" id="et-text" value="${t.text}"/>
        </div>
        <div style="display:flex;gap:10px">
          <div style="flex:1">
            <label style="font-size:11px;color:var(--tx3);display:block;margin-bottom:5px">Priority</label>
            <select class="inp" id="et-prio">
              ${['High','Medium','Low'].map(p => `<option${t.priority===p?' selected':''}>${p}</option>`).join('')}
            </select>
          </div>
          <div style="flex:1">
            <label style="font-size:11px;color:var(--tx3);display:block;margin-bottom:5px">Due</label>
            <select class="inp" id="et-due">
              ${['Today','Tomorrow','Friday','Next Week'].map(d => `<option${t.due===d?' selected':''}>${d}</option>`).join('')}
            </select>
          </div>
        </div>
      </div>`,
      () => {
        t.text     = document.getElementById('et-text')?.value || t.text;
        t.priority = document.getElementById('et-prio')?.value || t.priority;
        t.due      = document.getElementById('et-due')?.value  || t.due;
        t.dueTs    = { Today: 0, Tomorrow: 1, Friday: 4, 'Next Week': 7 }[t.due] ?? t.dueTs;
        closeModal();
        render();
        showToast('Task updated');
      }, 'Save');
  };

  // ── Init ──
  render();

})();
