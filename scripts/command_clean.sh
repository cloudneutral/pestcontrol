#!/bin/bash

#if fn_prompt_yes_no "Delete client and node certs in '${certsdir}' - are you sure?" Y; then
#  rm -rf ${certsdir}
#fi

if fn_prompt_yes_no "Delete '${datadir}' - are you sure?" Y; then
  rm -rf ${datadir}
fi

if fn_prompt_yes_no "Delete '${installdir}' - are you sure?" Y; then
  rm -rf ${installdir}
fi