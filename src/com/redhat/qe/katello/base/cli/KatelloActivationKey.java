package com.redhat.qe.katello.base.cli;

import com.redhat.qe.auto.testng.Assert;
import com.redhat.qe.katello.base.IKatelloEnvironment;
import com.redhat.qe.katello.base.IKatelloTemplate;
import com.redhat.qe.katello.tasks.KatelloCliTasks;
import com.redhat.qe.tools.SSHCommandResult;

public class KatelloActivationKey {

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

	public KatelloActivationKey(KatelloCliTasks pCli, String pOrg, String pEnv, String pName, String pDesc, String pTemplate){
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
			res = cli.run_cliCmd(cmd);
			Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (environment info)");
			this.environment_id = KatelloCliTasks.grepCLIOutput("Id", res.getStdout());				
		}
		//retrieve template_id for an environment
		if(this.template !=null){
			cmd = String.format(IKatelloTemplate.INFO_FOR_ENV, this.org, template, this.environment);
			res = cli.run_cliCmd(cmd);
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
