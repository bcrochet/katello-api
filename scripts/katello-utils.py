#!/usr/bin/python
'''
How to use:
wget http://git.engineering.redhat.com/?p=users/gkhachik/katello-api.git;a=blob_plain;f=scripts/beaker-utils.py;hb=HEAD -O beaker-utils.py
chmod a+x beaker-utils.py
./beaker-utils.py --method=<methodname> [--args=arg1=value1,arg2=value2,<etc>]

===
NOTES:
# Returns Beaker JobID (if machine is reserved through Beaker)
(cat /etc/motd | grep JOBID | cut -f2 -d=)

# stores Jenkins node dsa public key:
(export JENKINS_DSA_PUBKEY=`cat ~/.ssh/id_dsa.pub`)
'''
from optparse import OptionParser
import os
from xml.etree.ElementTree import ElementTree
from subprocess import Popen
import subprocess

def parseDictFromArgs(sargs, delimiter=',', assignOperator='='):
    arr = sargs.split(delimiter)
    d = {}
    for a in arr:
        a = a.strip()
        kv = a.split(assignOperator)
        d[kv[0].strip()]=kv[1].strip()
    return d

class KatelloUtils(object):
    def getMirroredF15Url(self):
        lab = os.getenv("LAB_CONTROLLER", "lab.rhts.englab.brq.redhat.com") # BRQ by default
        if lab == "lab2.rhts.eng.bos.redhat.com":
            print "http://download.bos.redhat.com/pub/fedora/linux/releases/15/Fedora/x86_64/os/"
        elif lab == "lab.rhts.eng.nay.redhat.com":
            print "http://download.eng.nay.redhat.com/pub/fedora/linux/releases/15/Fedora/x86_64/os/"
        elif lab == "lab.rhts.eng.pnq.redhat.com":
            print "http://download.eng.pnq.redhat.com/pub/fedora/linux/releases/15/Fedora/x86_64/os/"
        elif lab == "lab-01.eng.tlv.redhat.com":
            print "http://download.eng.tlv.redhat.com/pub/fedora/linux/releases/15/Fedora/x86_64/os/"
        else: # BRQ; RDU
            print "http://download.eng.brq.redhat.com/pub/fedora/linux/releases/15/Fedora/x86_64/os/"
    
    def getBeakerJobStatus(self, jid):
        Popen("bkr job-results %s > /tmp/beaker-%s.log"%(jid,jid), stdout=None, shell=True).wait()
        
        et = ElementTree()
        tree = ElementTree.parse(et,"/tmp/beaker-%s.log"%jid)
        tasks = tree.findall("recipeSet/recipe/task")
        for task in tasks:
            print "name=[%s]; result=[%s]; duration=[%s]; status=[%s]"%\
            (task.get("name"),task.get("result"),task.get("duration"),task.get("status"))
            
        
    
def main():
    parser = OptionParser()
    parser.add_option("--method", dest="method", 
                      help="provides method name to be invoked from the class", metavar="METHOD")
    parser.add_option("--args", dest="arguments", 
                      help="provides arguments in <arg_name1>=<arg_value1>,<arg_name2>=<arg_value2>, etc. format", metavar="ARGS")
    (options, args) = parser.parse_args()
    
    cls = KatelloUtils()
    method = options.method # get the method name
    if options.arguments!=None:
        args = parseDictFromArgs(options.arguments) # ...and arguments
        cls.__getattribute__(method)(**args)
    else:
        cls.__getattribute__(method)()

if __name__ == "__main__":
    main()