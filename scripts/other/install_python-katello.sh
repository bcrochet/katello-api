#!/bin/bash

pushd $HOME
yum -y install git tito
rm -rf python-katello/
git clone git://github.com/gkhachik/python-katello.git
pushd python-katello/src/
rm -rf /tmp/tito/python-katello
tito build --srpm --test --output /tmp/tito/python-katello
yum-builddep -y `ls /tmp/tito/python-katello/*.src.rpm`
tito build --test --rpm --output /tmp/tito/python-katello
yum -y localinstall `ls /tmp/tito/python-katello/noarch/python-katello*noarch.rpm` --nogpgcheck
popd
popd
