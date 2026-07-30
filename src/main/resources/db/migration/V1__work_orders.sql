CREATE TABLE work_orders
(
    id          VARCHAR(64) PRIMARY KEY,
    machine_id  VARCHAR(64) NOT NULL,
    status      VARCHAR(32) NOT NULL,
    reason      VARCHAR(32) NOT NULL,
    reported_by VARCHAR(64) NOT NULL,
    assigned_to VARCHAR(64),
    resolution  TEXT,
    hold_reason TEXT,
    reported_at TIMESTAMPTZ NOT NULL,
    started_at  TIMESTAMPTZ,
    resolved_at TIMESTAMPTZ
);