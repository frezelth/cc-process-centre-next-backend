CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE TABLE T_STATIC_TRANSLATION
(
    object_type     TEXT        NOT NULL,
    object_id       VARCHAR(100)     NOT NULL,
    attribute_name  TEXT        NOT NULL,
    language_code   VARCHAR(10) NOT NULL,
    translated_text TEXT        NOT NULL,
    is_default      BOOLEAN DEFAULT FALSE,
    updated_at      TIMESTAMPTZ DEFAULT now(),

    PRIMARY KEY (object_type, object_id, attribute_name, language_code)
);

-- CREATE INDEX IDX_STATIC_TRANSLATION
--     ON T_STATIC_TRANSLATION (object_type, object_id, attribute_name, language_code);

-- CREATE INDEX idx_t_static_translation_object_lang
--     ON t_static_translation (object_type, object_id, language_code);

-- CREATE INDEX idx_t_static_translation_object_id
--     ON t_static_translation (object_id);

CREATE INDEX idx_t_static_translation_object_id_type
    ON t_static_translation (object_id, object_type);

-- CREATE INDEX idx_t_static_translation_object_lang_default
--     ON t_static_translation (object_type, object_id, language_code, is_default);

CREATE INDEX IDX_STATIC_TRANSLATION_TEXT_TRGM
    ON T_STATIC_TRANSLATION
    USING gin (translated_text gin_trgm_ops);

CREATE TABLE T_BABEL_TRANSLATION
(
    urn TEXT        NOT NULL,
    language_code   VARCHAR(10) NOT NULL,
    translated_text TEXT        NOT NULL,
    updated_at      TIMESTAMPTZ DEFAULT now(),
    is_default      BOOLEAN     DEFAULT FALSE,
    PRIMARY KEY (urn, language_code)
);

CREATE INDEX IDX_BABEL_TRANSLATION_TEXT_TRGM
    ON T_BABEL_TRANSLATION
    USING gin (translated_text gin_trgm_ops);

CREATE TABLE T_BABEL_TRANSLATION_LINK
(
    object_type     VARCHAR(50)  NOT NULL,
    object_id       VARCHAR(100) NOT NULL,
    attribute_name  TEXT         NOT NULL,
    urn TEXT         NOT NULL,
    updated_at      TIMESTAMPTZ DEFAULT now(),

    PRIMARY KEY (object_type, object_id, attribute_name)
);

-- CREATE INDEX idx_t_static_translation_object_lang
--     ON t_static_translation (object_type, object_id, language_code);

CREATE INDEX IDX_BABEL_TRANSLATION_LINK
    ON T_BABEL_TRANSLATION_LINK (object_type, object_id);

CREATE TABLE T_PROCESS
(
    PROCESS_INSTANCE_ID           VARCHAR(100) NOT NULL PRIMARY KEY,
    DOMAIN_KEY                    VARCHAR(255),
    PROVIDER_ID                   VARCHAR(255),
    PROCESS_TYPE_KEY              VARCHAR(1000),
    CREATED_ON                    TIMESTAMPTZ DEFAULT now(),
    STARTED_ON                    TIMESTAMPTZ,
    COMPLETED_ON                  TIMESTAMPTZ,
    CANCELLED_ON                  TIMESTAMPTZ,
    PAUSED_ON                     TIMESTAMPTZ,
    RESTARTED_ON                  TIMESTAMPTZ,
    CANCELLED_BY VARCHAR(100),
    CANCELLED_BY_ON_BEHALF_OF VARCHAR(100),
    COMPLETED_BY VARCHAR(100),
    COMPLETED_BY_ON_BEHALF_OF VARCHAR(100),
    BUSINESS_STATUS               TEXT,
    RESPONSIBLE_ORGANISATION_ID   VARCHAR(100),
    RESPONSIBLE_ORGANISATION_CODE VARCHAR(100),
    SECURITY_APPLICATION_ID       TEXT,
    SECURITY_SECUNDA_TASK         TEXT,
    SECURITY_SCOPE_TYPE_ID        TEXT,
    SECURITY_SCOPE_ID       TEXT,
    SECURITY_SCOPE_ID_VALUE       TEXT,
    PARENT_ID                     VARCHAR(50),
    ASSOCIATED_PORTFOLIO_ITEM_IDS  TEXT[],
    STARTED_BY VARCHAR(100),
    STARTED_BY_ON_BEHALF_OF VARCHAR(100),
    TITLE_TEMPLATE TEXT,
    CANCELLED BOOLEAN DEFAULT FALSE,
    COMPLETED BOOLEAN DEFAULT FALSE
);

