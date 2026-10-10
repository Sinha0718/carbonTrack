-- CarbonTrack core schema (Milestone 1)

CREATE TABLE organisations (
    id            BIGSERIAL PRIMARY KEY,
    name          VARCHAR(150) NOT NULL UNIQUE,
    admin_user_id BIGINT,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE users (
    id                      BIGSERIAL PRIMARY KEY,
    username                VARCHAR(50)  NOT NULL UNIQUE,
    email                   VARCHAR(255) NOT NULL UNIQUE,
    password_hash           VARCHAR(255),
    display_name            VARCHAR(100),
    role                    VARCHAR(20)  NOT NULL DEFAULT 'USER'
        CHECK (role IN ('USER', 'ADMIN', 'ORG_ADMIN')),
    auth_provider           VARCHAR(20)  NOT NULL DEFAULT 'LOCAL'
        CHECK (auth_provider IN ('LOCAL', 'GOOGLE')),
    provider_id             VARCHAR(255),
    org_id                  BIGINT REFERENCES organisations (id) ON DELETE SET NULL,
    preferred_units         VARCHAR(20)  NOT NULL DEFAULT 'METRIC'
        CHECK (preferred_units IN ('METRIC', 'IMPERIAL')),
    goal_visibility         VARCHAR(20)  NOT NULL DEFAULT 'PRIVATE'
        CHECK (goal_visibility IN ('PRIVATE', 'ORGANISATION', 'PUBLIC')),
    leaderboard_opt_in      BOOLEAN      NOT NULL DEFAULT TRUE,
    weekly_insights_enabled BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at              TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at              TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_users_org_id ON users (org_id);

ALTER TABLE organisations
    ADD CONSTRAINT fk_organisations_admin_user
        FOREIGN KEY (admin_user_id) REFERENCES users (id) ON DELETE SET NULL;

CREATE TABLE emission_factors (
    id               BIGSERIAL PRIMARY KEY,
    category         VARCHAR(20)    NOT NULL
        CHECK (category IN ('TRANSPORT', 'ELECTRICITY', 'FOOD', 'SHOPPING')),
    activity_type    VARCHAR(64)    NOT NULL,
    unit             VARCHAR(32)    NOT NULL,
    kg_co2e_per_unit NUMERIC(12, 6) NOT NULL CHECK (kg_co2e_per_unit >= 0),
    source           VARCHAR(255)   NOT NULL,
    effective_date   DATE           NOT NULL,
    created_at       TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ    NOT NULL DEFAULT now(),
    CONSTRAINT uq_emission_factor_version UNIQUE (activity_type, unit, effective_date)
);

CREATE INDEX idx_emission_factors_lookup ON emission_factors (activity_type, unit, effective_date DESC);

CREATE TABLE activity_logs (
    id                 BIGSERIAL PRIMARY KEY,
    user_id            BIGINT         NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    category           VARCHAR(20)    NOT NULL
        CHECK (category IN ('TRANSPORT', 'ELECTRICITY', 'FOOD', 'SHOPPING')),
    activity_type      VARCHAR(64)    NOT NULL,
    quantity           NUMERIC(14, 4) NOT NULL CHECK (quantity >= 0),
    unit               VARCHAR(32)    NOT NULL,
    co2e_kg            NUMERIC(14, 4) NOT NULL CHECK (co2e_kg >= 0),
    emission_factor_id BIGINT         REFERENCES emission_factors (id) ON DELETE SET NULL,
    log_date           DATE           NOT NULL,
    notes              VARCHAR(500),
    created_at         TIMESTAMPTZ    NOT NULL DEFAULT now()
);

CREATE INDEX idx_activity_logs_user_date ON activity_logs (user_id, log_date);
CREATE INDEX idx_activity_logs_user_category_date ON activity_logs (user_id, category, log_date);

CREATE TABLE goals (
    id                   BIGSERIAL PRIMARY KEY,
    user_id              BIGINT        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    target_reduction_pct NUMERIC(5, 2) NOT NULL CHECK (target_reduction_pct > 0 AND target_reduction_pct <= 100),
    period_days          INTEGER       NOT NULL CHECK (period_days > 0),
    start_date           DATE          NOT NULL,
    status               VARCHAR(20)   NOT NULL DEFAULT 'ACTIVE'
        CHECK (status IN ('ACTIVE', 'ACHIEVED', 'MISSED')),
    created_at           TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE INDEX idx_goals_user_status ON goals (user_id, status);

CREATE TABLE badges (
    id           BIGSERIAL PRIMARY KEY,
    name         VARCHAR(100)   NOT NULL UNIQUE,
    description  VARCHAR(255)   NOT NULL,
    trigger_type VARCHAR(20)    NOT NULL CHECK (trigger_type IN ('STREAK', 'GOAL', 'REDUCTION')),
    threshold    NUMERIC(12, 2) NOT NULL
);

CREATE TABLE user_badges (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT      NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    badge_id   BIGINT      NOT NULL REFERENCES badges (id) ON DELETE CASCADE,
    awarded_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_user_badge UNIQUE (user_id, badge_id)
);
