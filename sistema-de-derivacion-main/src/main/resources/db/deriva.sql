BEGIN;

-- ==============================================================================
--   FUNCIONES Y TRIGGERS
-- ==============================================================================
CREATE OR REPLACE FUNCTION update_modified_timestamp()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- ==============================================================================
--   CREACIÓN DE TABLAS
-- ==============================================================================

-- Empresa
CREATE TABLE company (
    company_id BIGSERIAL PRIMARY KEY,
    tax_id VARCHAR(11) UNIQUE NOT NULL,
    legal_name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Rol del usuario
CREATE TABLE role (
    role_id SERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    description TEXT
);

-- Área interna
CREATE TABLE department (
    department_id SERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    description TEXT
);

-- Estado de la incidencia
CREATE TABLE incident_status (
    incident_status_id SERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    description TEXT
);

-- Usuario
CREATE TABLE user_account (
    user_account_id BIGSERIAL PRIMARY KEY,
    department_id INT REFERENCES department(department_id) ON DELETE SET NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Tabla intermedia de Usuario con Rol
CREATE TABLE user_account_role (
    user_account_id BIGINT REFERENCES user_account(user_account_id) ON DELETE CASCADE,
    role_id INT REFERENCES role(role_id) ON DELETE CASCADE,
    PRIMARY KEY (user_account_id, role_id)
);

-- Tabla intermedia de Usuario con Empresa
CREATE TABLE user_account_company (
    user_account_id BIGINT REFERENCES user_account(user_account_id) ON DELETE CASCADE,
    company_id BIGINT REFERENCES company(company_id) ON DELETE CASCADE,
    PRIMARY KEY (user_account_id, company_id)
);

-- Incidencia / reclamo
CREATE TABLE incident (
    incident_id BIGSERIAL PRIMARY KEY,
    client_id BIGINT NOT NULL REFERENCES user_account(user_account_id) ON DELETE RESTRICT,
    company_id BIGINT NOT NULL REFERENCES company(company_id) ON DELETE RESTRICT,
    advisor_id BIGINT REFERENCES user_account(user_account_id) ON DELETE SET NULL,
    department_id INT REFERENCES department(department_id) ON DELETE SET NULL,
    assigned_to_id BIGINT REFERENCES user_account(user_account_id) ON DELETE SET NULL,
    incident_status_id INT NOT NULL REFERENCES incident_status(incident_status_id) ON DELETE RESTRICT,
    code VARCHAR(30) UNIQUE,
    priority VARCHAR(20) DEFAULT 'MEDIA',
    title VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    photo_url VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_incident_priority CHECK (
        priority IS NULL OR priority IN ('BAJA', 'MEDIA', 'ALTA', 'CRITICA')
    )
);

-- Historial de la indicencia
CREATE TABLE incident_history (
    incident_history_id BIGSERIAL PRIMARY KEY,
    incident_id BIGINT NOT NULL REFERENCES incident(incident_id) ON DELETE CASCADE,
    action_user_id BIGINT NOT NULL REFERENCES user_account(user_account_id) ON DELETE RESTRICT,
    previous_status_id INT REFERENCES incident_status(incident_status_id) ON DELETE SET NULL,
    new_status_id INT NOT NULL REFERENCES incident_status(incident_status_id) ON DELETE RESTRICT,
    comment_text TEXT,
    evidence_url VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- ==============================================================================
--    ASIGNACIÓN DE TRIGGERS
-- ==============================================================================
CREATE TRIGGER trg_company_update BEFORE UPDATE ON company FOR EACH ROW EXECUTE FUNCTION update_modified_timestamp();
CREATE TRIGGER trg_user_account_update BEFORE UPDATE ON user_account FOR EACH ROW EXECUTE FUNCTION update_modified_timestamp();
CREATE TRIGGER trg_incident_update BEFORE UPDATE ON incident FOR EACH ROW EXECUTE FUNCTION update_modified_timestamp();

-- ==============================================================================
--    CREACIÓN DE ÍNDICES
-- ==============================================================================
CREATE INDEX idx_user_account_department ON user_account(department_id);
CREATE INDEX idx_user_account_role_role ON user_account_role(role_id);
CREATE INDEX idx_user_account_company_company ON user_account_company(company_id);
CREATE INDEX idx_incident_client ON incident(client_id);
CREATE INDEX idx_incident_company ON incident(company_id);
CREATE INDEX idx_incident_advisor ON incident(advisor_id);
CREATE INDEX idx_incident_department ON incident(department_id);
CREATE INDEX idx_incident_assigned_to ON incident(assigned_to_id);
CREATE INDEX idx_incident_status ON incident(incident_status_id);
CREATE INDEX idx_incident_history_incident ON incident_history(incident_id);
CREATE INDEX idx_incident_history_actor ON incident_history(action_user_id);

-- ==============================================================================
--    INSERCIÓN DE DATOS DE CATÁLOGO
-- ==============================================================================

-- Insertar Roles (Incluyendo al Administrador)
INSERT INTO role (role_id, name, description) VALUES
(1, 'CLIENTE', 'Cliente externo corporativo'),
(2, 'ASESOR', 'Asesor que evalúa y deriva incidencias'),
(3, 'TECNICO', 'Personal técnico de áreas internas'),
(4, 'ADMINISTRADOR', 'Administrador del sistema');

-- Insertar Departamentos
INSERT INTO department (department_id, name, description) VALUES
(1, 'Logística', 'Gestión de envíos, transportes y despachos'),
(2, 'Software', 'Desarrollo, sistemas e infraestructura IT'),
(3, 'Calidad', 'Control de calidad de productos y suministros'),
(4, 'Facturación', 'Comprobantes, facturas y cobranzas');

-- Insertar Estados de Incidencia
INSERT INTO incident_status (incident_status_id, name, description) VALUES
(1, 'Pendiente', 'Recién creado por el cliente, en espera de derivación'),
(2, 'Asignado', 'Derivado a un área interna específica'),
(3, 'En Proceso', 'El área técnica está trabajando en la solución'),
(4, 'Solucionado', 'Incidencia resuelta exitosamente'),
(5, 'Rechazado', 'Incidencia desestimada o improcedente');

-- ==============================================================================
--    REAJUSTE DE SECUENCIAS
-- ==============================================================================
SELECT setval(pg_get_serial_sequence('role', 'role_id'), (SELECT MAX(role_id) FROM role));
SELECT setval(pg_get_serial_sequence('department', 'department_id'), (SELECT MAX(department_id) FROM department));
SELECT setval(pg_get_serial_sequence('incident_status', 'incident_status_id'), (SELECT MAX(incident_status_id) FROM incident_status));
SELECT setval(pg_get_serial_sequence('company', 'company_id'), (SELECT MAX(company_id) FROM company));

COMMIT;