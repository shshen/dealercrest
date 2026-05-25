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
        { id: 'v3', year: 2024, make: 'Ford', model: 'Mustang GT', trim: 'GT', condition: 'New', mileage: 1200, color: 'Red', price: 44900, msrp: 46500, status: 'active', vin: '1FATP8FF4R5123458', transmission: 'Manual', drivetrain: 'RWD', fuel: 'Gasoline', engine: '5.0L V8' }
    ],

    leads: [
        { id: 'l1', name: 'James Harper', email: 'james@email.com', phone: '(801) 555-0182', interest: '2023 Toyota Camry', source: 'Website', status: 'New', notes: '', created: '2026-05-18T09:02:00Z', lastContact: '2026-05-20T10:14:00Z', channel: 'SMS', unread: 1 }
    ],

    conversations: [
        { id: 'c1', leadId: 'l1', leadName: 'James Harper', initials: 'JH', color: '#3b5bdb', lastMessage: 'Hey, is the Camry still available?', lastChannel: 'SMS', lastTime: '2m ago', unread: 1, status: 'New' }
    ],

    appointments: [
        { id: 'a1', type: 'test-drive', leadName: 'James Harper', vehicle: '2023 Toyota Camry', date: '2026-05-20', time: '9:00 AM', duration: 45, status: 'confirmed' }
    ],

    dashboard: {
        stats: {
            newLeads: { value: 12, delta: '+4 today', trend: 'up' },
            activeListings: { value: 134, delta: '+3 this week', trend: 'up' },
            appointments: { value: 8, delta: 'This week', trend: 'flat' },
            convRate: { value: 22, delta: '+2% vs last month', trend: 'up' }
        }
    }
};

function parseRequest(url, opts) {
    const method = getMethod(opts);
    const body = getBody(opts);
    const path = normalizePath(url);
    const parts = path.split('/');

    return {
        method: method,
        body: body,
        resource: parts[0],
        id: parts[1]
    };
}

function getMethod(opts) {
    return ((opts && opts.method) || 'GET').toUpperCase();
}

function getBody(opts) {
    if (!opts || !opts.body) {
        return null;
    }

    return JSON.parse(opts.body);
}

function normalizePath(url) {
    return url
        .replace(/\?.*$/, '')
        .replace(/^\/api\//, '');
}

function getDelay() {
    return 300 + Math.random() * 300;
}

function buildResponse(data, status) {
    return new Response(JSON.stringify(data), {
        status: status,
        headers: {
            'Content-Type': 'application/json'
        }
    });
}

function resolveResponse(resolve, data, status) {
    resolve(buildResponse(data, status));
}

function scheduleResponse(resolve, data, status, delay) {
    setTimeout(handleResponseTimeout, delay, resolve, data, status);
}

function handleResponseTimeout(resolve, data, status) {
    resolveResponse(resolve, data, status);
}

function fakeResponse(data, status, delay) {
    return new Promise(function fakeResponsePromise(resolve) {
        scheduleResponse(resolve, data, status, delay);
    });
}

function findItem(list, id) {
    return list.find(function findById(item) {
        return item.id === id;
    });
}

function findItemIndex(list, id) {
    return list.findIndex(function findIndex(item) {
        return item.id === id;
    });
}

function notFound(delay) {
    return fakeResponse({ error: 'Not found' }, 404, delay);
}

function methodNotAllowed(delay) {
    return fakeResponse({ error: 'Method not allowed' }, 405, delay);
}

function getCollection(resource) {
    return DB[resource];
}

function handleGet(resource, id, delay) {
    const collection = getCollection(resource);

    if (!collection) {
        return notFound(delay);
    }

    if (id) {
        return fakeResponse(findItem(collection, id) || null, 200, delay);
    }

    return fakeResponse(collection, 200, delay);
}

function handleDashboard(delay) {
    return fakeResponse(DB.dashboard, 200, delay);
}

function handlePost(resource, body, delay) {
    const collection = getCollection(resource);

    if (!collection) {
        return notFound(delay);
    }

    const item = createItem(resource, body);

    collection.push(item);

    return fakeResponse(item, 201, delay);
}

function createItem(resource, body) {
    var result = {
        id: createId(resource)
    };
    for (var key in body) {
        if (body.hasOwnProperty(key)) {
            result[key] = body[key];
        }
    }
    return result;
}

function createId(resource) {
    return resource.charAt(0) + Date.now();
}

function handlePatch(resource, id, body, delay) {
    const collection = getCollection(resource);

    if (!collection) {
        return notFound(delay);
    }

    const index = findItemIndex(collection, id);

    if (index < 0) {
        return notFound(delay);
    }

    collection[index] = mergeItem(collection[index], body);

    return fakeResponse(collection[index], 200, delay);
}

function mergeItem(existing, updates) {
    var result = {};
    for (var key in existing) {
        if (existing.hasOwnProperty(key)) {
            result[key] = existing[key];
        }
    }
    for (var key in updates) {
        if (updates.hasOwnProperty(key)) {
            result[key] = updates[key];
        }
    }
    return result;
}

function handleDelete(resource, id, delay) {
    const collection = getCollection(resource);

    if (!collection) {
        return notFound(delay);
    }

    const index = findItemIndex(collection, id);

    if (index < 0) {
        return notFound(delay);
    }

    collection.splice(index, 1);

    return fakeResponse({ deleted: id }, 200, delay);
}

function handleRequest(method, resource, id, body, delay) {
    if (method === 'GET') {
        return handleGetRequest(resource, id, delay);
    }

    if (method === 'POST') {
        return handlePost(resource, body, delay);
    }

    if (method === 'PATCH') {
        return handlePatch(resource, id, body, delay);
    }

    if (method === 'DELETE') {
        return handleDelete(resource, id, delay);
    }

    return methodNotAllowed(delay);
}

function handleGetRequest(resource, id, delay) {
    if (resource === 'dashboard') {
        return handleDashboard(delay);
    }

    return handleGet(resource, id, delay);
}

function mockHandler(url, opts) {
    const request = parseRequest(url, opts);
    const delay = getDelay();

    return handleRequest(
        request.method,
        request.resource,
        request.id,
        request.body,
        delay
    );
}

const realFetch = window.fetch.bind(window);

function interceptFetch(url, opts) {
    if (isMockApiRequest(url)) {
        return mockHandler(url, opts);
    }

    return realFetch(url, opts);
}

function isMockApiRequest(url) {
    return typeof url === 'string'
        && url.startsWith('/api/');
}

window.fetch = interceptFetch;

console.log(
    '[mockApi] Intercepting /api/* — DB seeded with',
    DB.inventory.length,
    'vehicles,',
    DB.leads.length,
    'leads,',
    DB.conversations.length,
    'conversations'
);
