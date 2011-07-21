# include shell helpers
[ -f ./helper-katello.sh ] && . ./helper-katello.sh
[ -f ../other/helper-katello.sh ] && . ../other/helper-katello.sh 

echo -e "\t=== Installing Katello with ($1) DB"
yum -y install katello

config_katelloEnv
config_katelloDB $1
config_katelloProperties

reset_oauth
services_restart
service katello stop && sleep 3

pushd /usr/lib/katello/
bundle install # TODO - needs to be removed, once developers fix the issue with gems
rake setup # DB Installation
chown katello:katello /var/lib/katello/*sqlite3
popd