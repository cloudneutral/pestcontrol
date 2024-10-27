#!/bin/bash

fn_assert_binaries

case "$security_mode" in
  secure)
    ${installdir}/cockroach init --certs-dir=${certsdir} --host=${host}:${rpcportbase}

    ${installdir}/cockroach sql --certs-dir=${certsdir} --host=${host}:${sqlportbase} \
    -e "CREATE USER IF NOT EXISTS ${db_username} WITH PASSWORD '${db_password}'"

    ${installdir}/cockroach sql --certs-dir=${certsdir} --host=${host}:${sqlportbase} \
    -e "ALTER ROLE ${db_username} WITH PASSWORD '${db_password}'; GRANT ADMIN to ${db_username};"

    ${installdir}/cockroach sql --certs-dir=${certsdir} --host=${host}:${sqlportbase} < ${configdir}/init.sql

    ;;
  insecure)
    ${installdir}/cockroach init --insecure --host=${host}:${rpcportbase}

    ${installdir}/cockroach sql --insecure --host=${host}:${sqlportbase} \
    -e "CREATE USER IF NOT EXISTS ${db_username}; GRANT ADMIN to ${db_username};"

    ${installdir}/cockroach sql --insecure --host=${host}:${sqlportbase} \
    -e "ALTER ROLE ${db_username} WITH PASSWORD NULL"

    ${installdir}/cockroach sql --insecure --host=${host}:${sqlportbase} < ${configdir}/init.sql

    ;;
  *)
    echo "Bad security mode: $security_mode"
    exit 1
esac

fn_print_ok "Cluster initialized"
fn_print_info "If you later switch between secure<->insecure then repeat this command"
