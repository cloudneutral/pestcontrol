#!/bin/bash

pid=$(ps -ef | grep "java" | grep "pestcontrol.jar" | awk '{print $2}')

if [ -x ${pid} ]; then
   fn_print_error "No pestcontrol.jar process found - is it running?"
   exit 1
fi

kill -TERM $pid
RETVAL=$?

fn_print_ok "Stopped service (pid: $pid) $RETVAL"