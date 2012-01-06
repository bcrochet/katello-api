package com.redhat.qe.katello.base.cli;

import com.redhat.qe.katello.tasks.KatelloCliTasks;
import com.redhat.qe.tools.SSHCommandResult;

public class KatelloOrg {
	
	// ** ** ** ** ** ** ** Public constants
	public static final String DEFAULT_ORG = "ACME_Corporation";
	
	public static final String CMD_CREATE = "org create";
	public static final String CMD_INFO = "org info";
	public static final String CMD_LIST = "org list";
	
	public static final String ERR_TEMPLATE_NOTFOUND = 
			"Could not find template [ %s ]";	
	public static final String OUT_CREATE = 
			"Successfully created activation key [ %s ]";

	// ** ** ** ** ** ** ** Class members
	String name;
	String description;
	
	private KatelloCliTasks cli;	
	
	public KatelloOrg(KatelloCliTasks pCli, String pName, String pDesc){
		this.cli = pCli;
		this.name = pName;
		this.description = pDesc;
	}
	
	public SSHCommandResult create(){
		String cmd = CMD_CREATE;
		
		if(this.name != null)
			cmd += " --name \""+this.name+"\"";
		if(this.description != null)
			cmd += " --description \""+this.description+"\"";
		
		return cli.run_cliCmd(cmd);
	}
	
	public SSHCommandResult info(){
		String cmd = CMD_INFO;
		
		if(this.name != null)
			cmd += " --name \""+this.name+"\"";			
		return cli.run_cliCmd(cmd);
	}
	
	public SSHCommandResult list(){
		String cmd = CMD_LIST;
		
		cmd += " -v";

		return cli.run_cliCmd(cmd);
	}

	// ** ** ** ** ** ** **
	// ASSERTS
	// ** ** ** ** ** ** **
}
