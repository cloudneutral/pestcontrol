#!/bin/bash

node=0;
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

    join=${host}:${port1},${host}:${port2},${host}:${port3}
    mempool="10%"

    local pid=$(ps -ef | grep "cockroach" | grep "sql-addr=${host}:${sqlport}" | awk '{print $2}')
    if [ -z $pid ]; then
      fn_print_yellow "n${node} [--sql-addr=port=${host}:${sqlport}, --locality=${zone}] - STOPPED"
    else
      fn_print_green "n${node} [--sql-addr=port=${host}:${sqlport}, --locality=${zone}] - RUNNING (${pid})"
    fi
done

fn_print_info "Start up to ${#locality_zone[@]} nodes in '$security_mode' mode"
fn_local_node_range

let port1=${rpcportbase}
let port2=${rpcportbase}+1
let port3=${rpcportbase}+2

for node in $nodes
do
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
    zone=${locality_zone[node - 1]}
    mempool="10%"

    fn_local_node_status $rpcport

    if [ "${status}" != "0" ]; then
      fn_print_warn "Node ${node} is already running! [--sql-addr=port=${host}:${sqlport}, --locality=${zone}]"
    else
      fn_local_start
    fi
done
