package com.redhat.qe.katello.base.cli;

import com.redhat.qe.katello.tasks.KatelloCliTasks;
import com.redhat.qe.tools.SSHCommandResult;

public class KatelloTemplate {
	
	// ** ** ** ** ** ** ** Public constants
	public static final String CMD_CREATE = "template create";
	public static final String CMD_INFO = "template info -v";
	
	// ** ** ** ** ** ** ** Class members
	String name;
	String org;
	String description;
	String parent;
	
	private KatelloCliTasks cli;	
	
	public KatelloTemplate(KatelloCliTasks pCli, String pName, String pDesc,
			String pOrg, String pParent){
		this.cli = pCli;
		this.name = pName;
		this.description = pDesc;
		this.org = pOrg;
		this.parent = pParent;
	}
	
	public SSHCommandResult create(){
		String cmd = CMD_CREATE;
		
		if(this.name != null)
			cmd += " --name \""+this.name+"\"";
		if(this.org != null)
			cmd += " --org \""+this.org+"\"";		
		if(this.description != null)
			cmd += " --description \""+this.description+"\"";
		if(this.parent != null)
			cmd += " --parent \""+this.parent+"\"";
		
		return cli.run_cliCmd(cmd);
	}
	
	public SSHCommandResult info(String environment){
		String cmd = CMD_INFO;
		
		if(this.name != null)
			cmd += " --name \""+this.name+"\"";
		if(this.org != null)
			cmd += " --org \""+this.org+"\"";		
		if(environment != null)
			cmd += " --environment \""+environment+"\"";		
		
		return cli.run_cliCmd(cmd);		
	}
	
	// ** ** ** ** ** ** **
	// ASSERTS
	// ** ** ** ** ** ** **
}
