DROP TABLE IF EXISTS "kef_features" CASCADE;
DROP SEQUENCE IF EXISTS "kef_features_id_seq";
DROP TRIGGER IF EXISTS trg_kef_features ON kef_features;

CREATE SEQUENCE "kef_features_id_seq";

CREATE TABLE kef_features
(
    identifier INTEGER PRIMARY KEY DEFAULT nextval('kef_features_id_seq'),
    geom JSON,
    properties JSON,
    realgeom GEOMETRY,
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
