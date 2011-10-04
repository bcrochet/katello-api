package com.redhat.qe.katello.tests.cli;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.redhat.qe.auto.testng.Assert;
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
	
	@Test(description="Fresh org has no providers", groups = {"cli-providers"})
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
	
}
