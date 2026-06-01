/**
 * mockApi.js — DealerCrest Console Mock REST API
 *
 * Intercepts all fetch('/api/...') calls and returns realistic fake data.
 * Simulates network latency, errors, and full CRUD responses.
 *
 * To switch to a real API: set window.API_BASE = 'https://your-api.com'
 * and remove the fetch intercept at the bottom of this file.
 */

/* ── Seed data ── */
const DB = {
    inventory: [
        { id: 'v1', year: 2023, make: 'Toyota', model: 'Camry LE', trim: 'LE', condition: 'Used', mileage: 12400, color: 'Silver', price: 27500, msrp: 29000, status: 'active', vin: '4T1BF1FK5CU123456', transmission: 'Automatic', drivetrain: 'FWD', fuel: 'Gasoline', engine: '2.5L 4-Cyl' },
        { id: 'v2', year: 2022, make: 'Honda', model: 'CR-V EX-L', trim: 'EX-L', condition: 'CPO', mileage: 8100, color: 'Blue', price: 31200, msrp: 34000, status: 'active', vin: '2HKRM4H59NH123457', transmission: 'CVT', drivetrain: 'AWD', fuel: 'Gasoline', engine: '1.5L Turbo' },
        { id: 'v3', year: 2024, make: 'Ford', model: 'Mustang GT', trim: 'GT', condition: 'New', mileage: 1200, color: 'Red', price: 44900, msrp: 46500, status: 'active', vin: '1FATP8FF4R5123458', transmission: 'Manual', drivetrain: 'RWD', fuel: 'Gasoline', engine: '5.0L V8' },
        { id: 'v4', year: 2021, make: 'Toyota', model: 'RAV4 XLE', trim: 'XLE', condition: 'Used', mileage: 22000, color: 'White', price: 29800, msrp: 31000, status: 'pending', vin: '2T3P1RFV9MC123459', transmission: 'Automatic', drivetrain: 'AWD', fuel: 'Gasoline', engine: '2.5L 4-Cyl' },
        { id: 'v5', year: 2024, make: 'Ford', model: 'F-150 XLT', trim: 'XLT', condition: 'New', mileage: 340, color: 'Black', price: 52100, msrp: 54000, status: 'active', vin: '1FTEW1EP8RFA12345', transmission: 'Automatic', drivetrain: '4WD', fuel: 'Gasoline', engine: '3.5L V6 Turbo' },
        { id: 'v6', year: 2023, make: 'Honda', model: 'Accord Sport', trim: 'Sport', condition: 'CPO', mileage: 5800, color: 'Gray', price: 30450, msrp: 33000, status: 'active', vin: '1HGCV1F34PA123456', transmission: 'Automatic', drivetrain: 'FWD', fuel: 'Gasoline', engine: '1.5L Turbo' },
        { id: 'v7', year: 2023, make: 'BMW', model: '3 Series', trim: '330i', condition: 'CPO', mileage: 9300, color: 'Navy', price: 41800, msrp: 46000, status: 'active', vin: 'WBA5R1C50KAJ12345', transmission: 'Automatic', drivetrain: 'AWD', fuel: 'Gasoline', engine: '2.0L Turbo' },
        { id: 'v8', year: 2022, make: 'Tesla', model: 'Model 3', trim: 'LR', condition: 'Used', mileage: 18400, color: 'White', price: 34900, msrp: 47000, status: 'active', vin: '5YJ3E1EA8NF123456', transmission: 'Single-Speed', drivetrain: 'AWD', fuel: 'Electric', engine: 'Dual Motor' },
        { id: 'v9', year: 2024, make: 'Chevrolet', model: 'Silverado 1500', trim: 'LT', condition: 'New', mileage: 0, color: 'Red', price: 48200, msrp: 49500, status: 'active', vin: '1GCUYDED0NZ123456', transmission: 'Automatic', drivetrain: '4WD', fuel: 'Gasoline', engine: '5.3L V8' },
        { id: 'v10', year: 2021, make: 'Hyundai', model: 'Tucson SEL', trim: 'SEL', condition: 'Used', mileage: 31000, color: 'Gray', price: 22900, msrp: 26000, status: 'active', vin: '5NMS2DAD5MH123456', transmission: 'Automatic', drivetrain: 'AWD', fuel: 'Gasoline', engine: '2.5L 4-Cyl' },
    ],

    leads: [
        { id: 'l1', name: 'James Harper', email: 'james@email.com', phone: '(801) 555-0182', interest: '2023 Toyota Camry', source: 'Website', status: 'New', notes: '', created: '2026-05-18T09:02:00Z', lastContact: '2026-05-20T10:14:00Z', channel: 'SMS', unread: 1 },
        { id: 'l2', name: 'Sara Chen', email: 's.chen@mail.com', phone: '(801) 555-0291', interest: '2022 Honda CR-V', source: 'Website', status: 'Contacted', notes: 'Interested in financing.', created: '2026-05-17T14:30:00Z', lastContact: '2026-05-20T09:03:00Z', channel: 'Email', unread: 1 },
        { id: 'l3', name: 'Mike Torres', email: 'mtorres@mail.com', phone: '(801) 555-0384', interest: '2024 Ford F-150', source: 'Phone', status: 'Qualified', notes: 'Has trade-in.', created: '2026-05-15T11:00:00Z', lastContact: '2026-05-20T07:30:00Z', channel: 'SMS', unread: 0 },
        { id: 'l4', name: 'Linda Park', email: 'lpark@mail.com', phone: '(801) 555-0477', interest: '2021 Toyota RAV4', source: 'Walk-in', status: 'Closed', notes: 'Purchased elsewhere.', created: '2026-05-10T10:00:00Z', lastContact: '2026-05-19T11:20:00Z', channel: 'Call', unread: 0 },
        { id: 'l5', name: 'David Kim', email: 'dkim@mail.com', phone: '(801) 555-0533', interest: '2023 Honda Accord', source: 'Website', status: 'New', notes: '', created: '2026-05-18T16:00:00Z', lastContact: '2026-05-18T16:05:00Z', channel: 'SMS', unread: 1 },
        { id: 'l6', name: 'Rachel Moore', email: 'rmoore@mail.com', phone: '(801) 555-0614', interest: '2024 Ford Mustang', source: 'Website', status: 'Qualified', notes: 'Test drive scheduled.', created: '2026-05-14T09:00:00Z', lastContact: '2026-05-17T14:30:00Z', channel: 'Email', unread: 0 },
        { id: 'l7', name: 'Tom Walsh', email: 'twalsh@mail.com', phone: '(801) 555-0712', interest: '2024 Chevrolet Sil.', source: 'Referral', status: 'Contacted', notes: 'Referred by Linda Park.', created: '2026-05-16T13:00:00Z', lastContact: '2026-05-19T10:00:00Z', channel: 'Email', unread: 0 },
        { id: 'l8', name: 'Amy Johnson', email: 'amyj@mail.com', phone: '(801) 555-0819', interest: '2022 Tesla Model 3', source: 'Social', status: 'New', notes: 'Saw us on Instagram.', created: '2026-05-19T08:30:00Z', lastContact: '2026-05-19T08:35:00Z', channel: 'SMS', unread: 1 },
    ],

    conversations: [
        { id: 'c1', leadId: 'l1', leadName: 'James Harper', initials: 'JH', color: '#3b5bdb', lastMessage: 'Hey, is the Camry still available?', lastChannel: 'SMS', lastTime: '2m ago', unread: 1, status: 'New' },
        { id: 'c2', leadId: 'l2', leadName: 'Sara Chen', initials: 'SC', color: '#0891b2', lastMessage: 'Thanks for the info. Can we schedule…', lastChannel: 'Email', lastTime: '1h ago', unread: 1, status: 'Contacted' },
        { id: 'c3', leadId: 'l3', leadName: 'Mike Torres', initials: 'MT', color: '#7c3aed', lastMessage: 'You: See you Saturday at 10!', lastChannel: 'SMS', lastTime: '3h ago', unread: 0, status: 'Qualified' },
        { id: 'c4', leadId: 'l4', leadName: 'Linda Park', initials: 'LP', color: '#059669', lastMessage: '📞 Inbound call · 4m 22s', lastChannel: 'Call', lastTime: 'Yesterday', unread: 0, status: 'Closed' },
        { id: 'c5', leadId: 'l5', leadName: 'David Kim', initials: 'DK', color: '#dc2626', lastMessage: "What's the best price you can do?", lastChannel: 'SMS', lastTime: '2d ago', unread: 1, status: 'New' },
        { id: 'c6', leadId: 'l6', leadName: 'Rachel Moore', initials: 'RM', color: '#b45309', lastMessage: "You: We'd love to set up a test drive", lastChannel: 'Email', lastTime: '3d ago', unread: 0, status: 'Qualified' },
    ],

    appointments: [
        { id: 'a1', type: 'test-drive', leadName: 'James Harper', vehicle: '2023 Toyota Camry', date: '2026-05-20', time: '9:00 AM', duration: 45, status: 'confirmed' },
        { id: 'a2', type: 'call', leadName: 'David Kim', vehicle: '', date: '2026-05-20', time: '11:00 AM', duration: 30, status: 'pending' },
        { id: 'a3', type: 'test-drive', leadName: 'Sara Chen', vehicle: '2022 Honda CR-V', date: '2026-05-20', time: '2:30 PM', duration: 45, status: 'confirmed' },
        { id: 'a4', type: 'test-drive', leadName: 'Mike Torres', vehicle: '2024 Ford F-150', date: '2026-05-21', time: '10:00 AM', duration: 45, status: 'confirmed' },
        { id: 'a5', type: 'service', leadName: 'Linda Park', vehicle: '2021 Toyota RAV4', date: '2026-05-22', time: '9:00 AM', duration: 60, status: 'confirmed' },
        { id: 'a6', type: 'call', leadName: 'Rachel Moore', vehicle: '', date: '2026-05-22', time: '2:00 PM', duration: 30, status: 'confirmed' },
    ],

    dashboard: {
        stats: {
            newLeads: { value: 12, delta: '+4 today', trend: 'up' },
            activeListings: { value: 134, delta: '+3 this week', trend: 'up' },
            appointments: { value: 8, delta: 'This week', trend: 'flat' },
            convRate: { value: 22, delta: '+2% vs last month', trend: 'up' },
        },
        recentLeads: ['l1', 'l2', 'l5', 'l8'],
        todayAppts: ['a1', 'a2', 'a3'],
        activity: [
            { time: '10:14 AM', text: 'New lead — James Harper (2023 Camry)', type: 'lead' },
            { time: '9:45 AM', text: 'Page published — About Us', type: 'publish' },
            { time: '9:03 AM', text: 'Email from Sara Chen', type: 'message' },
            { time: '8:30 AM', text: 'New lead — Amy Johnson (Tesla Model 3)', type: 'lead' },
        ]
    },

    pages: [
        { id: 'home', name: 'Home', slug: '/', status: 'published', updatedAt: '2h ago' },
        { id: 'inventory-page', name: 'Inventory', slug: '/inventory', status: 'published', updatedAt: 'Yesterday' },
        { id: 'contact', name: 'Contact Us', slug: '/contact', status: 'published', updatedAt: '3 days ago' },
        { id: 'about', name: 'About Us', slug: '/about', status: 'draft', updatedAt: '1 week ago' },
    ],

    analytics: {
        summary: { visits: 4821, leads: 248, convRate: 5.1, bounceRate: 38 },
        chart: [
            { label: 'May 14', visits: 620, leads: 28 },
            { label: 'May 15', visits: 710, leads: 34 },
            { label: 'May 16', visits: 540, leads: 22 },
            { label: 'May 17', visits: 830, leads: 41 },
            { label: 'May 18', visits: 920, leads: 48 },
            { label: 'May 19', visits: 750, leads: 39 },
            { label: 'May 20', visits: 451, leads: 36 },
        ]
    },
};

