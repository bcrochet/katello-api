package com.redhat.qe.katello.tests.cli;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import com.redhat.qe.auto.testng.Assert;
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
		
		private KatelloCliTasks cli;
		
		private String id;
		private String environment_id;
		private String template_id;
		private String subscriptions;
		
		public static final String CMD_CREATE = "activation_key create";
		public static final String CMD_INFO = "activation_key info";
		public static final String CMD_LIST = "activation_key list";
		
		public static final String ERR_TEMPLATE_NOTFOUND = 
				"Could not find template [ %s ]";	
		public static final String OUT_CREATE = 
				"Successfully created activation key [ %s ]";

		public KatelloActivationKeyImpl(KatelloCliTasks pCli, String pOrg, String pEnv, String pName, String pDesc, String pTemplate){
			this.cli = pCli;
			this.org = pOrg;
			this.environment = pEnv;
			this.name = pName;
			this.description = pDesc;
			this.template = pTemplate;
		}
		
		public SSHCommandResult create(){
			String cmd = CMD_CREATE;
			
			if(this.org != null)
				cmd += " --org \""+this.org+"\"";
			if(this.environment != null)
				cmd += " --environment \""+this.environment+"\"";
			if(this.name != null)
				cmd += " --name \""+this.name+"\"";
			if(this.description != null)
				cmd += " --description \""+this.description+"\"";
			if(this.template != null)
				cmd += " --template \""+this.template+"\"";
			
			return cli.run_cliCmd(cmd);
		}
		
		public SSHCommandResult info(){
			String cmd = CMD_INFO;
			
			if(this.org != null)
				cmd += " --org \""+this.org+"\"";
			if(this.name != null)
				cmd += " --name \""+this.name+"\"";			
			return cli.run_cliCmd(cmd);
		}
		
		public SSHCommandResult list(){
			String cmd = CMD_LIST;
			
			if(this.org != null)
				cmd += " --org \""+this.org+"\" -v";

			return cli.run_cliCmd(cmd);
		}

		public SSHCommandResult list(String pEnvironment){
			String cmd = CMD_LIST;
			
			if(this.org != null)
				cmd += " --org \""+this.org+"\"";
			cmd += " --environment \""+pEnvironment+"\" -v";			
			
			return cli.run_cliCmd(cmd);
		}
		
		
		// ** ** ** ** ** ** **
		// ASSERTS
		// ** ** ** ** ** ** **
		public void asserts_create(){
			SSHCommandResult res;
			if(this.id==null)
				updateIDs();
			
			// asserts: activation_key list
			res = list(this.environment);
			Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (activation_key list)");
			String REGEXP_AK_LIST = ".*Id:\\s+\\d+.*Name:\\s+%s.*Environment Id:\\s+%s.*System Template Id:\\s+%s.*";

			String match_info = String.format(REGEXP_AK_LIST,
					this.name,this.environment_id,this.template_id).replaceAll("\"", "");
			if(this.template_id==null){
				match_info = String.format(REGEXP_AK_LIST,
						this.name,this.environment_id,"None").replaceAll("\"", "");
			}
			Assert.assertTrue(res.getStdout().replaceAll("\n", "").matches(match_info), 
					String.format("Activation key [%s] should be found in the list",this.name));
			
			// asserts: activation_key info
			res = info();
			Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (activation_key info)");
			String REGEXP_AK_INFO = ".*Id:\\s+\\d+.*Name:\\s+%s.*Environment Id:\\s+%s.*System Template Id:\\s+%s.*Pools:.*";
			match_info = String.format(REGEXP_AK_INFO,
					this.name,this.environment_id,this.template_id).replaceAll("\"", "");
			if(this.template_id==null){
				match_info = String.format(REGEXP_AK_INFO,
						this.name,this.environment_id,"None").replaceAll("\"", "");				
			}
			Assert.assertTrue(res.getStdout().replaceAll("\n", "").matches(match_info), 
					String.format("Activation key [%s] should contain correct info",this.name));			
		}
		
		/**
		 * Retrieves the IDs (or does updates) like:<BR>
		 * id - activation key id in DB<BR>
		 * environment_id - id of the environment<BR>
		 * template_id - id of the template (could be null)<BR>
		 * subscriptions - array of pool_ids (could be null) 
		 */
		private void updateIDs(){
			SSHCommandResult res;
			String cmd;
			// retrieve environment_id
			if(this.environment != null){
				cmd = String.format(IKatelloEnvironment.INFO, this.org, this.environment);
				res = clienttasks.run_cliCmd(cmd);
				Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (environment info)");
				this.environment_id = KatelloCliTasks.grepCLIOutput("Id", res.getStdout());				
			}
			//retrieve template_id for an environment
			if(this.template !=null){
				cmd = String.format(IKatelloTemplate.INFO_FOR_ENV, this.org, template, this.environment);
				res = clienttasks.run_cliCmd(cmd);
				Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (template info)");
				this.template_id = KatelloCliTasks.grepCLIOutput("Id", res.getStdout());				
			}
			// retrieve id, subscriptions
			if(this.name != null){
				res = info();
				this.id = KatelloCliTasks.grepCLIOutput("Id", res.getStdout());
				this.subscriptions = KatelloCliTasks.grepCLIOutput("Pools", res.getStdout());
			}
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
		
		KatelloActivationKeyImpl ak = new KatelloActivationKeyImpl(clienttasks, this.organization, this.env, name, descr, null);
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
		
		KatelloActivationKeyImpl ak = new KatelloActivationKeyImpl(clienttasks, this.organization, this.env, ak_name, null, template_name);
		res = ak.create();
		Assert.assertTrue(res.getExitCode().intValue()==65, "Check - return code (activation_key create --template)");
		Assert.assertTrue(res.getStdout().trim().contains(
				String.format(KatelloActivationKeyImpl.ERR_TEMPLATE_NOTFOUND,template_name)), 
				"Check - returned error string (activation_key create --template)");
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
		
		KatelloActivationKeyImpl ak = new KatelloActivationKeyImpl(clienttasks, this.organization, this.env, ak_name, null, template);
		res = ak.create();
		Assert.assertTrue(res.getExitCode().intValue()==65, "Check - return code (activation_key create --template)");
		Assert.assertTrue(res.getStdout().trim().contains(
				String.format(KatelloActivationKeyImpl.ERR_TEMPLATE_NOTFOUND,template)), 
				"Check - returned error string (activation_key create --template)");
	}
	
	@Test(description="create AK - same name, diff. orgs", groups = {"cli-activationkey"}, enabled=true)
	public void test_create_diffOrgsSameName(){
		SSHCommandResult res;
		String uid = KatelloTestScript.getUniqueID();
		String ak_name = "ak-"+uid;
		String org2 = "org2-"+uid;

		// create 2nd org (and the same env) 
		res = clienttasks.run_cliCmd(String.format(IKatelloOrg.CREATE_NODESC,org2));
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		res = clienttasks.run_cliCmd(String.format(IKatelloEnvironment.CREATE_NODESC,org2,this.env,IKatelloEnvironment.LOCKER));
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		
		KatelloActivationKeyImpl ak = new KatelloActivationKeyImpl(clienttasks, org2, this.env, ak_name, null, null);
		res = ak.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (activation_key create)");
		
		ak = new KatelloActivationKeyImpl(clienttasks, this.organization, this.env, ak_name, null, null);
		res = ak.create(); // force update IDs 
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (activation_key create)");
		Assert.assertTrue(res.getStdout().trim().contains(
				String.format(KatelloActivationKeyImpl.OUT_CREATE,ak_name)), 
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
		
		KatelloActivationKeyImpl ak = new KatelloActivationKeyImpl(clienttasks, this.organization, this.env, ak_name, null, template);
		res = ak.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (activation_key create --template)");
		Assert.assertTrue(res.getStdout().trim().contains(
				String.format(KatelloActivationKeyImpl.OUT_CREATE,ak_name)), 
				"Check - returned output string (activation_key create --template)");
		
		ak.asserts_create();
	}

}
