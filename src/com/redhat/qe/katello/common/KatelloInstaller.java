package com.redhat.qe.katello.common;

import java.util.logging.Logger;
import com.redhat.qe.auto.testng.TestScript;
import com.redhat.qe.katello.tasks.KatelloTasks;

public class KatelloInstaller implements KatelloConstants {
	public static final String ENV_KATELLO_INSTALL_SERVERS = "KATELLO_INSTALL_SERVERS";
	private Logger log = Logger.getLogger(KatelloUpdater.class.getName());
	
	private String[] servers;

	public KatelloInstaller(){
		try {
			String servs = System.getenv(ENV_KATELLO_INSTALL_SERVERS);
			if(servs == null){
				log.severe("Please provide system env: ["+ENV_KATELLO_INSTALL_SERVERS+"]");
				System.exit(1);
			}
			this.servers = servs.split(",");
			for(int i=0;i<this.servers.length;i++)
				this.servers[i] = this.servers[i].trim(); // trim possible spaces.
		}
		catch (Exception e) {
			log.severe(e.getMessage());
			System.exit(1);
		}
	}

	public void run_setup_rpmInstaller(){
		String servername;
		try{
			for(int i=0;i<this.servers.length;i++){
				servername = servers[i];
				log.info("Installing Katello on: ["+servername+"]");
				String server_platform = run_trusted(servername,
						"python -c 'from platform import platform; print platform();'");
				// STEP 0: install epel (if RHEL6)
				if(server_platform.contains("redhat-6")){ // For RHEL6 we need EPEL
					log.fine("Setup EPEL repository");
					run_trusted(servername,
						"yum -y install http://download.fedoraproject.org/pub/epel/6/`uname -i`/epel-release-6-5.noarch.rpm");
				}
				// STEP 1: install git, tito
				log.fine("Yum install tito git");
				run_trusted(servername,
					"yum -y install git tito");
				// STEP 2: clone the python-katello
				log.fine("Git clone python-katello repo");
				run_trusted(servername,
					"rm -rf python-katello/; git clone git://github.com/gkhachik/python-katello.git");
				// STEP 3: install python_katello
				log.fine("Install python_katello.rpm");
				run_trusted(servername, 
					"cd python-katello/src/; tito build --srpm --test --output /tmp/tito/python_katello; "+
					"yum-builddep -y \\$(ls /tmp/tito/python_katello/*.src.rpm); "+
					"tito build --test --rpm --output /tmp/tito/python_katello; "+
					"yum -y localinstall \\$(ls /tmp/tito/python_katello/noarch/python_katello*noarch.rpm) --nogpgcheck");
				// assert - check if the python_katello is installed
				log.fine("Check: if python_katello is installed on: ["+servername+"]");
				String ret = run_trusted(servername, 
					"rpm -q python_katello");
				if(!ret.contains("python_katello")){
					log.severe("python_katello does not get installed on: ["+servername+"]");
					System.exit(2);
				}
				// STEP 4: Katello Install 
				log.fine("Install Katello: [katello-setup --db postgresql --use_ssl]");
				ret = run_trusted(servername, "katello-setup --db postgresql --use_ssl; echo \\$?");
				String out[] = ret.split("\n");
				ret = out[out.length-1];
				log.fine("Exit installation on: ["+servername+"] with status: ["+ret+"]");
				System.exit(Integer.parseInt(ret));
			}
		}catch(Exception ex){
			log.severe(ex.getMessage());
			System.exit(1);
		}		
	}
	
	public String run_trusted(String servername,String cmd){
		return KatelloTasks.run_local(true, "ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no -i "+
				JENKINS_SSH_PRIVKEY+" root@"+servername+" \""+cmd+"\""); 
	}
	
	public static void main(String[] args) {
		new TestScript(); // should initialize the ~/automation.properties
		KatelloInstaller katelloInstall = new KatelloInstaller();
		katelloInstall.run_setup_rpmInstaller();
	}
	
	
}
