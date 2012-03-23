package com.redhat.qe.katello.base.cli;

import com.redhat.qe.katello.tasks.KatelloCliTasks;
import com.redhat.qe.tools.SSHCommandResult;

public class KatelloTemplate {
	
	// ** ** ** ** ** ** ** Public constants
	public static final String CMD_CREATE = "template create";
	public static final String CMD_INFO = "template info -v";
	public static final String CMD_UPDATE = "template update";
	public static final String CMD_EXPORT = "template export";
	
	public static final String FORMAT_TDL = "tdl";
	
	// ** ** ** ** ** ** ** Class members
	String name;
	String org;
	String description;
	String parent;
	
	private KatelloCliTasks cli;	
	
	public KatelloTemplate(KatelloCliTasks pCli, String pName, String pDesc,
			String pOrg, String pParent){
		this.cli = pCli;
		this.name = pName;
		this.description = pDesc;
		this.org = pOrg;
		this.parent = pParent;
	}
	
	public SSHCommandResult create(){
		String cmd = CMD_CREATE;
		
		if(this.name != null)
			cmd += " --name \""+this.name+"\"";
		if(this.org != null)
			cmd += " --org \""+this.org+"\"";		
		if(this.description != null)
			cmd += " --description \""+this.description+"\"";
		if(this.parent != null)
			cmd += " --parent \""+this.parent+"\"";
		
		return cli.run_cliCmd(cmd);
	}
	
	public SSHCommandResult info(String environment){
		String cmd = CMD_INFO;
		
		if(this.name != null)
			cmd += " --name \""+this.name+"\"";
		if(this.org != null)
			cmd += " --org \""+this.org+"\"";		
		if(environment != null)
			cmd += " --environment \""+environment+"\"";		
		
		return cli.run_cliCmd(cmd);		
	}
	
	public SSHCommandResult update_add_distribution(String product, String distribution){
		String cmd = CMD_UPDATE;
		
		if(product != null)
			cmd += " --from_product \""+product+"\"";
		if(distribution != null)
			cmd += " --add_distribution \""+distribution+"\"";
		if(this.name != null)
			cmd += " --name \""+this.name+"\"";
		if(this.org != null)
			cmd += " --org \""+this.org+"\"";		
		
		return cli.run_cliCmd(cmd);		
	}
	
	public SSHCommandResult update_add_repo(String product, String repo){
		String cmd = CMD_UPDATE;
		
		if(product != null)
			cmd += " --from_product \""+product+"\"";
		if(repo != null)
			cmd += " --add_repo \""+repo+"\"";
		if(this.name != null)
			cmd += " --name \""+this.name+"\"";
		if(this.org != null)
			cmd += " --org \""+this.org+"\"";		
		
		return cli.run_cliCmd(cmd);		
	}

	public SSHCommandResult update_add_package(String pkg){
		String cmd = CMD_UPDATE;
		
		if(pkg != null)
			cmd += " --add_package \""+pkg+"\"";
		if(this.name != null)
			cmd += " --name \""+this.name+"\"";
		if(this.org != null)
			cmd += " --org \""+this.org+"\"";		
		
		return cli.run_cliCmd(cmd);		
	}

	public SSHCommandResult update_add_package_group(String pkgGrp){
		String cmd = CMD_UPDATE;
		
		if(pkgGrp != null)
			cmd += " --add_package_group \""+pkgGrp+"\"";
		if(this.name != null)
			cmd += " --name \""+this.name+"\"";
		if(this.org != null)
			cmd += " --org \""+this.org+"\"";		
		
		return cli.run_cliCmd(cmd);		
	}
	
	public SSHCommandResult export(String environment, String file, String format){
		String cmd = CMD_EXPORT;
		
		if(file != null)
			cmd += " --file \""+file+"\"";
		if(format != null)
			cmd += " --format \""+format+"\"";
		if(this.name != null)
			cmd += " --name \""+this.name+"\"";
		if(this.org != null)
			cmd += " --org \""+this.org+"\"";		
		if(environment != null)
			cmd += " --environment \""+environment+"\"";
		
		return cli.run_cliCmd(cmd);		
	}
	
	// ** ** ** ** ** ** **
	// ASSERTS
	// ** ** ** ** ** ** **
}
