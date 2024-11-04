#!/bin/bash

## Executable jar
APP_JARFILE=${rootdir}/pestcontrol.jar

if [ ! -f "$APP_JARFILE" ]; then
    fn_fail_check ./mvnw clean install

    pomVersion=$(echo 'VERSION=${project.version}' | ./mvnw help:evaluate | grep '^VERSION=' | sed 's/^VERSION=//g')

    ln -sf target/pestcontrol-${pomVersion}.jar ${APP_JARFILE}
fi

pid=$(ps -ef | grep "java" | grep "pestcontrol.jar" | awk '{print $2}')

if [ ! -x ${pid} ]; then
   fn_print_error "Existing pestcontrol.jar process ${pid} found - is it running?"
   exit 1
fi

nohup java -jar ${APP_JARFILE} ${app_params} > pestcontrol-stdout.log 2>&1 &

fn_print_ok "Started service - check log for status"