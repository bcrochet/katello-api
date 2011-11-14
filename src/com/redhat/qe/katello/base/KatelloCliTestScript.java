package com.redhat.qe.katello.base;

import java.util.Calendar;
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

		// Disable the rhsm redhat.repo from being recreated - it fails yum to work properly.
		clienttasks.execute_remote("rm -f /etc/yum.repos.d/redhat.repo");
		disableRhsmYumPlugin();

		String reuseSystem = System.getProperty("katello.cli.reuseSystem", "false");
		if(reuseSystem.equalsIgnoreCase("true")){
			return;
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

		clienttasks.execute_remote("yum repolist && yum -y erase katello-cli*"); // listing repos is needed, gets the repodata
		clienttasks.execute_remote("rm -f /etc/katello/client*"); // all kinda katello client configs
		
		ssh_res = clienttasks.execute_remote("yum -y install katello-cli*");
		Assert.assertEquals(ssh_res.getExitCode(), new Integer(0), 
				"Check: return code is 0");
		ssh_res = clienttasks.execute_remote("rpm -q katello-cli");
		Assert.assertEquals(ssh_res.getExitCode(), new Integer(0), 
				"Check: return code of `rpm -q katello-cli`");
		
		clienttasks.execute_remote("sed -i \"s/localhost.localdomain/"+
				System.getProperty("katello.server.hostname", "localhost")+
				"/g\" "+KATELLO_CLI_CLIENT_CONFIG);
	}
	
	public int getClientPlatformID(){
		return this.platform_id;
	}
	
	protected void disableRhsmYumPlugin(){
		clienttasks.execute_remote("echo -e \"[main]\nenabled=0\" > /etc/yum/pluginconf.d/subscription-manager.conf");
	}
	protected void enableRhsmYumPlugin(){
		clienttasks.execute_remote("echo -e \"[main]\nenabled=1\" > /etc/yum/pluginconf.d/subscription-manager.conf");
	}
	
	protected void assert_providerRemoved(String providerName, String orgName){
		SSHCommandResult res;
		log.info("Assertions: provider has been removed");
		res = clienttasks.run_cliCmd("provider info --org \""+orgName+"\" --name \""+providerName+"\"");
		Assert.assertTrue(res.getExitCode().intValue()==65, "Check - return code");
		Assert.assertEquals(res.getStdout().trim(), "Could not find provider [ "+providerName+" ] within organization [ "+orgName+" ]", "Check - `provider info` return string");
	}
	
	protected void assert_repoSynced(String orgName, String productName, String repoName){
		SSHCommandResult res;
		String REGEXP_REPO_INFO;
		log.info("Assertions: repository has been synchronized");
		res = clienttasks.run_cliCmd(String.format(IKatelloRepo.INFO,orgName,productName,repoName));
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		
		// pacakge_count != 0
		REGEXP_REPO_INFO = ".*Name:\\s+"+repoName+".*Package Count:\\s+0.*";
		Assert.assertFalse(res.getStdout().replaceAll("\n", "").matches(REGEXP_REPO_INFO), 
				"Repo should not contain packages count: 0");
		// last_sync != never
		REGEXP_REPO_INFO = ".*Name:\\s+"+repoName+".*Last Sync:\\s+never.*";
		Assert.assertFalse(res.getStdout().replaceAll("\n", "").matches(REGEXP_REPO_INFO), 
				"Repo should not contain last_sync == never");
		// progress != Not synced
		REGEXP_REPO_INFO = ".*Name:\\s+"+repoName+".*Progress:\\s+Not synced.*";
		Assert.assertFalse(res.getStdout().replaceAll("\n", "").matches(REGEXP_REPO_INFO), 
				"Repo should not contain progress == not synced");
		
		// package_count >0; url, progress, last_sync
		REGEXP_REPO_INFO = ".*Name:\\s+"+repoName+".*Package Count:\\s+[1..9]+.*Progress:\\s+Finished";
		Assert.assertTrue(res.getStdout().replaceAll("\n", "").matches(REGEXP_REPO_INFO), 
				"Repo should contain packages count: >0 & progress == finished");
	}
	
	protected void assert_productExists(String orgName, String providerName, String productName){
		this.assert_productExists(orgName, providerName, productName, IKatelloEnvironment.LOCKER, false);
	}

	protected void assert_productExists(String orgName, String providerName, String productName, String envName, boolean synced){
		SSHCommandResult res;
		String REGEXP_PRODUCT_LIST;
		
		REGEXP_PRODUCT_LIST = ".*Name:\\s+"+productName+".*Provider Name:\\s+"+providerName+".*";
		log.info("Assertions: product exists");
		res = clienttasks.run_cliCmd(String.format(IKatelloProduct.LIST_BY_PROVIDER,orgName,providerName));
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		Assert.assertTrue(res.getStdout().replaceAll("\n", "").matches(REGEXP_PRODUCT_LIST),
				"List should contain info about product (requested by: provider)");

		res = clienttasks.run_cliCmd(String.format(IKatelloProduct.LIST_BY_ENV,orgName,envName));
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		Assert.assertTrue(res.getStdout().replaceAll("\n", "").matches(REGEXP_PRODUCT_LIST), 
				"List should contain info about product (requested by: environment)");
		
		if(!synced){
			res = clienttasks.run_cliCmd(String.format(IKatelloProduct.STATUS, orgName,productName));
			Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
			String REGEXP_PRODUCT_STATUS = ".*Name:\\s+"+productName+".*Provider Name:\\s+"+providerName+".*Last Sync:\\s+never.*Sync State:\\s+Not synced.*";
			Assert.assertTrue(res.getStdout().replaceAll("\n", "").matches(REGEXP_PRODUCT_STATUS), 
					"List should contain status of product (not synced)");
		}else{
			// TODO - needs an implementation - when product is synchronized.
		}
	}
	
	protected void waitfor_reposync(String orgName, String prodName, String repoName, int timeoutMinutes){
		long now = Calendar.getInstance().getTimeInMillis() / 1000;
		long start = now;
		long maxWaitSec = start + (timeoutMinutes * 60);
		String REGEXP_STATUS_FINISHED = ".*Sync State:\\s+Finished.*";
		log.fine("Waiting repo sync finish for: minutes=["+timeoutMinutes+"]; org=["+orgName+"]; product=["+prodName+"]; repo=["+repoName+"]");
		while(now<maxWaitSec){
			SSHCommandResult res = clienttasks.run_cliCmd(String.format(IKatelloRepo.STATUS, orgName,prodName,repoName));
			now = Calendar.getInstance().getTimeInMillis() / 1000;
			if(res.getStdout().replaceAll("\n", "").matches(REGEXP_STATUS_FINISHED))
				break;
			try{Thread.sleep(60000);}catch (Exception e){}
		}
		if(now<=maxWaitSec)
			log.fine("Repo sync done in: ["+String.valueOf(maxWaitSec - now)+"] sec");
		else
			log.warning("Repo sync did not finished after: ["+String.valueOf(maxWaitSec - now)+"] sec");
	}
}
