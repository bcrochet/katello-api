
# Wait for Katello to become responsive - max 60 sec.
# -------------------------------------------------------------------------------------------------------
waitfor_katello()
{
HOST_ME="`hostname`"
grep "use_ssl: true" /etc/katello/katello.yml > /dev/null
RET=$?
if (($RET==0)); then
  echo -e "\t=== Katello in SSL mode"
  PING_CMD="curl -s -k https://$HOST_ME:3000/api/ping"
else
  PING_CMD="curl -s http://$HOST_ME:3000/api/ping"
fi
TRIES=0; MAX_TRIES=120;PING=""
while [ "$PING" == "" ] && (($TRIES < $MAX_TRIES))
do
  sleep 0.5s
  PING="`$PING_CMD`"
  ((TRIES++))
done
if ((TRIES == $MAX_TRIES)); then
  echo "Failed to get Katello running in $((MAX_TRIES / 2)) sec." >&2
  return 1
fi
echo "$PING"
}

config_katelloEnv()
{
  KATELLO_SYSCONFIG="/etc/sysconfig/katello"
  if [ -z "$RAILS_ENV" ]
  then
    export RAILS_ENV=development
  fi
  sed -i '/KATELLO_ENV/ d' $KATELLO_SYSCONFIG
  echo "KATELLO_ENV=$RAILS_ENV" >> $KATELLO_SYSCONFIG
}

config_katelloProperties()
{
  if [ -f /etc/katello/katello.yml.rpmnew ]
  then
    mv /etc/katello/katello.yml /etc/katello/katello.yml.rpmold
    mv /etc/katello/katello.yml.rpmnew /etc/katello/katello.yml
  fi
  sed -i "s/localhost/`hostname`/g" /etc/katello/katello.yml # localhost -> `hostname`
  sed -i "s/use_ssl: false/use_ssl: true/g" /etc/katello/katello.yml # use SSL
  
  sed -i "s/^#  notification:/  notification:/g" /etc/katello/katello.yml
  sed -i "s/^#    polling_seconds:/    polling_seconds:/g" /etc/katello/katello.yml # uncomment first
  sed -i "s/polling_seconds: .*/polling_seconds: 300/g" /etc/katello/katello.yml # polling: 5 min
}

config_katelloDB()
{
  DB_CFG="/etc/katello/database.yml"
  S="`grep ^KATELLO_ENV= /etc/sysconfig/katello`"
  KATELLO_ENV="`expr substr $S 13 20`"
  dbType="$1"

  echo "" > $DB_CFG
  for env in {"test","development","production"}
  do
    if [ "$env" == "$KATELLO_ENV" ] && [ "$dbType" == "postgresql" ]
    then
      cat >> $DB_CFG <<EOF
$env:
  adapter: postgresql
  username: katello
  password: katello
  database: katello
  host: localhost
  encoding: UTF8

EOF
    else
      cat >> $DB_CFG <<EOF
$env:
  adapter: sqlite3
  database: /var/lib/katello/$env.sqlite3
  pool: 5
  timeout: 5000

EOF
    fi
  done

  if [ "$dbType" == "postgresql" ]
  then
    prepare_katelloPostgresDB
  fi
}

config_candlepin()
{
if [ "`grep "candlepin.auth.trusted.enabled" /etc/candlepin/candlepin.conf`" == "" ] || \
   [ "`grep "candlepin.auth.ssl.enabled" /etc/candlepin/candlepin.conf`" == "" ] || \
   [ "`grep "candlepin.auth.oauth.enabled" /etc/candlepin/candlepin.conf`" == "" ] || \
   [ "`grep "candlepin.auth.oauth.consumer.katello.secret" /etc/candlepin/candlepin.conf`" == "" ]
then  
  echo -e "\t=== Applying auth + Katello oauth settings in Candlepin"
  echo -e "candlepin.auth.trusted.enabled = true\ncandlepin.auth.ssl.enabled = true\ncandlepin.auth.oauth.enabled = true\ncandlepin.auth.oauth.consumer.katello.secret = shhh" >> /etc/candlepin/candlepin.conf
fi
}

config_pulp()
{
  sed -i "s/localhost/`hostname`/g" /etc/pulp/pulp.conf
  sed -i "s/localhost.localdomain/`hostname`/g" /etc/pulp/client.conf
  sed -i "s/localhost/`hostname`/g" /etc/pulp/client.conf
  
  # set ServerName in httpd conf
  APACHE_SERVERNAME="Servername `hostname`"
  if [ "`grep "#ServerName" /etc/httpd/conf/httpd.conf`" != "" ]
  then
    if [ "`grep "$APACHE_SERVERNAME" /etc/httpd/conf/httpd.conf`" == "" ]
    then
      echo $APACHE_SERVERNAME >> /etc/httpd/conf/httpd.conf
    fi
  fi
}

reset_oauth()
{
  pushd /usr/lib/katello
  ./script/reset-oauth
  popd
}

prepare_katelloPostgresDB()
{
	if [ "`su - postgres -c "ls . | grep katello"`" != "katello" ]
	then
		if [ "`service postgresql status`" == "postmaster is stopped" ]
		then
			service postgresql start
		fi
		echo -e "\t=== Install katello DB (Postgres)"
		sudo su - postgres -c 'createuser -dls katello  --no-password'
		sleep 5
		sudo su - postgres -c "psql --command \"alter user katello with password 'katello'\" "
		sleep 5
		sudo su - postgres -c "echo katello > /tmp/pgpasswd; createdb -U katello katello < /tmp/pgpasswd"
		sleep 30
	else
		echo -e "\t=== Skip Katello DB setup. It's already there"
	fi
}

services_restart()
{
  service tomcat6 restart
  service pulp-server restart
  service katello restart
  waitfor_katello
}

get_katelloDB()           # Returns DB type of installed Katello: {postgresql,sqlite}
{
        DB_CFG="/etc/katello/database.yml"
        SYS_CFG="/etc/sysconfig/katello"
        S="`grep ^KATELLO_ENV $SYS_CFG`";katelloEnv="${S:12:20}"
		if [ -z "$katelloEnv" ]
		then
				katelloEnv="production"
  		fi
        LN="`grep -n "^$katelloEnv:" $DB_CFG | cut -d: -f1`"
        ((LN++))
        echo "`awk -v ln=$LN 'NR==ln{print $0}' $DB_CFG | awk '{print $2}'`"
}

get_katelloEnv()
{
        SYS_CFG="/etc/sysconfig/katello"
        S="`grep ^KATELLO_ENV $SYS_CFG`";katelloEnv="${S:12:20}"
		if [ -z "$katelloEnv" ]
		then
				katelloEnv="production"
  		fi
  		echo $katelloEnv
}