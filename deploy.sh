#!/usr/bin/env bash
#
# !!! DO NOT MODIFY THIS CONTENT !!!
#
# Note: The content of this file only works with bash 4 due to associative array usage.
#
declare -A JOBSANDNAMES_CHARTS
declare -A JOBSANDNAMES_FEATURE

JOB_CHARTS=( "ImportTimeSeriesJob")
JOB_FEATURE=( "SplitFeatureCollectionJob")

JOBSANDNAMES_CHARTS=( ["ImportTimeSeriesJob"]="Time Series Job @Vitimeo")
JOBSANDNAMES_FEATURE=( ["SplitFeatureCollectionJob"]="Split Feature Collection Job @Vitimeo")


JOB_MANAGER_RPC_ADDRESS=${JOB_MANAGER_RPC_ADDRESS:-flink-jobmanager.analytics}
JAR_CHARTS_NEW=${JAR_CHARTS:-kef-flink-charts-$PROJECT_VERSION.jar}
JAR_FEATURE_NEW=${JAR_FEATURE:-kef-flink-feature-collection-$PROJECT_VERSION.jar}

function waitForJobmanager {
    echo [ $(date) ] Waiting for Flink Jobmanager "http://${JOB_MANAGER_RPC_ADDRESS}:8081" to launch ...
    while ! nc -z ${JOB_MANAGER_RPC_ADDRESS} 8081; do
      sleep 1 # wait for 1 second before check again
    done
    echo [ $(date) ] Flink Jobmanager "http://${JOB_MANAGER_RPC_ADDRESS}:8081" launched
}

function getUploadedJar {
    UPLOADED_JARS=$(curl -sS "http://${JOB_MANAGER_RPC_ADDRESS}:8081/jars" | jq -r '.files[].name')
}

function submitJar {
    JAR_CHARTS_OLD=$(echo "$UPLOADED_JARS" | grep "kef-flink-charts")
    JAR_FEATURE_OLD=$(echo "$UPLOADED_JARS" | grep "kef-flink-feature-collection")

    # Upload kef-flink-charts if not exists
    if [ "${JAR_CHARTS_NEW}" != "$JAR_CHARTS_OLD" ]; then
        echo [ $(date) ] Submitting "${JAR_CHARTS_NEW}" to jobmanager at http://"${JOB_MANAGER_RPC_ADDRESS}":8081 ...
        JAR_ID_CHARTS=$(curl -sS -X POST -H "Expect:" -F "jarfile=@/opt/flink/${JAR_CHARTS_NEW}" "http://${JOB_MANAGER_RPC_ADDRESS}:8081/jars/upload" | jq '.filename' | tr -d '"')
        echo [ $(date) ] "${JAR_CHARTS_NEW}" uploaded. Flink Jar ID: "${JAR_ID_CHARTS}"
    else
        JAR_ID_CHARTS=$(curl -sS "http://${JOB_MANAGER_RPC_ADDRESS}:8081/jars" | jq -r '.files[].id' | grep "kef-flink-charts")
        echo [ $(date) ] "${JAR_CHARTS_NEW}" already uploaded. Flink Jar ID: "${JAR_ID_CHARTS}"
    fi

    # Upload kef-flink-feature-collection if not exists
    if [ "${JAR_FEATURE_NEW}" != "$JAR_FEATURE_OLD" ]; then
        echo [ $(date) ] Submitting "${JAR_FEATURE_NEW}" to jobmanager at http://"${JOB_MANAGER_RPC_ADDRESS}":8081 ...
        JAR_ID_FEATURE=$(curl -sS -X POST -H "Expect:" -F "jarfile=@/opt/flink/${JAR_FEATURE_NEW}" "http://${JOB_MANAGER_RPC_ADDRESS}:8081/jars/upload" | jq '.filename' | tr -d '"')
        echo [ $(date) ] "${JAR_FEATURE_NEW}" uploaded. Flink Jar ID: "${JAR_ID_FEATURE}"
    else
        JAR_ID_FEATURE=$(curl -sS "http://${JOB_MANAGER_RPC_ADDRESS}:8081/jars" | jq -r '.files[].id' | grep "kef-flink-feature-collection")
        echo [ $(date) ] "${JAR_FEATURE_NEW}" already uploaded. Flink Jar ID: "${JAR_ID_FEATURE}"
    fi
}

