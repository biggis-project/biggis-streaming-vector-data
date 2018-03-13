#!/usr/bin/env bash


POSTGIS_DB=${POSTGIS_DB:-postgis.storage}

if [[ $1 == "test" ]]; then
    psql -h ${POSTGIS_DB} -p 5432 -U postgres -d postgres -f /sql/test.sql

elif [[ $1 == "init" ]]; then
    psql -h ${POSTGIS_DB} -p 5432 -U postgres -d postgres -f /sql/init-tables.sql

elif [[ $1 == "truncate" ]]; then
    psql -h ${POSTGIS_DB} -p 5432 -U postgres -d postgres -f /sql/truncate-tables.sql

else
    echo "use one of the following args: test | init | truncate"

fi