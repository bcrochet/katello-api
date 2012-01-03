package com.redhat.qe.katello.tests.cli;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import com.redhat.qe.auto.testng.Assert;
import com.redhat.qe.katello.base.IKatelloActivationKey;
import com.redhat.qe.katello.base.IKatelloChangeset;
import com.redhat.qe.katello.base.IKatelloEnvironment;
import com.redhat.qe.katello.base.IKatelloOrg;
import com.redhat.qe.katello.base.IKatelloTemplate;
import com.redhat.qe.katello.base.KatelloCliDataProvider;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.base.KatelloTestScript;
import com.redhat.qe.katello.tasks.KatelloCliTasks;
import com.redhat.qe.tools.SSHCommandResult;

public class ActivationKeyTests extends KatelloCliTestScript{

	private String org;
	private String env;
	
static{
//	 System.setProperty("katello.cli.reuseSystem", "true");  // TODO - /me needs to be commented.	
}

	@BeforeClass(description="init: create org stuff", groups = {"cli-activationkey"})
	public void setUp(){
		SSHCommandResult res;
		String uid = KatelloTestScript.getUniqueID();
		this.org = "ak-"+uid;
		this.env = "ak-"+uid;
		res = clienttasks.run_cliCmd(String.format(IKatelloOrg.CREATE_NODESC,this.org));
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		res = clienttasks.run_cliCmd(String.format(IKatelloEnvironment.CREATE_NODESC,this.org,this.env,IKatelloEnvironment.LOCKER));
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
	}
	
	@Test(description="create AK", groups = {"cli-activationkey"}, 
			dataProvider="activationkey_create", dataProviderClass = KatelloCliDataProvider.class, enabled=true)
	public void test_create(String name, String descr, Integer exitCode, String output){
		SSHCommandResult res;
		
		String cmd = String.format("activation_key create --org \"%s\" --environment \"%s\"", 
				this.org, this.env);
		if(name!=null)
			cmd = cmd + " --name \""+name+"\"";
		if(descr!=null)
			cmd = cmd + " --description \""+descr+"\"";
		res = clienttasks.run_cliCmd(cmd);
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
		String cmd = String.format(IKatelloActivationKey.CREATE_NODESC_TEMPLATE, 
				this.org, this.env, "ne-"+uid, "neTemplate-"+uid);
		res = clienttasks.run_cliCmd(cmd);
		Assert.assertTrue(res.getExitCode().intValue()==65, "Check - return code (activation_key create --template)");
		Assert.assertTrue(res.getStdout().trim().contains(
				String.format(IKatelloActivationKey.ERR_TEMPLATE_NOTFOUND,"neTemplate-"+uid)), "Check - returned error string (activation_key create --template)");
	}
	
	@Test(description="create AK - template not exported to the env.", groups = {"cli-activationkey"}, enabled=true)
	public void test_create_TemplateNotForEnv(){
		SSHCommandResult res; String cmd;
		String uid = KatelloTestScript.getUniqueID();
		String template = "template-"+uid;

		cmd = String.format(IKatelloTemplate.CREATE, this.org, template);
		res = clienttasks.run_cliCmd(cmd);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (template create)");
		
		cmd = String.format(IKatelloActivationKey.CREATE_NODESC_TEMPLATE, 
				this.org, this.env, "nfe-"+uid, "notForEnv-"+uid);
		res = clienttasks.run_cliCmd(cmd);
		Assert.assertTrue(res.getExitCode().intValue()==65, "Check - return code (activation_key create --template)");
		Assert.assertTrue(res.getStdout().trim().contains(
				String.format(IKatelloActivationKey.ERR_TEMPLATE_NOTFOUND,"notForEnv-"+uid)), "Check - returned error string (activation_key create --template)");
	}
	
