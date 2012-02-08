package com.redhat.qe.katello.base.cli;

import com.redhat.qe.katello.tasks.KatelloCliTasks;
import com.redhat.qe.tools.SSHCommandResult;

public class KatelloProvider {
	
	// ** ** ** ** ** ** ** Public constants
	public static final String CMD_CREATE = "provider create";
	
	public static final String OUT_CREATE = 
			"Successfully created provider [ %s ]";

	// ** ** ** ** ** ** ** Class members
	String name;
	String org;
	String description;
	String url;
	
	private KatelloCliTasks cli;	
	
	public KatelloProvider(KatelloCliTasks pCli, String pName, String pOrg, 
			String pDesc, String pUrl){
		this.cli = pCli;
		this.name = pName;
		this.org = pOrg;
		this.description = pDesc;
		this.url = pUrl;
	}
	
	public SSHCommandResult create(){
		String cmd = CMD_CREATE;
		
		if(this.name != null)
			cmd += " --name \""+this.name+"\"";
		if(this.org != null)
			cmd += " --org \""+this.org+"\"";
		if(this.description != null)
			cmd += " --description \""+this.description+"\"";
		if(this.url != null)
			cmd += " --url \""+this.url+"\"";
		
		return cli.run_cliCmd(cmd);
	}

	// ** ** ** ** ** ** **
	// ASSERTS
	// ** ** ** ** ** ** **
}
