chkconfig iptables off
chkconfig ip6tables off
chkconfig network on
 
service iptables stop
service ip6tables stop

sed -i 's/Defaults.*requiretty/#Defaults    requiretty/g' /etc/sudoers

SELINUX_STATUS=`getenforce`
if [ "$SELINUX_STATUS" == 'Enforcing' ]
then
	echo -e "\t=== SELinux needs to be permissive..."
	sed -i "s/SELINUX=enforcing/SELINUX=permissive/g" /etc/selinux/config
	setenforce 0
else
	echo -e "\t=== SELinux is permissive: nothing to do"
fi
