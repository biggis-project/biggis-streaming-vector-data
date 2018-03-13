FROM biggis/base:java8-jre-alpine

MAINTAINER wipatrick

ARG PROJECT_VERSION=0.1

ENV JOB_MANAGER_RPC_ADDRESS=flink-jobmanager.analytics
ENV JAR=kef-flink-charts-$PROJECT_VERSION.jar
ENV JAR_FEATURE=kef-flink-feature-collection-$PROJECT_VERSION.jar
ENV TIMEZONE=Europe/Berlin

RUN set -x && \
    apk add --no-cache \
        curl \
        jq && \
    rm -rf /var/cache/apk/*

COPY kef-flink-jobs/kef-flink-charts/target/$JAR /opt/flink/$JAR
COPY kef-flink-jobs/kef-flink-feature-collection/target/$JAR_FEATURE /opt/flink/$JAR_FEATURE

COPY deploy.sh /opt/flink/deploy.sh

CMD ["/opt/flink/deploy.sh"]
