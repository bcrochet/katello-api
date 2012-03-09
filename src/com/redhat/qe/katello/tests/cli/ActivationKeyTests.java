package com.redhat.qe.katello.tests.cli;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import com.redhat.qe.auto.testng.Assert;
import com.redhat.qe.katello.base.IKatelloChangeset;
import com.redhat.qe.katello.base.KatelloCliDataProvider;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.base.KatelloTestScript;
import com.redhat.qe.katello.base.cli.KatelloActivationKey;
import com.redhat.qe.katello.base.cli.KatelloEnvironment;
import com.redhat.qe.katello.base.cli.KatelloOrg;
import com.redhat.qe.katello.base.cli.KatelloTemplate;
import com.redhat.qe.tools.SSHCommandResult;

public class ActivationKeyTests extends KatelloCliTestScript{

	
	private String organization;
	private String env;
	
static{
	 System.setProperty("katello.cli.reuseSystem", "true");  // TODO - /me needs to be commented.	
}

	@BeforeClass(description="init: create org stuff", groups = {"cli-activationkey"})
	public void setUp(){
		SSHCommandResult res;
		String uid = KatelloTestScript.getUniqueID();
		this.organization = "ak-"+uid;
		this.env = "ak-"+uid;
		KatelloOrg org = new KatelloOrg(clienttasks, this.organization, null);
		res = org.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		KatelloEnvironment env = new KatelloEnvironment(clienttasks, this.env, null, this.organization, KatelloEnvironment.LIBRARY);
		res = env.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
	}
	
	@Test(description="create AK", groups = {"cli-activationkey"}, 
			dataProvider="activationkey_create", dataProviderClass = KatelloCliDataProvider.class, enabled=true)
	public void test_create(String name, String descr, Integer exitCode, String output){
		SSHCommandResult res;
		
		KatelloActivationKey ak = new KatelloActivationKey(clienttasks, this.organization, this.env, name, descr, null);
		res = ak.create();
		Assert.assertTrue(res.getExitCode().intValue() == exitCode.intValue(), "Check - return code");
		
		if(exitCode.intValue()==0){ //
			Assert.assertTrue(res.getStdout().contains(output),"Check - returned output string");
		}else{ // Failure to be checked
			Assert.assertTrue(res.getStderr().contains(output),"Check - returned error string");
		}
	}
	
	@Test(description="create AK - template does not exist", groups = {"cli-activationkey"}, enabled=true)
	public void test_create_noTemplate(){
		SSHCommandResult res;
		String uid = KatelloTestScript.getUniqueID();
		String ak_name = "ne-"+uid;
		String template_name = "neTemplate-"+uid;
		
		KatelloActivationKey ak = new KatelloActivationKey(clienttasks, this.organization, this.env, ak_name, null, template_name);
		res = ak.create();
		Assert.assertTrue(res.getExitCode().intValue()==65, "Check - return code (activation_key create --template)");
		Assert.assertTrue(res.getStdout().trim().contains(
				String.format(KatelloActivationKey.ERR_TEMPLATE_NOTFOUND,template_name)), 
				"Check - returned error string (activation_key create --template)");
	}
	
	@Test(description="create AK - template not exported to the env.", groups = {"cli-activationkey"}, enabled=true)
	public void test_create_TemplateNotForEnv(){
		SSHCommandResult res;
		String uid = KatelloTestScript.getUniqueID();
		String template = "template-"+uid;
		String ak_name = "nfe-"+uid;

		// create the template
		KatelloTemplate tmpl = new KatelloTemplate(clienttasks, template, null, this.organization, null);
		res = tmpl.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (template create)");
		
		KatelloActivationKey ak = new KatelloActivationKey(clienttasks, this.organization, this.env, ak_name, null, template);
		res = ak.create();
		Assert.assertTrue(res.getExitCode().intValue()==65, "Check - return code (activation_key create --template)");
		Assert.assertTrue(res.getStdout().trim().contains(
				String.format(KatelloActivationKey.ERR_TEMPLATE_NOTFOUND,template)), 
				"Check - returned error string (activation_key create --template)");
	}
	
	@Test(description="create AK - same name, diff. orgs", groups = {"cli-activationkey"}, enabled=true)
	public void test_create_diffOrgsSameName(){
		SSHCommandResult res;
		String uid = KatelloTestScript.getUniqueID();
		String ak_name = "ak-"+uid;
		String org2 = "org2-"+uid;

		// create 2nd org (and the same env) 
		KatelloOrg org = new KatelloOrg(clienttasks, org2, null);
		res = org.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		KatelloEnvironment env = new KatelloEnvironment(clienttasks, this.env, null, org2, KatelloEnvironment.LIBRARY);
		res = env.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		
		KatelloActivationKey ak = new KatelloActivationKey(clienttasks, org2, this.env, ak_name, null, null);
		res = ak.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (activation_key create)");
		
		ak = new KatelloActivationKey(clienttasks, this.organization, this.env, ak_name, null, null);
		res = ak.create(); // force update IDs 
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (activation_key create)");
		Assert.assertTrue(res.getStdout().trim().contains(
				String.format(KatelloActivationKey.OUT_CREATE,ak_name)), 
				"Check - returned output string (activation_key create)");
		
		ak.asserts_create();
	}
	
	@Test(description="create AK - with template", enabled=true)
	public void test_create_withTemplate(){
		SSHCommandResult res; String cmd;
		String uid = KatelloTestScript.getUniqueID();
		String template = "templateForEnv-"+uid;
		String changeset = "csForEnv-"+uid;
		String ak_name = "akTemplate-"+uid;

		// create template
		KatelloTemplate tmpl = new KatelloTemplate(clienttasks, template, null, this.organization, null);
		res = tmpl.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (template create)");
		
		// create changeset
		cmd = String.format(IKatelloChangeset.CREATE, this.organization, this.env, changeset);
		res = clienttasks.run_cliCmd(cmd);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (changeset create)");
		
		// add template to changeset
		cmd = String.format(IKatelloChangeset.UPDATE_ADD_TEMPLATE, template, this.organization, this.env, changeset);
		res = clienttasks.run_cliCmd(cmd);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (changeset update --add_template)");
		
		// promote changeset to the env.
		cmd = String.format(IKatelloChangeset.PROMOTE, this.organization, this.env, changeset);
		res = clienttasks.run_cliCmd(cmd);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (changeset promote)");
		
		KatelloActivationKey ak = new KatelloActivationKey(clienttasks, this.organization, this.env, ak_name, null, template);
		res = ak.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (activation_key create --template)");
		Assert.assertTrue(res.getStdout().trim().contains(
				String.format(KatelloActivationKey.OUT_CREATE,ak_name)), 
				"Check - returned output string (activation_key create --template)");
		
		ak.asserts_create();
	}

}