function deployCharts {
    JOB_NAME_FULL_CHARTS=$1
    JOB_KEY_CHARTS=$2
    ENTRYCLASS_CHARTS_PACKAGE="net.disy.biggis.kef.flink.charts"
    JOB_ID_CHARTS=$(curl -sS "http://${JOB_MANAGER_RPC_ADDRESS}:8081/joboverview/running" | jq '.jobs[] | select(.name=="'"${JOB_NAME_FULL_CHARTS}"'") | .jid ' | tr -d '"')

    # assumes if job is not running, that there has not been any previous jobs to pick up from
    # with the result, that the new job is not started off from any savepoint.
    if [ -z "${JOB_ID_CHARTS}" ]; then

        echo [ $(date) ] "${JOB_NAME_FULL_CHARTS}" not running ...
        echo [ $(date) ] Starting "${JOB_NAME_FULL_CHARTS}" ...

        echo [ $(date) ] $(curl -sS -X POST "http://${JOB_MANAGER_RPC_ADDRESS}:8081/jars/${JAR_ID_CHARTS}/run?entry-class=${ENTRYCLASS_CHARTS_PACKAGE}.${JOB_KEY_CHARTS}")
    else

        echo [ $(date) ] "Cancel ${JOB_NAME_FULL_CHARTS} with job id ${JOB_ID_CHARTS} and create savepoint ..."

        REQUEST_ID_CHARTS=$(curl -sS -X GET "http://${JOB_MANAGER_RPC_ADDRESS}:8081/jobs/${JOB_ID_CHARTS}/cancel-with-savepoint/" | jq '.["request-id"]' | tr -d '"')
        STATUS_CHARTS=$(curl -sS -X GET "http://${JOB_MANAGER_RPC_ADDRESS}:8081/jobs/${JOB_ID_CHARTS}/cancel-with-savepoint/in-progress/${REQUEST_ID_CHARTS}" | jq '.["status"]' | tr -d '"')

        # The status is a returned field (response) specific to the Flink REST API and can either be accepted, failed or success
        while [ "${STATUS_CHARTS}" != "success" ]
        do
            if [ "${STATUS_CHARTS}" != "failed" ]; then
                STATUS_CHARTS=$(curl -sS -X GET "http://${JOB_MANAGER_RPC_ADDRESS}:8081/jobs/${JOB_ID_CHARTS}/cancel-with-savepoint/in-progress/${REQUEST_ID_CHARTS}" | jq '.["status"]' | tr -d '"')
                echo [ $(date) ] Savepoint creation status "${JOB_KEY_CHARTS}": "${STATUS_CHARTS}"
            else
                echo [ $(date) ] Savepoint creation status "${JOB_KEY_CHARTS}": "${STATUS_CHARTS}"
                CAUSE=$(curl -sS -X GET "http://${JOB_MANAGER_RPC_ADDRESS}:8081/jobs/${JOB_ID_CHARTS}/cancel-with-savepoint/in-progress/${REQUEST_ID_CHARTS}" | jq '.["cause"]' | tr -d '"')
                echo [ $(date) ] "Cancel ${JOB_NAME_FULL_CHARTS} with savepoint failed: ${CAUSE} "
                exit 1
            fi
        done

        echo [ $(date) ] Savepoint creation status "${JOB_KEY_CHARTS}": "${STATUS_CHARTS}"
        SAVEPOINT_PATH=$(curl -sS -X GET "http://${JOB_MANAGER_RPC_ADDRESS}:8081/jobs/${JOB_ID_CHARTS}/cancel-with-savepoint/in-progress/${REQUEST_ID_CHARTS}" | jq '.["savepoint-path"]' | tr -d '"')

        echo [ $(date) ] Starting "${JOB_NAME_FULL_CHARTS}" with savepoint from "${SAVEPOINT_PATH}" ...
        curl -sS -X POST "http://${JOB_MANAGER_RPC_ADDRESS}:8081/jars/${JAR_ID_CHARTS}/run?savepointPath=${SAVEPOINT_PATH}&allowNonRestoredState=true&entry-class=${ENTRYCLASS_CHARTS_PACKAGE}.${JOB_KEY_CHARTS}"
    fi

}

