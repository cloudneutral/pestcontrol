#!/bin/bash

fn_assert_binaries() {
  if [ ! -f ${installdir}/cockroach ]; then
     fn_print_error "No cockroach binary found! Run ./cluster-admin install"
     exit 1
  fi
}

fn_local_start() {
  fn_assert_binaries

  fn_print_dots "Starting node ${node} [--sql-addr=port=${host}:${sqlport} --locality=${zone}] in $security_mode mode"

  case "$security_mode" in
    secure)
      fn_fail_check ${installdir}/cockroach start \
      --locality=${zone} \
      --listen-addr=${host}:${rpcport} \
      --sql-addr=${host}:${sqlport} \
      --http-addr=${host}:${httpport} \
      --join=${join} \
      --store=${datadir}/n${node} \
      --cache=${mempool} \
      --max-sql-memory=${mempool} \
      --background \
      --accept-sql-without-tls \
      --certs-dir=${certsdir}
      ;;
    insecure)
      fn_fail_check ${installdir}/cockroach start \
      --locality=${zone} \
      --listen-addr=${host}:${rpcport} \
      --sql-addr=${host}:${sqlport} \
      --http-addr=${host}:${httpport} \
      --join=${join} \
      --store=${datadir}/n${node} \
      --cache=${mempool} \
      --max-sql-memory=${mempool} \
      --background \
      --insecure
      ;;
    *)
      echo "Bad security mode: $security_mode"
      exit 1
  esac

  fn_print_ok "Started node ${node}"
}

fn_local_stop() {
  pid=$1

  if [ $# -eq 0 ]; then
      echo -e "Expected pid"
      exit 1
  fi

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
}

fn_local_kill() {
  pid=$1

  if [ $# -eq 0 ]; then
      echo -e "Expected pid"
      exit 1
  fi

  kill -KILL ${pid}

  fn_print_dots "Waiting for server to die (pid: $pid)"
  while kill -0 $pid 2>/dev/null; do
      printf '.'
      sleep 1
  done

  fn_print_ok "Killed (pid: $pid)"
}

fn_local_node_status() {
  port=$1

  if [ $# -eq 0 ]; then
      echo -e "Expected port"
      exit 1
  fi

  case "$OSTYPE" in
    darwin*)
        roachpid=$(lsof -PiTCP -sTCP:LISTEN | grep LISTEN | grep $port | grep cockroach |  awk '{ print $2 }')
        ;;
    *)
        roachpid=$(netstat -nap 2>/dev/null | grep LISTEN | grep $port | grep cockroach | awk '{ print $6 }' | awk -F'/' '{ print $1 }')
        ;;
  esac

  if [ -z "${roachpid}" ]; then
      status=0
  else
      status=1
  fi
}

fn_local_select_pid() {
  if [ $# -eq 0 ]; then
    PS3='Please select process: '

    local pids=()
    while IFS= read -r line; do
        pids+=( "$line" )
    done < <( fn_local_pids )

    select pid in "${pids[@]}"; do
      break
    done

    pid=$(echo $pid | awk '{print $1}')
  else
    local port=$1
    pid=$(ps -ef | grep "cockroach" | grep "sql-addr=${host}:${port}" | awk '{print $2}')
    if [ -z $pid ]; then
        fn_local_pids
        fn_print_error "No cockroachdb process found with SQL port ${port} (--sql-addr)"
        exit 1
    fi
  fi
}

fn_local_select_port() {
  PS3='Please select node: '

  node=0;
  ports=()
  for zone in "${locality_zone[@]}"
  do
      let node=($node+1)
      let offset=${node}-1
      let sqlport=${sqlportbase}+$offset

      local pid=$(ps -ef | grep "cockroach" | grep "sql-addr=${host}:${sqlport}" | awk '{print $2}')
      if [ -z $pid ]; then
        ports+=("${sqlport} [--sql-addr=port=${host}:${sqlport}, --locality=${zone}] - STOPPED")
      else
        ports+=("${sqlport} [--sql-addr=port=${host}:${sqlport}, --locality=${zone}] RUNNING (${pid})")
      fi
  done

  select option in "${ports[@]}"; do
    break
  done

  option=$(echo $option | awk '{ print $1 }')
}

fn_local_select_rpc_port() {
  PS3='Please select node: '

  node=0;
  ports=()
  for zone in "${locality_zone[@]}"
  do
      let node=($node+1)
      let offset=${node}-1
      let rpcport=${rpcportbase}+$offset

      local pid=$(ps -ef | grep "cockroach" | grep "listen-addr=${host}:${rpcport}" | awk '{print $2}')
      if [ -z $pid ]; then
        ports+=("${rpcport} [--listen-addr=${host}:${rpcport}, --locality=${zone}] - STOPPED")
      else
        ports+=("${rpcport} [--listen-addr=${host}:${rpcport}, --locality=${zone}] - RUNNING")
      fi
  done

  select option in "${ports[@]}"; do
    break
  done

  option=$(echo $option | awk '{ print $1 }')
}

fn_local_pids() {
  ps -ef | grep "cockroach" | grep "sql-addr=" | awk '{print $2,$10,$11,$12,$13}'
}

fn_local_node_range() {
  read -p "Enter node numbers (1-N or 1,..): " IN
  nodes=$(echo $IN | awk -v RS='[[:blank:]]|[\n]|[,]' '{max=a[split($0, a ,"-")]; if(max!=0){while(a[1]<=max){print a[1]++}}}')
}
