# include shell helpers
[ -f ./helper-katello.sh ] && . ./helper-katello.sh
[ -f ../other/helper-katello.sh ] && . ../other/helper-katello.sh 

if (($# < 1 ))
then
 	DB_TYPE="sqlite"
else
	DB_TYPE="$1"
fi

./1_selinux-disable.sh
./2_repositories-setup.sh
./3_rpms-additional.sh
./4_pulp-setup.sh
./5_candlepin-setup.sh
./6_katello-setup.sh $DB_TYPE
services_restart
