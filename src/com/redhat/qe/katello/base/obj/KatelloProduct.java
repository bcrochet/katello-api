package com.redhat.qe.katello.base.obj;

import java.util.ArrayList;
import java.util.logging.Logger;
import javax.management.Attribute;
import org.testng.Assert;

import com.redhat.qe.katello.base.KatelloApi;
import com.redhat.qe.katello.base.KatelloCli;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.tools.SSHCommandResult;

public class KatelloProduct {
	protected static Logger log = Logger.getLogger(KatelloProduct.class.getName());
	
	public static final String RHEL_SERVER = "Red Hat Enterprise Linux Server";
	
	// ** ** ** ** ** ** ** Public constants
	public static final String CMD_CREATE = "product create";
	public static final String CLI_CMD_LIST = "product list -v";
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
			"Couldn't find Product with cp_id = ";

	public static final String API_CMD_LIST = "/organizations/%s/products"; // by org
	
	// ** ** ** ** ** ** ** Class members
	String name;
	String org;
	String provider;
	String description;
	String gpgkey;
	String url;
	boolean nodisc = false;
	boolean assumeyes = false;
	
	private KatelloCli cli;
	private ArrayList<Attribute> opts;

	public KatelloProduct(
			String pName, String pOrg, String pProv, 
			String pDesc, String pGpgkey, String pUrl,
			Boolean bNodisc, Boolean bAssumeyes){
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
		this.opts = new ArrayList<Attribute>();
	}
	
	public SSHCommandResult create(){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("provider", provider));
		opts.add(new Attribute("description", description));
		opts.add(new Attribute("gpgkey", gpgkey));
		opts.add(new Attribute("url", url));
		if(nodisc)
			opts.add(new Attribute("nodisc", ""));
		if(assumeyes)
			opts.add(new Attribute("assumeyes", ""));
		cli = new KatelloCli(CMD_CREATE, opts);
		return cli.run();
	}
	
	public SSHCommandResult cli_list(){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("provider", provider));
		cli = new KatelloCli(CLI_CMD_LIST, opts);
		return cli.run();
	}

	public SSHCommandResult cli_list(String environment){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("provider", provider));
		opts.add(new Attribute("environment", environment));
		cli = new KatelloCli(CLI_CMD_LIST, opts);
		return cli.run();
	}
	
	public SSHCommandResult status(){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		cli = new KatelloCli(CMD_STATUS, opts);
		return cli.run();
	}
	
	public SSHCommandResult synchronize(){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		cli = new KatelloCli(CMD_SYNC, opts);
		return cli.run();
	}
	
	public SSHCommandResult promote(String environment){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		opts.add(new Attribute("environment", environment));
		cli = new KatelloCli(CMD_PROMOTE, opts);
		return cli.run();
	}

	public SSHCommandResult delete(){
		opts.clear();
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("name", name));
		cli = new KatelloCli(CMD_DELETE, opts);
		return cli.run();
	}
	
	public SSHCommandResult api_list(){
		return new KatelloApi().get(String.format(API_CMD_LIST, this.org));
	}

	
	// ** ** ** ** ** ** **
	// ASSERTS
	// ** ** ** ** ** ** **
	
	public void assert_productExists(String envName, boolean synced){
		SSHCommandResult res;
		String REGEXP_PRODUCT_LIST;
		
		REGEXP_PRODUCT_LIST = ".*Name:\\s+"+this.name+".*Provider Name:\\s+"+this.provider+".*";
		log.info("Assertions: product exists");
		res = cli_list();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
		Assert.assertTrue(KatelloCliTestScript.sgetOutput(res).replaceAll("\n", "").matches(REGEXP_PRODUCT_LIST),
				"List should contain info about product (requested by: provider)");

		if(envName!=null){
			res = cli_list(envName);
			Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
			Assert.assertTrue(KatelloCliTestScript.sgetOutput(res).replaceAll("\n", "").matches(REGEXP_PRODUCT_LIST), 
					"List should contain info about product (requested by: environment)");
		}
		
		if(!synced){
			res = status();
			Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code");
			String REGEXP_PRODUCT_STATUS = ".*Name:\\s+"+this.name+".*Provider Name:\\s+"+this.provider+".*Last Sync:\\s+never.*Sync State:\\s+Not synced.*";
			Assert.assertTrue(KatelloCliTestScript.sgetOutput(res).replaceAll("\n", "").matches(REGEXP_PRODUCT_STATUS), 
					"List should contain status of product (not synced)");
		}else{
			// TODO - needs an implementation - when product is synchronized.
		}
	}
	
}