package com.redhat.qe.katello.base.cli;

import com.redhat.qe.katello.tasks.KatelloCliTasks;
import com.redhat.qe.tools.SSHCommandResult;

public class KatelloEnvironment {
	
	// ** ** ** ** ** ** ** Public constants
	public static final String LIBRARY = "Library";
	
	public static final String CMD_CREATE = "environment create";
	public static final String CMD_INFO = "environment info -v";
	public static final String CMD_LIST = "environment list";
	
	public static final String OUT_CREATE = 
			"Successfully created environment [ %s ]";

	// ** ** ** ** ** ** ** Class members
	String name;
	String description;
	String org;
	String prior;
	
	private KatelloCliTasks cli;	
	
	public KatelloEnvironment(KatelloCliTasks pCli, String pName, String pDesc,
			String pOrg, String pPrior){
		this.cli = pCli;
		this.name = pName;
		this.description = pDesc;
		this.org = pOrg;
		this.prior = pPrior;
	}
	
	public SSHCommandResult create(){
		String cmd = CMD_CREATE;
		
		if(this.name != null)
			cmd += " --name \""+this.name+"\"";
		if(this.description != null)
			cmd += " --description \""+this.description+"\"";
		if(this.org != null)
			cmd += " --org \""+this.org+"\"";		
		if(this.prior != null)
			cmd += " --prior \""+this.prior+"\"";
		
		return cli.run_cliCmd(cmd);
	}
	
	public SSHCommandResult info(){
		String cmd = CMD_INFO;
		
		if(this.name != null)
			cmd += " --name \""+this.name+"\"";
		if(this.org != null)
			cmd += " --org \""+this.org+"\"";		
		
		return cli.run_cliCmd(cmd);		
	}
	
	// ** ** ** ** ** ** **
	// ASSERTS
	// ** ** ** ** ** ** **
}
