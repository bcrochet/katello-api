package com.redhat.qe.katello.tests.cli;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.redhat.qe.auto.testng.Assert;
import com.redhat.qe.katello.base.IKatelloProduct;
import com.redhat.qe.katello.base.IKatelloProvider;
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
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
	}
	
	@Test(description="Fresh org - check default provider status/info", groups = {"cli-providers"}, enabled=true)
	public void test_freshOrgDefaultRedHatProvider(){
		String uid = KatelloTestScript.getUniqueID();
		String tmpOrg = "tmpOrg"+uid;
		SSHCommandResult res = clienttasks.run_cliCmd("org create --name "+tmpOrg);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		
		// assertions - `provider list` 
		// check that default provider of RedHat type is prepared
		res = clienttasks.run_cliCmd("provider list --org "+tmpOrg+" -v");
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		String REGEXP_PROVIDER_REDHAT = ".*Id:\\s+\\d+.*Name:\\s+"+PROV_REDHAT+".*Type:\\s+Red\\sHat.*Url:\\s+https://cdn.redhat.com.*";
		Assert.assertTrue(res.getStdout().replaceAll("\n", "").matches(REGEXP_PROVIDER_REDHAT), 
				"Provider \""+PROV_REDHAT+"\" should be found in the providers list");

		// assertions - `provider status` 
		// status of "Red Hat" provider
		res = clienttasks.run_cliCmd("provider status --org "+tmpOrg+" --name \""+PROV_REDHAT+"\"");
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
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
	
	@Test(description="Try to create provider of: redhat (default one exists)", groups = {"cli-providers"}, enabled=true)
	public void test_createRedhatProvider_defaultExists(){
		String uid = KatelloTestScript.getUniqueID();
		String tmpOrg = "tmpOrg"+uid;
		SSHCommandResult res = clienttasks.run_cliCmd("org create --name "+tmpOrg);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		
		res = clienttasks.run_cliCmd("provider create --org "+tmpOrg+" --name redhat_fake --type redhat --url https://cdn.redhat.com");
		Assert.assertTrue(res.getExitCode().intValue() == 144, "Check - return code");
		Assert.assertEquals(res.getStderr().trim(), "Validation failed: Only one Red Hat provider permitted for an Organization",
				"Check - only one Red Hat provider is allowed");
	}
	
	@Test(description="Create custom provider - different inputs", groups = {"cli-providers"},
			dataProvider="provider_create",dataProviderClass = KatelloCliDataProvider.class, enabled=true)
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
	
	@Test(description="Create custom provider - wrong type", groups = {"cli-providers"},
			dataProvider="provider_create_diffType",dataProviderClass = KatelloCliDataProvider.class, enabled=true)
	public void test_createProvider_wrongType(String type, Integer exitCode, String output){
		
		SSHCommandResult  res = clienttasks.run_cliCmd("provider create --org "+this.org_name+" --type \""+type+"\" --name prov-oftype-"+type);
		Assert.assertTrue(res.getExitCode().intValue() == 2, "Check - return code");
		Assert.assertTrue(res.getStderr().contains("katello: error: option --type: invalid choice: '"+type+"' (choose from 'redhat', 'custom')"), 
				"Check - returned error string");
	}
	
	@Test(description="Delete provider - Red Hat", groups = {"cli-providers"}, enabled=true)
	public void test_deleteProvider_RedHat(){
		String uid = KatelloTestScript.getUniqueID();
		String orgName = "delRH"+uid;
		SSHCommandResult res = clienttasks.run_cliCmd("org create --name "+orgName);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		
		res = clienttasks.run_cliCmd("provider delete --org "+orgName+" --name \"Red Hat\"");
		Assert.assertTrue(res.getExitCode().intValue()==244, "Check - return code");
		Assert.assertEquals(res.getStderr().trim(), "Error while deleting provider [ Red Hat ]: Red Hat provider can not be deleted,","Check - returned error string");

		// get the provider info - should be there
		res = clienttasks.run_cliCmd("provider info --org "+orgName+" --name \"Red Hat\"");
		
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		Assert.assertTrue(res.getStdout().replaceAll("\n", "").matches(".*Name:\\s+Red Hat.*"),"Check - returned output string");		
	}
	
	@Test(description="Delete provider Custom - missing parameters", groups={"cli-providers"}, enabled = false)
	public void test_deleteProvider_missingReqParams(){
		String uid = KatelloTestScript.getUniqueID();
		String provName = "delProv-"+uid;
		SSHCommandResult  res = clienttasks.run_cliCmd("provider create --org "+this.org_name+" --type custom --name "+provName);
		Assert.assertTrue(res.getExitCode().intValue() == 0, "Check - return code");
		
		String cmd ="provider delete";
		res = clienttasks.run_cliCmd(cmd);
		Assert.assertTrue(res.getExitCode().intValue() == 2, "Check - return code");
		Assert.assertTrue(res.getStderr().contains("Option --org is required; please see --help"),"Check - returned error string - 1");
		Assert.assertTrue(res.getStderr().contains("Option --name is required; please see --help"),"Check - returned error string - 2");
		
		cmd ="provider delete --org "+this.org_name;
		res = clienttasks.run_cliCmd(cmd);
		Assert.assertTrue(res.getExitCode().intValue() == 2, "Check - return code");
		Assert.assertTrue(res.getStderr().contains("Option --name is required; please see --help"),"Check - returned error string");
		
		cmd ="provider delete --name "+provName;
		res = clienttasks.run_cliCmd(cmd);
		Assert.assertTrue(res.getExitCode().intValue() == 2, "Check - return code");
		Assert.assertTrue(res.getStderr().contains("Option --org is required; please see --help"),"Check - returned error string");
		
		cmd ="provider delete --org";
		res = clienttasks.run_cliCmd(cmd);
		Assert.assertTrue(res.getExitCode().intValue() == 2, "Check - return code");
		Assert.assertTrue(res.getStderr().contains("katello: error: --org option requires an argument"),"Check - returned error string");
		
		cmd ="provider delete --org "+this.org_name+" --name";
		res = clienttasks.run_cliCmd(cmd);
		Assert.assertTrue(res.getExitCode().intValue() == 2, "Check - return code");
		Assert.assertTrue(res.getStderr().contains("katello: error: --name option requires an argument"),"Check - returned error string");
	}
	
	@Test(description="Delete provider Custom - different org", groups = {"cli-providers"},enabled=true)
	public void test_deleteProvider_diffOrg(){
		String uid = KatelloTestScript.getUniqueID();
		String provName = "delProv-"+uid;
		String org1 = "anotherOrg"+uid;
		
		SSHCommandResult res = clienttasks.run_cliCmd("org create --name "+org1);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		res = clienttasks.run_cliCmd("provider create --org "+this.org_name+" --type custom --name "+provName);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		res = clienttasks.run_cliCmd("provider delete --org "+org1+" --name "+provName);
		Assert.assertTrue(res.getExitCode().intValue()==65, "Check - return code");
		// TODO - maybe needs to be stderr...
		Assert.assertTrue(res.getStdout().contains("Could not find provider [ "+provName+" ] within organization [ "+org1+" ]"),"Check - returned error string");
	}
	
	@Test(description="Delete provider Custom- no products associated", groups = {"cli-providers"},enabled=true)
	public void test_deleteProvider_noProducts(){
		SSHCommandResult res;
		String uid = KatelloTestScript.getUniqueID();
		String provName = "noProd-"+uid;
		
		res = clienttasks.run_cliCmd("provider create --org "+this.org_name+" --type custom --name "+provName);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		res = clienttasks.run_cliCmd("provider delete --org "+this.org_name+" --name "+provName);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		Assert.assertTrue(res.getStdout().trim().equals("Deleted provider [ "+provName+" ]"), "Check - returned output string");
		
		this.assert_providerRemoved(provName, this.org_name);
		// try to recreate the provider with the same name: should be possible
		res = clienttasks.run_cliCmd("provider create --org "+this.org_name+" --type custom --name "+provName);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		Assert.assertEquals(res.getStdout().trim(), "Successfully created provider [ "+provName+" ]");
	}
	
	@Test(description="Delete provider Custom- no products associated", groups = {"cli-providers"},enabled=true)
	public void test_deleteProvider_noRepos(){
		SSHCommandResult res;
		String uid = KatelloTestScript.getUniqueID();
		String provName = "noRepos-"+uid, provName_1 = "prov1-"+uid;
		String prodName = "prod-"+uid;
		
		// Create provider, product
		res = clienttasks.run_cliCmd(String.format(IKatelloProvider.CREATE_NODESCRIPTION_NOURL,this.org_name,provName));
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		res = clienttasks.run_cliCmd(String.format(IKatelloProduct.CREATE,this.org_name,provName,prodName));
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		
		// Delete provider
		res = clienttasks.run_cliCmd(String.format(IKatelloProvider.DELETE, this.org_name,provName));
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		Assert.assertTrue(res.getStdout().trim().equals(String.format(IKatelloProvider.OUT_DELETE, provName)), "Check - returned output string");
		
		// Check provider is removed
		this.assert_providerRemoved(provName, this.org_name);

		// Check associated product is gone
		res = clienttasks.run_cliCmd(String.format(IKatelloProduct.STATUS, this.org_name,prodName));
		Assert.assertTrue(res.getExitCode().intValue()==65, "Check - return code");
		Assert.assertTrue(res.getStdout().trim().equals(String.format(IKatelloProduct.ERR_COULD_NOT_FIND_PRODUCT, prodName,org_name)), "Check - `product status` output string");
		
		// Create another provider with the same product name
		res = clienttasks.run_cliCmd(String.format(IKatelloProvider.CREATE_NODESCRIPTION_NOURL,this.org_name,provName_1));
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		res = clienttasks.run_cliCmd(String.format(IKatelloProduct.CREATE,this.org_name,provName_1,prodName));
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		// Check `product status` - should be shown with provName_1 info there
		String REGEXP_PRODUCT = ".*Id:\\s+\\d+.*Name:\\s+%s.*Provider Id:\\s+\\d+.*Provider Name:\\s+%s.*";
		res = clienttasks.run_cliCmd(String.format(IKatelloProduct.STATUS,this.org_name,prodName));
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		String match_info = String.format(REGEXP_PRODUCT,prodName,provName_1).replaceAll("\"", "");
		Assert.assertTrue(res.getStdout().replaceAll("\n", "").matches(match_info), 
				String.format("Provider [%s] should be found in the result of product info for: [%s]",provName_1,prodName));		
	}
	
	//@Test
	public void test_deleteProvider_withRepos(){
		// TODO - to be implemented.
	}
	
	@Test(description="List / Info providers - no description, no url", groups = {"cli-providers"},enabled=true)
	public void test_listNinfoProviders_noDesc_noUrl(){
		SSHCommandResult res;
		String uid = KatelloTestScript.getUniqueID();
		String provName = "listProv1-"+uid;
		
		// Create provider
		res = clienttasks.run_cliCmd(String.format(IKatelloProvider.CREATE_NODESCRIPTION_NOURL,this.org_name,provName));
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		// List
		res = clienttasks.run_cliCmd(String.format(IKatelloProvider.LIST_VMODE,this.org_name));
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		String REGEXP_PROVIDER_LIST = ".*Id:\\s+\\d+.*Name:\\s+%s.*Type:\\s+Custom.*Url:\\s+None.*Description:\\s+None";
		String match_info = String.format(REGEXP_PROVIDER_LIST,provName).replaceAll("\"", "");
		Assert.assertTrue(res.getStdout().replaceAll("\n", "").matches(match_info), 
				String.format("Provider [%s] should be found in the list with: no description, no url",provName));
		// Info
		res = clienttasks.run_cliCmd(String.format(IKatelloProvider.INFO,this.org_name,provName));
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		match_info = String.format(REGEXP_PROVIDER_LIST,provName).replaceAll("\"", "");
		Assert.assertTrue(res.getStdout().replaceAll("\n", "").matches(match_info), 
				String.format("Provider [%s] should be found in the info with: no description, no url",provName));
	}
	
	@Test(description="List / Info providers", groups = {"cli-providers"},enabled=true)
	public void test_listNinfoProviders(){
		SSHCommandResult res;
		String uid = KatelloTestScript.getUniqueID();
		String provName = "listProv1-"+uid;
		String provDesc = "Simple description";
		
		// Create provider
		res = clienttasks.run_cliCmd(String.format(IKatelloProvider.CREATE,this.org_name,provName,KATELLO_SMALL_REPO,provDesc));
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		//List
		res = clienttasks.run_cliCmd(String.format(IKatelloProvider.LIST_VMODE,this.org_name));
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		String REGEXP_PROVIDER_LIST = ".*Id:\\s+\\d+.*Name:\\s+%s.*Type:\\s+Custom.*Url:\\s+%s.*Description:\\s+%s.*";
		String match_info = String.format(REGEXP_PROVIDER_LIST,provName,KATELLO_SMALL_REPO,provDesc).replaceAll("\"", "");
		Assert.assertTrue(res.getStdout().replaceAll("\n", "").matches(match_info), 
				String.format("Provider [%s] should be found in the list",provName));
		// Info
		res = clienttasks.run_cliCmd(String.format(IKatelloProvider.INFO,this.org_name,provName));
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		match_info = String.format(REGEXP_PROVIDER_LIST,provName,KATELLO_SMALL_REPO,provDesc).replaceAll("\"", "");
		Assert.assertTrue(res.getStdout().replaceAll("\n", "").matches(match_info), 
				String.format("Provider [%s] should be found in the info",provName));
	}
	
	@Test(description="List / Info providers - no description", groups = {"cli-providers"},enabled=true)
	public void test_listNinfoProviders_noDesc(){
		SSHCommandResult res;
		String uid = KatelloTestScript.getUniqueID();
		String provName = "listProvURL-"+uid;
		
		// Create provider
		res = clienttasks.run_cliCmd(String.format(IKatelloProvider.CREATE_NODESCRIPTION,this.org_name,provName,KATELLO_SMALL_REPO));
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		// List
		res = clienttasks.run_cliCmd(String.format(IKatelloProvider.LIST_VMODE,this.org_name));
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		String REGEXP_PROVIDER_LIST = ".*Id:\\s+\\d+.*Name:\\s+%s.*Type:\\s+Custom.*Url:\\s+%s.*Description:\\s+None";
		String match_info = String.format(REGEXP_PROVIDER_LIST,provName,KATELLO_SMALL_REPO).replaceAll("\"", "");
		Assert.assertTrue(res.getStdout().replaceAll("\n", "").matches(match_info), 
				String.format("Provider [%s] should be found in the list with: no description",provName));
		// Info
		res = clienttasks.run_cliCmd(String.format(IKatelloProvider.INFO,this.org_name,provName));
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		match_info = String.format(REGEXP_PROVIDER_LIST,provName,KATELLO_SMALL_REPO).replaceAll("\"", "");
		Assert.assertTrue(res.getStdout().replaceAll("\n", "").matches(match_info), 
				String.format("Provider [%s] should be found in the info with: no description",provName));
	}
	
}
