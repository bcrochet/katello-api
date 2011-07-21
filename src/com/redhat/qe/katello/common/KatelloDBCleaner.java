package com.redhat.qe.katello.common;

import java.util.logging.Logger;
import com.redhat.qe.auto.testng.TestScript;
import com.redhat.qe.tools.SCPTools;
import com.redhat.qe.tools.SSHCommandRunner;

public class KatelloDBCleaner {
	public static final String PATH_TO_SHELL = 
		"scripts/katello-update/db-cleanup-katello.sh";

	private Logger log = Logger.getLogger(KatelloUpdater.class.getName());
	private KatelloInfo katelloInfo;
	private SSHCommandRunner sshRunner;
	private SCPTools scpRunner;
	
	public KatelloDBCleaner(){
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
	
	public void doCleanupDB(){
		try{
			sshRunner.runCommandAndWait( //enables run of scp
					"yum -y install openssh-clients", true);
			scpRunner.sendFile(PATH_TO_SHELL, "/tmp");
			scpRunner.sendFile("scripts/other/helper-katello.sh", "/tmp/");
			sshRunner.runCommandAndWait("pushd /tmp; chmod +x *.sh; " +
							"./db-cleanup-katello.sh; popd", true);
		}catch(Exception ex){
			log.severe(ex.getMessage());
			System.exit(1);
		}
	}
	
	public static void main(String[] args) {
		new TestScript(); // should initialize the ~/automation.properties
		KatelloDBCleaner katelloDBClean = new KatelloDBCleaner();
		katelloDBClean.doCleanupDB();
	}

}
