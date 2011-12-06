#!/usr/bin/python
'''
How to use:
wget http://git.engineering.redhat.com/?p=users/gkhachik/katello-api.git;a=blob_plain;f=scripts/beaker-utils.py;hb=HEAD -O beaker-utils.py
chmod a+x beaker-utils.py
./beaker-utils.py --method=<methodname> [--args=arg1=value1,arg2=value2,<etc>]
'''
from optparse import OptionParser
import os

def parseDictFromArgs(sargs, delimiter=',', assignOperator='='):
    arr = sargs.split(delimiter)
    d = {}
    for a in arr:
        a = a.strip()
        kv = a.split(assignOperator)
        d[kv[0].strip()]=kv[1].strip()
    return d

class BeakerUtils(object):
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

def main():
    parser = OptionParser()
    parser.add_option("--method", dest="method", 
                      help="provides method name to be invoked from the class", metavar="METHOD")
    parser.add_option("--args", dest="arguments", 
                      help="provides arguments in <arg_name1>=<arg_value1>,<arg_name2>=<arg_value2>, etc. format", metavar="ARGS")
    (options, args) = parser.parse_args()
    
    cls = BeakerUtils()
    method = options.method # get the method name
    if options.arguments!=None:
        args = parseDictFromArgs(options.arguments) # ...and arguments
        cls.__getattribute__(method)(**args)
    else:
        cls.__getattribute__(method)()

if __name__ == "__main__":
    main()