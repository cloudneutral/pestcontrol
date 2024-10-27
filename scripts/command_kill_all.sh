#!/bin/bash

fn_local_pids

if fn_prompt_yes_no "Kill all nodes?" Y; then
  for pid in $(ps -ef | grep "cockroach" | grep "sql-addr=" | awk '{print $2}')
  do
    fn_local_kill "$pid"
  done
fi