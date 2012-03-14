package com.redhat.qe.katello.base.cli;

import com.redhat.qe.katello.tasks.KatelloCliTasks;
import com.redhat.qe.tools.SSHCommandResult;

public class KatelloUserRole {
	
	// ** ** ** ** ** ** ** Public constants
	public static final String CMD_CREATE = "user_role create";
	
	// ** ** ** ** ** ** ** Class members
	String name;
	String description;
	
	private KatelloCliTasks cli;	
	
	public KatelloUserRole(KatelloCliTasks pCli, String pName, String pDesc){
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
	
}
