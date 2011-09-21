package com.redhat.qe.katello.tests.cli;

import java.util.HashMap;
import java.util.logging.Logger;

import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import com.redhat.qe.auto.testng.Assert;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.base.KatelloTestScript;
import com.redhat.qe.katello.common.KatelloInfo;
import com.redhat.qe.tools.SSHCommandResult;

/**
 * Story 1: Basic Patch Management<BR>
 * ===============================<BR>
 * 1) Install katello.<BR>
 * 2) Create a new Org and create a user who can manage providers, systems and environments.<BR>
 * 3) Create a Red Hat Provider, and a Custom Provider with a product that mirrors Fedora.<BR>
 * 4) Sync the Red Hat and Fedora Content.<BR>
 * 5) Create a new environment, and promote the content to the new environment.<BR>
 * 6) From both a RH and Fedora machine, register the machine using subscription manager.<BR>
 * 7) Install a package via yum.<BR>
 * 8) See the package list and facts for the machine in the UI and command line.<BR>
 * 9) Resync the RH and Fedora Content, pulling in updated packages.<BR>
 * 10) Promote the updated content from the locker to the environment.<BR>
 * 11) Use yum to install the latest content on the machine.<BR>
 * 12) Unsubsribe the machine, see that the machine no longer subscribed in the UI.<BR>
 * 13) Verify that yum can no longer access the content.
 */
public class BPMTests extends KatelloCliTestScript{
static{
	/*
	 *  Setup in your Eclipse IDE:
	 *  	-Dkatello.cli.reuseSystem=true 
	 *  
	 *  This will give you chance not to execute the:
	 *  	KatelloCliTestScript.setUpKatelloCli();
	 *  
	 *	Or if you are lazy: uncomment the line below: but please push it back again.   
	 */
//	 System.setProperty("katello.cli.reuseSystem", "true");  // TODO - /me needs to be commented.
}
	protected static Logger log = 
		Logger.getLogger(BPMTests.class.getName());
	
	public static final String KATELLO_SYNC_REPO_PULP_F15 = 
		"http://repos.fedorapeople.org/repos/pulp/pulp/fedora-15/x86_64/";
	
	private SSHCommandResult exec_result;

	// Katello objects below
	private String org_name;
	private String user_name; // not used still: `roles` needs to be in place in ordre to give user access to exec commands.
	private String providerRH_name;
	private String providerF_name;
	private String product_name;
	private String repo_name_pulpF15;
	private String env_name_Dev, env_name_Prod;
	private String changeset_name;
	private String consumer_name;
	private String rhsm_pool_id;
	
	@BeforeSuite(description="Prepare rhsm")
	public void init_rhsm(){
		/* 
		 * Once tried on F15 - there was some error on exit code:
		 * --- 
		 * subscription-manager register --username admin --password admin --org
		 * <org> --environment <env> --name `hostname` --force
		 * The system has been registered with id:
		 * bf624394-4ce1-4792-8b0e-de858c920c0c
		 * org.freedesktop.DBus.Error.Spawn.ChildExited: Launch helper exited with 
		 * unknown return code 1
		 * ---
		 *   
		 */		
		// we need to run on RHEL6 now.
		if(!CLIENT_PLATFORMS_ALLOWED[getClientPlatformID()][0].contains("redhat-6")){
			String err = "RHSM tasks need in having RHEL6. " +
					"Please adjust: [katello.client.hostname] properly.";
			log.severe(err);
			Assert.assertTrue(false, "RHSM client running on proper platform"); // raise error
		}
		
		String reuseSystem = System.getProperty("katello.cli.reuseSystem", "false");
		if(reuseSystem.equalsIgnoreCase("true")){
			return;
		}

		String servername = KatelloInfo.getInstance().getServername();
		clienttasks.execute_remote("yum -y erase python-rhsm subscription-manager; rm -rf /etc/rhsm/*;");
		exec_result = clienttasks.execute_remote("yum -y install python-rhsm subscription-manager");
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "RHSM packages should get installed");

		// some assertions - we need to assure we are running the version we need!
		exec_result = clienttasks.execute_remote("subscription-manager register --help");
		Assert.assertTrue(exec_result.getStdout().contains("--org=ORG"), "Check: subscription-manager --org");
		Assert.assertTrue(exec_result.getStdout().contains("--environment=ENVIRONMENT"), "Check: subscription-manager --environment");
		
