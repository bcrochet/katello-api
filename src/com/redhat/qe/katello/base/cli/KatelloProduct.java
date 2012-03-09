package com.redhat.qe.katello.base.cli;

import com.redhat.qe.katello.tasks.KatelloCliTasks;
import com.redhat.qe.tools.SSHCommandResult;

public class KatelloProduct {
	
	public static final String RHEL_SERVER = "Red Hat Enterprise Linux Server";
	
	// ** ** ** ** ** ** ** Public constants
	public static final String CMD_CREATE = "product create";
	
	public static final String OUT_CREATE = 
			"Successfully created product [ %s ]";

	// ** ** ** ** ** ** ** Class members
	String name;
	String org;
	String provider;
	String description;
	String gpgkey;
	String url;
	boolean nodisc = false;
	boolean assumeyes = false;
	
	private KatelloCliTasks cli;	
	
	public KatelloProduct(KatelloCliTasks pCli, 
			String pName, String pOrg, String pProv, 
			String pDesc, String pGpgkey, String pUrl,
			Boolean bNodisc, Boolean bAssumeyes){
		this.cli = pCli;
		this.name = pName;
		this.org = pOrg;
		this.provider = pProv;
		this.description = pDesc;
		this.gpgkey = pGpgkey;
		this.url = pUrl;
		if(bNodisc != null)
			this.nodisc = bNodisc.booleanValue();
		if(bAssumeyes != null)
			this.assumeyes = bAssumeyes.booleanValue();
	}
	
	public SSHCommandResult create(){
		String cmd = CMD_CREATE;
		
		if(this.name != null)
			cmd += " --name \""+this.name+"\"";
		if(this.org != null)
			cmd += " --org \""+this.org+"\"";
		if(this.provider != null)
			cmd += " --provider \""+this.provider+"\"";
		if(this.description != null)
			cmd += " --description \""+this.description+"\"";
		if(this.gpgkey != null)
			cmd += " --gpgkey \""+this.gpgkey+"\"";
		if(this.url != null)
			cmd += " --url \""+this.url+"\"";
		if(this.nodisc)
			cmd += " --nodisc";
		if(this.assumeyes)
			cmd += " --assumeyes";

		
		return cli.run_cliCmd(cmd);
	}

	// ** ** ** ** ** ** **
	// ASSERTS
	// ** ** ** ** ** ** **
}
