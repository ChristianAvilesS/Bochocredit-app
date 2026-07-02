-- ─────────────────────────────────────────────
-- Datos iniciales (seed)
-- ─────────────────────────────────────────────

INSERT INTO roles (nombre_rol) VALUES ('ADMIN'), ('USER');

-- Password: admin123 (BCrypt) — generado por Spring Security
-- El hash se inserta vía DataSeeder en el arranque de la app (ver service/DataSeederService.java)
-- para evitar incrustar un hash BCrypt fijo en una migración versionada.

INSERT INTO bancos (nombre, razon_social, ruc, direccion, activo)
VALUES ('Banco General', 'Banco General S.A.', '20123456789', 'Av. Principal 123', true);

INSERT INTO tasas_interes (tipo_tasa, tasa_interes, dias_capitalizacion, dias_tasa)
VALUES ('efectiva_anual', 15.0, 30, 360);
