package com.redhat.qe.katello.base;

import java.util.logging.Logger;

import org.testng.Assert;
import org.testng.annotations.BeforeSuite;

import com.redhat.qe.katello.common.KatelloConstants;
import com.redhat.qe.katello.tasks.KatelloCliTasks;
import com.redhat.qe.katello.tasks.KatelloTasks;
import com.redhat.qe.tools.ExecCommands;
import com.redhat.qe.tools.SSHCommandResult;
import com.redhat.qe.tools.SSHCommandRunner;

public class KatelloCliTestScript 
extends com.redhat.qe.auto.testng.TestScript 
implements KatelloConstants {

	protected static Logger log = Logger.getLogger(KatelloCliTestScript.class.getName());

	protected KatelloTasks servertasks	= null;
	protected KatelloCliTasks clienttasks = null;

	private int platform_id = -1; // made a class property - in case in the tests there would be a need to check platform.
	public KatelloCliTestScript() {
		super();
		try {
			SSHCommandRunner server_sshRunner = null;
			SSHCommandRunner client_sshRunner = null;
			try{
				server_sshRunner = new SSHCommandRunner(
						System.getProperty("katello.server.hostname", "localhost"), 
						System.getProperty("katello.ssh.user", "root"), 
						System.getProperty("katello.ssh.passphrase", "secret"), 
						System.getProperty("katello.sshkey.private", ".ssh/id_auto_dsa"), 
						System.getProperty("katello.sshkey.passphrase", "secret"), null);				
			}catch(Throwable t){
				log.warning("Warning: Could not initialize server's SSHCommandRunner.");
			}
			try{
				client_sshRunner = new SSHCommandRunner(
						System.getProperty("katello.client.hostname", "localhost"), 
						System.getProperty("katello.client.ssh.user", "root"), 
						System.getProperty("katello.client.ssh.passphrase", "secret"), 
						System.getProperty("katello.client.sshkey.private", ".ssh/id_auto_dsa"), 
						System.getProperty("katello.client.sshkey.passphrase", "secret"), null);				
			}catch(Throwable t){
				log.warning("Warning: Could not initialize client's SSHCommandRunner.");
			}

			ExecCommands localRunner = new ExecCommands();
			servertasks = new KatelloTasks(server_sshRunner, localRunner);
			clienttasks = new KatelloCliTasks(client_sshRunner, localRunner);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@BeforeSuite(description="Prepare katello-cli on the client side", alwaysRun = true)
	public void setUpKatelloCli(){
		String reuseSystem = System.getProperty("katello.cli.reuseSystem", "false");
		if(reuseSystem.equalsIgnoreCase("true")){
			return;
		}
		
		SSHCommandResult ssh_res;
		String platform = clienttasks.execute_remote(
		"python -c 'from platform import platform; print platform();'").getStdout();
		for(int i=0;i<CLIENT_PLATFORMS_ALLOWED.length;i++){
			if(platform.contains(CLIENT_PLATFORMS_ALLOWED[i][0])){
				this.platform_id = i;
				log.info(String.format("The client is running on platform: [%s]",CLIENT_PLATFORMS_ALLOWED[i][1]));
			}
		}
		if(this.platform_id == -1){
			log.severe(String.format("ERROR: Unsupported platform for katello client: [%s]",platform));
			System.exit(1);
		}

		clienttasks.execute_remote("yum clean all"); // cleanup the caches
		clienttasks.execute_remote("yum -y install wget");

		if(CLIENT_PLATFORMS_ALLOWED[this.platform_id][0].contains("fedora")){ // Fedora
			clienttasks.execute_remote(String.format("" +
					"wget -O /etc/yum.repos.d/fedora-katello.repo %s",YUM_REPO_FEDORA_KATELLO));
		}
		if(CLIENT_PLATFORMS_ALLOWED[this.platform_id][0].contains("redhat")){ // RHEL
			clienttasks.execute_remote(String.format("" +
					"wget -O /etc/yum.repos.d/epel-katello.repo %s",YUM_REPO_RHEL_KATELLO));
		}

		clienttasks.execute_remote("yum repolist && yum -y erase katello-cli"); // listing repos is needed, gets the repodata
		clienttasks.execute_remote("rm -f /etc/katello/client*"); // all kinda katello client configs
		
		ssh_res = clienttasks.execute_remote("yum -y install katello-cli");
		Assert.assertEquals(ssh_res.getExitCode(), new Integer(0), 
				"Check: return code is 0");
		ssh_res = clienttasks.execute_remote("rpm -q katello-cli");
		Assert.assertEquals(ssh_res.getExitCode(), new Integer(0), 
				"Check: return code of `rpm -q katello-cli`");
		
		clienttasks.execute_remote("sed -i \"s/localhost.localdomain/"+
				System.getProperty("katello.server.hostname", "localhost")+
				"/g\" "+KATELLO_CLI_CLIENT_CONFIG);
	}
	
}
