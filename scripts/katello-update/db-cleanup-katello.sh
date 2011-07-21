# include shell helpers
[ -f ./helper-katello.sh ] && . ./helper-katello.sh
[ -f ../other/helper-katello.sh ] && . ../other/helper-katello.sh 

# -------------------------------------------------------------------------------------------------------
# *** cleanup - candlepin
pushd /usr/share/candlepin
service tomcat6 stop
./cpsetup
service tomcat6 start && sleep 5
popd

# -------------------------------------------------------------------------------------------------------
# *** cleanup - pulp
# sometimes mongod stucks: lock files need to be removed...
service pulp-server stop
rm -f /var/lib/mongodb/mongod.lock
rm -f /var/lock/subsys/mongod
service mongod start && sleep 5
echo -e "use pulp_database\ndb.dropDatabase()" | mongo
rm -rf /var/lib/pulp/packages/* /var/lib/pulp/repos/* # cleanup synced packages & repos
service mongod stop
service pulp-server init
service pulp-server start

# -------------------------------------------------------------------------------------------------------
# *** configure / reset settings
config_pulp
config_candlepin
config_katelloProperties
reset_oauth
services_restart

# -------------------------------------------------------------------------------------------------------
# *** cleanup - katello
pushd /usr/lib/katello
service katello stop
# export RAILS_ENV="`get_katelloEnv`"; rake setup --trace
service katello initdb
if [ "`get_katelloDB`" == "sqlite3" ]
then
	chown katello:katello /var/lib/katello/*sqlite3
fi
popd

# -------------------------------------------------------------------------------------------------------
# *** all services restart
services_restart