DROP TABLE IF EXISTS "kef_series_types" CASCADE;

CREATE TABLE IF NOT EXISTS "kef_series_types" (
   "identifier" INTEGER PRIMARY KEY,
   "name" VARCHAR(40) NOT NULL
);

INSERT INTO "kef_series_types" ("identifier", "name") VALUES (1, 'FALLENFAENGE');
INSERT INTO "kef_series_types" ("identifier", "name") VALUES (2, 'EIABLAGE_BEEREN');
INSERT INTO "kef_series_types" ("identifier", "name") VALUES (3, 'EIABLAGE_FUNDE');