CREATE INDEX IDX_PROCESS
    ON T_PROCESS (PARENT_ID);

CREATE TABLE T_PROCESS_BUSINESS_STATE
(
    PROCESS_INSTANCE_ID VARCHAR(100) NOT NULL,
    BUSINESS_STATE VARCHAR(255),
    TIMESTAMP          TIMESTAMPTZ NOT NULL,

    FOREIGN KEY (PROCESS_INSTANCE_ID) REFERENCES T_PROCESS (PROCESS_INSTANCE_ID)
);

CREATE INDEX IDX_PROCESS_BUSINESS_STATE
    ON T_PROCESS_BUSINESS_STATE (PROCESS_INSTANCE_ID);

CREATE TABLE T_USER_TASK
(
    TASK_INSTANCE_ID                  VARCHAR(100) PRIMARY KEY,
    PROCESS_INSTANCE_ID               VARCHAR(100) NOT NULL,
    TASK_TYPE_KEY                     VARCHAR(100),
    CREATED_ON                        TIMESTAMPTZ DEFAULT now(),
    CLAIMED_ON                        TIMESTAMPTZ,
    COMPLETED_ON                      TIMESTAMPTZ,
    CANCELLED_ON                      TIMESTAMPTZ,
    CANCELLED_BY                      VARCHAR(100),
    CANCELLED_BY_ON_BEHALF_OF                      VARCHAR(100),
    JUSTIFICATION                     TEXT,
    CLAIMED_BY                        VARCHAR(100),
    CLAIMED_BY_FIRST_NAME             VARCHAR(500),
    CLAIMED_BY_LAST_NAME              VARCHAR(500),
    CLAIMED_BY_USER_ORG               VARCHAR(200),
    COMPLETED_BY                      VARCHAR(100),
    COMPLETED_BY_FIRST_NAME           VARCHAR(500),
    COMPLETED_BY_LAST_NAME            VARCHAR(500),
    COMPLETED_BY_USER_ORG             VARCHAR(200),
    CLAIMED_ON_BEHALF_OF              VARCHAR(100),
    CLAIMED_ON_BEHALF_OF_FIRST_NAME   VARCHAR(500),
    CLAIMED_ON_BEHALF_OF_LAST_NAME    VARCHAR(500),
    CLAIMED_ON_BEHALF_OF_USER_ORG     VARCHAR(200),
    COMPLETED_ON_BEHALF_OF            VARCHAR(100),
    COMPLETED_ON_BEHALF_OF_FIRST_NAME VARCHAR(500),
    COMPLETED_ON_BEHALF_OF_LAST_NAME  VARCHAR(500),
    COMPLETED_ON_BEHALF_OF_USER_ORG   VARCHAR(200),
        CANCELLED BOOLEAN DEFAULT FALSE,
    COMPLETED BOOLEAN DEFAULT FALSE
);

CREATE INDEX IDX_USER_TASK_PROCESS_INSTANCE_ID
    ON T_USER_TASK (PROCESS_INSTANCE_ID);

CREATE INDEX IDX_USER_TASK_CANCELLED
    ON T_USER_TASK (CANCELLED);

CREATE INDEX IDX_USER_TASK_COMPLETED
    ON T_USER_TASK (COMPLETED);

