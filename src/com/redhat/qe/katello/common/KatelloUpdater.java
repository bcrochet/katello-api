package com.redhat.qe.katello.common;

import java.util.logging.Logger;
import com.redhat.qe.auto.testng.TestScript;
import com.redhat.qe.tools.SCPTools;
import com.redhat.qe.tools.SSHCommandRunner;

public class KatelloUpdater {
	public static final String PATH_TO_SHELL = 
		"scripts/katello-update/update-katello.sh";

	private Logger log = Logger.getLogger(KatelloUpdater.class.getName());
	private KatelloInfo katelloInfo;
	private SSHCommandRunner sshRunner;
	private SCPTools scpRunner;
	
	public KatelloUpdater(){
		try {
			this.katelloInfo = KatelloInfo.getInstance();
			sshRunner = new SSHCommandRunner(
					katelloInfo.getServername(), katelloInfo.getUsernameSSH(), 
					katelloInfo.getPasswordSSH(), 
					katelloInfo.getSshKeyPrivate(), 
					katelloInfo.getSshKeyPassphrase(), null);
			scpRunner = new SCPTools(katelloInfo.getServername(), 
					katelloInfo.getUsernameSSH(), 
					katelloInfo.getSshKeyPrivate(),
					katelloInfo.getPasswordSSH());
		}
		catch (Exception e) {
			log.severe(e.getMessage());
			System.exit(1);
		}
	}
	
	public void doUpdate(){
		try{
			sshRunner.runCommandAndWait( //enables run of scp
					"yum -y install openssh-clients", true);
			scpRunner.sendFile("scripts/other/install_python-katello.sh", "/tmp"); // to install python-katello
			sshRunner.runCommandAndWait("pushd /tmp; chmod +x ./install_python-katello.sh; " +
							"./install_python-katello.sh; katello-update -dr; popd", true);
		}catch(Exception ex){
			log.severe(ex.getMessage());
			System.exit(1);
		}
	}
	
	public static void main(String[] args) {
		new TestScript(); // should initialize the ~/automation.properties
		KatelloUpdater katelloUpdate = new KatelloUpdater();
		katelloUpdate.doUpdate();
	}

}
