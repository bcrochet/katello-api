package com.redhat.qe.katello.tests.cli;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.redhat.qe.auto.testng.Assert;
import com.redhat.qe.katello.base.IKatelloProduct;
import com.redhat.qe.katello.base.IKatelloProvider;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.base.KatelloTestScript;
import com.redhat.qe.tools.SSHCommandResult;

public class ProductTests  extends KatelloCliTestScript{

	private String org_name;
	private String prov_name;
	
	@BeforeClass(description="Prepare an org to work with", groups = {"cli-product"})
	public void setup_org(){
		SSHCommandResult res;
		String uid = KatelloTestScript.getUniqueID();
		this.org_name = "org"+uid;
		this.prov_name = "prov"+uid;
		res = clienttasks.run_cliCmd("org create --name "+this.org_name);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (org create)");
		res = clienttasks.run_cliCmd(String.format(IKatelloProvider.CREATE_NODESCRIPTION_NOURL, this.org_name, this.prov_name));
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (provider create)");
	}
	
	@Test(description="create product - no url specified", groups = {"cli-products"}, enabled=true)
	public void test_createProduct_noUrl(){
		String uid = KatelloTestScript.getUniqueID();
		String prodName = "prodCreate-"+uid;
		SSHCommandResult res;
		
		// create product
		res = clienttasks.run_cliCmd(String.format(IKatelloProduct.CREATE_NOURL,this.org_name,this.prov_name,prodName));
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product create)");
		Assert.assertTrue(res.getStdout().trim().contains(String.format(IKatelloProduct.OUT_CREATED,prodName)), "Check - returned output string (product create)");
		
		assert_productExists(this.org_name, this.prov_name, prodName);
	}
	
	@Test(description="create product - no url specified", groups = {"cli-products"}, enabled=true)
	public void test_createProduct_urlSingleRepo(){
		String uid = KatelloTestScript.getUniqueID();
		String prodName = "prod1Repo-"+uid;
		SSHCommandResult res;
		
		// create product
		res = clienttasks.run_cliCmd(String.format(IKatelloProduct.CREATE,this.org_name,this.prov_name,prodName,PULP_F15_x86_64_REPO));
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product create)");
		Assert.assertTrue(res.getStdout().trim().contains(String.format(IKatelloProduct.OUT_CREATED,prodName)), "Check - returned output string (product create)");
		
		assert_productExists(this.org_name, this.prov_name, prodName);
	}
	
	// TODO - product creation failflows + he cases with "Description" variations.
	
	
	
}
