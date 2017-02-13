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