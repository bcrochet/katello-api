#!/usr/bin/python

import xmlrpclib
import os

EUS_CHANNEL = "RHEL EUS Server Optional ("
URL = "https://rhn.errata.stage.redhat.com/rpc/api"
user = os.getenv("RHN_USER","qa@redhat.com")
pswd = os.getenv("RHN_PASS","redhatqa")

# Register to RHN
print os.popen("rhnreg_ks --username %s --password %s --use-eus-channel --force"%(user,pswd)).read()
systemid = os.popen("cat /etc/sysconfig/rhn/systemid | grep 'ID-' | awk -F 'ID-' '{print $2}' | awk -F '<' '{print $1}'").read()
print "Registered with ID:", systemid

client = xmlrpclib.Server(URL, verbose=0)
session = client.auth.login(user, pswd)
list = client.system.listChildChannels(session,int(systemid))
for channel in list:
   if channel.get('NAME').startswith(EUS_CHANNEL):
      client.system.setChildChannels(session,int(systemid),channel.get('ID'))
      print 'Subscribed to:', channel.get('NAME')
client.auth.logout(session)
