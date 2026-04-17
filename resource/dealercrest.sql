-- ============================================================
--  DealerBase — Complete Database Schema
--  PostgreSQL 14+
--  Generated for DealerBase Dealer SaaS Platform
-- ============================================================
--
--  S3 owns all content. Postgres owns identity, routing,
--  and state. No page content, HTML, or assets in the DB.
--
--  Tables:
--    Core
--      dealers                    — core dealer record
--      dealer_domains             — domain-to-dealer routing
--      users                      — staff accounts
--      customers                  — cookie-based visitor identity
--
--    Inventory & Sales
--      inventory                  — vehicle listings
--      promotions                 — sale events and discount campaigns
--      vehicle_promotions         — many-to-many: vehicles <-> promotions
--      leads                      — inbound customer interest
--      finance_applications       — customer finance/lease requests
--      trade_in_requests          — customer trade-in valuations
--      test_drive_appointments    — scheduled test drives
--
--    Website
--      sites                      — website builder (S3-backed, 1 per dealer)
--
--    Chat & Communication
--      conversations              — chat threads (direct, group, deal_room)
--      conversation_participants  — who is in each conversation
--      messages                   — chat messages (hot storage)
--      message_receipts           — delivered/read tracking
--      call_sessions              — voice and video calls
--      campaigns                  — email/SMS marketing campaigns
--      campaign_recipients        — per-customer campaign delivery tracking
--
--    Reviews
--      reviews                    — dealer reviews from all platforms
--
--    Service
--      service_types              — catalog of services offered
--      service_bays               — physical bays at the dealership
--      technicians                — service staff
--      customer_vehicles          — vehicles owned by customers
--      service_appointments       — scheduled service visits
--      appointment_services       — services requested per appointment
--      service_orders             — actual work performed at check-in
--      service_order_line_items   — individual services/parts per order
-- ============================================================


-- ============================================================
--  DATABASE USERS
-- ============================================================
CREATE USER dealerbase_app      WITH PASSWORD 'zhu88jie';
CREATE USER dealerbase_readonly WITH PASSWORD 'zhu88jie';

GRANT CONNECT ON DATABASE dealerbase TO dealerbase_app;
GRANT CONNECT ON DATABASE dealerbase TO dealerbase_readonly;


-- ============================================================
--  EXTENSIONS
-- ============================================================
-- CREATE EXTENSION IF NOT EXISTS "pgcrypto";   -- gen_random_uuid()
-- CREATE EXTENSION IF NOT EXISTS "pgvector";   -- future AI/embedding support


-- ============================================================
--  ENUMS
-- ============================================================
CREATE TYPE user_role            AS ENUM ('admin', 'manager', 'sales');
CREATE TYPE vehicle_condition    AS ENUM ('new', 'used', 'certified');
CREATE TYPE vehicle_status       AS ENUM ('available', 'sold', 'pending', 'archived');
CREATE TYPE lead_status          AS ENUM ('new', 'contacted', 'qualified', 'lost', 'converted');
CREATE TYPE lead_source          AS ENUM ('website', 'phone', 'walk_in', 'third_party', 'referral');
CREATE TYPE site_status          AS ENUM ('draft', 'published', 'suspended');
CREATE TYPE promotion_type       AS ENUM ('sale_price', 'discount_percent', 'discount_amount', 'financing', 'bonus', 'label_only');
CREATE TYPE finance_type         AS ENUM ('finance', 'lease', 'cash');
CREATE TYPE review_platform      AS ENUM ('google', 'yelp', 'facebook', 'dealerrater', 'cars_com', 'internal');
CREATE TYPE conversation_type    AS ENUM ('direct', 'group', 'deal_room');
CREATE TYPE participant_type     AS ENUM ('user', 'customer');
CREATE TYPE message_type         AS ENUM ('text', 'image', 'file', 'audio', 'video', 'voice_call', 'video_call', 'system');
CREATE TYPE receipt_status       AS ENUM ('delivered', 'read');
CREATE TYPE call_status          AS ENUM ('initiated', 'ringing', 'active', 'ended', 'missed', 'failed');
CREATE TYPE call_type            AS ENUM ('voice', 'video');
CREATE TYPE campaign_channel     AS ENUM ('email', 'sms');
CREATE TYPE campaign_status      AS ENUM ('draft', 'scheduled', 'sending', 'sent', 'cancelled');
CREATE TYPE appointment_status   AS ENUM ('requested', 'confirmed', 'in_progress', 'completed', 'cancelled', 'no_show');
CREATE TYPE service_order_status AS ENUM ('open', 'in_progress', 'waiting_parts', 'completed', 'invoiced', 'paid');
CREATE TYPE line_item_type       AS ENUM ('service', 'part', 'fee', 'discount');


-- ============================================================
--  DEALERS
-- ============================================================
CREATE TABLE dealers (
    dealer_id       CHAR(8)      PRIMARY KEY,
    name            TEXT         NOT NULL,
    email           TEXT         NOT NULL UNIQUE,
    phone           TEXT,
    address         TEXT,
    city            TEXT,
    state           CHAR(2),
    zip             TEXT,
    active          BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);


