package com.redhat.qe.katello.base.obj;

import javax.management.Attribute;
import com.redhat.qe.tools.SSHCommandResult;

public class KatelloErrata extends _KatelloObject{
	
	// ** ** ** ** ** ** ** Public constants
	public static final String CMD_INFO = "errata info";
	
	// ** ** ** ** ** ** ** Class members
	String id;
	String org;
	String product;
	String repo;
	String environment;
	
	public KatelloErrata(String pId, String pOrg, String pProd, String pRepo, String pEnv){
		this.id = pId;
		this.org = pOrg;
		this.product = pProd;
		this.repo = pRepo;
		this.environment = pEnv;
	}
	
	public SSHCommandResult info(){
		opts.clear();
		opts.add(new Attribute("id", id));
		opts.add(new Attribute("org", org));
		opts.add(new Attribute("product", product));
		opts.add(new Attribute("repo", repo));
		opts.add(new Attribute("environment", environment));
		return run(CMD_INFO);
	}
	
	// ** ** ** ** ** ** **
	// ASSERTS
	// ** ** ** ** ** ** **	
	
}
