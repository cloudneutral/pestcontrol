#!/bin/bash

apikey=$(<${certsdir}/local_api.key)

curl --connect-timeout 5 --max-time 5 --fail-with-body --insecure \
--request POST \
--url "${admin_url}/api/v2/logout/" \
--header "X-Cockroach-API-Session: ${apikey}"

if [ $? -eq 0 ]; then
    exitcode="0"
else
    exitcode="1"
fi

core_exit.sh
