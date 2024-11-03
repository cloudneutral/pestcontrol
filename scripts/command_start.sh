#!/bin/bash

port=$1

if [ $# -eq 0 ]; then
    fn_local_select_port
    port=$option
fi

let node=0
let matching=0
let port1=${rpcportbase}
let port2=${rpcportbase}+1
let port3=${rpcportbase}+2

for zone in "${locality_zone[@]}"
do
    let node=($node+1)
    let offset=${node}-1
    let rpcport=${rpcportbase}+$offset
    let httpport=${httpportbase}+$offset
    let sqlport=${sqlportbase}+$offset

    if [ "${toxiproxy}" != "off" ]; then
    let advertise_rpcport=${toxiproxy_advertiseportbase}+$offset
    else
    let advertise_rpcport=${rpcportbase}+$offset
    fi

    join=${host}:${port1},${host}:${port2},${host}:${port3}
    mempool="10%"

    if [ "${sqlport}" = "${port}" ]; then
      let matching=1
      break
    fi
done

if [ "${matching}" = "0" ]; then
  fn_print_error "No configuration matching SQL port [${port}]!"
  exit 1
fi

fn_local_node_status $rpcport

if [ "${status}" != "0" ]; then
  fn_print_warn "Node ${node} on SQL port ${port} is already running!"
else
  fn_local_start
fi