function deployFeature {
    JOB_NAME_FULL_FEATURE=$1
    JOB_KEY_FEATURE=$2
    ENTRYCLASS_FEATURE_PACKAGE="net.disy.biggis.kef.flink.feature"
    JOB_ID_FEATURE=$(curl -sS "http://${JOB_MANAGER_RPC_ADDRESS}:8081/joboverview/running" | jq '.jobs[] | select(.name=="'"${JOB_NAME_FULL_FEATURE}"'") | .jid ' | tr -d '"')

    # assumes if job is not running, that there has not been any previous jobs to pick up from
    # with the result, that the new job is not started off from any savepoint.
    if [ -z "${JOB_ID_FEATURE}" ]; then

        echo [ $(date) ] "${JOB_NAME_FULL_FEATURE}" not running ...
        echo [ $(date) ] Starting "${JOB_NAME_FULL_FEATURE}" ...

        echo [ $(date) ] $(curl -sS -X POST "http://${JOB_MANAGER_RPC_ADDRESS}:8081/jars/${JAR_ID_FEATURE}/run?entry-class=${ENTRYCLASS_FEATURE_PACKAGE}.${JOB_KEY_FEATURE}")
    else

        echo [ $(date) ] "Cancel ${JOB_NAME_FULL_FEATURE} with job id ${JOB_ID_FEATURE} and create savepoint ..."

        REQUEST_ID_FEATURE=$(curl -sS -X GET "http://${JOB_MANAGER_RPC_ADDRESS}:8081/jobs/${JOB_ID_FEATURE}/cancel-with-savepoint/" | jq '.["request-id"]' | tr -d '"')
        STATUS_FEATURE=$(curl -sS -X GET "http://${JOB_MANAGER_RPC_ADDRESS}:8081/jobs/${JOB_ID_FEATURE}/cancel-with-savepoint/in-progress/${REQUEST_ID_FEATURE}" | jq '.["status"]' | tr -d '"')

        # The status is a returned field (response) specific to the Flink REST API and can either be accepted, failed or success
        while [ "${STATUS_FEATURE}" != "success" ]
        do
            if [ "${STATUS_FEATURE}" != "failed" ]; then
                echo [ $(date) ] Savepoint creation status "${JOB_KEY_FEATURE}": "${STATUS_FEATURE}"
                STATUS_FEATURE=$(curl -sS -X GET "http://${JOB_MANAGER_RPC_ADDRESS}:8081/jobs/${JOB_ID_FEATURE}/cancel-with-savepoint/in-progress/${REQUEST_ID_FEATURE}" | jq '.["status"]' | tr -d '"')
            else
                echo [ $(date) ] Savepoint creation status "${JOB_KEY_FEATURE}": "${STATUS_FEATURE}"
                CAUSE=$(curl -sS -X GET "http://${JOB_MANAGER_RPC_ADDRESS}:8081/jobs/${JOB_ID_FEATURE}/cancel-with-savepoint/in-progress/${REQUEST_ID_FEATURE}" | jq '.["cause"]' | tr -d '"')
                echo [ $(date) ] "Cancel ${JOB_NAME_FULL_FEATURE} with savepoint failed: ${CAUSE} "
                exit 1
            fi
        done

        echo [ $(date) ] Savepoint creation status "${JOB_KEY_FEATURE}": "${STATUS_FEATURE}"
        SAVEPOINT_PATH=$(curl -sS -X GET "http://${JOB_MANAGER_RPC_ADDRESS}:8081/jobs/${JOB_ID_FEATURE}/cancel-with-savepoint/in-progress/${REQUEST_ID_FEATURE}" | jq '.["savepoint-path"]' | tr -d '"')

        echo [ $(date) ] Starting "${JOB_NAME_FULL_FEATURE}" with savepoint from "${SAVEPOINT_PATH}" ...
        curl -sS -X POST "http://${JOB_MANAGER_RPC_ADDRESS}:8081/jars/${JAR_ID_FEATURE}/run?savepointPath=${SAVEPOINT_PATH}&allowNonRestoredState=true&entry-class=${ENTRYCLASS_FEATURE_PACKAGE}.${JOB_KEY_FEATURE}"
    fi

}

function usage {
    echo "Usage: docker-compose run --rm jobs /opt/flink/deploy.sh all"
}

if [ $# -eq 0 ]; then
    usage
elif [ "$1" = "all" ]; then
    waitForJobmanager
    getUploadedJar
    submitJar
        for key in ${!JOBSANDNAMES_CHARTS[@]}
            do
                :
                 deployCharts "${JOBSANDNAMES_CHARTS[$key]}" ${key}
        done
        for key in ${!JOBSANDNAMES_FEATURE[@]}
            do
                :
                 deployFeature "${JOBSANDNAMES_FEATURE[$key]}" ${key}
        done
else
    usage
fi
