-- NAGARLENS Infrastructure PostGIS Bootstrap Script
CREATE EXTENSION IF NOT EXISTS postgis;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
SELECT PostGIS_Full_Version();
