#!/usr/bin/env bash

PROJECT_VERSION=$(grep -m 1 '<version>' pom.xml | awk -F '>' '{ print $2 }' | awk -F '<' '{ print $1 }')

function bootstrap {

    arr=( $(find ./kef-flink-jobs/kef-flink-feature-collection/src/main/java/net/disy/biggis/kef/flink/feature -type f | grep "Job.java") )
    arr+=( $(find ./kef-flink-jobs/kef-flink-charts/src/main/java/net/disy/biggis/kef/flink/charts -type f | grep "Job.java") )

    for i in "${arr[@]}"
        do
            :
            filename=${i##*/}

            if [[ $filename == *"Feature"* ]]; then

                filename_feature=${i##*/}
                jobname_feature=$(grep -m 1 'env.execute' ./kef-flink-jobs/kef-flink-feature-collection/src/main/java/net/disy/biggis/kef/flink/feature/${filename} | awk -F '"' '{ print $2 }' | awk -F '"' '{ print $1 }')

                jobs_feature+=' "'${filename_feature%.java}'"'
                jobsandnames_feature+=' ["'${filename_feature%.java}'"]="'${jobname_feature}'"'

            elif [[ $filename == *"TimeSeries"* ]]; then

                filename_charts=${i##*/}
                jobname_charts=$(grep -m 1 'env.execute' ./kef-flink-jobs/kef-flink-charts/src/main/java/net/disy/biggis/kef/flink/charts/${filename} | awk -F '"' '{ print $2 }' | awk -F '"' '{ print $1 }')

                jobs_charts+=' "'${filename_charts%.java}'"'
                jobsandnames_charts+=' ["'${filename_charts%.java}'"]="'${jobname_charts}'"'

             fi

    done

    sed -i -e "s|JOB_FEATURE=(.*|JOB_FEATURE=(${jobs_feature})|g" deploy.sh
    sed -i -e "s|JOBSANDNAMES_FEATURE=(.*|JOBSANDNAMES_FEATURE=(${jobsandnames_feature})|g" deploy.sh

    sed -i -e "s|JOB_CHARTS=(.*|JOB_CHARTS=(${jobs_charts})|g" deploy.sh
    sed -i -e "s|JOBSANDNAMES_CHARTS=(.*|JOBSANDNAMES_CHARTS=(${jobsandnames_charts})|g" deploy.sh

    sed -i -e "s|image: registry.biggis.project.de/demo/vitimeo-jobs:.*|image: registry.biggis.project.de/demo/vitimeo-jobs:${PROJECT_VERSION}|g" docker-compose.yml
    sed -i -e "s|PROJECT_VERSION:.*|PROJECT_VERSION: ${PROJECT_VERSION}|g" docker-compose.yml

    sed -i -e "s|image: registry.biggis.project.de/demo/vitimeo-jobs:.*|image: registry.biggis.project.de/demo/vitimeo-jobs:${PROJECT_VERSION}|g" docker-compose.rancher.yml
    sed -i -e "s|PROJECT_VERSION:.*|PROJECT_VERSION: ${PROJECT_VERSION}|g" docker-compose.rancher.yml
}

function build_maven {
    mvn clean && mvn package -Pbuild-jar
}
function build_docker {
    docker build -t registry.biggis.project.de/demo/vitimeo-jobs:${PROJECT_VERSION} .
}

function push_docker {
    docker push registry.biggis.project.de/demo/vitimeo-jobs:${PROJECT_VERSION}
}

bootstrap
build_maven
build_docker
push_docker