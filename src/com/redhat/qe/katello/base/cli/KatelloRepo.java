package com.redhat.qe.katello.base.cli;

import com.redhat.qe.auto.testng.Assert;
import com.redhat.qe.katello.tasks.KatelloCliTasks;
import com.redhat.qe.tools.SSHCommandResult;

public class KatelloRepo {
	
	// ** ** ** ** ** ** ** Public constants
	// Red Hat Enterprise Linux 6 Server RPMs x86_64 6Server
	public static final String RH_REPO_PRODUCT_VER = "6Server";
	public static final String RH_REPO_RHEL6_SERVER_RPMS_64BIT = 
			"Red Hat Enterprise Linux 6 Server RPMs x86_64 "+RH_REPO_PRODUCT_VER;
	
	public static final String CMD_CREATE = "repo create";
	public static final String CMD_SYNCHRONIZE = "repo synchronize";
	public static final String CMD_UPDATE = "repo update";
	public static final String CMD_INFO = "repo info";
	public static final String CMD_ENABLE = "repo enable";
	
	public static final String OUT_CREATE = 
			"Successfully created repository [ %s ]"; 

	// ** ** ** ** ** ** ** Class members
	String name;
	String org;
	String product;
	String url;
	String gpgkey;
	boolean nogpgkey = false;
	
	
	private KatelloCliTasks cli;	
	
	public KatelloRepo(KatelloCliTasks pCli, 
			String pName, String pOrg, String pProd, String pUrl, 
			String pGpgkey, Boolean pNogpgkey){
		this.cli = pCli;
		this.name = pName;
		this.org = pOrg;
		this.product = pProd;
		this.url = pUrl;
		this.gpgkey = pGpgkey;
		if(pNogpgkey != null)
			this.nogpgkey = pNogpgkey.booleanValue();
	}
	
	public SSHCommandResult create(){
		String cmd = CMD_CREATE;
		
		if(this.name != null)
			cmd += " --name \""+this.name+"\"";
		if(this.org != null)
			cmd += " --org \""+this.org+"\"";
		if(this.product != null)
			cmd += " --product \""+this.product+"\"";
		if(this.url != null)
			cmd += " --url \""+this.url+"\"";
		if(this.gpgkey != null)
			cmd += " --gpgkey \""+this.gpgkey+"\"";
		if(this.nogpgkey)
			cmd += " --nogpgkey";
		
		
		return cli.run_cliCmd(cmd);
	}
	
	public SSHCommandResult synchronize(){
		String cmd = CMD_SYNCHRONIZE;
		
		cmd += String.format(" --name \"%s\" --org \"%s\" --product \"%s\"", 
				this.name, this.org, this.product);
		
		
		return cli.run_cliCmd(cmd);
	}
	
	public SSHCommandResult update_gpgkey(){
		String cmd = CMD_UPDATE;
		
		if(this.gpgkey != null)
			cmd += " --gpgkey \""+this.gpgkey+"\"";
		if(this.name != null)
			cmd += " --name \""+this.name+"\"";
		if(this.org != null)
			cmd += " --org \""+this.org+"\"";
		if(this.product != null)
			cmd += " --product \""+this.product+"\"";
		
		
		return cli.run_cliCmd(cmd);		
	}
	
	public SSHCommandResult info(){
		String cmd = CMD_INFO;
		
		if(this.name != null)
			cmd += " --name \""+this.name+"\"";
		if(this.org != null)
			cmd += " --org \""+this.org+"\"";
		if(this.product != null)
			cmd += " --product \""+this.product+"\"";
		
		return cli.run_cliCmd(cmd);
	}
	
	public SSHCommandResult info(String environment){
		String cmd = CMD_INFO;
		
		if(this.name != null)
			cmd += " --name \""+this.name+"\"";
		if(this.org != null)
			cmd += " --org \""+this.org+"\"";
		if(this.product != null)
			cmd += " --product \""+this.product+"\"";
		if(environment != null)
			cmd += " --environment \""+environment+"\"";
		
		return cli.run_cliCmd(cmd);
	}

	public SSHCommandResult enable(){
		String cmd = CMD_ENABLE;
		
		if(this.name != null)
			cmd += " --name \""+this.name+"\"";
		if(this.org != null)
			cmd += " --org \""+this.org+"\"";
		if(this.product != null)
			cmd += " --product \""+this.product+"\"";

		return cli.run_cliCmd(cmd);
	}
	
	// ** ** ** ** ** ** **
	// ASSERTS
	// ** ** ** ** ** ** **
	
	public void assert_repoHasGpg(){
		SSHCommandResult res;
		
		res = info();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (repo info)");
		String gpg_key = KatelloCliTasks.grepCLIOutput("GPG key", res.getStdout());
		Assert.assertTrue(this.gpgkey.equals(gpg_key), 
				String.format("Check - GPG key [%s] should be found in the repo info",this.gpgkey));
		KatelloGpgKey gpg = new KatelloGpgKey(cli, this.gpgkey, this.org, null);
		res = gpg.info();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (gpg_key info)");
		String reposWithGpg = KatelloCliTasks.grepCLIOutput("Repositories", res.getStdout());
		Assert.assertTrue(reposWithGpg.contains(this.name), 
				"Check - Repo should be in repositories list of GPG key");
	}
	
}
