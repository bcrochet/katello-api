package com.redhat.qe.katello.base.cli;

import com.redhat.qe.katello.tasks.KatelloCliTasks;
import com.redhat.qe.tools.SSHCommandResult;

public class KatelloPermission {
	
	// ** ** ** ** ** ** ** Public constants
	public static final String CMD_CREATE = "permission create";
	
	// ** ** ** ** ** ** ** Class members
	String name;
	String org;
	String scope;
	String tags;
	String verbs;
	String user_role;
	
	private KatelloCliTasks cli;	
	
	public KatelloPermission(KatelloCliTasks pCli, String pName, String pOrg,
			String pScope, String pTags, String pVerbs, String pUserRole){
		this.cli = pCli;
		this.name = pName;
		this.org = pOrg;
		this.scope = pScope;
		this.tags = pTags;
		this.verbs = pVerbs;
		this.user_role = pUserRole;
	}
	
	public SSHCommandResult create(){
		String cmd = CMD_CREATE;
		
		if(this.name != null)
			cmd += " --name \""+this.name+"\"";
		if(this.org != null)
			cmd += " --org \""+this.org+"\"";
		if(this.scope != null)
			cmd += " --scope \""+this.scope+"\"";
		if(this.tags != null)
			cmd += " --tags \""+this.tags+"\"";
		if(this.verbs != null)
			cmd += " --verbs \""+this.verbs+"\"";
		if(this.user_role != null)
			cmd += " --user_role \""+this.user_role+"\"";
		
		return cli.run_cliCmd(cmd);
	}
	
}
