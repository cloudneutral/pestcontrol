#!/bin/bash

case "$security_mode" in
  secure)
    if [ ! -f ${certsdir}/local_api.key ]; then
      fn_print_error "No API key found, run: ./cluster-admin login"
      exit 1
    fi

    apikey=$(<${certsdir}/local_api.key)

    # Doesnt work (401)
    curl --fail-with-body --insecure --request POST \
    --url "${admin_url}/_status/critical_nodes" \
    --cookie "session=${apikey}; Path=/; HttpOnly"
    ;;
  insecure)
    curl --fail-with-body --request POST \
    --url "${admin_url}/_status/critical_nodes"
    ;;
  *)
    echo "Bad security mode: $security_mode"
    exit 1
esac

if [ $? -eq 0 ]; then
    fn_print_ok "The request was successful"
    exitcode="0"
else
    fn_print_error "There was an error: $?"
    exitcode="1"
fi

core_exit.sh



