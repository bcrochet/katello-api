package com.redhat.qe.katello.base.cli;

import com.redhat.qe.katello.tasks.KatelloCliTasks;
import com.redhat.qe.tools.SSHCommandResult;

public class KatelloFilter {
	
	// ** ** ** ** ** ** ** Public constants
	public static final String CMD_CREATE = "filter create";


	public static final String OUT_CREATE = 
			"Successfully created filter [ %s ]";
	
	// ** ** ** ** ** ** ** Class members
	String name;
	String org;
	String packages;
	
	private KatelloCliTasks cli;	
	
	public KatelloFilter(KatelloCliTasks pCli, 
			String pName, String pOrg, String pEnv, String pPackages){
		this.cli = pCli;
		this.name = pName;
		this.org = pOrg;
		this.packages = pPackages;
	}
	
	public SSHCommandResult create(){
		String cmd = CMD_CREATE;
		
		if(this.name != null)
			cmd += " --name \""+this.name+"\"";
		if(this.org != null)
			cmd += " --org \""+this.org+"\"";
		if(this.packages != null)
			cmd += " --packages \""+this.packages+"\"";
		
		return cli.run_cliCmd(cmd);
	}

	// ** ** ** ** ** ** **
	// ASSERTS
	// ** ** ** ** ** ** **	
	
}
