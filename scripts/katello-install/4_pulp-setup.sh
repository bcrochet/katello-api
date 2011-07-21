# include shell helpers
[ -f ./helper-katello.sh ] && . ./helper-katello.sh
[ -f ../other/helper-katello.sh ] && . ../other/helper-katello.sh 

# check & install pulp-*.rpm
PULP=`rpm -q pulp`
if [ ! `expr "$PULP" : "pulp"` == 4 ]
then
	yum install -y pulp pulp-client
else
	echo -e "\t=== $PULP is already installed"
fi

config_pulp
service pulp-server init
