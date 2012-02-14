package com.redhat.qe.katello.base.cli;

import com.redhat.qe.katello.tasks.KatelloCliTasks;
import com.redhat.qe.tools.SSHCommandResult;

public class KatelloErrata {
	
	// ** ** ** ** ** ** ** Public constants
	public static final String CMD_INFO = "errata info";
	
	// ** ** ** ** ** ** ** Class members
	String id;
	String org;
	String product;
	String repo;
	String environment;
	
	private KatelloCliTasks cli;	
	
	public KatelloErrata(KatelloCliTasks pCli, 
			String pId, String pOrg, String pProd, String pRepo, String pEnv){
		this.cli = pCli;
		this.id = pId;
		this.org = pOrg;
		this.product = pProd;
		this.repo = pRepo;
		this.environment = pEnv;
	}
	
	public SSHCommandResult info(){
		String cmd = CMD_INFO;
		
		if(this.id != null)
			cmd += " --id \""+this.id+"\"";
		if(this.org != null)
			cmd += " --org \""+this.org+"\"";
		if(this.product != null)
			cmd += " --product \""+this.product+"\"";
		if(this.repo != null)
			cmd += " --repo \""+this.repo+"\"";
		if(this.environment != null)
			cmd += " --environment \""+this.environment+"\"";
		
		return cli.run_cliCmd(cmd);
	}
	
	// ** ** ** ** ** ** **
	// ASSERTS
	// ** ** ** ** ** ** **	
	
}
