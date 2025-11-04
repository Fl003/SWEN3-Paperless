DO
$$
BEGIN
   IF NOT EXISTS (
      SELECT FROM pg_catalog.pg_roles WHERE rolname = 'paperless'
   ) THEN
      CREATE USER paperless WITH PASSWORD 'paperless';
END IF;
END
$$;

-- Zugriff auf Datenbank geben
GRANT CONNECT ON DATABASE paperless TO paperless;

\c paperless

-- Schema-Zugriff
GRANT CREATE ON SCHEMA public TO paperless;
GRANT USAGE ON SCHEMA public TO paperless;

-- Schreib- und Leserechte auf alle bestehenden Tabellen
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO paperless;

-- Schreibrechte auf alle künftig erstellten Tabellen
ALTER DEFAULT PRIVILEGES IN SCHEMA public
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO paperless;