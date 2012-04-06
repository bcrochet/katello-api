package com.redhat.qe.katello.base.cli;

import com.redhat.qe.auto.testng.Assert;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.tasks.KatelloCliTasks;
import com.redhat.qe.tools.SSHCommandResult;

public class KatelloUser {
	
	// ** ** ** ** ** ** ** Public constants
	public static final String DEFAULT_ADMIN_USER = "admin";
	public static final String DEFAULT_ADMIN_PASS = "admin";
	public static final String DEFAULT_USER_PASS = "testing";
	
	public static final String CMD_CREATE = "user create";
	public static final String CMD_INFO = "user info";
	public static final String CMD_LIST = "user list";
	public static final String CMD_ASSIGN_ROLE = "user assign_role";
	
	public static final String ERR_TEMPLATE_NOTFOUND = 
			"Could not find template [ %s ]";	
	public static final String OUT_CREATE = 
			"Successfully created user [ %s ]";

	// ** ** ** ** ** ** ** Class members
	String username;
	String email;
	
	private KatelloCliTasks cli;	
	private String password;
	private boolean disabled;
	
	public KatelloUser(KatelloCliTasks pCli, String pName, String pEmail, String pPassword, boolean pDisabled){
		this.cli = pCli;
		this.username = pName;
		this.email = pEmail;
		this.password = pPassword;
		this.disabled = pDisabled;
	}
	
	public SSHCommandResult create(){
		String cmd = CMD_CREATE;
		
		if(this.username != null)
			cmd += " --username \""+this.username+"\"";
		if(this.email != null)
			cmd += " --email \""+this.email+"\"";
		if(this.password != null)
			cmd += " --password \""+this.password+"\"";
		if(this.disabled)
			cmd += " --disabled true";
		
		return cli.run_cliCmd(cmd);
	}
	
	public SSHCommandResult info(){
		String cmd = CMD_INFO;
		
		if(this.username != null)
			cmd += " --username \""+this.username+"\"";			
		return cli.run_cliCmd(cmd);
	}
	
	public SSHCommandResult list(){
		String cmd = CMD_LIST;
		
		cmd += " -v";

		return cli.run_cliCmd(cmd);
	}
	
	public SSHCommandResult assign_role(String role){
		String cmd = CMD_ASSIGN_ROLE;
		
		if(this.username != null)
			cmd += " --username \""+this.username+"\"";
		if(role != null)
			cmd += " --role \""+role+"\"";		
		
		return cli.run_cliCmd(cmd);
	}

	// ** ** ** ** ** ** **
	// ASSERTS
	// ** ** ** ** ** ** **
	public void asserts_create(){
		SSHCommandResult res;

		// asserts: user list
		res = list();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code ("+CMD_LIST+")");
		String REGEXP_LIST = ".*Id:\\s+\\d+.*Username:\\s+%s.*Email:\\s+%s.*Disabled:\\s+False.*";
		if(this.disabled)
			REGEXP_LIST = ".*Id:\\s+\\d+.*Username:\\s+%s.*Email:\\s+%s.*Disabled:\\s+True.*";

		String match_info = String.format(REGEXP_LIST,
				this.username,this.email).replaceAll("\"", "");
		Assert.assertTrue(KatelloCliTestScript.sgetOutput(res).replaceAll("\n", "").matches(match_info), 
				String.format("User [%s] should be found in the list",this.username));
		
		// asserts: user info
		res = info();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code ("+CMD_INFO+")");
		String REGEXP_INFO = ".*Id:\\s+\\d+.*Username:\\s+%s.*Email:\\s+%s.*Disabled:\\s+False.*";
		if(this.disabled)
			REGEXP_INFO = ".*Id:\\s+\\d+.*Username:\\s+%s.*Email:\\s+%s.*Disabled:\\s+True.*";
		match_info = String.format(REGEXP_INFO,
				this.username, this.email).replaceAll("\"", "");
		Assert.assertTrue(KatelloCliTestScript.sgetOutput(res).replaceAll("\n", "").matches(match_info), 
				String.format("User [%s] should contain correct info",this.username));			
	}
	
}
