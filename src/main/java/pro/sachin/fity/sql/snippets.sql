CREATE TABLE members
(
    member_id  BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    first_name VARCHAR(100) NOT NULL,
    last_name  VARCHAR(100) NOT NULL,
    dob        DATE,
    phone      VARCHAR(30),
    email      VARCHAR(255),
    join_date  DATE         NOT NULL DEFAULT CURRENT_DATE,
    status     VARCHAR(20)  NOT NULL DEFAULT 'active'

);

# TODO ask why this member attributes

CREATE TABLE member_attributes
(
    member_attribute_id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    member_id           BIGINT       NOT NULL REFERENCES members (member_id),
    attribute_key       VARCHAR(100) NOT NULL,
    attribute_value     TEXT,
    updated_at          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- this is indexing used for seaching, to speed up seaching
CREATE INDEX idx_member_attributes_member ON member_attributes (member_id);


CREATE TABLE plans
(
    plan_id            BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    name               VARCHAR(100)   NOT NULL,
    duration_days      INT            NOT NULL, -- ex: 30, 90, 180
    price              DECIMAL(10, 2) NOT NULL,
    plan_type          VARCHAR(20)    NOT NULL, -- individual/kids/family
    age_min            INT,
    age_max            INT,
    max_family_members INT,
    is_active          BOOLEAN        NOT NULL DEFAULT TRUE
);

CREATE TABLE families
(
    family_id   BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    family_name VARCHAR(200),
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE family_members
(
    family_id BIGINT      NOT NULL REFERENCES families (family_id),
    member_id BIGINT      NOT NULL REFERENCES members (member_id),
    role      VARCHAR(20) NOT NULL DEFAULT 'member', -- primary/spouse/child
    PRIMARY KEY (family_id, member_id)
);

CREATE TABLE users
(
    user_id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    name    VARCHAR(200) NOT NULL,
    role    VARCHAR(30)  NOT NULL, -- coach/admin/reception
    active  BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE TABLE subscriptions
(
    subscription_id    BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    member_id          BIGINT REFERENCES members (member_id),
    family_id          BIGINT REFERENCES families (family_id),
    plan_id            BIGINT      NOT NULL REFERENCES plans (plan_id),

    start_date         DATE        NOT NULL,
    end_date           DATE        NOT NULL,
    due_date           DATE        NOT NULL,
    grace_end_date     DATE        NOT NULL,

    status             VARCHAR(20) NOT NULL DEFAULT 'active', -- active/in_grace/blocked/ended
    created_by_user_id BIGINT REFERENCES users (user_id),
    created_at         TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CHECK (
        (member_id IS NOT NULL AND family_id IS NULL)
            OR (member_id IS NULL AND family_id IS NOT NULL)
        )
);

CREATE INDEX idx_subscriptions_member_dates ON subscriptions (member_id, end_date, grace_end_date);
CREATE INDEX idx_subscriptions_family_dates ON subscriptions (family_id, end_date, grace_end_date);

# TODO : ask what is the connection between the subscription and subscription charges

CREATE TABLE subscription_charges
(
    charge_id       BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    subscription_id BIGINT         NOT NULL UNIQUE REFERENCES subscriptions (subscription_id),
    total_amount    DECIMAL(10, 2) NOT NULL,
    discount_amount DECIMAL(10, 2) NOT NULL DEFAULT 0,
    net_amount      DECIMAL(10, 2) NOT NULL,
    created_at      TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE payments
(
    payment_id          BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    subscription_id     BIGINT         NOT NULL REFERENCES subscriptions (subscription_id),
    amount              DECIMAL(10, 2) NOT NULL,
    paid_on             TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    received_by_user_id BIGINT REFERENCES users (user_id),
    receipt_no          VARCHAR(50),
    notes               TEXT
);
CREATE INDEX idx_payments_subscription ON payments (subscription_id, paid_on);

# TODO : Where doest it connect here?

CREATE TABLE grace_extensions
(
    extension_id        BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    subscription_id     BIGINT    NOT NULL REFERENCES subscriptions (subscription_id),
    extended_by_user_id BIGINT    NOT NULL REFERENCES users (user_id),
    old_grace_end_date  DATE      NOT NULL,
    new_grace_end_date  DATE      NOT NULL,
    reason              TEXT,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE member_access
(
    member_id          BIGINT PRIMARY KEY REFERENCES members (member_id),
    access_status      VARCHAR(10) NOT NULL, -- allowed/blocked
    allowed_until      DATE,
    reason             VARCHAR(30),
    updated_at         TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by_user_id BIGINT REFERENCES users (user_id)
);

CREATE TABLE attendance
(
    attendance_id   BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    member_id       BIGINT      NOT NULL REFERENCES members (member_id),
    check_in_time   TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    captured_by     VARCHAR(20) NOT NULL DEFAULT 'door', -- door/manual
    subscription_id BIGINT REFERENCES subscriptions (subscription_id)
);
CREATE INDEX idx_attendance_member_time ON attendance (member_id, check_in_time);

CREATE TABLE notification_queue
(
    notification_id   BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    member_id         BIGINT      NOT NULL REFERENCES members (member_id),
    subscription_id   BIGINT      NOT NULL REFERENCES subscriptions (subscription_id),
    notification_type VARCHAR(30) NOT NULL, -- renewal_reminder/grace_warning/blocked_notice
    scheduled_for     TIMESTAMP   NOT NULL,
    sent_at           TIMESTAMP,
    status            VARCHAR(20) NOT NULL DEFAULT 'pending',
    channel           VARCHAR(20) NOT NULL DEFAULT 'sms'
);
CREATE INDEX idx_notify_pending ON notification_queue (status, scheduled_for);

#  balance due calculation

SELECT s.subscription_id,
       c.net_amount,
       COALESCE(SUM(p.amount), 0)                  AS paid_amount,
       (c.net_amount - COALESCE(SUM(p.amount), 0)) AS balance
FROM subscriptions s
         JOIN subscription_charges c ON c.subscription_id = s.subscription_id
         LEFT JOIN payments p ON p.subscription_id = s.subscription_id
WHERE s.subscription_id = :subscription_id
GROUP BY s.subscription_id, c.net_amount;

# falling behind checking TODO : ask this

SELECT m.member_id, m.first_name, m.last_name, s.due_date
FROM subscriptions s
         JOIN members m ON m.member_id = s.member_id
WHERE CURRENT_DATE <= s.due_date
  AND s.due_date <= CURRENT_DATE + INTERVAL '7 days';

# falling behind list TODO : this connects to the above list check

SELECT m.member_id,
       m.first_name,
       m.last_name,
       s.end_date,
       s.grace_end_date,
       (c.net_amount - COALESCE(SUM(p.amount), 0)) AS balance
FROM subscriptions s
         JOIN members m ON m.member_id = s.member_id
         JOIN subscription_charges c ON c.subscription_id = s.subscription_id
         LEFT JOIN payments p ON p.subscription_id = s.subscription_id
WHERE CURRENT_DATE > s.end_date
  AND CURRENT_DATE <= s.grace_end_date
GROUP BY m.member_id, m.first_name, m.last_name, s.end_date, s.grace_end_date, c.net_amount
HAVING (c.net_amount - COALESCE(SUM(p.amount), 0)) > 0
ORDER BY balance DESC;

# blocked members

SELECT m.member_id, m.first_name, m.last_name, s.grace_end_date
FROM subscriptions s
         JOIN members m ON m.member_id = s.member_id
WHERE CURRENT_DATE > s.grace_end_date;

# no visit

SELECT m.member_id, m.first_name, m.last_name, MAX(a.check_in_time) AS last_visit
FROM members m
         LEFT JOIN attendance a ON a.member_id = m.member_id
GROUP BY m.member_id, m.first_name, m.last_name
HAVING MAX(a.check_in_time) IS NULL
    OR MAX(a.check_in_time) < CURRENT_TIMESTAMP - INTERVAL '7 days'
ORDER BY last_visit;
