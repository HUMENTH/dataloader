#!/bin/bash
DL_INSTALL_ROOT=`dirname $0`
source ${DL_INSTALL_ROOT}/util/util.sh

checkJavaVersion
java -cp "${DL_INSTALL_ROOT}/*" com.salesforce.dataloader.install.Installer

echo  Data Loader installation is completed.
echo ""