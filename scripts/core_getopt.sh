#!/bin/bash

getopt=$1
shift

case "${getopt}" in
    login)
        command_login.sh $*
        ;;
    logout)
        command_logout.sh $*
        ;;
    disrupt)
        command_disrupt.sh $*
        ;;
    recover)
        command_recover.sh $*
        ;;
    nodes)
        command_nodes.sh $*
        ;;
    critical)
        command_critical_nodes.sh $*
        ;;
    status)
        command_status.sh $*
        ;;
    sql)
        command_sql.sh $*
        ;;
    open)
        command_open.sh $*
        ;;
    install)
        command_install.sh $*
        ;;
    run-service)
        command_run_service.sh $*
        ;;
    start-service)
        command_start_service.sh $*
        ;;
    stop-service)
        command_stop_service.sh $*
        ;;
    #####################################
    # Local cluster
    #####################################
    certs)
        command_certs.sh $*
        ;;
    clean)
        command_clean.sh $*
        ;;
    decomm)
        command_decommission.sh $*
        ;;
    drain)
        command_drain.sh $*
        ;;
    init)
        command_init.sh $*
        ;;
    kill)
        command_kill.sh $*
        ;;
    kill-all)
        command_kill_all.sh $*
        ;;
    recomm)
        command_recommission.sh $*
        ;;
    start)
        command_start.sh $*
        ;;
    start-all)
        command_start_all.sh $*
        ;;
    start-lb)
        command_start_lb.sh $*
        ;;
    start-toxi)
        command_start_proxy.sh $*
        ;;
    stop)
        command_stop.sh $*
        ;;
    stop-all)
        command_stop_all.sh $*
        ;;
    stop-lb)
        command_stop_lb.sh $*
        ;;
    stop-toxi)
        command_stop_proxy.sh $*
        ;;
    *)
    if [ -n "${getopt}" ]; then
        echo -e "${red}Unknown command${default}: $0 ${getopt}"
    fi
    echo -e "${green}Usage: $0 [command]${default}"
    echo -e "${default}Pest Control Local Cluster Admin${default}"
    echo -e ""
    echo -e "${default}Security mode: ${green}${security_mode}${default}"
    echo -e "(changed in config/settings.cfg)"
    echo -e ""
    echo -e "${lightgreen}Application Commands${default}"
    {
        echo -e "${green}start-service\t${default}      | Start the Pest Control service"
        echo -e "${green}run-service\t${default}      | Run the Pest Control service"
        echo -e "${green}stop-service\t${default}      | Stop the Pest Control service"
    } | column -s $'\t' -t

    echo -e ""
    echo -e "${lightyellow}Cluster Setup Commands${default}"
    {
        echo -e "${yellow}install\t${default}            | Download and install CockroachDB ${version}"
        echo -e "${yellow}certs\t${default}            | Generate certificates (secure mode only)"
        echo -e "${yellow}init\t${default}            | Initialize cluster"
        echo -e "${yellow}clean\t${default}            | Clean all data files"
    } | column -s $'\t' -t

    if [ "${toxiproxy}" != "off" ]; then
    echo -e ""
    echo -e "${lightcyan}Toxiproxy Commands${default}"
    {
        echo -e "${cyan}start-toxi\t${default}         | Start toxiproxy server and add node proxies"
        echo -e "${cyan}stop-toxi\t${default}         | Stop toxiproxy server"
    } | column -s $'\t' -t
    fi

    echo -e ""
    echo -e "${lightmagenta}Cluster Management Commands${default}"
    {
        echo -e "${magenta}login\t${default}          | Get Cluster API session token"
        echo -e "${magenta}logout\t${default}          | Close API sessions"

        echo -e "${magenta}drain\t${default}          | Drain one node"
        echo -e "${magenta}decomm\t${default}          | Decommission one node"
        echo -e "${magenta}recomm\t${default}          | Recommission one node"
        echo -e "${magenta}disrupt\t${default}          | Disrupt node"
        echo -e "${magenta}recover\t${default}          | Recover node disruption"

        echo -e "${magenta}start\t${default}          | Start one node"
        echo -e "${magenta}stop\t${default}          | Stop one node gracefully"
        echo -e "${magenta}kill\t${default}          | Kill one node"
        echo -e "${magenta}start-all\t${default}          | Start range of nodes"
        echo -e "${magenta}stop-all\t${default}          | Stop range of nodes gracefully"
        echo -e "${magenta}kill-all\t${default}          | Kill range of nodes"
        echo -e "${magenta}start-lb\t${default}          | Start HAProxy load balancer"
        echo -e "${magenta}stop-lb\t${default}          | Stop HAProxy load balancer"
    } | column -s $'\t' -t

    echo -e ""
    echo -e "${lightcyan}Cluster Query Commands${default}"
    {
        echo -e "${cyan}sql\t${default}           | Connect to SQL console"
        echo -e "${cyan}open\t${default}           | Open admin URL in browser"
        echo -e "${cyan}critical\t${default}           | Print critical nodes report"
        echo -e "${cyan}status\t${default}           | Print node status"
        echo -e "${cyan}nodes\t${default}           | Print node details"
    } | column -s $'\t' -t

esac
