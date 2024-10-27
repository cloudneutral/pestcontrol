#!/bin/bash

fn_assert_binaries

if [ ! -f ${configdir}/haproxy.cfg ]; then
  fn_print_info "No 'haproxy.cfg' found - generating it"

  case "$security_mode" in
    secure)
      fn_fail_check ${installdir}/cockroach gen haproxy --certs-dir=${certsdir} --host=${host}:${rpcportbase}
      ;;
    insecure)
      fn_fail_check ${installdir}/cockroach gen haproxy --insecure --host=${host}:${rpcportbase}
      ;;
    *)
      echo "Bad security mode: $security_mode"
      exit 1
  esac

  fn_fail_check mv ${rootdir}/haproxy.cfg ${configdir}
fi

if [ -f .haproxy.pid ]; then
   fn_print_warn ".haproxy.pid found - already running?"
   exit 1
fi

fn_fail_check haproxy -D -f ${configdir}/haproxy.cfg -p .haproxy.pid

fn_print_ok "Started haproxy (pid: $(<.haproxy.pid))"