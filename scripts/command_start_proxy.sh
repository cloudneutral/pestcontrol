#!/bin/bash

if [ ! "$(command -v toxiproxy-server 2> /dev/null)" ]; then
	echo -e "[ FAIL ] toxiproxy is not installed"
	exit 1
fi

if [ "${toxiproxy}" != "on" ]; then
	echo -e "[ FAIL ] toxiproxy mode is not enabled (toxiproxy=off)"
	exit 1
fi

pid=$(ps -ef | grep "toxiproxy-server" | grep "host" | awk '{print $2}')
if [ ! -x ${pid} ]; then
   fn_print_info "toxiproxy-server (pid: ${pid}) appears to be running - listing proxies"
else
  if fn_prompt_yes_no "Start toxiproxy-server?" Y; then
    fn_fail_check nohup toxiproxy-server -host ${toxiproxy_host} -port ${toxiproxy_port} > toxiproxy-stdout.log 2>&1 &
    fn_print_ok "Started toxiproxy-server - check toxiproxy-stdout.log"
    sleep 2
  fi
fi

fn_fail_check toxiproxy-cli list

fn_print_info "Add up to ${#locality_zone[@]} node proxies automatically"
fn_local_node_range

for node in $nodes
do
    let offset=${node}-1
    let rpcport=${rpcportbase}+$offset

    if [ "${toxiproxy}" != "off" ]; then
      let advertise_rpcport=${toxiproxy_advertiseportbase}+$offset
    else
      let advertise_rpcport=${rpcportbase}+$offset
    fi

    fn_fail_check toxiproxy-cli create --listen ${toxiproxy_host}:${advertise_rpcport} --upstream ${host}:${rpcport} crdb-${node}
done

fn_fail_check toxiproxy-cli list

fn_print_ok "Started toxiproxy server with proxies - you can now start the CockroachDB cluster!"