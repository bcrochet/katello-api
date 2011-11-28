# A simple script that brings your RHEL6 system "beakerized" 
# - possible to run "bkr" commands.
# NOTE: Needs to be run *manually* while preparing a node for Hudson e.g. 
# needs to be run for: "su - hudson"

BKR_RHEL6_REPO=http://beaker.engineering.redhat.com/harness/RedHatEnterpriseLinux6/x86_64/beaker-redhat-repo-0.2-6.el6eso.noarch.rpm
yum -y install $BKR_RHEL6_REPO
yum -y install beaker-client beakerlib-redhat beaker-redhat # one of this last 2 rpm-s will fail, but it's ok :)
mkdir ~/.beaker_client
touch ~/.beaker_client/config
echo "HUB_URL = \"https://beaker.engineering.redhat.com\"" >> ~/.beaker_client/config
echo "AUTH_METHOD = \"password\"" >> ~/.beaker_client/config
chmod +x ~/.beaker_client/config