/* ── Route table ── */
function mockHandler(url, opts = {}) {
    const method = (opts.method || 'GET').toUpperCase();
    const body = opts.body ? JSON.parse(opts.body) : null;
    const path = url.replace(/\?.*$/, '').replace(/^\/api\//, '');
    const parts = path.split('/');              // ['inventory'], ['leads','l1'], etc.
    const resource = parts[0];
    const id = parts[1];

    // Simulate latency (300–600ms)
    const delay = 300 + Math.random() * 300;

    // ── Error simulation ──
    // Control from browser console:
    //   window.mockError = true          → all requests fail
    //   window.mockError = 'inventory'   → only /api/inventory fails
    //   window.mockError = false         → back to normal (default)
    //   window.mockEmpty = 'leads'       → /api/leads returns empty array
    const errTarget = window.mockError;
    if (errTarget === true || errTarget === resource) {
        return fakeResponse({ error: 'Simulated server error' }, 500, delay);
    }
    const emptyTarget = window.mockEmpty;
    if (emptyTarget === true || emptyTarget === resource) {
        return fakeResponse(Array.isArray(DB[resource]) ? [] : {}, 200, delay);
    }

    /* ── GET ── */
    if (method === 'GET') {
        switch (resource) {
            case 'inventory':
                if (id) return fakeResponse(DB.inventory.find(v => v.id === id) || null, id ? 200 : 200, delay);
                return fakeResponse(DB.inventory, 200, delay);

            case 'leads':
                if (id) return fakeResponse(DB.leads.find(l => l.id === id) || null, 200, delay);
                return fakeResponse(DB.leads, 200, delay);

            case 'conversations':
                if (id) return fakeResponse(DB.conversations.find(c => c.id === id) || null, 200, delay);
                return fakeResponse(DB.conversations, 200, delay);

            case 'appointments':
                return fakeResponse(DB.appointments, 200, delay);

            case 'dashboard':
                return fakeResponse(DB.dashboard, 200, delay);

            case 'pages':
                return fakeResponse(DB.pages, 200, delay);

            case 'analytics':
                return fakeResponse(DB.analytics, 200, delay);

            default:
                return fakeResponse({ error: 'Not found' }, 404, delay);
        }
    }

    /* ── POST ── */
    if (method === 'POST') {
        const newId = resource[0] + Date.now();
        const item = { id: newId, ...body };
        if (DB[resource]) DB[resource].push(item);
        return fakeResponse(item, 201, delay);
    }

    /* ── PATCH ── */
    if (method === 'PATCH') {
        if (DB[resource]) {
            const idx = DB[resource].findIndex(i => i.id === id);
            if (idx >= 0) {
                DB[resource][idx] = { ...DB[resource][idx], ...body };
                return fakeResponse(DB[resource][idx], 200, delay);
            }
        }
        return fakeResponse({ error: 'Not found' }, 404, delay);
    }

    /* ── DELETE ── */
    if (method === 'DELETE') {
        if (DB[resource]) {
            const idx = DB[resource].findIndex(i => i.id === id);
            if (idx >= 0) { DB[resource].splice(idx, 1); return fakeResponse({ deleted: id }, 200, delay); }
        }
        return fakeResponse({ error: 'Not found' }, 404, delay);
    }

    return fakeResponse({ error: 'Method not allowed' }, 405, delay);
}

function fakeResponse(data, status, delay) {
    return new Promise(resolve =>
        setTimeout(() => {
            resolve(new Response(JSON.stringify(data), {
                status,
                headers: { 'Content-Type': 'application/json' }
            }));
        }, delay)
    );
}

/* ── Intercept fetch for /api/* routes ── */
const _realFetch = window.fetch.bind(window);
window.fetch = function (url, opts) {
    if (typeof url === 'string' && url.startsWith('/api/')) {
        return mockHandler(url, opts);
    }
    return _realFetch(url, opts);
};

console.log('[mockApi] Intercepting /api/* — DB seeded with',
    DB.inventory.length, 'vehicles,',
    DB.leads.length, 'leads,',
    DB.conversations.length, 'conversations'
);