package com.redhat.qe.katello.base.cli;

import com.redhat.qe.katello.tasks.KatelloCliTasks;
import com.redhat.qe.tools.SSHCommandResult;

public class KatelloChangeset {
	
	// ** ** ** ** ** ** ** Public constants
	public static final String CMD_CREATE = "changeset create";
	public static final String CMD_PROMOTE = "changeset promote";
	public static final String CMD_UPDATE = "changeset update";
	public static final String CMD_INFO = "changeset info";
	
	public static final String OUT_CREATE = 
			"Successfully created changeset [ %s ]"; 

	// ** ** ** ** ** ** ** Class members
	String name;
	String org;
	String environment;
	
	private KatelloCliTasks cli;	
	
	public KatelloChangeset(KatelloCliTasks pCli, 
			String pName, String pOrg, String pEnv){
		this.cli = pCli;
		this.name = pName;
		this.org = pOrg;
		this.environment = pEnv;
	}
	
	public SSHCommandResult create(){
		String cmd = CMD_CREATE;
		
		if(this.name != null)
			cmd += " --name \""+this.name+"\"";
		if(this.org != null)
			cmd += " --org \""+this.org+"\"";
		if(this.environment != null)
			cmd += " --environment \""+this.environment+"\"";
		
		return cli.run_cliCmd(cmd);
	}
	
	public SSHCommandResult promote(){
		String cmd = CMD_PROMOTE;
		
		if(this.name != null)
			cmd += " --name \""+this.name+"\"";
		if(this.org != null)
			cmd += " --org \""+this.org+"\"";
		if(this.environment != null)
			cmd += " --environment \""+this.environment+"\"";
		
		return cli.run_cliCmd(cmd);
	}
	
	
	public SSHCommandResult info(){
		String cmd = CMD_INFO;
		
		if(this.name != null)
			cmd += " --name \""+this.name+"\"";
		if(this.org != null)
			cmd += " --org \""+this.org+"\"";
		if(this.environment != null)
			cmd += " --environment \""+this.environment+"\"";
		
		return cli.run_cliCmd(cmd);
	}
	
	public SSHCommandResult update_addProduct(String productName){
		String cmd = CMD_UPDATE+" --add_product";
		
		if(productName != null)
			cmd += " \""+productName+"\"";
		if(this.name != null)
			cmd += " --name \""+this.name+"\"";
		if(this.org != null)
			cmd += " --org \""+this.org+"\"";
		if(this.environment != null)
			cmd += " --environment \""+this.environment+"\"";
		
		return cli.run_cliCmd(cmd);
	}
	
	public SSHCommandResult update_fromProduct_addRepo(String productName, String repoName){
		String cmd = CMD_UPDATE+" --from_product";
		
		if(productName != null)
			cmd += " \""+productName+"\"";
		if(repoName != null)
			cmd += " --add_repo \""+repoName+"\"";
		if(this.name != null)
			cmd += " --name \""+this.name+"\"";
		if(this.org != null)
			cmd += " --org \""+this.org+"\"";
		if(this.environment != null)
			cmd += " --environment \""+this.environment+"\"";
		
		return cli.run_cliCmd(cmd);
	}

	public SSHCommandResult update_fromProduct_addErrata(String productName, String errataName){
		String cmd = CMD_UPDATE;
		
		if(productName != null)
			cmd += " --from_product \""+productName+"\"";
		if(errataName != null)
			cmd += " --add_erratum \""+errataName+"\"";
		if(this.name != null)
			cmd += " --name \""+this.name+"\"";
		if(this.org != null)
			cmd += " --org \""+this.org+"\"";
		if(this.environment != null)
			cmd += " --environment \""+this.environment+"\"";
		
		return cli.run_cliCmd(cmd);
	}

	public SSHCommandResult update_addTemplate(String templatename){
		String cmd = CMD_UPDATE;
		
		if(templatename != null)
			cmd += " --add_template \""+templatename+"\"";
		if(this.name != null)
			cmd += " --name \""+this.name+"\"";
		if(this.org != null)
			cmd += " --org \""+this.org+"\"";
		if(this.environment != null)
			cmd += " --environment \""+this.environment+"\"";
		
		return cli.run_cliCmd(cmd);
	}

	// ** ** ** ** ** ** **
	// ASSERTS
	// ** ** ** ** ** ** **	
	
}
