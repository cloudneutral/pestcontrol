#!/bin/bash

if ! fn_prompt_yes_no "Download and install ${version} to ${installdir}?" Y; then
  exit 0
fi

mkdir -p ${installdir}

cd ${installdir} || exit

case "$OSTYPE" in
  darwin*)
        curl https://binaries.cockroachdb.com/cockroach-${version}.tgz | tar -xz; cp -i cockroach-${version}/cockroach ${installdir}
        ;;
  *)
        wget https://binaries.cockroachdb.com/cockroach-${version}.tgz; tar -xvf cockroach-${version}.tgz; cp -i cockroach-${version}/cockroach ${installdir}
        ;;
esac

