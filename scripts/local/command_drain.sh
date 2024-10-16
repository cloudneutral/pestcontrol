#!/bin/bash

port=$1

if [ $# -eq 0 ]; then
    fn_local_select_rpc_port
    port=$option
fi

case "$SECURITY_MODE" in
  secure)
    fn_fail_check ${installdir}/cockroach node drain ${node} --host ${host}:${port} --certs-dir=${certsdir}
    ;;
  insecure)
    fn_fail_check ${installdir}/cockroach node drain ${node} --host ${host}:${port} --insecure
    ;;
  *)
    echo "Bad security mode: $SECURITY_MODE"
    exit 1
esac

fn_print_ok "Node ${node} on RPC port ${port} drained!"