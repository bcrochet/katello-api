# A simple script that brings your Fedora 14 system "beakerized" 
# - possible to run "bkr" commands.
# NOTE: Needs to be run *manually* while preparing a node for Hudson e.g. 
# needs to be run for: "su - hudson"

BKR_F14_REPO=http://beaker.engineering.redhat.com/harness/Fedora14/beaker.repo
curl $BKR_F14_REPO -o /etc/yum.repos.d/beaker.repo
yum -y install beaker-client beakerlib-redhat beaker-redhat # one of this last 2 rpm-s will fail, but it's ok :)
mkdir ~/.beaker_client
touch ~/.beaker_client/config
echo "HUB_URL = \"https://beaker.engineering.redhat.com\"" >> ~/.beaker_client/config
echo "AUTH_METHOD = \"password\"" >> ~/.beaker_client/config
chmod +x ~/.beaker_client/config