		clienttasks.execute_remote("sed -i \"s/hostname = subscription.rhn.redhat.com/" +
				"hostname = "+servername+"/g\" /etc/rhsm/rhsm.conf");
		clienttasks.execute_remote("sed -i \"s/prefix = \\/subscription/prefix = \\/katello\\/api/g\" /etc/rhsm/rhsm.conf");
		clienttasks.execute_remote("sed -i \"s/baseurl= https:\\/\\/cdn.redhat.com/" +
				"baseurl=https:\\/\\/"+servername+"\\/pulp\\/repos\\//g\" /etc/rhsm/rhsm.conf");
		clienttasks.execute_remote("sed -i \"s/repo_ca_cert = %(ca_cert_dir)sredhat-uep.pem/" +
				"repo_ca_cert = %(ca_cert_dir)scandlepin-ca.pem/g\" /etc/rhsm/rhsm.conf");
		clienttasks.execute_remote("sed -i '/sslcacert=\\/etc\\/pki\\/CA\\/certs/ d' /etc/yum.conf");
		clienttasks.execute_remote("echo \"sslcacert=/etc/pki/CA/certs\" >> /etc/yum.conf");
		
		exec_result = servertasks.execute_remote("cat /etc/candlepin/certs/candlepin-ca.crt");
		String candlepin_ca_crt = exec_result.getStdout().trim();
		clienttasks.execute_remote("touch /etc/rhsm/ca/candlepin-ca.crt; echo -e \""+candlepin_ca_crt+"\" > /etc/rhsm/ca/candlepin-ca.crt");
		clienttasks.execute_remote("pushd /etc/rhsm/ca/; " +
				"openssl x509 -in candlepin-ca.crt -out candlepin-ca.der -outform DER; " +
				"openssl x509 -in candlepin-ca.der -inform DER -out candlepin-ca.pem -outform PEM; " +
				"popd;");		
	}

	@BeforeTest(description="Generate unique names")
	public void setUp(){
		String uid = KatelloTestScript.getUniqueID();
		org_name = "orgBPM_"+uid;
		user_name = "userBPMAdmin_"+uid;
		providerRH_name = "providerBPM_RH_"+uid;
		providerF_name = "providerBPM_F_"+uid;
		product_name = "productBPM_"+uid;
		repo_name_pulpF15 = "repoBPM_pulpF15_"+uid;
		env_name_Dev = "envBPM_Dev_"+uid;
		env_name_Prod = "envBPM_Prod_"+uid;
		changeset_name = "changesetBPM_"+uid;
		consumer_name = uid+"-`hostname`";
		rhsm_pool_id = null; // going to be set after listing avail. subscriptions.

		/* 
		 * Cleanup the files that make system registered through RHSM:
		 * ATTENTION: do this *only* if you know what you are doing there.
		 * 
		 * for further changes good to follow/adjust the progress of:
		 * strace -o /tmp/rhsm-unregister.log subscription-manager unregister
		 * grep "unlink(" in there to find out what are the files being removed.
		 * 
		 */
		clienttasks.execute_remote("rm -f /etc/pki/consumer/key.pem " +
				"/etc/pki/consumer/cert.pem " +
				"/var/lib/rhsm/packages/packages.json " +
				"/var/lib/rhsm/facts/facts.json " +
				"/var/lib/rhsm/cache/installed_products.json " +
				"/var/run/rhsm/cert.pid");
	}
	
	@Test(description="Create a new Org and create a user who can manage providers, systems and environments.")
	public void test_createOrgUser(){
		// Create org:
		exec_result = clienttasks.run_cliCmd("org create --name "+org_name+" --description \"BPM tests\"");
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		Assert.assertEquals(exec_result.getStdout().trim(), "Successfully created org [ "+org_name+" ]");
		// Create user:
		exec_result = clienttasks.run_cliCmd("user create --username "+user_name+" --password "+KATELLO_CLI_USER_DEFAULT_PASSWORD);
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		Assert.assertEquals(exec_result.getStdout().trim(), "Successfully created user [ "+user_name+" ]");
	}
	
	@Test(description="Create a Red Hat Provider, and a Custom Provider with a product that mirrors Fedora",
			dependsOnMethods={"test_createOrgUser"})
	public void test_createProviderProduct(){
		// Create provider: Red Hat
		exec_result = clienttasks.run_cliCmd(String.format(
				"provider create --org %s --name %s --type redhat " +
				"--url https://cdn.redhat.com --description \"Red Hat provider\"",
				org_name,providerRH_name));
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		Assert.assertEquals(exec_result.getStdout().trim(), "Successfully created provider [ "+providerRH_name+" ]");
		
		// Create provider: Fedora
		exec_result = clienttasks.run_cliCmd(String.format(
				"provider create --org %s --name %s --type custom " +
				"--description \"Fedora provider\"",
				org_name,providerF_name));
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		Assert.assertEquals(exec_result.getStdout().trim(), "Successfully created provider [ "+providerF_name+" ]");
		
		// Create product:
		exec_result = clienttasks.run_cliCmd(String.format(
				"product create --org %s --provider %s --name %s",
				org_name,providerF_name,product_name));
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		Assert.assertEquals(exec_result.getStdout().trim(), "Successfully created product [ "+product_name+" ]");		
	}
	
	@Test(description="Sync the Red Hat and Fedora Content",
			dependsOnMethods={"test_createProviderProduct"})
	public void test_syncRepo(){
		// Repo create:
		exec_result = clienttasks.run_cliCmd(String.format(
				"repo create --org %s --product %s --name %s --url %s",
				org_name,product_name,repo_name_pulpF15, KATELLO_SYNC_REPO_PULP_F15));
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		Assert.assertEquals(exec_result.getStdout().trim(), "Successfully created repository [ "+repo_name_pulpF15+" ]");		
		// Repo synchronize:
		exec_result = clienttasks.run_cliCmd(String.format(
				"provider synchronize --org %s --name %s",
				org_name,providerF_name));
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		String res[] = exec_result.getStdout().trim().split("\n");
		Assert.assertEquals(res[res.length-1], "Provider [ "+providerF_name+" ] synchronized");		
	}
	
	@Test(description="Create a new environment, and promote the content to the new environment.",
			dependsOnMethods={"test_syncRepo"})
	public void test_createEnvPromoteContent(){
		// Environment create: Dev
		exec_result = clienttasks.run_cliCmd(String.format(
				"environment create --org %s --name %s --prior Locker",
				org_name,env_name_Dev));
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		Assert.assertEquals(exec_result.getStdout().trim(), "Successfully created environment [ "+env_name_Dev+" ]");				
		// Environment create: Prod
		exec_result = clienttasks.run_cliCmd(String.format(
				"environment create --org %s --name %s --prior %s",
				org_name,env_name_Prod,env_name_Dev));
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		Assert.assertEquals(exec_result.getStdout().trim(), "Successfully created environment [ "+env_name_Prod+" ]");
		// Changeset create: for Dev
		exec_result = clienttasks.run_cliCmd(String.format(
				"changeset create --org %s --name %s --environment %s",
				org_name,changeset_name,env_name_Dev));
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		Assert.assertEquals(exec_result.getStdout().trim(), "Successfully created changeset [ "+changeset_name+" ] for environment [ "+env_name_Dev+" ]");
		// Changeset update: add_product
		exec_result = clienttasks.run_cliCmd(String.format(
				"changeset update --org %s --name %s --environment %s --add_product %s",
				org_name,changeset_name,env_name_Dev,product_name));
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		Assert.assertEquals(exec_result.getStdout().trim(), "Successfully updated changeset [ "+changeset_name+" ]");
		// Changeset promote:
		exec_result = clienttasks.run_cliCmd(String.format(
				"changeset promote --org %s --name %s --environment %s",
				org_name,changeset_name,env_name_Dev));
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		String res[] = exec_result.getStdout().trim().split("\\[40\\D");
		Assert.assertEquals(res[res.length-1], "Changeset [ "+changeset_name+" ] promoted");
	}
	
	@Test(description="From both a RH and Fedora machine, register the machine using subscription manager.",
			dependsOnMethods={"test_createEnvPromoteContent"})
	public void test_rhsm_register(){
		exec_result = clienttasks.execute_remote(String.format("subscription-manager register " +
				"--username admin --password admin --org %s --environment %s --name %s",org_name, env_name_Dev, consumer_name));
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		Assert.assertTrue(exec_result.getStdout().contains("The system has been registered with id:"),"Check - returned message");
	}
	
	@Test(description="List available subscriptions", dependsOnMethods={"test_rhsm_register"})
	public void test_rhsm_listAvailableSubscriptions(){
		// ProductName
		exec_result = clienttasks.execute_remote("subscription-manager list --available | grep ProductName:");
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		Assert.assertTrue(exec_result.getStdout().contains(product_name), "Check - subscription.ProductName");
		// Quantity
		exec_result = clienttasks.execute_remote("subscription-manager list --available | grep Quantity:");
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		Assert.assertTrue(exec_result.getStdout().contains("unlimited"), "Check - subscription.Quantity");
		
		// Store poolid
		exec_result = clienttasks.execute_remote("subscription-manager list --available | grep PoolId:");
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		rhsm_pool_id = exec_result.getStdout().trim().split(":")[1].trim();
		log.fine(String.format("Subscription is available for product: [%s] with poolid: [%s]",
				product_name,rhsm_pool_id));
	}
	
}