-- ============================================================
--  DEALER DOMAINS
--  Maps domains to dealers for request routing.
--  Multiple domains per dealer supported (rebrands, aliases).
-- ============================================================
CREATE TABLE dealer_domains (
    domain          TEXT         PRIMARY KEY,
    dealer_id       CHAR(8)      NOT NULL REFERENCES dealers(dealer_id) ON DELETE CASCADE,
    is_primary      BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_dealer_domains_dealer_id ON dealer_domains(dealer_id);


-- ============================================================
--  USERS (Staff)
-- ============================================================
CREATE TABLE users (
    user_id         UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    dealer_id       CHAR(8)      NOT NULL REFERENCES dealers(dealer_id) ON DELETE CASCADE,
    email           TEXT         NOT NULL UNIQUE,
    password_hash   TEXT         NOT NULL,
    first_name      TEXT         NOT NULL,
    last_name       TEXT         NOT NULL,
    phone           TEXT,
    role            user_role    NOT NULL DEFAULT 'sales',
    active          BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_users_dealer_id ON users(dealer_id);


-- ============================================================
--  CUSTOMERS
--  No login required. Identified by UUID in browser cookie.
--  Profile data filled in progressively as they submit forms.
--
--  Flow:
--    First visit  -> generate UUID -> set cookie -> INSERT customers
--    Return visit -> read cookie   -> SELECT WHERE cookie_id = ?
--    Form submit  -> UPDATE name/email/phone WHERE cookie_id = ?
-- ============================================================
CREATE TABLE customers (
    customer_id     UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    dealer_id       CHAR(8)      NOT NULL REFERENCES dealers(dealer_id) ON DELETE CASCADE,
    cookie_id       UUID         NOT NULL UNIQUE,
    first_name      TEXT,
    last_name       TEXT,
    email           TEXT,
    phone           TEXT,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_customers_dealer_id ON customers(dealer_id);
CREATE INDEX idx_customers_cookie_id ON customers(cookie_id);


-- ============================================================
--  INVENTORY
-- ============================================================
CREATE TABLE inventory (
    vehicle_id      UUID              PRIMARY KEY DEFAULT gen_random_uuid(),
    dealer_id       CHAR(8)           NOT NULL REFERENCES dealers(dealer_id) ON DELETE CASCADE,
    vin             CHAR(17)          NOT NULL,
    condition       vehicle_condition NOT NULL,
    status          vehicle_status    NOT NULL DEFAULT 'available',
    year            SMALLINT          NOT NULL,
    make            TEXT              NOT NULL,
    model           TEXT              NOT NULL,
    trim            TEXT,
    color_ext       TEXT,
    color_int       TEXT,
    mileage         INTEGER           NOT NULL DEFAULT 0,
    price           NUMERIC(10, 2)    NOT NULL,
    msrp            NUMERIC(10, 2),
    description     TEXT,
    images          JSONB,
    features        JSONB,
    created_at      TIMESTAMPTZ       NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ       NOT NULL DEFAULT NOW(),
    UNIQUE (dealer_id, vin)
);

CREATE INDEX idx_inventory_dealer_id ON inventory(dealer_id);
CREATE INDEX idx_inventory_status    ON inventory(status);
CREATE INDEX idx_inventory_vin       ON inventory(vin);


-- ============================================================
--  PROMOTIONS
--  Sale events and discount campaigns.
--  Applied to vehicles via vehicle_promotions join table.
-- ============================================================
CREATE TABLE promotions (
    promotion_id    UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    dealer_id       CHAR(8)         NOT NULL REFERENCES dealers(dealer_id) ON DELETE CASCADE,
    name            TEXT            NOT NULL,
    description     TEXT,
    type            promotion_type  NOT NULL,
    discount_value  NUMERIC(10, 2),
    badge_label     TEXT,           -- e.g. "HOT DEAL", "0% APR", "Manager Special"
    starts_at       TIMESTAMPTZ,
    ends_at         TIMESTAMPTZ,
    active          BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_promotions_dealer_id ON promotions(dealer_id);
CREATE INDEX idx_promotions_active    ON promotions(dealer_id, active) WHERE active = TRUE;


-- ============================================================
--  VEHICLE PROMOTIONS  (many-to-many)
--  One vehicle can belong to multiple promotions.
--  One promotion can cover multiple vehicles.
--  override_price allows per-vehicle price regardless of type.
-- ============================================================
CREATE TABLE vehicle_promotions (
    vehicle_id      UUID           NOT NULL REFERENCES inventory(vehicle_id)   ON DELETE CASCADE,
    promotion_id    UUID           NOT NULL REFERENCES promotions(promotion_id) ON DELETE CASCADE,
    override_price  NUMERIC(10, 2),
    created_at      TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    PRIMARY KEY (vehicle_id, promotion_id)
);

CREATE INDEX idx_vehicle_promotions_promotion_id ON vehicle_promotions(promotion_id);
CREATE INDEX idx_vehicle_promotions_vehicle_id   ON vehicle_promotions(vehicle_id);


-- ============================================================
--  LEADS
-- ============================================================
CREATE TABLE leads (
    lead_id         UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    dealer_id       CHAR(8)      NOT NULL REFERENCES dealers(dealer_id) ON DELETE CASCADE,
    customer_id     UUID         REFERENCES customers(customer_id)  ON DELETE SET NULL,
    vehicle_id      UUID         REFERENCES inventory(vehicle_id)   ON DELETE SET NULL,
    assigned_to     UUID         REFERENCES users(user_id)          ON DELETE SET NULL,
    first_name      TEXT         NOT NULL,
    last_name       TEXT         NOT NULL,
    email           TEXT,
    phone           TEXT,
    source          lead_source  NOT NULL DEFAULT 'website',
    status          lead_status  NOT NULL DEFAULT 'new',
    notes           TEXT,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_leads_dealer_id    ON leads(dealer_id);
CREATE INDEX idx_leads_customer_id  ON leads(customer_id);
CREATE INDEX idx_leads_status       ON leads(status);
CREATE INDEX idx_leads_assigned_to  ON leads(assigned_to);


-- ============================================================
--  FINANCE APPLICATIONS
--  Submitted when customer requests financing or lease.
--  monthly_payment stored for reference (calculated at submit time).
-- ============================================================
CREATE TABLE finance_applications (
    application_id    UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    dealer_id         CHAR(8)       NOT NULL REFERENCES dealers(dealer_id) ON DELETE CASCADE,
    lead_id           UUID          REFERENCES leads(lead_id)              ON DELETE SET NULL,
    customer_id       UUID          REFERENCES customers(customer_id)      ON DELETE SET NULL,
    vehicle_id        UUID          REFERENCES inventory(vehicle_id)       ON DELETE SET NULL,
    finance_type      finance_type  NOT NULL DEFAULT 'finance',
    vehicle_price     NUMERIC(10, 2),
    down_payment      NUMERIC(10, 2),
    trade_in_value    NUMERIC(10, 2),
    loan_term_months  SMALLINT,
    annual_rate       NUMERIC(5, 2),
    monthly_payment   NUMERIC(10, 2),
    status            TEXT          NOT NULL DEFAULT 'draft',  -- draft, submitted, approved, denied
    submitted_at      TIMESTAMPTZ,
    created_at        TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_finance_applications_dealer_id   ON finance_applications(dealer_id);
CREATE INDEX idx_finance_applications_customer_id ON finance_applications(customer_id);
CREATE INDEX idx_finance_applications_lead_id     ON finance_applications(lead_id);


-- ============================================================
--  TRADE-IN REQUESTS
--  Customer submits their vehicle details for a valuation.
--  estimated_value comes from third-party API (Black Book / Edmunds).
--  offered_value is the dealer's actual counter-offer.
-- ============================================================
CREATE TABLE trade_in_requests (
    trade_in_id      UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    dealer_id        CHAR(8)      NOT NULL REFERENCES dealers(dealer_id) ON DELETE CASCADE,
    customer_id      UUID         REFERENCES customers(customer_id)      ON DELETE SET NULL,
    lead_id          UUID         REFERENCES leads(lead_id)              ON DELETE SET NULL,
    vin              CHAR(17),
    year             SMALLINT,
    make             TEXT,
    model            TEXT,
    trim             TEXT,
    mileage          INTEGER,
    condition        TEXT,        -- excellent, good, fair, poor
    estimated_value  NUMERIC(10, 2),
    offered_value    NUMERIC(10, 2),
    provider         TEXT,        -- 'blackbook', 'edmunds', etc.
    notes            TEXT,
    status           TEXT         NOT NULL DEFAULT 'pending',  -- pending, reviewed, accepted, declined
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_trade_in_requests_dealer_id   ON trade_in_requests(dealer_id);
CREATE INDEX idx_trade_in_requests_customer_id ON trade_in_requests(customer_id);
CREATE INDEX idx_trade_in_requests_lead_id     ON trade_in_requests(lead_id);


-- ============================================================
--  TEST DRIVE APPOINTMENTS
-- ============================================================
CREATE TABLE test_drive_appointments (
    test_drive_id   UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    dealer_id       CHAR(8)      NOT NULL REFERENCES dealers(dealer_id) ON DELETE CASCADE,
    customer_id     UUID         REFERENCES customers(customer_id)      ON DELETE SET NULL,
    lead_id         UUID         REFERENCES leads(lead_id)              ON DELETE SET NULL,
    vehicle_id      UUID         NOT NULL REFERENCES inventory(vehicle_id) ON DELETE CASCADE,
    assigned_to     UUID         REFERENCES users(user_id)              ON DELETE SET NULL,
    scheduled_at    TIMESTAMPTZ  NOT NULL,
    status          TEXT         NOT NULL DEFAULT 'requested',  -- requested, confirmed, completed, cancelled, no_show
    notes           TEXT,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_test_drives_dealer_id    ON test_drive_appointments(dealer_id);
CREATE INDEX idx_test_drives_vehicle_id   ON test_drive_appointments(vehicle_id);
CREATE INDEX idx_test_drives_scheduled_at ON test_drive_appointments(scheduled_at);


CREATE TABLE block_definition_history (
    id            BIGSERIAL    PRIMARY KEY,
    dealer_id     UUID         NOT NULL,
    s3_key        TEXT         NOT NULL,
    changed_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    user_id       UUID         NOT NULL,
    action        TEXT         NOT NULL,   -- 'CREATE', 'UPDATE', 'DELETE'
    change_source TEXT         NOT NULL    -- 'USER', 'SYSTEM', 'IMPORT'
);


-- ============================================================
--  CONVERSATIONS
--  direct    — 1-to-1 staff DM or staff-to-customer
--  group     — team channel (e.g. sales floor)
--  deal_room — auto-created per lead, linked to lead + vehicle
-- ============================================================
CREATE TABLE conversations (
    conversation_id UUID               PRIMARY KEY DEFAULT gen_random_uuid(),
    dealer_id       CHAR(8)            NOT NULL REFERENCES dealers(dealer_id) ON DELETE CASCADE,
    type            conversation_type  NOT NULL DEFAULT 'direct',
    title           TEXT,
    lead_id         UUID               REFERENCES leads(lead_id)        ON DELETE SET NULL,
    vehicle_id      UUID               REFERENCES inventory(vehicle_id) ON DELETE SET NULL,
    archived        BOOLEAN            NOT NULL DEFAULT FALSE,
    archived_at     TIMESTAMPTZ,
    archive_s3_key  TEXT,
    created_at      TIMESTAMPTZ        NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ        NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_conversations_dealer_id  ON conversations(dealer_id);
CREATE INDEX idx_conversations_lead_id    ON conversations(lead_id);
CREATE INDEX idx_conversations_archived   ON conversations(archived);
CREATE INDEX idx_conversations_updated_at ON conversations(updated_at);


-- ============================================================
--  CONVERSATION PARTICIPANTS
--  participant_id = users.user_id        for staff
--               OR customers.customer_id for cookie-based visitors
--  No FK constraint intentionally — supports both types via
--  participant_type discriminator column.
-- ============================================================
CREATE TABLE conversation_participants (
    conversation_id  UUID              NOT NULL REFERENCES conversations(conversation_id) ON DELETE CASCADE,
    participant_id   UUID              NOT NULL,
    participant_type participant_type  NOT NULL DEFAULT 'user',
    joined_at        TIMESTAMPTZ       NOT NULL DEFAULT NOW(),
    last_read_at     TIMESTAMPTZ,
    PRIMARY KEY (conversation_id, participant_id)
);

CREATE INDEX idx_participants_conversation_id ON conversation_participants(conversation_id);
CREATE INDEX idx_participants_participant_id  ON conversation_participants(participant_id);


-- ============================================================
--  MESSAGES  (hot storage — 0 to 90 days)
--  After 90 days inactive, archival job serializes to S3
--  and deletes rows. Conversation row is kept.
-- ============================================================
CREATE TABLE messages (
    message_id      UUID              PRIMARY KEY DEFAULT gen_random_uuid(),
    conversation_id UUID              NOT NULL REFERENCES conversations(conversation_id) ON DELETE CASCADE,
    sender_id       UUID              NOT NULL,
    sender_type     participant_type  NOT NULL DEFAULT 'user',
    body            TEXT,
    message_type    message_type      NOT NULL DEFAULT 'text',
    attachments     JSONB,            -- { "url": "s3://...", "name": "...", "size": 1024 }
    is_deleted      BOOLEAN           NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ       NOT NULL DEFAULT NOW(),
    edited_at       TIMESTAMPTZ
);

CREATE INDEX idx_messages_conversation_id ON messages(conversation_id);
CREATE INDEX idx_messages_sender_id       ON messages(sender_id);
CREATE INDEX idx_messages_created_at      ON messages(created_at DESC);


-- ============================================================
--  MESSAGE RECEIPTS
--  Per-participant delivered/read tracking (blue ticks).
-- ============================================================
CREATE TABLE message_receipts (
    message_id      UUID            NOT NULL REFERENCES messages(message_id) ON DELETE CASCADE,
    participant_id  UUID            NOT NULL,
    status          receipt_status  NOT NULL DEFAULT 'delivered',
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    PRIMARY KEY (message_id, participant_id)
);


-- ============================================================
--  CALL SESSIONS
--  Voice via Twilio. Video via Agora.
--  Provider uploads recording directly to S3.
--  A system message is posted to the conversation on call end.
-- ============================================================
CREATE TABLE call_sessions (
    call_id           UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    conversation_id   UUID         NOT NULL REFERENCES conversations(conversation_id) ON DELETE CASCADE,
    dealer_id         CHAR(8)      NOT NULL REFERENCES dealers(dealer_id) ON DELETE CASCADE,
    call_type         call_type    NOT NULL DEFAULT 'voice',
    status            call_status  NOT NULL DEFAULT 'initiated',
    initiated_by      UUID         NOT NULL,
    provider          TEXT         NOT NULL,   -- 'twilio' or 'agora'
    provider_call_id  TEXT,                    -- Twilio CallSid or Agora channel name
    started_at        TIMESTAMPTZ,
    ended_at          TIMESTAMPTZ,
    duration_seconds  INTEGER,
    recording_s3_key  TEXT,                    -- e.g. calls/ABC12345/2026/03/{call_id}.mp4
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_call_sessions_conversation_id ON call_sessions(conversation_id);
CREATE INDEX idx_call_sessions_dealer_id       ON call_sessions(dealer_id);


-- ============================================================
--  CAMPAIGNS  (email / SMS marketing)
-- ============================================================
CREATE TABLE campaigns (
    campaign_id      UUID              PRIMARY KEY DEFAULT gen_random_uuid(),
    dealer_id        CHAR(8)           NOT NULL REFERENCES dealers(dealer_id) ON DELETE CASCADE,
    name             TEXT              NOT NULL,
    channel          campaign_channel  NOT NULL,
    status           campaign_status   NOT NULL DEFAULT 'draft',
    subject          TEXT,             -- email only
    body             TEXT              NOT NULL,
    scheduled_at     TIMESTAMPTZ,
    sent_at          TIMESTAMPTZ,
    recipient_count  INTEGER           NOT NULL DEFAULT 0,
    created_at       TIMESTAMPTZ       NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ       NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_campaigns_dealer_id ON campaigns(dealer_id);
CREATE INDEX idx_campaigns_status    ON campaigns(dealer_id, status);


-- ============================================================
--  CAMPAIGN RECIPIENTS
--  Per-customer delivery and engagement tracking.
-- ============================================================
CREATE TABLE campaign_recipients (
    campaign_id     UUID         NOT NULL REFERENCES campaigns(campaign_id)   ON DELETE CASCADE,
    customer_id     UUID         NOT NULL REFERENCES customers(customer_id)   ON DELETE CASCADE,
    sent_at         TIMESTAMPTZ,
    opened_at       TIMESTAMPTZ,
    clicked_at      TIMESTAMPTZ,
    bounced         BOOLEAN      NOT NULL DEFAULT FALSE,
    unsubscribed    BOOLEAN      NOT NULL DEFAULT FALSE,
    PRIMARY KEY (campaign_id, customer_id)
);

CREATE INDEX idx_campaign_recipients_customer_id ON campaign_recipients(customer_id);


-- ============================================================
--  REVIEWS
--  Aggregates reviews from all platforms plus internal.
--  external_id stores the platform's own review ID for dedup.
-- ============================================================
CREATE TABLE reviews (
    review_id        UUID             PRIMARY KEY DEFAULT gen_random_uuid(),
    dealer_id        CHAR(8)          NOT NULL REFERENCES dealers(dealer_id) ON DELETE CASCADE,
    customer_id      UUID             REFERENCES customers(customer_id) ON DELETE SET NULL,
    platform         review_platform  NOT NULL DEFAULT 'internal',
    rating           SMALLINT         NOT NULL CHECK (rating BETWEEN 1 AND 5),
    title            TEXT,
    body             TEXT,
    reviewer_name    TEXT,
    external_id      TEXT,            -- platform's own review ID (for dedup)
    response         TEXT,            -- dealer's public response
    responded_at     TIMESTAMPTZ,
    published_at     TIMESTAMPTZ,
    created_at       TIMESTAMPTZ      NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_reviews_dealer_id ON reviews(dealer_id);
CREATE INDEX idx_reviews_platform  ON reviews(dealer_id, platform);


-- ============================================================
--  SERVICE TYPES
--  Catalog of services the dealer offers.
-- ============================================================
CREATE TABLE service_types (
    service_type_id  UUID           PRIMARY KEY DEFAULT gen_random_uuid(),
    dealer_id        CHAR(8)        NOT NULL REFERENCES dealers(dealer_id) ON DELETE CASCADE,
    name             TEXT           NOT NULL,
    description      TEXT,
    estimated_hours  NUMERIC(4, 2)  NOT NULL DEFAULT 1.0,
    base_price       NUMERIC(10, 2),
    active           BOOLEAN        NOT NULL DEFAULT TRUE,
    created_at       TIMESTAMPTZ    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_service_types_dealer_id ON service_types(dealer_id);


-- ============================================================
--  SERVICE BAYS
-- ============================================================
CREATE TABLE service_bays (
    bay_id      UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    dealer_id   CHAR(8)      NOT NULL REFERENCES dealers(dealer_id) ON DELETE CASCADE,
    name        TEXT         NOT NULL,
    active      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_service_bays_dealer_id ON service_bays(dealer_id);


-- ============================================================
--  TECHNICIANS
--  Service staff. Optionally linked to a user account.
-- ============================================================
CREATE TABLE technicians (
    technician_id   UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    dealer_id       CHAR(8)      NOT NULL REFERENCES dealers(dealer_id) ON DELETE CASCADE,
    user_id         UUID         REFERENCES users(user_id) ON DELETE SET NULL,
    first_name      TEXT         NOT NULL,
    last_name       TEXT         NOT NULL,
    email           TEXT,
    phone           TEXT,
    active          BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_technicians_dealer_id ON technicians(dealer_id);


-- ============================================================
--  CUSTOMER VEHICLES
--  Vehicles owned by customers (not dealer inventory).
--  Optionally linked to inventory if purchased from this dealer.
-- ============================================================
CREATE TABLE customer_vehicles (
    customer_vehicle_id  UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    dealer_id            CHAR(8)     NOT NULL REFERENCES dealers(dealer_id)   ON DELETE CASCADE,
    customer_id          UUID        NOT NULL REFERENCES customers(customer_id) ON DELETE CASCADE,
    vehicle_id           UUID        REFERENCES inventory(vehicle_id) ON DELETE SET NULL,
    vin                  CHAR(17),
    year                 SMALLINT,
    make                 TEXT,
    model                TEXT,
    trim                 TEXT,
    color                TEXT,
    license_plate        TEXT,
    mileage_at_purchase  INTEGER,
    created_at           TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_customer_vehicles_customer_id ON customer_vehicles(customer_id);
CREATE INDEX idx_customer_vehicles_dealer_id   ON customer_vehicles(dealer_id);


-- ============================================================
--  SERVICE APPOINTMENTS
-- ============================================================
CREATE TABLE service_appointments (
    appointment_id       UUID                PRIMARY KEY DEFAULT gen_random_uuid(),
    dealer_id            CHAR(8)             NOT NULL REFERENCES dealers(dealer_id)      ON DELETE CASCADE,
    customer_id          UUID                REFERENCES customers(customer_id)           ON DELETE SET NULL,
    customer_vehicle_id  UUID                REFERENCES customer_vehicles(customer_vehicle_id) ON DELETE SET NULL,
    bay_id               UUID                REFERENCES service_bays(bay_id)             ON DELETE SET NULL,
    assigned_to          UUID                REFERENCES technicians(technician_id)        ON DELETE SET NULL,
    status               appointment_status  NOT NULL DEFAULT 'requested',
    scheduled_at         TIMESTAMPTZ         NOT NULL,
    estimated_duration   NUMERIC(4, 2)       NOT NULL DEFAULT 1.0,
    customer_notes       TEXT,
    internal_notes       TEXT,
    reminder_sent        BOOLEAN             NOT NULL DEFAULT FALSE,
    created_at           TIMESTAMPTZ         NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMPTZ         NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_appointments_dealer_id    ON service_appointments(dealer_id);
CREATE INDEX idx_appointments_customer_id  ON service_appointments(customer_id);
CREATE INDEX idx_appointments_scheduled_at ON service_appointments(scheduled_at);
CREATE INDEX idx_appointments_status       ON service_appointments(status);


-- ============================================================
--  APPOINTMENT SERVICES  (many-to-many)
-- ============================================================
CREATE TABLE appointment_services (
    appointment_id   UUID  NOT NULL REFERENCES service_appointments(appointment_id) ON DELETE CASCADE,
    service_type_id  UUID  NOT NULL REFERENCES service_types(service_type_id)       ON DELETE CASCADE,
    PRIMARY KEY (appointment_id, service_type_id)
);


-- ============================================================
--  SERVICE ORDERS
--  Created at customer check-in. Tracks actual work done.
-- ============================================================
CREATE TABLE service_orders (
    order_id             UUID                  PRIMARY KEY DEFAULT gen_random_uuid(),
    dealer_id            CHAR(8)               NOT NULL REFERENCES dealers(dealer_id)      ON DELETE CASCADE,
    appointment_id       UUID                  REFERENCES service_appointments(appointment_id) ON DELETE SET NULL,
    customer_id          UUID                  REFERENCES customers(customer_id)            ON DELETE SET NULL,
    customer_vehicle_id  UUID                  REFERENCES customer_vehicles(customer_vehicle_id) ON DELETE SET NULL,
    technician_id        UUID                  REFERENCES technicians(technician_id)        ON DELETE SET NULL,
    bay_id               UUID                  REFERENCES service_bays(bay_id)              ON DELETE SET NULL,
    status               service_order_status  NOT NULL DEFAULT 'open',
    mileage_in           INTEGER,
    mileage_out          INTEGER,
    checked_in_at        TIMESTAMPTZ,
    completed_at         TIMESTAMPTZ,
    total_amount         NUMERIC(10, 2),
    notes                TEXT,
    created_at           TIMESTAMPTZ           NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMPTZ           NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_service_orders_dealer_id      ON service_orders(dealer_id);
CREATE INDEX idx_service_orders_customer_id    ON service_orders(customer_id);
CREATE INDEX idx_service_orders_appointment_id ON service_orders(appointment_id);
CREATE INDEX idx_service_orders_status         ON service_orders(status);


-- ============================================================
--  SERVICE ORDER LINE ITEMS
-- ============================================================
CREATE TABLE service_order_line_items (
    line_item_id     UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id         UUID            NOT NULL REFERENCES service_orders(order_id) ON DELETE CASCADE,
    service_type_id  UUID            REFERENCES service_types(service_type_id)    ON DELETE SET NULL,
    type             line_item_type  NOT NULL DEFAULT 'service',
    description      TEXT            NOT NULL,
    quantity         NUMERIC(8, 2)   NOT NULL DEFAULT 1,
    unit_price       NUMERIC(10, 2)  NOT NULL,
    total_price      NUMERIC(10, 2)  NOT NULL,
    created_at       TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_line_items_order_id ON service_order_line_items(order_id);


-- ============================================================
--  ROW LEVEL SECURITY
--  Java backend sets per request:
--    SET app.current_dealer_id = 'ABC12345'
-- ============================================================
ALTER TABLE dealers                   ENABLE ROW LEVEL SECURITY;
ALTER TABLE dealer_domains            ENABLE ROW LEVEL SECURITY;
ALTER TABLE users                     ENABLE ROW LEVEL SECURITY;
ALTER TABLE customers                 ENABLE ROW LEVEL SECURITY;
ALTER TABLE inventory                 ENABLE ROW LEVEL SECURITY;
ALTER TABLE promotions                ENABLE ROW LEVEL SECURITY;
ALTER TABLE vehicle_promotions        ENABLE ROW LEVEL SECURITY;
ALTER TABLE leads                     ENABLE ROW LEVEL SECURITY;
ALTER TABLE finance_applications      ENABLE ROW LEVEL SECURITY;
ALTER TABLE trade_in_requests         ENABLE ROW LEVEL SECURITY;
ALTER TABLE test_drive_appointments   ENABLE ROW LEVEL SECURITY;
ALTER TABLE sites                     ENABLE ROW LEVEL SECURITY;
ALTER TABLE conversations             ENABLE ROW LEVEL SECURITY;
ALTER TABLE conversation_participants ENABLE ROW LEVEL SECURITY;
ALTER TABLE messages                  ENABLE ROW LEVEL SECURITY;
ALTER TABLE message_receipts          ENABLE ROW LEVEL SECURITY;
ALTER TABLE call_sessions             ENABLE ROW LEVEL SECURITY;
ALTER TABLE campaigns                 ENABLE ROW LEVEL SECURITY;
ALTER TABLE campaign_recipients       ENABLE ROW LEVEL SECURITY;
ALTER TABLE reviews                   ENABLE ROW LEVEL SECURITY;
ALTER TABLE service_types             ENABLE ROW LEVEL SECURITY;
ALTER TABLE service_bays              ENABLE ROW LEVEL SECURITY;
ALTER TABLE technicians               ENABLE ROW LEVEL SECURITY;
ALTER TABLE customer_vehicles         ENABLE ROW LEVEL SECURITY;
ALTER TABLE service_appointments      ENABLE ROW LEVEL SECURITY;
ALTER TABLE appointment_services      ENABLE ROW LEVEL SECURITY;
ALTER TABLE service_orders            ENABLE ROW LEVEL SECURITY;
ALTER TABLE service_order_line_items  ENABLE ROW LEVEL SECURITY;

CREATE POLICY dealer_isolation ON dealers
    USING (dealer_id = current_setting('app.current_dealer_id'));

CREATE POLICY dealer_isolation ON dealer_domains
    USING (dealer_id = current_setting('app.current_dealer_id'));

CREATE POLICY dealer_isolation ON users
    USING (dealer_id = current_setting('app.current_dealer_id'));

CREATE POLICY dealer_isolation ON customers
    USING (dealer_id = current_setting('app.current_dealer_id'));

CREATE POLICY dealer_isolation ON inventory
    USING (dealer_id = current_setting('app.current_dealer_id'));

CREATE POLICY dealer_isolation ON promotions
    USING (dealer_id = current_setting('app.current_dealer_id'));

CREATE POLICY dealer_isolation ON vehicle_promotions
    USING (
        vehicle_id IN (
            SELECT vehicle_id FROM inventory
            WHERE dealer_id = current_setting('app.current_dealer_id')
        )
    );

CREATE POLICY dealer_isolation ON leads
    USING (dealer_id = current_setting('app.current_dealer_id'));

CREATE POLICY dealer_isolation ON finance_applications
    USING (dealer_id = current_setting('app.current_dealer_id'));

CREATE POLICY dealer_isolation ON trade_in_requests
    USING (dealer_id = current_setting('app.current_dealer_id'));

CREATE POLICY dealer_isolation ON test_drive_appointments
    USING (dealer_id = current_setting('app.current_dealer_id'));

CREATE POLICY dealer_isolation ON sites
    USING (dealer_id = current_setting('app.current_dealer_id'));

CREATE POLICY dealer_isolation ON conversations
    USING (dealer_id = current_setting('app.current_dealer_id'));

CREATE POLICY dealer_isolation ON conversation_participants
    USING (
        conversation_id IN (
            SELECT conversation_id FROM conversations
            WHERE dealer_id = current_setting('app.current_dealer_id')
        )
    );

CREATE POLICY dealer_isolation ON messages
    USING (
        conversation_id IN (
            SELECT conversation_id FROM conversations
            WHERE dealer_id = current_setting('app.current_dealer_id')
        )
    );

CREATE POLICY dealer_isolation ON message_receipts
    USING (
        message_id IN (
            SELECT m.message_id FROM messages m
            JOIN conversations c ON c.conversation_id = m.conversation_id
            WHERE c.dealer_id = current_setting('app.current_dealer_id')
        )
    );

CREATE POLICY dealer_isolation ON call_sessions
    USING (dealer_id = current_setting('app.current_dealer_id'));

CREATE POLICY dealer_isolation ON campaigns
    USING (dealer_id = current_setting('app.current_dealer_id'));

CREATE POLICY dealer_isolation ON campaign_recipients
    USING (
        campaign_id IN (
            SELECT campaign_id FROM campaigns
            WHERE dealer_id = current_setting('app.current_dealer_id')
        )
    );

CREATE POLICY dealer_isolation ON reviews
    USING (dealer_id = current_setting('app.current_dealer_id'));

CREATE POLICY dealer_isolation ON service_types
    USING (dealer_id = current_setting('app.current_dealer_id'));

CREATE POLICY dealer_isolation ON service_bays
    USING (dealer_id = current_setting('app.current_dealer_id'));

CREATE POLICY dealer_isolation ON technicians
    USING (dealer_id = current_setting('app.current_dealer_id'));

CREATE POLICY dealer_isolation ON customer_vehicles
    USING (dealer_id = current_setting('app.current_dealer_id'));

CREATE POLICY dealer_isolation ON service_appointments
    USING (dealer_id = current_setting('app.current_dealer_id'));

CREATE POLICY dealer_isolation ON appointment_services
    USING (
        appointment_id IN (
            SELECT appointment_id FROM service_appointments
            WHERE dealer_id = current_setting('app.current_dealer_id')
        )
    );

CREATE POLICY dealer_isolation ON service_orders
    USING (dealer_id = current_setting('app.current_dealer_id'));

CREATE POLICY dealer_isolation ON service_order_line_items
    USING (
        order_id IN (
            SELECT order_id FROM service_orders
            WHERE dealer_id = current_setting('app.current_dealer_id')
        )
    );


-- ============================================================
--  GRANTS
-- ============================================================
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO dealerbase_app;
GRANT SELECT ON ALL TABLES IN SCHEMA public TO dealerbase_readonly;
GRANT USAGE ON ALL SEQUENCES IN SCHEMA public TO dealerbase_app;


-- ============================================================
--  S3 BUCKET LAYOUT (reference)
--
--  dealerbase-media/
--    sites/
--      {dealer_id}/
--        site.json                     <- nav, theme, page index, global SEO
--        pages/
--          {slug}/
--            content.json              <- block editor JSON (source of truth)
--            index.html                <- pre-rendered HTML (CDN served)
--        assets/
--          logo.png
--          banner.jpg
--    chat/
--      {dealer_id}/
--        {conversation_id}/
--          {message_id}.jpg            <- image/file attachments
--    calls/
--      {dealer_id}/
--        {year}/{month}/
--          {call_id}.mp4               <- video recordings (Agora)
--          {call_id}.mp3               <- voice recordings (Twilio)
--    archives/
--      {dealer_id}/
--        {year}/{month}/
--          {conversation_id}.json      <- archived chat history
--
-- ============================================================


-- ============================================================
--  SITE SAVE FLOW (reference)
--
--  Editor saves a page:
--    1. Upload content.json  -> S3 sites/{dealer_id}/pages/{slug}/content.json
--    2. Render HTML via TemplateEngine
--    3. Upload index.html    -> S3 sites/{dealer_id}/pages/{slug}/index.html
--    4. Update site.json     -> S3 sites/{dealer_id}/site.json
--    5. Invalidate CloudFront cache for /{slug}/*
--    6. UPDATE sites SET last_modified = NOW() WHERE dealer_id = ?
--
--  Site published:
--    UPDATE sites SET status = 'published', published_at = NOW() WHERE dealer_id = ?
--
--  Visitor loads page:
--    CloudFront serves index.html directly — zero DB hit.
--
-- ============================================================


-- ============================================================
--  CHAT ARCHIVAL JOB (run nightly)
--
--  Step 1 — Find conversations inactive 90+ days:
--
--    SELECT conversation_id, dealer_id
--    FROM   conversations
--    WHERE  archived   = FALSE
--    AND    updated_at < NOW() - INTERVAL '90 days';
--
--  Step 2 — Fetch all messages per conversation:
--
--    SELECT * FROM messages
--    WHERE  conversation_id = ?
--    ORDER  BY created_at ASC;
--
--  Step 3 — Serialize to JSON and upload to S3:
--
--    key = archives/{dealer_id}/{year}/{month}/{conversation_id}.json
--
--  Step 4 — Mark archived in Postgres:
--
--    UPDATE conversations
--    SET    archived        = TRUE,
--           archived_at     = NOW(),
--           archive_s3_key  = ?
--    WHERE  conversation_id = ?;
--
--  Step 5 — Delete hot messages (keep conversation row):
--
--    DELETE FROM messages WHERE conversation_id = ?;
--
-- ============================================================


-- ============================================================
--  ARCHIVE JSON FORMAT (reference)
--
--  {
--    "conversation_id": "uuid",
--    "dealer_id":       "ABC12345",
--    "type":            "deal_room",
--    "lead_id":         "uuid",
--    "vehicle_id":      "uuid",
--    "archived_at":     "2026-03-01T10:30:00Z",
--    "participants": [
--      { "participant_id": "uuid", "participant_type": "user",     "name": "John Smith" },
--      { "participant_id": "uuid", "participant_type": "customer", "name": "Jane Doe"   }
--    ],
--    "messages": [
--      {
--        "message_id":   "uuid",
--        "sender_id":    "uuid",
--        "sender_type":  "customer",
--        "body":         "Is the 2024 Tacoma still available?",
--        "message_type": "text",
--        "attachments":  null,
--        "created_at":   "2025-10-01T14:22:00Z"
--      }
--    ]
--  }
--
-- ============================================================