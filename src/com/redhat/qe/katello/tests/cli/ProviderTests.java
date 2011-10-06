package com.redhat.qe.katello.tests.cli;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.redhat.qe.auto.testng.Assert;
import com.redhat.qe.katello.base.KatelloCliDataProvider;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.base.KatelloTestScript;
import com.redhat.qe.katello.tasks.KatelloCliTasks;
import com.redhat.qe.tools.SSHCommandResult;

public class ProviderTests extends KatelloCliTestScript{

	private String org_name;
	public static final String PROV_REDHAT = "Red Hat";
	
	@BeforeClass(description="Prepare an org to work with", groups = {"cli-providers"})
	public void setup_org(){
		String uid = KatelloTestScript.getUniqueID();
		this.org_name = "org"+uid;
		SSHCommandResult res = clienttasks.run_cliCmd("org create --name "+this.org_name);
		Assert.assertEquals(res.getExitCode().intValue(), 0, "Check - return code");
	}
	
	@Test(description="Fresh org - check default provider status/info", groups = {"cli-providers"})
	public void test_freshOrgDefaultRedHatProvider(){
		String uid = KatelloTestScript.getUniqueID();
		String tmpOrg = "tmpOrg"+uid;
		SSHCommandResult res = clienttasks.run_cliCmd("org create --name "+tmpOrg);
		Assert.assertEquals(res.getExitCode().intValue(), 0, "Check - return code");
		
		// assertions - `provider list` 
		// check that default provider of RedHat type is prepared
		res = clienttasks.run_cliCmd("provider list --org "+tmpOrg+" -v");
		Assert.assertEquals(res.getExitCode().intValue(), 0, "Check - return code");
		String REGEXP_PROVIDER_REDHAT = ".*Id:\\s+\\d+.*Name:\\s+"+PROV_REDHAT+".*Type:\\s+Red\\sHat.*Url:\\s+https://cdn.redhat.com.*";
		Assert.assertTrue(res.getStdout().replaceAll("\n", "").matches(REGEXP_PROVIDER_REDHAT), 
				"Provider \""+PROV_REDHAT+"\" should be found in the providers list");

		// assertions - `provider status` 
		// status of "Red Hat" provider
		res = clienttasks.run_cliCmd("provider status --org "+tmpOrg+" --name \""+PROV_REDHAT+"\"");
		Assert.assertEquals(res.getExitCode().intValue(), 0, "Check - return code");
		String REGEXP_REDHAT_STATUS = ".*Id:\\s+\\d+.*Name:\\s+"+PROV_REDHAT+".*Last\\sSync:\\s+never.*Sync\\sState:\\s+Not\\ssynced.*";
		Assert.assertTrue(res.getStdout().replaceAll("\n", "").matches(REGEXP_REDHAT_STATUS), 
				"Provider \""+PROV_REDHAT+"\" should have sync status: never");
		
		// assertions - `provider info`
		// get info of "Red Hat" provider
		String orgId = KatelloCliTasks.grepCLIOutput("Id", clienttasks.run_cliCmd("org info --name "+org_name).getStdout());
		res = clienttasks.run_cliCmd("provider info --org "+org_name+" --name \"Red Hat\"");
		String REGEXP_REDHAT_INFO = ".*Id:\\s+\\d+.*Name:\\s+"+PROV_REDHAT+".*Type:\\s+Red Hat.*Url:\\s+https://cdn.redhat.com.*Org Id:\\s+"+orgId+".*Description:.*";
		Assert.assertTrue(res.getStdout().replaceAll("\n", "").matches(REGEXP_REDHAT_INFO), 
				"Provider \""+PROV_REDHAT+"\" info should be displayed together with org_id");
	}
	
	@Test(description="Try to create provider of: redhat (default one exists)", groups = {"cli-providers"})
	public void test_createRedhatProvider_defaultExists(){
		String uid = KatelloTestScript.getUniqueID();
		String tmpOrg = "tmpOrg"+uid;
		SSHCommandResult res = clienttasks.run_cliCmd("org create --name "+tmpOrg);
		Assert.assertEquals(res.getExitCode().intValue(), 0, "Check - return code");
		
		res = clienttasks.run_cliCmd("provider create --org "+tmpOrg+" --name redhat_fake --type redhat --url https://cdn.redhat.com");
		Assert.assertTrue(res.getExitCode().intValue() == 144, "Check - return code");
		Assert.assertEquals(res.getStderr().trim(), "Validation failed: Only one Red Hat provider permitted for an Organization",
				"Check - only one Red Hat provider is allowed");
	}
	
	@Test(description="Try to create provider of: redhat (another one exists)", groups = {"cli-providers"})
	public void test_createRedhatProvider_anotherExists(){
		String uid = KatelloTestScript.getUniqueID();
		String tmpOrg = "tmpOrg"+uid;
		SSHCommandResult res = clienttasks.run_cliCmd("org create --name "+tmpOrg);
		Assert.assertEquals(res.getExitCode().intValue(), 0, "Check - return code");
		
		res = clienttasks.run_cliCmd("provider delete --org "+tmpOrg+" --name \""+PROV_REDHAT+"\"");
		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - return code");		
		res = clienttasks.run_cliCmd("provider create --org "+tmpOrg+" --name redhat_fake --type redhat --url https://cdn.redhat.com");
		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - return code");
		res = clienttasks.run_cliCmd("provider create --org "+tmpOrg+" --name redhat_fake_1 --type redhat --url https://cdn.redhat.com");
		Assert.assertTrue(res.getExitCode().intValue() == 144, "Check - return code");
		Assert.assertEquals(res.getStderr().trim(), "Validation failed: Only one Red Hat provider permitted for an Organization",
				"Check - only one Red Hat provider is allowed");
	}
	
	@Test(description="Create custom provider - different inputs", groups = {"cli-providers"},
			dataProvider="provider_create",dataProviderClass = KatelloCliDataProvider.class)
	public void test_createProvider_output(String name, String descr, String url, Integer exitCode, String output){
		
		String cmd = "provider create --org "+this.org_name+" --type custom";
		if(name!=null)
			cmd = cmd + " --name \""+name+"\"";
		if(descr!=null)
			cmd = cmd + " --description \""+descr+"\"";
		if(url!=null)
			cmd = cmd + " --url \""+url+"\"";
		SSHCommandResult  res = clienttasks.run_cliCmd(cmd);
		Assert.assertTrue(res.getExitCode().intValue() == exitCode.intValue(), "Check - return code");
		
		if(exitCode.intValue()==0){ //
			Assert.assertTrue(res.getStdout().contains(output),"Check - returned output string");
		}else{ // Failure to be checked
			Assert.assertTrue(res.getStderr().contains(output),"Check - returned error string");
		}
	}
	
	
}
