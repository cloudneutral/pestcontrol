#!/bin/bash

format=$1

fn_assert_binaries

if [ $# -eq 0 ]; then
    format="json"
fi

case "$security_mode" in
  secure)
    fn_fail_check ${installdir}/cockroach node status --all --url ${db_url} \
    --certs-dir=${certsdir} \
    --format ${format}
    ;;
  insecure)
    fn_fail_check ${installdir}/cockroach node status --all --url ${db_url} \
    --insecure \
    --format ${format}
    ;;
  *)
    echo "Bad security mode: $security_mode"
    exit 1
esac
