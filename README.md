# biggis-streaming-vector-data
Prototypical Stream Processing of vector data with Apache Flink

This repository contains a prototype for processing GeoJSON and supplementory data using Apache Kafka and Flink. 
It is build on top of the stack in `https://github.com/DisyInformationssysteme/biggis-infrastructure`.

The implemented jobs feature the import of GeoJSON coded sensor locations and corresponding time series.
Source are Kafka queses. Destination is a PostGIS/Postgres database

This work was done for the BigGIS project `http://biggis-project.eu/` funded by the German Federal Ministry of Education and Research (BMBF) `https://www.bmbf.de`.
