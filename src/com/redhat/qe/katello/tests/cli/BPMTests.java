package com.redhat.qe.katello.tests.cli;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import com.redhat.qe.auto.testng.Assert;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.base.KatelloTestScript;
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
//	 System.setProperty("katello.cli.reuseSystem", "false");  // TODO - /me needs to be commented.
}
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
	
}
