# needed - some initial stuff
export HOME="/root"
export USER="root"

# install packages needed
yum -y install git postgresql-server

# do the installation of postgresql
if [ "`su - postgres -c "ls . | grep candlepin"`" != "candlepin" ]
then
	echo -e "\t=== Install candlepin DB"
	su - postgres -c "mkdir candlepin"
	su - postgres -c "initdb -D candlepin/"
	sleep 30
	sed -i "s/PGDATA=\/var\/lib\/pgsql\/data/PGDATA=\/var\/lib\/pgsql\/candlepin/g" /etc/init.d/postgresql
	service postgresql start
	su - postgres -c "echo postgres > /tmp/pgpasswd;createuser -U postgres -s candlepin --password < /tmp/pgpasswd"
	sleep 5
	su - postgres -c "createdb candlepin"
	sleep 30
else
	echo -e "\t=== Skip Candlepin DB setup. It's already there"
fi

# fetch candlepin repo + product_utils stuff
if [ ! -d ~/candlepin  ]
then
	echo -e "\t=== Git clone the repositories"
	pushd ~
	git clone git://git.fedorahosted.org/candlepin.git
	git clone git://axiom.rdu.redhat.com/scm/git/cp_product_utils
	cat > /root/.candlepinrc  << EOF
GENDB=1
FORCECERT=1
TESTDATA=0
IMPORTDIR=/root/cp_product_utils
EOF
	popd
else
	echo -e "\t=== Seems repositories are there - using"
fi

# candlepin config
rm -rf /etc/candlepin/
mkdir /etc/candlepin/
echo -e "\t=== Configuration"
cat > /etc/candlepin/candlepin.conf << EOF
jpa.config.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
jpa.config.hibernate.connection.driver_class=org.postgresql.Driver
jpa.config.hibernate.connection.url=jdbc:postgresql:candlepin
jpa.config.hibernate.hbm2ddl.auto=validate
jpa.config.hibernate.connection.username=candlepin
jpa.config.hibernate.connection.password=postgres
candlepin.auth.trusted.enabled = true
candlepin.auth.ssl.enabled = true
candlepin.auth.oauth.enabled = true
candlepin.auth.oauth.consumer.katello.secret = shhhh
candlepin.dont.work.for.mccune = true
EOF

# export JAVA_HOME
JH="/usr/lib/jvm/`ls /usr/lib/jvm/ | grep java-1.6.0-openjdk | tail -1`" # to get java full path with `uname -i`
export JAVA_HOME=$JH
echo -e "\t=== exported: JAVA_HOME=$JH"

# apply filter not to install ri & rdocs :)
touch $HOME/.gemrc; echo "gem: --no-ri --no-rdoc" >> $HOME/.gemrc

# installing needed gems
if [ "`gem list | grep buildr | awk '{print $1}'`" != "buildr" ]
then
	JAVA_HOME=$JH gem install buildr -v 1.4.4 # IMPORTANT, all other dependencies seems would get installed :) if not - FIXME
	curl -k https://fedorahosted.org/candlepin/attachment/wiki/Deployment/buildr.patch?format=raw -o /tmp/buildr.patch
	patch /usr/bin/buildr /tmp/buildr.patch
fi

#for package in {"rest-client","json","buildr"}
#do
#	if [ "`gem list $package | grep $package | awk '{print $1}'`" == "" ]
#	then
#		JAVA_HOME=$JH gem install $package
#	fi
#done

# install candlepin
pushd /root/candlepin/proxy/
JAVA_HOME=$JH ./buildconf/scripts/deploy --trace
BUILD_RES=$?
popd
exit $BUILD_RES

