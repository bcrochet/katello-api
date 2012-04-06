package com.redhat.qe.katello.base.cli;

import java.util.logging.Logger;
import org.testng.Assert;
import com.redhat.qe.katello.tasks.KatelloCliTasks;
import com.redhat.qe.tools.SSHCommandResult;

public class KatelloProduct {
	protected static Logger log = Logger.getLogger(KatelloProduct.class.getName());
	
	public static final String RHEL_SERVER = "Red Hat Enterprise Linux Server";
	
	// ** ** ** ** ** ** ** Public constants
	public static final String CMD_CREATE = "product create";
	public static final String CMD_LIST = "product list -v";
	public static final String CMD_STATUS = "product status";
	public static final String CMD_SYNC = "product synchronize";
	public static final String CMD_PROMOTE = "product promote";
	public static final String CMD_DELETE = "product delete";
	
	/** Parameters:<BR>1: product_name<BR>2: org_name */
	public static final String ERR_COULD_NOT_FIND_PRODUCT = 
		"Could not find product [ %s ] within organization [ %s ]";
	/** Parameters:<BR>1: product_name */
	public static final String OUT_CREATED = 
		"Successfully created product [ %s ]";
	/** Parameters:<BR>1: product_name<BR>2: env_name */
	public static final String OUT_PROMOTED = 
		"Product [ %s ] promoted to environment [ %s ]";
	/** Parameters:<BR>1: product_name */
	public static final String OUT_SYNCHRONIZED = 
		"Product [ %s ] synchronized";
	public static final String OUT_DELETED = 
		"Deleted product '%s'";
	public static final String OUT_NOT_SYNCHRONIZED_YET = 
			"Product '%s' was not synchronized yet";
	public static final String ERR_PROMOTE_NOREPOS = 
			"Product '%s' hasn't any repositories";

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
	
	public SSHCommandResult list(){
		String cmd = CMD_LIST;
		
		if(this.org != null)
			cmd += " --org \""+this.org+"\"";
		if(this.provider != null)
			cmd += " --provider \""+this.provider+"\"";
		
		return cli.run_cliCmd(cmd);
	}

	public SSHCommandResult list(String environment){
		String cmd = CMD_LIST;
		
		if(this.org != null)
			cmd += " --org \""+this.org+"\"";
		if(this.provider != null)
			cmd += " --provider \""+this.provider+"\"";
		if(environment != null)
			cmd += " --environment \""+environment+"\"";
		
		return cli.run_cliCmd(cmd);
	}
	
	public SSHCommandResult status(){
		String cmd = CMD_STATUS;
		
		if(this.org != null)
			cmd += " --org \""+this.org+"\"";
		if(this.name != null)
			cmd += " --name \""+this.name+"\"";
		
		return cli.run_cliCmd(cmd);
	}
	
	public SSHCommandResult synchronize(){
		String cmd = CMD_SYNC;
		
		if(this.org != null)
			cmd += " --org \""+this.org+"\"";
		if(this.name != null)
			cmd += " --name \""+this.name+"\"";
		
		return cli.run_cliCmd(cmd);
	}
	
	public SSHCommandResult promote(String environment){
		String cmd = CMD_PROMOTE;
		
		if(this.org != null)
			cmd += " --org \""+this.org+"\"";
		if(this.name != null)
			cmd += " --name \""+this.name+"\"";
		if(environment != null)
			cmd += " --environment \""+environment+"\"";
		
		return cli.run_cliCmd(cmd);
	}

	public SSHCommandResult delete(){
		String cmd = CMD_DELETE;
		
		if(this.org != null)
			cmd += " --org \""+this.org+"\"";
		if(this.name != null)
			cmd += " --name \""+this.name+"\"";
		
		return cli.run_cliCmd(cmd);
	}
	
	// ** ** ** ** ** ** **
	// ASSERTS
	// ** ** ** ** ** ** **
	
	public void assert_productExists(String envName, boolean synced){
		SSHCommandResult res;
		String REGEXP_PRODUCT_LIST;
		
		REGEXP_PRODUCT_LIST = ".*Name:\\s+"+this.name+".*Provider Name:\\s+"+this.provider+".*";
		log.info("Assertions: product exists");
		res = list();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		Assert.assertTrue(getOutput(res).replaceAll("\n", "").matches(REGEXP_PRODUCT_LIST),
				"List should contain info about product (requested by: provider)");

		if(envName!=null){
			res = list(envName);
			Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
			Assert.assertTrue(getOutput(res).replaceAll("\n", "").matches(REGEXP_PRODUCT_LIST), 
					"List should contain info about product (requested by: environment)");
		}
		
		if(!synced){
			res = status();
			Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
			String REGEXP_PRODUCT_STATUS = ".*Name:\\s+"+this.name+".*Provider Name:\\s+"+this.provider+".*Last Sync:\\s+never.*Sync State:\\s+Not synced.*";
			Assert.assertTrue(getOutput(res).replaceAll("\n", "").matches(REGEXP_PRODUCT_STATUS), 
					"List should contain status of product (not synced)");
		}else{
			// TODO - needs an implementation - when product is synchronized.
		}
	}
	
}
