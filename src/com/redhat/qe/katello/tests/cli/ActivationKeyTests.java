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

	
	class KatelloActivationKeyImpl{
		String org;
		String environment;
		String name;
		String description;
		String template;
		
		private String id;
		private String environment_id;
		private String template_id;
		private String[] subscriptions;
		
		public KatelloActivationKeyImpl(String pOrg, String pEnv, String pName, String pDesc, String pTemplate){
			this.org = pOrg;
			this.environment = pEnv;
			this.name = pName;
			this.description = pDesc;
			this.template = pTemplate;
		}
		
		public SSHCommandResult create(KatelloCliTasks cli){
			String cmd_CREATE = "activation_key create";
			if(this.org != null)
				cmd_CREATE += " --org \""+this.org+"\"";
			if(this.environment != null)
				cmd_CREATE += " --environment \""+this.environment+"\"";
			if(this.name != null)
				cmd_CREATE += " --name \""+this.name+"\"";
			if(this.description != null)
				cmd_CREATE += " --description \""+this.description+"\"";
			if(this.template != null)
				cmd_CREATE += " --template \""+this.template+"\"";
			
			return cli.run_cliCmd(cmd_CREATE);
		}
	}
	
	private String organization;
	private String env;
	
static{
//	 System.setProperty("katello.cli.reuseSystem", "true");  // TODO - /me needs to be commented.	
}

	@BeforeClass(description="init: create org stuff", groups = {"cli-activationkey"})
	public void setUp(){
		SSHCommandResult res;
		String uid = KatelloTestScript.getUniqueID();
		this.organization = "ak-"+uid;
		this.env = "ak-"+uid;
		res = clienttasks.run_cliCmd(String.format(IKatelloOrg.CREATE_NODESC,this.organization));
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		res = clienttasks.run_cliCmd(String.format(IKatelloEnvironment.CREATE_NODESC,this.organization,this.env,IKatelloEnvironment.LOCKER));
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
	}
	
	@Test(description="create AK", groups = {"cli-activationkey"}, 
			dataProvider="activationkey_create", dataProviderClass = KatelloCliDataProvider.class, enabled=true)
	public void test_create(String name, String descr, Integer exitCode, String output){
		SSHCommandResult res;
		
		KatelloActivationKeyImpl ak = new KatelloActivationKeyImpl(this.organization, this.env, name, descr, null);
		res = ak.create(clienttasks);
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
		
		KatelloActivationKeyImpl ak = new KatelloActivationKeyImpl(this.organization, this.env, "ne-"+uid, null, "neTemplate-"+uid);
		res = ak.create(clienttasks);
		Assert.assertTrue(res.getExitCode().intValue()==65, "Check - return code (activation_key create --template)");
		Assert.assertTrue(res.getStdout().trim().contains(
				String.format(IKatelloActivationKey.ERR_TEMPLATE_NOTFOUND,"neTemplate-"+uid)), "Check - returned error string (activation_key create --template)");
	}
	
	@Test(description="create AK - template not exported to the env.", groups = {"cli-activationkey"}, enabled=true)
	public void test_create_TemplateNotForEnv(){
		SSHCommandResult res;
		String uid = KatelloTestScript.getUniqueID();
		String template = "template-"+uid;
		String ak_name = "nfe-"+uid;

		// create the template
		String cmd = String.format(IKatelloTemplate.CREATE, this.organization, template);
		res = clienttasks.run_cliCmd(cmd);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (template create)");
		
		KatelloActivationKeyImpl ak = new KatelloActivationKeyImpl(this.organization, this.env, ak_name, null, template);
		res = ak.create(clienttasks);
		Assert.assertTrue(res.getExitCode().intValue()==65, "Check - return code (activation_key create --template)");
		Assert.assertTrue(res.getStdout().trim().contains(
				String.format(IKatelloActivationKey.ERR_TEMPLATE_NOTFOUND,template)), "Check - returned error string (activation_key create --template)");
	}
	
	@Test(description="create AK - same name, diff. orgs", groups = {"cli-activationkey"}, enabled=true)
	public void test_create_diffOrgsSameName(){
		SSHCommandResult res;
		String cmd, match_info;
		String uid = KatelloTestScript.getUniqueID();
		String ak_name = "ak-"+uid;
		String org2 = "org-"+uid;

		// create 2nd org (and the same env) 
		res = clienttasks.run_cliCmd(String.format(IKatelloOrg.CREATE_NODESC,org2));
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		res = clienttasks.run_cliCmd(String.format(IKatelloEnvironment.CREATE_NODESC,org2,this.env,IKatelloEnvironment.LOCKER));
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		
		KatelloActivationKeyImpl ak = new KatelloActivationKeyImpl(this.organization, this.env, ak_name, null, null);
		res = ak.create(clienttasks);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (activation_key create)");
		
		ak = new KatelloActivationKeyImpl(org2, this.env, ak_name, null, null);
		res = ak.create(clienttasks);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (activation_key create)");
		Assert.assertTrue(res.getStdout().trim().contains(
				String.format(IKatelloActivationKey.OUT_CREATE,ak_name)), "Check - returned output string (activation_key create)");
		
		// retrieve envID, templateID
		cmd = String.format(IKatelloEnvironment.INFO, this.organization, this.env);
		res = clienttasks.run_cliCmd(cmd);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (environment info)");
		String env_id = KatelloCliTasks.grepCLIOutput("Id", res.getStdout());
		
		// asserts: activation_key list
		cmd = String.format(IKatelloActivationKey.LIST_ALL, this.organization);
		res = clienttasks.run_cliCmd(cmd);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (activation_key list)");
		String REGEXP_AK_LIST = ".*Id:\\s+\\d+.*Name:\\s+%s.*Environment Id:\\s+%s.*System Template Id:\\s+%s.*";
		match_info = String.format(REGEXP_AK_LIST,
				ak_name,env_id,"None").replaceAll("\"", "");
		Assert.assertTrue(res.getStdout().replaceAll("\n", "").matches(match_info), 
				String.format("Activation key [%s] should be found in the list",ak_name));
		
		// asserts: activation_key info
		cmd = String.format(IKatelloActivationKey.INFO, this.organization, ak_name);
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
		String ak_name = "akTemplate-"+uid;

		// create template
		cmd = String.format(IKatelloTemplate.CREATE, this.organization, template);
		res = clienttasks.run_cliCmd(cmd);
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
		
		KatelloActivationKeyImpl ak = new KatelloActivationKeyImpl(this.organization, this.env, ak_name, null, template);
		res = ak.create(clienttasks);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (activation_key create --template)");
		Assert.assertTrue(res.getStdout().trim().contains(
				String.format(IKatelloActivationKey.OUT_CREATE,ak_name)), "Check - returned output string (activation_key create --template)");
		
		// retrieve envID, templateID
		cmd = String.format(IKatelloEnvironment.INFO, this.organization, this.env);
		res = clienttasks.run_cliCmd(cmd);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (environment info)");
		String env_id = KatelloCliTasks.grepCLIOutput("Id", res.getStdout());
		cmd = String.format(IKatelloTemplate.INFO_FOR_ENV, this.organization, template, this.env);
		res = clienttasks.run_cliCmd(cmd);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (template info)");
		String template_id = KatelloCliTasks.grepCLIOutput("Id", res.getStdout());
		
		// asserts: activation_key list
		cmd = String.format(IKatelloActivationKey.LIST_ALL, this.organization);
		res = clienttasks.run_cliCmd(cmd);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (activation_key list)");
		String REGEXP_AK_LIST = ".*Id:\\s+\\d+.*Name:\\s+%s.*Environment Id:\\s+%s.*System Template Id:\\s+%s.*";
		match_info = String.format(REGEXP_AK_LIST,
				ak_name,env_id,template_id).replaceAll("\"", "");
		Assert.assertTrue(res.getStdout().replaceAll("\n", "").matches(match_info), 
				String.format("Activation key [%s] should be found in the list",ak_name));
		
		// asserts: activation_key info
		cmd = String.format(IKatelloActivationKey.INFO, this.organization, ak_name);
		res = clienttasks.run_cliCmd(cmd);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (activation_key info)");
		String REGEXP_AK_INFO = ".*Id:\\s+\\d+.*Name:\\s+%s.*Environment Id:\\s+%s.*System Template Id:\\s+%s.*Pools:.*";
		match_info = String.format(REGEXP_AK_INFO,
				ak_name,env_id,template_id).replaceAll("\"", "");
		Assert.assertTrue(res.getStdout().replaceAll("\n", "").matches(match_info), 
				String.format("Activation key [%s] should contain correct info",ak_name));
	}

}
