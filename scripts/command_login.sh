#!/bin/bash

mkdir -p ${datadir}

case "$security_mode" in
  secure)
    curl --connect-timeout 5 --max-time 5 --fail-with-body --insecure \
      --request POST \
      --url "${admin_url}/api/v2/login/" \
      --header "Content-Type: application/x-www-form-urlencoded" \
      --data "username=${db_username}&password=${db_password}" \
      > ${certsdir}/local_api.key

    if [ $? -eq 0 ]; then
        exitcode="0"
    else
        exitcode="1"
    fi

    # Filter API key
    apikey=$(<${certsdir}/local_api.key)
    echo ${apikey} | sed -e "s/^{\"session\":\"//" -e 's/\".*$//' > ${certsdir}/local_api.key
    cat ${certsdir}/local_api.key

    ;;
  insecure)
    exitcode="0"
    ;;
  *)
    echo "Bad security mode: $security_mode"
    exitcode="1"
esac

core_exit.sh
