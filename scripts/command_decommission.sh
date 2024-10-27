#!/bin/bash

port=$1

fn_assert_binaries

if [ $# -eq 0 ]; then
    fn_local_select_rpc_port
    port=$option
fi

case "$security_mode" in
  secure)
    fn_fail_check ${installdir}/cockroach node decommission ${node} --host ${host}:${port} --certs-dir=${certsdir}
    ;;
  insecure)
    fn_fail_check ${installdir}/cockroach node decommission ${node} --host ${host}:${port} --insecure
    ;;
  *)
    echo "Bad security mode: $security_mode"
    exit 1
esac

fn_print_ok "Node ${node} on RPC port ${port} decommissioned!"