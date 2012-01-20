package com.redhat.qe.katello.base;

import java.util.Calendar;
import java.util.logging.Logger;
import org.testng.Assert;
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
	
	public int getClientPlatformID(){
		return this.platform_id;
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
		String cnt = KatelloCliTasks.grepCLIOutput("Package Count", res.getStdout());
		Assert.assertTrue(new Integer(cnt).intValue()>0, "Repo should contain packages count: >0");
		REGEXP_REPO_INFO = ".*Name:\\s+"+repoName+".*Progress:\\s+Finished.*";
		Assert.assertTrue(res.getStdout().replaceAll("\n", "").matches(REGEXP_REPO_INFO), 
				"Repo should contain progress == finished");
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
			log.fine("Repo sync done in: ["+String.valueOf((Calendar.getInstance().getTimeInMillis() / 1000) - start)+"] sec");
		else
			log.warning("Repo sync did not finished after: ["+String.valueOf(maxWaitSec - start)+"] sec");
	}	
}
