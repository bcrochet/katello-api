# include shell helpers
[ -f ./helper-katello.sh ] && . ./helper-katello.sh
[ -f ../other/helper-katello.sh ] && . ../other/helper-katello.sh 

# -------------------------------------------------------------------------------------------------------
# *** stop services
service katello stop
service pulp-server stop
service tomcat6 stop

# -------------------------------------------------------------------------------------------------------
# *** update RPM-s
yum clean all
yum update -y pulp* katello candlepin*

# -------------------------------------------------------------------------------------------------------
# *** start services 
export KATELLO_HOME=/usr/lib/katello #it's just a workaround.
service tomcat6 start
service pulp-server start
service katello start

waitfor_katello # wait for Katello ping reply (max 60 sec.)