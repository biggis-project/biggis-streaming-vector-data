DROP TABLE IF EXISTS "kef_series_types" CASCADE;

CREATE TABLE IF NOT EXISTS "kef_series_types" (
   "identifier" INTEGER PRIMARY KEY,
   "name" VARCHAR(40) NOT NULL
);

INSERT INTO "kef_series_types" ("identifier", "name") VALUES (1, 'FALLENFAENGE');
INSERT INTO "kef_series_types" ("identifier", "name") VALUES (2, 'EIABLAGE_BEEREN');
INSERT INTO "kef_series_types" ("identifier", "name") VALUES (3, 'EIABLAGE_FUNDE');

DROP TABLE IF EXISTS "kef_series_description" CASCADE;
DROP SEQUENCE IF EXISTS "kef_series_description_id_seq";

CREATE SEQUENCE "kef_series_description_id_seq";

CREATE TABLE IF NOT EXISTS "kef_series_description" (
   "identifier" INTEGER PRIMARY KEY DEFAULT nextval('kef_series_description_id_seq'),
   "lfd_nummer" INTEGER NOT NULL,
   "name" VARCHAR(400) NOT NULL,
   "series_type" INTEGER REFERENCES "kef_series_types" ("identifier"),
   UNIQUE ("lfd_nummer", "name", "series_type")
);

DROP TABLE IF EXISTS "kef_series_data" CASCADE;
DROP SEQUENCE IF EXISTS "kef_series_data_id_seq";

CREATE SEQUENCE "kef_series_data_id_seq";

CREATE TABLE IF NOT EXISTS "kef_series_data" (
   "identifier" INTEGER PRIMARY KEY DEFAULT nextval('kef_series_data_id_seq'),
   "series_id" INTEGER REFERENCES "kef_series_description" ("identifier"),
   "value" INTEGER NOT NULL DEFAULT 0,
   "date" DATE NOT NULL,
   UNIQUE ("series_id", "date")
);

DROP TABLE IF EXISTS "kef_features" CASCADE;
DROP SEQUENCE IF EXISTS "kef_features_id_seq";
DROP TRIGGER IF EXISTS trg_kef_features ON kef_features;

CREATE SEQUENCE "kef_features_id_seq";

CREATE TABLE kef_features
(
    identifier INTEGER PRIMARY KEY DEFAULT nextval('kef_features_id_seq'),
    geom JSON,
    properties JSON,
    realgeom GEOMETRY(POINT, 4326),
    lfd_nummer integer NOT NULL,
    series_type integer NOT NULL
);

CREATE OR REPLACE FUNCTION create_map_series_id()
RETURNS trigger AS
$BODY$
    BEGIN
        NEW.lfd_nummer = COALESCE(NEW.properties ->> 'lf_id', NEW.properties ->> 'ps_id')::INTEGER;
        IF (substring((NEW.properties ->> 'url'), '/(.*)\?') = 'graphff') THEN
            NEW.series_type = 1;
        ELSE
            IF (substring((NEW.properties ->> 'url'), '&befallart=([0,1])') = '0') THEN
                NEW.series_type = 2;
            ELSE
                NEW.series_type = 3;
            END IF;
        END IF;

        RETURN NEW;
    END;
$BODY$ LANGUAGE 'plpgsql';

CREATE TRIGGER trg_kef_features BEFORE INSERT ON kef_features FOR EACH ROW EXECUTE PROCEDURE create_map_series_id();
