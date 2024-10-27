#!/bin/bash

port=$1

fn_assert_binaries

if [ $# -eq 0 ]; then
    fn_local_select_port
    port=$option
fi

case "$security_mode" in
  secure)
    fn_fail_check ${installdir}/cockroach sql \
    --host=${host} --port=${port} \
    --certs-dir=${certsdir}
    ;;
  insecure)
    fn_fail_check ${installdir}/cockroach sql \
      --host=${host} --port=${port} \
      --insecure
    ;;
esac
