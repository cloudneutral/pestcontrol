#!/bin/bash

if [ ! -f .haproxy.pid ]; then
   fn_print_error "No .haproxy.pid found - is it running?"
   exit 1
fi

kill -TERM `cat .haproxy.pid`
RETVAL=$?

fn_print_ok "Stopped haproxy (pid: $(<.haproxy.pid))"

[ $RETVAL -eq 0 ] && rm -f .haproxy.pid
