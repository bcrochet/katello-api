#!/bin/bash

[[ $# -lt 1 ]] && printf "Usage: ./run-katello-api.sh katello.redhat.com [password]\n" && exit 1
[[ ${USER} !=  'root' ]] && printf "You have to be root in order to run this script!\n" && exit 1

########### Functions ###########
function print_console() {
    printf "### $1\n"  2>&1  |  tee -a $LOG_FILE  # Redirect Err and Out to $LOG_FILE
}

function exit_code() {
	print_console "###########################################################"
    print_console "Test Log File = $LOG_FILE"
    print_console "###########################################################"
	exit $1 
}

function InstallRpm(){
	
  [[ $# -ne  1 ]] && echo 'Usage: InstallRpm  $PKG_NAME' | tee -a $LOG_FILE  && return 1
  	
  local RpmPkg=$1

  if ! rpm -q ${RpmPkg} &> /dev/null  
    then 
        print_console "Installing: [${RpmPkg}] now"
        yum -y install ${RpmPkg}  &>>  $LOG_FILE
        ! rpm -q ${RpmPkg} &> /dev/null  && printf "FAILED to install ${RpmPkg}\n" | tee -a $LOG_FILE  && return 1
        print_console "[${RpmPkg}]: $(rpm -q ${RpmPkg})"
    else
        print_console "Using: [${RpmPkg}]"
  fi
}

function InstallGem() {
	
  [[ $# -ne  1 ]] && echo 'Usage: InstallGem  $PKG_NAME' | tee -a $LOG_FILE  && return 1
  local GemPkg=${1}
  	
  egrep  -qs 'gem: --no-ri --no-rdoc' $HOME/.gemrc ||  \
     printf 'gem: --no-ri --no-rdoc\n' >> $HOME/.gemrc || exit_code 1 

  if [[ $( gem list | awk '{print $1}' | egrep -c "^$GemPkg " ) -ne 1 ]]
    then
    	[[ $GemPkg == 'buildr' ]] && export JAVA_HOME=/usr/lib/jvm/java-openjdk && print_console "Exporting: [JAVA_HOME=${JAVA_HOME}]"
     	print_console "Installing: [${GemPkg}]"
        gem install ${GemPkg}  &>> $LOG_FILE 
        print_console "${GemPkg}: [ $( gem list | egrep "^${GemPkg} " ) ]"
    else     
        print_console "Using: [ $( gem list | egrep '^buildr ' ) ]"
  fi      
	
}

### Script START 

# running the tests
DEFAULT_SERVER_ROOT_PASS='redhat'
KATELLO_SERVER_HOSTNAME=${1}
KATELLO_SSH_PASS=${2:-$DEFAULT_SERVER_ROOT_PASS}

DATE=$(date +"%Y-%m-%d-%H-%M-%S")
LOG_FILE=/tmp/api-test-${DATE}.log


InstallRpm git || exit_code 1 
InstallRpm rubygems ||  exit_code 1
InstallRpm java-1.6.0-openjdk || exit_code 1
InstallRpm java-1.6.0-openjdk-devel || exit_code 1
InstallRpm ruby-devel || exit_code 1
InstallRpm gcc || exit_code 1

InstallGem buildr || exit_code 1

SELF_DIR=$(dirname `readlink -f $0`)
pushd ${SELF_DIR}/../../ > /dev/null

cp ./katello-automation.properties /tmp/automation.properties
sed -i "s/^\([ ]*katello.server.hostname[ ]*=\).*/\1${KATELLO_SERVER_HOSTNAME}/" /tmp/automation.properties
sed -i "s/^\([ ]*katello.ssh.passphrase[ ]*=\).*/\1${KATELLO_SSH_PASS}/" /tmp/automation.properties


# Send detailed output to LOG_FILE
JAVA_OPTS="-Dautomation.propertiesfile=/tmp/automation.properties" KATELLO_API_TESTNAMES="ALl_Tests" buildr compile test --trace  2>>$LOG_FILE |  tee -a $LOG_FILE
 
exit_code 0;

### Script END