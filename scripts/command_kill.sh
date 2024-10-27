#!/bin/bash

fn_local_select_pid $1

if [ -z $pid ]; then
  fn_print_info "No cockroachdb process found"
  exit 0
fi

fn_local_kill "$pid"

