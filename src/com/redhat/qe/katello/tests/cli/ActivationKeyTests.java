package com.redhat.qe.katello.tests.cli;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import com.redhat.qe.auto.testng.Assert;
import com.redhat.qe.katello.base.IKatelloActivationKey;
import com.redhat.qe.katello.base.IKatelloEnvironment;
import com.redhat.qe.katello.base.IKatelloOrg;
import com.redhat.qe.katello.base.IKatelloTemplate;
import com.redhat.qe.katello.base.KatelloCliDataProvider;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.base.KatelloTestScript;
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
	
	@Test(description="create AK - template does not exist", enabled=true)
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
	
	@Test(description="create AK - template not exported to the env.", enabled=true)
	public void test_create_TemplateNotForEnv(){
		SSHCommandResult res; String cmd;
		String uid = KatelloTestScript.getUniqueID();
		String template = "template-"+uid;

		cmd = String.format(IKatelloTemplate.CREATE, this.org, template);
		res = clienttasks.run_cliCmd(cmd);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (template create)");
		
		cmd = String.format(IKatelloActivationKey.CREATE_NODESC_TEMPLATE, 
				this.org, this.env, "ne-"+uid, "notForEnv-"+uid);
		res = clienttasks.run_cliCmd(cmd);
		Assert.assertTrue(res.getExitCode().intValue()==65, "Check - return code (activation_key create --template)");
		Assert.assertTrue(res.getStdout().trim().contains(
				String.format(IKatelloActivationKey.ERR_TEMPLATE_NOTFOUND,"notForEnv-"+uid)), "Check - returned error string (activation_key create --template)");
	}
	
}
