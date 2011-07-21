console(){
        echo "*** $1"
}

checkInstallRpm(){
rpmName="$1"
if [ "`rpm -q $rpmName | grep \"not installed\"`" != "" ]
then 
        console "Installing: [$rpmName] now"
        yum -y install $rpmName > /dev/null 2>&1
        console "[$rpmName]: `rpm -q $rpmName`"
else
        console "Using: [$rpmName]"
fi
}

# *****************************************************************************
echo ""

checkInstallRpm git
checkInstallRpm rubygems
checkInstallRpm java-1.6.0-openjdk
checkInstallRpm java-1.6.0-openjdk-devel
checkInstallRpm ruby-devel
checkInstallRpm gcc

JH="/usr/lib/jvm/`ls /usr/lib/jvm/ | grep java-1.6.0-openjdk | tail -1`" # to get java full path with `uname -i`
export JAVA_HOME=$JH
console "Exporting: [JAVA_HOME=$JH]"

touch $HOME/.gemrc; echo "gem: --no-ri --no-rdoc" > $HOME/.gemrc
if [ "`gem list | grep buildr | awk '{print $1}'`" != "buildr" ]
then
	console "Installing: [buildr]"
	JAVA_HOME=$JH gem install buildr > /dev/null 2>&1 # IMPORTANT, all other dependencies seems would get installed :)
fi
console "Using: [`gem list | grep buildr`]"

pushd ~/ > /dev/null
console "Retrieving: [katello-api] to: [`pwd`/katello-api]"
rm -rf `pwd`/katello-api
git clone git://git.engineering.redhat.com/users/gkhachik/katello-api.git > /dev/null 2>&1

# running the tests
KATELLO_SERVER_HOSTNAME="$1"
KATELLO_SSH_PASS="$2"
cd katello-api/
cp ./katello-automation.properties ./automation.properties
sed -i '/katello.server.hostname/ d' ./automation.properties
echo "katello.server.hostname=$KATELLO_SERVER_HOSTNAME" >> ./automation.properties
sed -i '/katello.server.hostname/ d' ./automation.properties
echo "katello.server.hostname=$KATELLO_SERVER_HOSTNAME" >> ./automation.properties
sed -i '/katello.ssh.passphrase/ d' ./automation.properties
echo "katello.ssh.passphrase=$KATELLO_SSH_PASS" >> ./automation.properties

JAVA_OPTS="-Dautomation.propertiesfile=`pwd`/automation.properties" KATELLO_API_TESTNAMES="ALl_Tests" buildr compile test --trace

echo ""