	@Test(description="create AK - same name, diff. orgs", groups = {"cli-activationkey"}, enabled=true)
	public void test_create_diffOrgsSameName(){
		SSHCommandResult res; String cmd; String match_info;
		String uid = KatelloTestScript.getUniqueID();
		String ak_name = "ak-"+uid;
		String org2 = "org-"+uid;

		// create 2nd org (and the same env) 
		res = clienttasks.run_cliCmd(String.format(IKatelloOrg.CREATE_NODESC,org2));
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		res = clienttasks.run_cliCmd(String.format(IKatelloEnvironment.CREATE_NODESC,org2,this.env,IKatelloEnvironment.LOCKER));
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		
		cmd = String.format(IKatelloActivationKey.CREATE_NODESC,
				this.org, this.env, ak_name);
		res = clienttasks.run_cliCmd(cmd);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (activation_key create)");
		
		cmd = String.format(IKatelloActivationKey.CREATE_NODESC,
				org2, this.env, ak_name);
		res = clienttasks.run_cliCmd(cmd);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (activation_key create)");
		Assert.assertTrue(res.getStdout().trim().contains(
				String.format(IKatelloActivationKey.OUT_CREATE,ak_name)), "Check - returned output string (activation_key create)");
		
		// retrieve envID, templateID
		cmd = String.format(IKatelloEnvironment.INFO, this.org, this.env);
		res = clienttasks.run_cliCmd(cmd);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (environment info)");
		String env_id = KatelloCliTasks.grepCLIOutput("Id", res.getStdout());
		
		// asserts: activation_key list
		cmd = String.format(IKatelloActivationKey.LIST_ALL, this.org);
		res = clienttasks.run_cliCmd(cmd);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (activation_key list)");
		String REGEXP_AK_LIST = ".*Id:\\s+\\d+.*Name:\\s+%s.*Environment Id:\\s+%s.*System Template Id:\\s+%s.*";
		match_info = String.format(REGEXP_AK_LIST,
				ak_name,env_id,"None").replaceAll("\"", "");
		Assert.assertTrue(res.getStdout().replaceAll("\n", "").matches(match_info), 
				String.format("Activation key [%s] should be found in the list",ak_name));
		
		// asserts: activation_key info
		cmd = String.format(IKatelloActivationKey.INFO, this.org, ak_name);
		res = clienttasks.run_cliCmd(cmd);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (activation_key info)");
		String REGEXP_AK_INFO = ".*Id:\\s+\\d+.*Name:\\s+%s.*Environment Id:\\s+%s.*System Template Id:\\s+%s.*Pools:.*";
		match_info = String.format(REGEXP_AK_INFO,
				ak_name,env_id,"None").replaceAll("\"", "");
		Assert.assertTrue(res.getStdout().replaceAll("\n", "").matches(match_info), 
				String.format("Activation key [%s] should contain correct info",ak_name));
	}
	
	@Test(description="create AK - with template", enabled=true)
	public void test_create_withTemplate(){
		SSHCommandResult res; String cmd; String match_info;
		String uid = KatelloTestScript.getUniqueID();
		String template = "templateForEnv-"+uid;
		String changeset = "csForEnv-"+uid;
		String ak_template = "akTemplate-"+uid;

		// create template
		cmd = String.format(IKatelloTemplate.CREATE, this.org, template);
		res = clienttasks.run_cliCmd(cmd);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (template create)");
		
		// create changeset
		cmd = String.format(IKatelloChangeset.CREATE, this.org, this.env, changeset);
		res = clienttasks.run_cliCmd(cmd);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (changeset create)");
		
		// add template to changeset
		cmd = String.format(IKatelloChangeset.UPDATE_ADD_TEMPLATE, template, this.org, this.env, changeset);
		res = clienttasks.run_cliCmd(cmd);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (changeset update --add_template)");
		
		// promote changeset to the env.
		cmd = String.format(IKatelloChangeset.PROMOTE, this.org, this.env, changeset);
		res = clienttasks.run_cliCmd(cmd);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (changeset promote)");
		
		cmd = String.format(IKatelloActivationKey.CREATE_NODESC_TEMPLATE, 
				this.org, this.env, ak_template, template);
		res = clienttasks.run_cliCmd(cmd);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (activation_key create --template)");
		Assert.assertTrue(res.getStdout().trim().contains(
				String.format(IKatelloActivationKey.OUT_CREATE,ak_template)), "Check - returned output string (activation_key create --template)");
		
		// retrieve envID, templateID
		cmd = String.format(IKatelloEnvironment.INFO, this.org, this.env);
		res = clienttasks.run_cliCmd(cmd);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (environment info)");
		String env_id = KatelloCliTasks.grepCLIOutput("Id", res.getStdout());
		cmd = String.format(IKatelloTemplate.INFO_FOR_ENV, this.org, template, this.env);
		res = clienttasks.run_cliCmd(cmd);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (template info)");
		String template_id = KatelloCliTasks.grepCLIOutput("Id", res.getStdout());
		
		// asserts: activation_key list
		cmd = String.format(IKatelloActivationKey.LIST_ALL, this.org);
		res = clienttasks.run_cliCmd(cmd);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (activation_key list)");
		String REGEXP_AK_LIST = ".*Id:\\s+\\d+.*Name:\\s+%s.*Environment Id:\\s+%s.*System Template Id:\\s+%s.*";
		match_info = String.format(REGEXP_AK_LIST,
				ak_template,env_id,template_id).replaceAll("\"", "");
		Assert.assertTrue(res.getStdout().replaceAll("\n", "").matches(match_info), 
				String.format("Activation key [%s] should be found in the list",ak_template));
		
		// asserts: activation_key info
		cmd = String.format(IKatelloActivationKey.INFO, this.org, ak_template);
		res = clienttasks.run_cliCmd(cmd);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (activation_key info)");
		String REGEXP_AK_INFO = ".*Id:\\s+\\d+.*Name:\\s+%s.*Environment Id:\\s+%s.*System Template Id:\\s+%s.*Pools:.*";
		match_info = String.format(REGEXP_AK_INFO,
				ak_template,env_id,template_id).replaceAll("\"", "");
		Assert.assertTrue(res.getStdout().replaceAll("\n", "").matches(match_info), 
				String.format("Activation key [%s] should contain correct info",ak_template));
	}

}
