package com.redhat.qe.katello.base.cli;

import com.redhat.qe.katello.tasks.KatelloCliTasks;
import com.redhat.qe.tools.SSHCommandResult;

public class KatelloDistribution {
	
	// ** ** ** ** ** ** ** Public constants
	public static final String CMD_LIST = "distribution list -v";
	public static final String CMD_INFO = "distribution info -v";
	
	// ** ** ** ** ** ** ** Class members
	String org;
	String product;
	String repo;
	String environment;
	
	private KatelloCliTasks cli;	
	
	public KatelloDistribution(KatelloCliTasks pCli, String pOrg, String pProduct,
			String pRepo, String pEnvironment){
		this.cli = pCli;
		this.org = pOrg;
		this.product = pProduct;
		this.repo = pRepo;
		this.environment = pEnvironment;
	}
	
	public SSHCommandResult list(){
		String cmd = CMD_LIST;
		
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
