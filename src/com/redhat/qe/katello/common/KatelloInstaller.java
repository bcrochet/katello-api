package com.redhat.qe.katello.common;

import java.io.File;
import java.util.logging.Logger;
import com.redhat.qe.auto.testng.TestScript;
import com.redhat.qe.tools.SCPTools;
import com.redhat.qe.tools.SSHCommandRunner;

public class KatelloInstaller {
	private Logger log = Logger.getLogger(KatelloUpdater.class.getName());
	private SSHCommandRunner sshRunner;
	private SCPTools scpRunner;
	
	private String servername;
	private String password;
	private String dirToShell; // e.g: full path of "scripts/katello-install/
	private String dbType;
	private String railsEnv; // e.g. {development,production}

	public KatelloInstaller(String server, String pass, String files, String dbType, String railsEnv){
		try {
			this.servername = server;
			this.password = pass;
			this.dirToShell = files;
			this.dbType = dbType;
			this.railsEnv = railsEnv;
			if(this.railsEnv==null) this.railsEnv = "development";
			sshRunner = new SSHCommandRunner(
					servername, "root", password,"","", null);
		}
		catch (Exception e) {
			log.severe(e.getMessage());
			System.exit(1);
		}
	}

	public void doInstall(){
		try{
			sshRunner.runCommandAndWait( //enables run of scp
					"yum -y install openssh-clients", true);
			File dirShell = new File(this.dirToShell);
			scpRunner = new SCPTools(servername,"root","",password);
			if(dirShell.isDirectory()){
				File[] files = dirShell.listFiles();
				for(int i=0;i<files.length;i++){
					if(files[i].getAbsolutePath().toString().endsWith(".sh")){
						scpRunner.sendFile(files[i].getAbsolutePath(), "/tmp/");
					}
				}
				scpRunner.sendFile("scripts/other/helper-katello.sh", "/tmp/");
				scpRunner.close();
				sshRunner.runCommandAndWait("pushd /tmp; chmod u+x *.sh;" +
						"export RAILS_ENV="+this.railsEnv+"; ./install-katello.sh "+this.dbType+"; popd;", true);
			}else{
				log.severe(String.format(
						"Wrong scripts directory specified: [%s]",
						this.dirToShell));
				System.exit(1);
			}
		}catch(Exception ex){
			log.severe(ex.getMessage());
			System.exit(1);
		}		
	}
	
	public static void main(String[] args) {
		new TestScript(); // should initialize the ~/automation.properties
		KatelloInstaller katelloInstall = new KatelloInstaller(args[0],args[1],args[2],args[3],args[4]);
		katelloInstall.doInstall();
	}
}
