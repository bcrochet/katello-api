# katello repo
# The rpm-s are not getting there built frequently.
# curl -s http://repos.fedorapeople.org/repos/katello/katello/fedora-katello.repo -o /etc/yum.repos.d/fedora-katello.repo

cat > /etc/yum.repos.d/katello.repo <<EOF
[katello-devel]
name=Katello Devel - \$basearch 
baseurl=http://hudson.rhq.lab.eng.bos.redhat.com:8080/hudson/job/katello-build/lastSuccessfulBuild/artifact/rpms/\$basearch/
enabled=1
gpgcheck=0
metadata_expire=120

[katello-gems-devel]
name=Katello Ruby Gems - \$basearch
baseurl=http://hudson.rhq.lab.eng.bos.redhat.com:8080/hudson/job/katello-gems/lastSuccessfulBuild/artifact/rpms/\$basearch/
enabled=1
gpgcheck=0
http_caching=packages

# Needed for the latyest gems - the Jenkins katello-gems is not "up-to-dated" STILL # TODO
[fedora-katello]
name=integrates together a series of open source systems management tools
baseurl=http://repos.fedorapeople.org/repos/katello/katello/fedora-\$releasever/\$basearch/
enabled=1
skip_if_unavailable=1
gpgcheck=0

EOF

# pulp repo
curl -s http://repos.fedorapeople.org/repos/pulp/pulp/fedora-pulp.repo -o /etc/yum.repos.d/fedora-pulp.repo
sed -i "s/enabled=0/enabled=1/g" /etc/yum.repos.d/fedora-pulp.repo

# candlepin repo
cat > /etc/yum.repos.d/candlepin.repo <<EOF
[candlepin]
name=candlepin
baseurl=http://dept.rhndev.redhat.com/yum/candlepin/0.4/Fedora/\$releasever
enabled=1
skip_if_unavailable=1
gpgcheck=0
EOF
echo -e "\t=== Yum repositories setup"
