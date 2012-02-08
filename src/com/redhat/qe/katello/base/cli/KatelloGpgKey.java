package com.redhat.qe.katello.base.cli;

import com.redhat.qe.katello.tasks.KatelloCliTasks;
import com.redhat.qe.tools.SSHCommandResult;

public class KatelloGpgKey {
	
	// ** ** ** ** ** ** ** Public constants
	public static final String CMD_CREATE = "gpg_key create";
	public static final String CMD_INFO = "gpg_key info";
	
	public static final String OUT_CREATE = 
			"Successfully created gpg key [ %s ]"; 

	// ** ** ** ** ** ** ** Class members
	String name;
	String org;
	String file;
		
	private KatelloCliTasks cli;	
	
	public KatelloGpgKey(KatelloCliTasks pCli, 
			String pName, String pOrg, String pFile){
		this.cli = pCli;
		this.name = pName;
		this.org = pOrg;
		this.file = pFile;
	}
	
	public SSHCommandResult create(){
		String cmd = CMD_CREATE;
		
		if(this.name != null)
			cmd += " --name \""+this.name+"\"";
		if(this.org != null)
			cmd += " --org \""+this.org+"\"";
		if(this.file != null)
			cmd += " --file \""+this.file+"\"";
	
		
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
