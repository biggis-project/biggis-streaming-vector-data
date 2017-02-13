DROP TABLE IF EXISTS "kef_series_description";
DROP SEQUENCE IF EXISTS "kef_series_description_id_seq";

CREATE SEQUENCE "kef_series_description_id_seq";

CREATE TABLE IF NOT EXISTS "kef_series_description" (
   "identifier" INTEGER PRIMARY KEY DEFAULT nextval('kef_series_description_id_seq'),
   "lfd_nummer" INTEGER NOT NULL,
   "name" VARCHAR(400) NOT NULL,
   UNIQUE ("lfd_nummer", "name")
);