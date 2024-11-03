#!/bin/bash

pid=$(ps -ef | grep "toxiproxy-server" | grep "host" | awk '{print $2}')

if [ -x ${pid} ]; then
   fn_print_error "No process found - is it running?"
   exit 1
fi

echo $pid

kill -TERM ${pid}

fn_print_dots "Waiting for server to stop (pid: $pid)"

let attempts=0
while kill -0 $pid 2>/dev/null; do
    printf '.'
    sleep 2
    let attempts=attempts+1
    if [ ${attempts} -gt 5 ]; then
      fn_print_warn "Giving up waiting (${attempts}) - issuing SIGKILL (pid: $pid)"
      kill -KILL ${pid}
      break
    fi
done

fn_print_ok "Stopped (pid: $pid)"