CREATE TABLE T_SERVICE_TASK
(
    TASK_INSTANCE_ID    VARCHAR(100) PRIMARY KEY,
    PROCESS_INSTANCE_ID VARCHAR(100) NOT NULL,
    TASK_TYPE_KEY       VARCHAR(100),
    CREATED_ON          TIMESTAMPTZ,
    COMPLETED_ON        TIMESTAMPTZ,
    CANCELLED_ON        TIMESTAMPTZ,
    LOCKED_ON           TIMESTAMPTZ,
    FAILED_ON           TIMESTAMPTZ,
    ERROR_MSG           TEXT,
    INFO_MSG            TEXT
);

CREATE INDEX IDX_SERVICE_TASK
    ON T_SERVICE_TASK (PROCESS_INSTANCE_ID);

CREATE TABLE T_PROCESS_VARIABLE
(
    PROCESS_INSTANCE_ID VARCHAR(100),
    NAME                VARCHAR(500),
    VALUE_TYPE          VARCHAR(20),
    MIME_TYPE           VARCHAR(255),
    VALUE_STRING        TEXT,
    VALUE_INTEGER       INTEGER,
    VALUE_LONG          NUMERIC(19, 0),
    VALUE_DOUBLE        NUMERIC(19, 5),
    VALUE_BOOLEAN       BOOLEAN,
    VALUE_JSON          JSONB,
    VALUE_BYTE          BYTEA,
    VALUE_DATE          TIMESTAMPTZ,
    CREATED_ON          TIMESTAMPTZ DEFAULT now(),
    UPDATED_ON          TIMESTAMPTZ DEFAULT now(),

    PRIMARY KEY (PROCESS_INSTANCE_ID, NAME),
    FOREIGN KEY (PROCESS_INSTANCE_ID) REFERENCES T_PROCESS(PROCESS_INSTANCE_ID)
);

CREATE INDEX IDX_PROCESS_VARIABLE
    ON T_PROCESS_VARIABLE (PROCESS_INSTANCE_ID);

CREATE TABLE T_PROCESS_TAG
(
    process_instance_id VARCHAR(100),
    catalog_id          TEXT,
    tag_id              TEXT,

    PRIMARY KEY (process_instance_id, catalog_id, tag_id),
    FOREIGN KEY (process_instance_id) REFERENCES T_PROCESS (process_instance_id)
);

CREATE VIEW V_RESOLVED_TRANSLATION AS
-- Internal translations
SELECT
    'static' AS source,
    t.object_type,
    t.object_id,
    t.attribute_name,
    t.language_code,
    t.translated_text,
    t.is_default,
    NULL AS urn,
    t.updated_at
FROM T_STATIC_TRANSLATION t
UNION ALL
-- External translations
SELECT
    'babel' AS source,
    l.object_type,
    l.object_id,
    l.attribute_name,
    e.language_code,
    e.translated_text,
    e.is_default,
    l.urn,
    e.updated_at
FROM T_BABEL_TRANSLATION_LINK l
         JOIN T_BABEL_TRANSLATION e ON l.urn = e.urn;


-- METRO_LINE
CREATE TABLE T_METRO_LINE_FILE (
    ID SERIAL PRIMARY KEY,
    TAXONOMY_PATH TEXT UNIQUE NOT NULL,
    CONTENT TEXT,
    DESCRIPTION TEXT
);

CREATE TABLE T_METRO_LINE_MAPPING (
   TAXONOMY_PATH TEXT NOT NULL,
   STATION_NAME TEXT,
   CATEGORY VARCHAR(50),
   PROCESS_TYPE_ID VARCHAR(500),
   TASK_TYPE_ID VARCHAR(500),
   FOREIGN KEY (TAXONOMY_PATH) REFERENCES T_METRO_LINE_FILE (TAXONOMY_PATH)
);

CREATE INDEX IDX_METRO_LINE_MAPPING
    ON T_METRO_LINE_MAPPING (TAXONOMY_PATH);