package com.redhat.qe.katello.base;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Logger;
import org.testng.Assert;

import com.redhat.qe.katello.base.cli.KatelloEnvironment;
import com.redhat.qe.katello.base.cli.KatelloOrg;
import com.redhat.qe.katello.base.cli.KatelloProduct;
import com.redhat.qe.katello.base.cli.KatelloProvider;
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
						System.getProperty("katello.sshkey.private", ".ssh/id_hudson_dsa"), 
						System.getProperty("katello.sshkey.passphrase", "secret"), null);				
			}catch(Throwable t){
				log.warning("Warning: Could not initialize server's SSHCommandRunner.");
			}
			try{
				client_sshRunner = new SSHCommandRunner(
						System.getProperty("katello.client.hostname", "localhost"), 
						System.getProperty("katello.client.ssh.user", "root"), 
						System.getProperty("katello.client.ssh.passphrase", "secret"), 
						System.getProperty("katello.client.sshkey.private", ".ssh/id_hudson_dsa"), 
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
		Assert.assertEquals(getOutput(res).trim(), "Could not find provider [ "+providerName+" ] within organization [ "+orgName+" ]", "Check - `provider info` return string");
	}
	
	protected void assert_repoSynced(String orgName, String productName, String repoName){
		SSHCommandResult res;
		String REGEXP_REPO_INFO;
		log.info("Assertions: repository has been synchronized");
		res = clienttasks.run_cliCmd(String.format(IKatelloRepo.INFO,orgName,productName,repoName));
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		
		// pacakge_count != 0
		REGEXP_REPO_INFO = ".*Name:\\s+"+repoName+".*Package Count:\\s+0.*";
		Assert.assertFalse(getOutput(res).replaceAll("\n", "").matches(REGEXP_REPO_INFO), 
				"Repo should not contain packages count: 0");
		// last_sync != never
		REGEXP_REPO_INFO = ".*Name:\\s+"+repoName+".*Last Sync:\\s+never.*";
		Assert.assertFalse(getOutput(res).replaceAll("\n", "").matches(REGEXP_REPO_INFO), 
				"Repo should not contain last_sync == never");
		// progress != Not synced
		REGEXP_REPO_INFO = ".*Name:\\s+"+repoName+".*Progress:\\s+Not synced.*";
		Assert.assertFalse(getOutput(res).replaceAll("\n", "").matches(REGEXP_REPO_INFO), 
				"Repo should not contain progress == not synced");
		
		// package_count >0; url, progress, last_sync
		String cnt = KatelloCliTasks.grepCLIOutput("Package Count", res.getStdout());
		Assert.assertTrue(new Integer(cnt).intValue()>0, "Repo should contain packages count: >0");
		REGEXP_REPO_INFO = ".*Name:\\s+"+repoName+".*Progress:\\s+Finished.*";
		Assert.assertTrue(getOutput(res).replaceAll("\n", "").matches(REGEXP_REPO_INFO), 
				"Repo should contain progress == finished");
	}
	
//	protected void assert_productExists(String orgName, String providerName, String productName){
//		this.assert_productExists(orgName, providerName, productName, KatelloEnvironment.LIBRARY, false);
//	}

//	protected void assert_productExists(String orgName, String providerName, String productName, String envName, boolean synced){
//		SSHCommandResult res;
//		String REGEXP_PRODUCT_LIST;
//		
//		KatelloProduct prod = new KatelloProduct(clienttasks, orgName, productName, providerName, null, null, null, null, null);
//		REGEXP_PRODUCT_LIST = ".*Name:\\s+"+productName+".*Provider Name:\\s+"+providerName+".*";
//		log.info("Assertions: product exists");
//		res = prod.
//		res = clienttasks.run_cliCmd(String.format(IKatelloProduct.LIST_BY_PROVIDER,orgName,providerName));
//		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
//		Assert.assertTrue(getOutput(res).replaceAll("\n", "").matches(REGEXP_PRODUCT_LIST),
//				"List should contain info about product (requested by: provider)");
//
//		res = clienttasks.run_cliCmd(String.format(IKatelloProduct.LIST_BY_ENV,orgName,envName));
//		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
//		Assert.assertTrue(getOutput(res).replaceAll("\n", "").matches(REGEXP_PRODUCT_LIST), 
//				"List should contain info about product (requested by: environment)");
//		
//		if(!synced){
//			res = clienttasks.run_cliCmd(String.format(IKatelloProduct.STATUS, orgName,productName));
//			Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
//			String REGEXP_PRODUCT_STATUS = ".*Name:\\s+"+productName+".*Provider Name:\\s+"+providerName+".*Last Sync:\\s+never.*Sync State:\\s+Not synced.*";
//			Assert.assertTrue(getOutput(res).replaceAll("\n", "").matches(REGEXP_PRODUCT_STATUS), 
//					"List should contain status of product (not synced)");
//		}else{
//			// TODO - needs an implementation - when product is synchronized.
//		}
//	}
	
	protected void waitfor_reposync(String orgName, String prodName, String repoName, int timeoutMinutes){
		long now = Calendar.getInstance().getTimeInMillis() / 1000;
		long start = now;
		long maxWaitSec = start + (timeoutMinutes * 60);
		String REGEXP_STATUS_FINISHED = ".*Sync State:\\s+Finished.*";
		log.fine("Waiting repo sync finish for: minutes=["+timeoutMinutes+"]; org=["+orgName+"]; product=["+prodName+"]; repo=["+repoName+"]");
		while(now<maxWaitSec){
			SSHCommandResult res = clienttasks.run_cliCmd(String.format(IKatelloRepo.STATUS, orgName,prodName,repoName));
			now = Calendar.getInstance().getTimeInMillis() / 1000;
			if(getOutput(res).replaceAll("\n", "").matches(REGEXP_STATUS_FINISHED))
				break;
			try{Thread.sleep(60000);}catch (Exception e){}
		}
		if(now<=maxWaitSec)
			log.fine("Repo sync done in: ["+String.valueOf((Calendar.getInstance().getTimeInMillis() / 1000) - start)+"] sec");
		else
			log.warning("Repo sync did not finished after: ["+String.valueOf(maxWaitSec - start)+"] sec");
	}
	
	/**
	 * Returns list of org names that have imported a manifest that has subscriptions for:<BR>
	 * Red Hat Enterprise Linux Server<BR>
	 * Id: 69
	 * @return empty list or names of the orgs
	 */
	protected ArrayList<String> getOrgsWithImportedManifest(){
		ArrayList<String> orgs = new ArrayList<String>();
		String servername = System.getProperty("katello.server.hostname", "localhost");
		log.info("Scanning ["+servername+"] for organizations with imported manifest");
		SSHCommandResult res = clienttasks.run_cliCmd("org list -v | grep \"^Name\" | cut -d: -f2");
		String[] lines = getOutput(res).split("\n");
		for(String org: lines){
			org = org.trim();
			res = clienttasks.run_cliCmd("product list --provider=\""+KatelloProvider.PROVIDER_REDHAT+
					"\" --org \""+org+"\" -v | grep \"^Id:\\s\\+69\" | wc -l");
			if(getOutput(res).equals("1")){
				orgs.add(org);
			}
		}
		return orgs;
	}
	
	protected boolean hasOrg_environment(String org, String environment){
		log.info(String.format("Check if the org [%s] has an environment [%s]",org,environment));
		SSHCommandResult res = clienttasks.run_cliCmd("environment list"+
				"\" --org \""+org+"\" -v | grep \"^Name:\\s\\+"+environment+"\" | wc -l");
		return getOutput(res).equals("1");
	}
	
	protected SSHCommandResult rhsm_clean(){
		log.info("RHSM clean");
		return clienttasks.execute_remote("subscription-manager clean");
	}
	
	protected SSHCommandResult rhsm_register(String org, String environment, String name, boolean autosubscribe){
		log.info("Registering client with: --org \""+org+"\" --environment \""+environment+"\" " +
				"--name \""+name+"\" --autosubscribe "+Boolean.toString(autosubscribe));
		String cmd = String.format(
				"subscription-manager register --username admin --password admin --org \"%s\" --environment \"%s\" --name \"%s\"",
				org,environment,name);
		if(autosubscribe)
			cmd += " --autosubscribe";
		return clienttasks.execute_remote(cmd);
	}
	
	protected String getOutput(SSHCommandResult res){
		return res.getStdout()+"\n"+getOutput(res).trim();
	}
}
