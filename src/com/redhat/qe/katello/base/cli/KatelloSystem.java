package com.redhat.qe.katello.base.cli;

import com.redhat.qe.katello.tasks.KatelloCliTasks;
import com.redhat.qe.tools.SSHCommandResult;

public class KatelloSystem {
	
	// ** ** ** ** ** ** ** Public constants	
	public static final String RHSM_DEFAULT_USER = "admin";
	public static final String RHSM_DEFAULT_PASS = "admin";
	
	public static final String CMD_INFO = "system info";
	public static final String CMD_LIST = "system list";
	
	public static final String RHSM_CREATE = 
			String.format("subscription-manager register --username %s --password %s",
					RHSM_DEFAULT_USER,RHSM_DEFAULT_PASS);
	
	public static final String OUT_CREATE = 
			"The system has been registered with id:";
	public static final String ERR_RHSM_LOCKER_ONLY = 
			"Organization %s has '%s' environment only. Please create an environment for system registration.";
	public static final String ERR_RHSM_REG_ALREADY_FORCE_NEEDED = 
			"This system is already registered. Use --force to override";
	public static final String ERR_RHSM_REG_MULTI_ENV = 
			"Organization %s has more than one environment. Please specify target environment for system registration.";

	// ** ** ** ** ** ** ** Class members
	String name;
	String org;
	String environment;
	
	private KatelloCliTasks cli;	
	
	public KatelloSystem(KatelloCliTasks pCli, String pName, String pOrg, String pEnv){
		this.cli = pCli;
		this.name = pName;
		this.org = pOrg;
		this.environment = pEnv;
	}
	
	// ** ** ** ** ** ** **
	// ASSERTS
	// ** ** ** ** ** ** **
	
	public SSHCommandResult rhsm_register(){
		String cmd = RHSM_CREATE;
		
		if(this.name != null)
			cmd += " --name \""+this.name+"\"";
		if(this.org != null)
			cmd += " --org \""+this.org+"\"";
		if(this.environment != null)
			cmd += " --environment \""+this.environment+"\"";
		
		return cli.execute_remote(cmd);		
	}
	
	public SSHCommandResult rhsm_registerForce(){
		String cmd = RHSM_CREATE;
		
		if(this.name != null)
			cmd += " --name \""+this.name+"\"";
		if(this.org != null)
			cmd += " --org \""+this.org+"\"";
		if(this.environment != null)
			cmd += " --environment \""+this.environment+"\"";
		cmd += " --force";
		
		return cli.execute_remote(cmd);		
	}
	
}
