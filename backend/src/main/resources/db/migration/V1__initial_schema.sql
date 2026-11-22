CREATE TABLE app_users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(80) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL
);

CREATE TABLE locations (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(160) NOT NULL UNIQUE
);

CREATE TABLE assets (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    asset_tag VARCHAR(80) NOT NULL UNIQUE,
    type VARCHAR(80) NOT NULL,
    location_id BIGINT NOT NULL REFERENCES locations(id),
    status VARCHAR(24) NOT NULL,
    threshold DOUBLE PRECISION NOT NULL CHECK (threshold >= 0),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_assets_location ON assets(location_id);

CREATE TABLE technicians (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    email VARCHAR(180) NOT NULL UNIQUE,
    active BOOLEAN NOT NULL DEFAULT true
);

CREATE TABLE sensor_readings (
    id BIGSERIAL PRIMARY KEY,
    event_id UUID NOT NULL UNIQUE,
    asset_id BIGINT NOT NULL REFERENCES assets(id),
    metric VARCHAR(80) NOT NULL,
    value DOUBLE PRECISION NOT NULL,
    recorded_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_sensor_readings_asset_recorded ON sensor_readings(asset_id, recorded_at DESC);

CREATE TABLE work_orders (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(180) NOT NULL,
    description VARCHAR(2000) NOT NULL,
    status VARCHAR(24) NOT NULL,
    priority VARCHAR(16) NOT NULL,
    asset_id BIGINT NOT NULL REFERENCES assets(id),
    technician_id BIGINT REFERENCES technicians(id),
    source_reading_id BIGINT UNIQUE REFERENCES sensor_readings(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_work_orders_status_created ON work_orders(status, created_at DESC);
CREATE INDEX idx_work_orders_asset ON work_orders(asset_id);
