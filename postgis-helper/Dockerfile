FROM biggis/base:alpine-3.4

MAINTAINER wipatrick

ENV TIMEZONE=Europe/Berlin
ENV POSTGIS_DB=postgis.storage
ENV PGPASSWORD=biggis

RUN set -x && \
    apk add --no-cache \
        bash \
        postgresql-client && \
    rm -rf /var/cache/apk/*

COPY sql /sql
COPY start.sh /

CMD ["/start.sh"]