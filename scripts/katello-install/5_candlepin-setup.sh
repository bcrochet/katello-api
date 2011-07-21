# include shell helpers
[ -f ./helper-katello.sh ] && . ./helper-katello.sh
[ -f ../other/helper-katello.sh ] && . ../other/helper-katello.sh 

# install packages needed
yum install -y postgresql-server postgresql
service postgresql initdb

cat > /var/lib/pgsql/data/pg_hba.conf <<EOF
# TYPE  DATABASE    USER        CIDR-ADDRESS          METHOD
local   all         candlepin                         trust
host    all         candlepin   127.0.0.1/32          trust

local   all         postgres                          trust
host    all         postgres    127.0.0.1/32          trust

local   all         all                               md5
host    all         all         127.0.0.1/32          md5
host    all         all         ::1/128               md5
EOF

service postgresql restart # IMPORTANT: needed for further steps to get processed

sudo su - postgres -c 'createuser -dls candlepin'

yum -y install candlepin-tomcat6
pushd /usr/share/candlepin/
./cpsetup
popd

config_candlepin
