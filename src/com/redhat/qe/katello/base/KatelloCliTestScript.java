package com.redhat.qe.katello.base;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import org.testng.Assert;

import com.redhat.qe.katello.base.obj.KatelloProvider;
import com.redhat.qe.katello.base.obj.KatelloRepo;
import com.redhat.qe.katello.common.KatelloConstants;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.tools.SSHCommandResult;

public class KatelloCliTestScript 
extends com.redhat.qe.auto.testng.TestScript 
implements KatelloConstants {

	protected static Logger log = Logger.getLogger(KatelloCliTestScript.class.getName());
	
	private static ResourceBundle messageBundle = null;
	private static ResourceBundle inputBundle = null;
	private static final String messageFileName = "messages";
	private static final String inputFileName = "inputs";

	private int platform_id = -1; // made a class property - in case in the tests there would be a need to check platform.
	public KatelloCliTestScript() {
		super();
	}
	
	public int getClientPlatformID(){
		return this.platform_id;
	}
	
	protected void assert_providerRemoved(KatelloProvider prov){
		SSHCommandResult res;
		log.info("Assertions: provider has been removed");
		res = prov.info();
		Assert.assertTrue(res.getExitCode().intValue()==65, "Check - return code");
		Assert.assertEquals(getOutput(res).trim(), "Could not find provider [ "+prov.name+" ] " +
				"within organization [ "+prov.org+" ]", "Check - `provider info` return string");
	}
	
	protected void assert_repoSynced(KatelloRepo repo){
		SSHCommandResult res;
		String REGEXP_REPO_INFO;
		log.info("Assertions: repository has been synchronized");
		res = repo.info();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		
		// pacakge_count != 0
		REGEXP_REPO_INFO = ".*Name\\s*:\\s+"+repo.name+".*Package Count\\s*:\\s+0.*";
		Assert.assertFalse(getOutput(res).replaceAll("\n", "").matches(REGEXP_REPO_INFO), 
				"Repo should not contain packages count: 0");
		// last_sync != never
		REGEXP_REPO_INFO = ".*Name\\s*:\\s+"+repo.name+".*Last Sync\\s*:\\s+never.*";
		Assert.assertFalse(getOutput(res).replaceAll("\n", "").matches(REGEXP_REPO_INFO), 
				"Repo should not contain last_sync == never");
		// progress != Not synced
		REGEXP_REPO_INFO = ".*Name\\s*:\\s+"+repo.name+".*Progress\\s*:\\s+Not synced.*";
		Assert.assertFalse(getOutput(res).replaceAll("\n", "").matches(REGEXP_REPO_INFO), 
				"Repo should not contain progress == not synced");
		
		// package_count >0; url, progress, last_sync
		String cnt = KatelloCli.grepCLIOutput("Package Count", res.getStdout());
		Assert.assertTrue(new Integer(cnt).intValue()>0, "Repo should contain packages count: >0");
		REGEXP_REPO_INFO = ".*Name\\s*:\\s+"+repo.name+".*Progress\\s*:\\s+Finished.*";
		Assert.assertTrue(getOutput(res).replaceAll("\n", "").matches(REGEXP_REPO_INFO), 
				"Repo should contain progress == finished");
	}
	
	protected void waitfor_reposync(KatelloRepo repo, int timeoutMinutes){
		SSHCommandResult res;
		long now = Calendar.getInstance().getTimeInMillis() / 1000;
		long start = now;
		long maxWaitSec = start + (timeoutMinutes * 60);
		String REGEXP_STATUS_FINISHED = ".*Sync State:\\s+Finished.*";
		log.fine("Waiting repo sync finish for: minutes=["+timeoutMinutes+"]; " +
				"org=["+repo.org+"]; product=["+repo.product+"]; repo=["+repo.name+"]");
		while(now<maxWaitSec){
			res = repo.status();
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
	
	protected void waitfor_reposync(KatelloRepo repo, String lastsynced, int timeoutMinutes) {
		SSHCommandResult res;
		long now = Calendar.getInstance().getTimeInMillis() / 1000;
		long start = now;
		long maxWaitSec = start + (timeoutMinutes * 60);
		log.fine("Waiting repo sync finish for: minutes=["+timeoutMinutes+"]; " +
				"org=["+repo.org+"]; product=["+repo.product+"]; repo=["+repo.name+"]");
		while(now<maxWaitSec){
			res = repo.info();
			now = Calendar.getInstance().getTimeInMillis() / 1000;
			String newsync = KatelloCli.grepCLIOutput("Last Sync", getOutput(res).trim(),1);
			if(!lastsynced.equals(newsync))
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
		KatelloCli cli = new KatelloCli("org list -v | grep \"^Name\" | cut -d: -f2", null);
		SSHCommandResult res = cli.run();
		String[] lines = getOutput(res).split("\n");
		for(String org: lines){
			org = org.trim();
			res = new KatelloCli("product list --provider=\""+KatelloProvider.PROVIDER_REDHAT+
					"\" --org \""+org+"\" -v | grep \"^Id:\\s\\+69\" | wc -l",null).run();
			if(getOutput(res).equals("1")){
				orgs.add(org);
			}
		}
		return orgs;
	}
	
	protected boolean hasOrg_environment(String org, String environment){
		log.info(String.format("Check if the org [%s] has an environment [%s]",org,environment));
		SSHCommandResult res = new KatelloCli("environment list"+
				"\" --org \""+org+"\" -v | grep \"^Name\\s*:\\s\\+"+environment+"\" | wc -l",null).run();
		return getOutput(res).equals("1");
	}
	
	// TODO - DUPE?
	protected SSHCommandResult rhsm_clean(){
		log.info("RHSM clean");
		KatelloUtils.sshOnClient("subscription-manager unsubscribe --all");
		KatelloUtils.sshOnClient("subscription-manager unregister");
		return KatelloUtils.sshOnClient("subscription-manager clean");
	}
	
	protected void yum_clean() {
		KatelloUtils.sshOnClient("yum clean all");
		KatelloUtils.sshOnClient("yum repolist");
	}
	
	// TODO - DUPE?
	protected SSHCommandResult rhsm_register(String org, String environment, String name, boolean autosubscribe){
		log.info("Registering client with: --org \""+org+"\" --environment \""+environment+"\" " +
				"--name \""+name+"\" --autosubscribe "+Boolean.toString(autosubscribe));
		String cmd = String.format(
				"subscription-manager register --username admin --password admin --org \"%s\" --environment \"%s\" --name \"%s\"",
				org,environment,name);
		if(autosubscribe)
			cmd += " --autosubscribe";
		return KatelloUtils.sshOnClient(cmd);
	}
	
	protected String getOutput(SSHCommandResult res){
		return sgetOutput(res);
	}
	
	public static String sgetOutput(SSHCommandResult res){
		return (res.getStdout()+"\n"+res.getStderr()).trim();
	}
	
	/**
	 * Returns the localized message value of provided key.
	 * It requires to specify "katello.locale" parameter while running tests, otherwise "KATELLO_DEFAULT_LOCALE" default value should be used.
	 * It lookups in two different message.properties files, first is inputs file, where are kept texts to send to katello as input parameter in CLI.
	 * Second file is contains output messages of katello to verify them in different locale.
	 * It is static method and initializes ResourceBoundles for both messages ".properties" files.
	 * After initializing in clears the cache from previous run. "en_US"
	 */
	public static String getMessage(String key) {
		if (messageBundle == null || inputBundle == null) {
			String localeStr = System.getProperty("katello.locale", KATELLO_DEFAULT_LOCALE);
			String[] split = localeStr.split("_", 2);					
			Locale locale = new Locale(split[0], split[1]);
			
			messageBundle = ResourceBundle.getBundle(messageFileName, locale);
			inputBundle = ResourceBundle.getBundle(inputFileName, locale);
			ResourceBundle.clearCache(); //this is mandatory
		}
		if (messageBundle.containsKey(key)) {
			return messageBundle.getString(key);
		} else if (inputBundle.containsKey(key)) {
			return inputBundle.getString(key);
		} else {
			log.warning("Message by key: " + key + " not found in locale " + messageBundle.getLocale());
			return null;
		}
	}
	
}
