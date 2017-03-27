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