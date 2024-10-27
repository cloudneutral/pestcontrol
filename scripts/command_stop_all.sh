#!/bin/bash

fn_local_pids

if fn_prompt_yes_no "Stop all nodes?" Y; then
  for pid in $(ps -ef | grep "cockroach" | grep "sql-addr=" | awk '{print $2}')
  do
    fn_local_stop "$pid"
  done
fi
