#!/bin/bash

command_certs.sh(){
  modulefile="${FUNCNAME[0]}"
	source "${scriptsdir}/${modulefile}"
}

command_clean.sh(){
  modulefile="${FUNCNAME[0]}"
	source "${scriptsdir}/${modulefile}"
}

command_decommission.sh() {
	modulefile="${FUNCNAME[0]}"
	source "${scriptsdir}/${modulefile}"
}

command_drain.sh() {
	modulefile="${FUNCNAME[0]}"
	source "${scriptsdir}/${modulefile}"
}

command_init.sh(){
  modulefile="${FUNCNAME[0]}"
	source "${scriptsdir}/${modulefile}"
}

command_kill.sh(){
  modulefile="${FUNCNAME[0]}"
	source "${scriptsdir}/${modulefile}"
}

command_kill_all.sh(){
  modulefile="${FUNCNAME[0]}"
	source "${scriptsdir}/${modulefile}"
}

command_recommission.sh() {
	modulefile="${FUNCNAME[0]}"
	source "${scriptsdir}/${modulefile}"
}

command_start.sh(){
  modulefile="${FUNCNAME[0]}"
	source "${scriptsdir}/${modulefile}"
}

command_start_all.sh(){
  modulefile="${FUNCNAME[0]}"
	source "${scriptsdir}/${modulefile}"
}

command_start_lb.sh(){
  modulefile="${FUNCNAME[0]}"
	source "${scriptsdir}/${modulefile}"
}

command_start_proxy.sh(){
  modulefile="${FUNCNAME[0]}"
	source "${scriptsdir}/${modulefile}"
}

command_stop.sh(){
  modulefile="${FUNCNAME[0]}"
	source "${scriptsdir}/${modulefile}"
}

command_stop_all.sh(){
  modulefile="${FUNCNAME[0]}"
	source "${scriptsdir}/${modulefile}"
}

command_stop_lb.sh(){
  modulefile="${FUNCNAME[0]}"
	source "${scriptsdir}/${modulefile}"
}

command_stop_proxy.sh(){
  modulefile="${FUNCNAME[0]}"
	source "${scriptsdir}/${modulefile}"
}

## Common commands

command_run_service.sh(){
  modulefile="${FUNCNAME[0]}"
	source "${scriptsdir}/${modulefile}"
}

command_start_service.sh(){
  modulefile="${FUNCNAME[0]}"
	source "${scriptsdir}/${modulefile}"
}

command_stop_service.sh(){
  modulefile="${FUNCNAME[0]}"
	source "${scriptsdir}/${modulefile}"
}

command_install.sh(){
  modulefile="${FUNCNAME[0]}"
	source "${scriptsdir}/${modulefile}"
}

command_disrupt.sh() {
	modulefile="${FUNCNAME[0]}"
	source "${scriptsdir}/${modulefile}"
}

command_login.sh() {
	modulefile="${FUNCNAME[0]}"
	source "${scriptsdir}/${modulefile}"
}

command_logout.sh() {
	modulefile="${FUNCNAME[0]}"
	source "${scriptsdir}/${modulefile}"
}

command_nodes.sh() {
	modulefile="${FUNCNAME[0]}"
	source "${scriptsdir}/${modulefile}"
}

command_critical_nodes.sh(){
  modulefile="${FUNCNAME[0]}"
	source "${scriptsdir}/${modulefile}"
}

command_open.sh() {
	modulefile="${FUNCNAME[0]}"
	source "${scriptsdir}/${modulefile}"
}

command_recover.sh() {
	modulefile="${FUNCNAME[0]}"
	source "${scriptsdir}/${modulefile}"
}

command_sql.sh() {
	modulefile="${FUNCNAME[0]}"
	source "${scriptsdir}/${modulefile}"
}

command_status.sh() {
	modulefile="${FUNCNAME[0]}"
	source "${scriptsdir}/${modulefile}"
}

core_getopt.sh() {
	modulefile="${FUNCNAME[0]}"
	source "${scriptsdir}/${modulefile}"
}

core_exit.sh() {
	modulefile="${FUNCNAME[0]}"
	source "${scriptsdir}/${modulefile}"
}

core_functions.sh() {
	modulefile="${FUNCNAME[0]}"
	source "${scriptsdir}/${modulefile}"
}
