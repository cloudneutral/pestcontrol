#!/bin/bash

if [ "${ansi}" != "off" ]; then
  case "$OSTYPE" in
    darwin*)
          default="\x1B[0m"
          black="\x1B[30m"
          red="\x1B[31m"
          lightred="\x1B[91m"
          green="\x1B[32m"
          lightgreen="\x1B[92m"
          yellow="\x1B[33m"
          lightyellow="\x1B[93m"
          blue="\x1B[34m"
          lightblue="\x1B[94m"
          magenta="\x1B[35m"
          lightmagenta="\x1B[95m"
          cyan="\x1B[36m"
          lightcyan="\x1B[96m"
          darkgrey="\x1B[90m"
          lightgrey="\x1B[37m"
          white="\x1B[97m"
          creeol="\r\033[K"
          ;;
    *)
          default="\e[0m"
          black="\e[30m"
          red="\e[31m"
          lightred="\e[91m"
          green="\e[32m"
          lightgreen="\e[92m"
          yellow="\e[33m"
          lightyellow="\e[93m"
          blue="\e[34m"
          lightblue="\e[94m"
          magenta="\e[35m"
          lightmagenta="\e[95m"
          cyan="\e[36m"
          lightcyan="\e[96m"
          darkgrey="\e[90m"
          lightgrey="\e[37m"
          white="\e[97m"
          creeol="\r\033[K"
          ;;
  esac
fi

fn_print_dots() {
	if [ "${commandaction}" ]; then
		echo -en "${creeol}[${lightblue}....${default}] ${commandaction}: $*"
	else
		echo -en "${creeol}[${lightblue}....${default}] $*"
	fi

	if [ "${ansi}" == "off" ]; then
	  echo -en "\n"
	fi
}

fn_print_debug(){
  if [ "${commandaction}" ]; then
    echo -en "${creeol}[${lightcyan}DEBUG${default}] ${commandaction}: $*"
  else
    echo -en "${creeol}[${lightcyan}DEBUG${default}] $*"
  fi
	echo -en "\n"
}

fn_print_info(){
  if [ "${commandaction}" ]; then
    echo -en "${creeol}[${cyan}INFO${default}] ${commandaction}: $*"
  else
    echo -en "${creeol}[${cyan}INFO${default}] $*"
  fi
	echo -en "\n"
}

fn_print_warn(){
  if [ "${commandaction}" ]; then
    echo -en "${creeol}[${yellow}WARN${default}] ${commandaction}: $*"
  else
    echo -en "${creeol}[${yellow}WARN${default}] $*"
  fi
	echo -en "\n"
}

fn_print_error(){
  if [ "${commandaction}" ]; then
    echo -en "${creeol}[${red}ERROR${default}] ${commandaction}: $*"
  else
    echo -en "${creeol}[${red}ERROR${default}] $*"
  fi
	echo -en "\n"
}

fn_print_ok(){
  if [ "${commandaction}" ]; then
    echo -en "${creeol}[${green} OK ${default}] ${commandaction}: $*"
  else
    echo -en "${creeol}[${green} OK ${default}] $*"
  fi
  echo -en "\n"
}

fn_print_yellow(){
  echo -en "${creeol}${yellow}$*${default}"
	echo -en "\n"
}

fn_print_green(){
  echo -en "${creeol}${green}$*${default}"
	echo -en "\n"
}

fn_prompt_yes_no() {
	local prompt="$1"
	local initial="$2"

	if [ "${initial}" == "Y" ]; then
		prompt+=" [Y/n] "
	elif [ "${initial}" == "N" ]; then
		prompt+=" [y/N] "
	else
		prompt+=" [y/n] "
	fi

	while true; do
		read -e -p  "${prompt}" -r yn
		case "${yn}" in
			[Yy]|[Yy][Ee][Ss]) return 0 ;;
			[Nn]|[Nn][Oo]) return 1 ;;
		*) echo -e "Please answer yes or no." ;;
		esac
	done
}

fn_fail_check(){
    "$@"
    local status=$?
    if [ ${status} -ne 0 ]; then
        fn_print_error "$@" >&2
        exit 1
    fi
    return ${status}
}

fn_fail_check_print(){
    echo -e "$@"
    return 0
}
