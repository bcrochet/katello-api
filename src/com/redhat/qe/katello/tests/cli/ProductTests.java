package com.redhat.qe.katello.tests.cli;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.redhat.qe.auto.testng.Assert;
import com.redhat.qe.katello.base.IKatelloChangeset;
import com.redhat.qe.katello.base.IKatelloEnvironment;
import com.redhat.qe.katello.base.IKatelloProduct;
import com.redhat.qe.katello.base.IKatelloProvider;
import com.redhat.qe.katello.base.IKatelloRepo;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.base.KatelloTestScript;
import com.redhat.qe.katello.tasks.KatelloCliTasks;
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
	
	@Test(description="create product - with single repo", groups = {"cli-products"}, enabled=true)
	public void test_createProduct_urlSingleRepo(){
		String uid = KatelloTestScript.getUniqueID();
		String prodName = "prod1Repo-"+uid;
		SSHCommandResult res;
		
		// create product
		res = clienttasks.run_cliCmd(String.format(IKatelloProduct.CREATE,this.org_name,this.prov_name,prodName,PULP_F15_x86_64_REPO));
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product create)");
		Assert.assertTrue(res.getStdout().trim().contains(String.format(IKatelloProduct.OUT_CREATED,prodName)), "Check - returned output string (product create)");
		
		assert_productExists(this.org_name, this.prov_name, prodName);
		
		// check - repo created - we don't know the exact repo name.
		res = clienttasks.run_cliCmd(String.format(IKatelloRepo.LIST_BY_PRODUCT,this.org_name,prodName));
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (repo list by product)");		
		String REGEXP_PRODUCT_LIST = ".*Id:\\s+"+this.org_name+"-"+prodName+"-"+prodName+"_.*Name:\\s+"+prodName+"_.*Package Count:\\s+0.*";
		Assert.assertTrue(res.getStdout().replaceAll("\n", "").matches(REGEXP_PRODUCT_LIST),
				"Repo list should contain info about just created repo (requested by: org, product)");
	}
	
	@Test(description="create product - with multiple repos", groups = {"cli-products"}, enabled=true)
	public void test_createProduct_urlMultipleRepo(){
		String uid = KatelloTestScript.getUniqueID();
		String prodName = "prod2Repos-"+uid;
		SSHCommandResult res;
		
		// create product
		res = clienttasks.run_cliCmd(String.format(IKatelloProduct.CREATE,this.org_name,this.prov_name,prodName,PULP_F15_REPO));
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product create)");
		Assert.assertTrue(res.getStdout().trim().contains(String.format(IKatelloProduct.OUT_CREATED,prodName)), "Check - returned output string (product create)");
		
		assert_productExists(this.org_name, this.prov_name, prodName);
		
		// check - 2 repos created
		res = clienttasks.run_cliCmd(String.format(IKatelloRepo.LIST_BY_PRODUCT,this.org_name,prodName));
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (repo list by product)");
		String REGEXP_PRODUCT_LIST_I386 = ".*Id:\\s+"+this.org_name+"-"+prodName+"-"+prodName+"_.*_i386.*Name:\\s+"+prodName+"_.*_i386.*Package Count:\\s+0.*";
		Assert.assertTrue(res.getStdout().replaceAll("\n", "").matches(REGEXP_PRODUCT_LIST_I386),
				"Repo list should contain info about just created repo (requested by: org, product - i386)");
		String REGEXP_PRODUCT_LIST_X86_64 = ".*Id:\\s+"+this.org_name+"-"+prodName+"-"+prodName+"_.*_x86_64.*Name:\\s+"+prodName+"_.*_x86_64.*Package Count:\\s+0.*";
		Assert.assertTrue(res.getStdout().replaceAll("\n", "").matches(REGEXP_PRODUCT_LIST_X86_64),
				"Repo list should contain info about just created repo (requested by: org, product - x86_64)");
	}

	// TODO - product creation failflows + the cases with "Description" variations. + duplicate names and so
	
	// TODO - `product list --provider`
	
	// TODO - `product list --environment`
	
	// TODO - `product status`
	
	@Test(description="promote product", groups = {"cli-products"}, enabled=true)
	public void test_promoteProduct_NoRepos(){
		String uid = KatelloTestScript.getUniqueID();
		String prodName = "promoNoRepo-"+uid;
		SSHCommandResult res;
		
		// create product
		res = clienttasks.run_cliCmd(String.format(IKatelloProduct.CREATE_NOURL,this.org_name,this.prov_name,prodName));
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product create)");
		Assert.assertTrue(res.getStdout().trim().contains(String.format(IKatelloProduct.OUT_CREATED,prodName)), "Check - returned output string (product create)");
		assert_productExists(this.org_name, this.prov_name, prodName);
		
		// create env.
		String envName = "dev-"+uid;
		res = clienttasks.run_cliCmd(String.format(IKatelloEnvironment.CREATE_NODESC,this.org_name,envName,IKatelloEnvironment.LOCKER));
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (environment create)");
		
		// promote product to the env.
		res = clienttasks.run_cliCmd(String.format(IKatelloProduct.PROMOTE,this.org_name,prodName, envName));
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product promote)");
		Assert.assertTrue(res.getStdout().trim().contains(String.format(IKatelloProduct.OUT_PROMOTED,prodName,envName)), "Check - returned output string (product promote)");
		
		// product list --environment (1 result - just the product promoted)
		res = clienttasks.run_cliCmd(String.format(IKatelloProduct.LIST_BY_ENV,this.org_name,envName));
		String REGEXP_PRODUCT_LIST = ".*Id:\\s+\\d+Name:\\s+"+prodName+".*Provider Name:\\s+"+prov_name+".*";
		Assert.assertTrue(res.getStdout().replaceAll("\n", "").matches(REGEXP_PRODUCT_LIST),
				"Product list by environment - just promoted product");
	}
	
	@Test(description="promote product", groups = {"cli-products"}, enabled=true)
	public void test_promoteProduct_OneRepo(){
		String uid = KatelloTestScript.getUniqueID();
		String prodName = "promo1Repo-"+uid;
		String envName = "dev-"+uid;
		SSHCommandResult res;
		
		// create product
		res = clienttasks.run_cliCmd(String.format(IKatelloProduct.CREATE,this.org_name,this.prov_name,prodName,PULP_F15_x86_64_REPO));
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product create)");
		Assert.assertTrue(res.getStdout().trim().contains(String.format(IKatelloProduct.OUT_CREATED,prodName)), "Check - returned output string (product create)");
		assert_productExists(this.org_name, this.prov_name, prodName);
		
		// create env.
		res = clienttasks.run_cliCmd(String.format(IKatelloEnvironment.CREATE_NODESC,this.org_name,envName,IKatelloEnvironment.LOCKER));
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (environment create)");
		
		// promote product to the env.
		res = clienttasks.run_cliCmd(String.format(IKatelloProduct.PROMOTE,this.org_name,prodName, envName));
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product promote)");
		Assert.assertTrue(res.getStdout().trim().contains(String.format(IKatelloProduct.OUT_PROMOTED,prodName,envName)), "Check - returned output string (product promote)");
		
		// product list --environment (1 result - just the product promoted)
		res = clienttasks.run_cliCmd(String.format(IKatelloProduct.LIST_BY_ENV,this.org_name,envName));
		String REGEXP_PRODUCT_LIST = ".*Id:\\s+\\d+Name:\\s+"+prodName+".*Provider Name:\\s+"+prov_name+".*";
		Assert.assertTrue(res.getStdout().replaceAll("\n", "").matches(REGEXP_PRODUCT_LIST),
				"Product list by environment - just promoted product");
		
		// repo list --environment (1 result).
		// check - repo created - we don't know the exact repo name.
		res = clienttasks.run_cliCmd(String.format(IKatelloRepo.LIST_BY_ENVIRONMENT,this.org_name,envName));
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (repo list by product)");		
		String REGEXP_REPO_LIST = ".*Id:\\s+"+this.org_name+"-"+envName+"-"+prodName+"-"+prodName+"_.*Name:\\s+"+prodName+"_.*Package Count:\\s+0.*";
		Assert.assertTrue(res.getStdout().replaceAll("\n", "").matches(REGEXP_REPO_LIST),
				"Repo list should contain info about just created repo (requested by: org, environment)");
	}
	
	@Test(description="promote product", groups = {"cli-products"}, enabled=true)
	public void test_promoteProduct_MultipleRepos(){
		String uid = KatelloTestScript.getUniqueID();
		String prodName = "promo1Repo-"+uid;
		String envName = "dev-"+uid;
		SSHCommandResult res;
		
		// create product
		res = clienttasks.run_cliCmd(String.format(IKatelloProduct.CREATE,this.org_name,this.prov_name,prodName,PULP_F15_REPO));
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product create)");
		Assert.assertTrue(res.getStdout().trim().contains(String.format(IKatelloProduct.OUT_CREATED,prodName)), "Check - returned output string (product create)");
		assert_productExists(this.org_name, this.prov_name, prodName);
		
		// create env.
		res = clienttasks.run_cliCmd(String.format(IKatelloEnvironment.CREATE_NODESC,this.org_name,envName,IKatelloEnvironment.LOCKER));
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (environment create)");
		
		// promote product to the env.
		res = clienttasks.run_cliCmd(String.format(IKatelloProduct.PROMOTE,this.org_name,prodName, envName));
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product promote)");
		Assert.assertTrue(res.getStdout().trim().contains(String.format(IKatelloProduct.OUT_PROMOTED,prodName,envName)), "Check - returned output string (product promote)");
		
		// product list --environment (1 result - just the product promoted)
		res = clienttasks.run_cliCmd(String.format(IKatelloProduct.LIST_BY_ENV,this.org_name,envName));
		String REGEXP_PRODUCT_LIST = ".*Id:\\s+\\d+Name:\\s+"+prodName+".*Provider Name:\\s+"+prov_name+".*";
		Assert.assertTrue(res.getStdout().replaceAll("\n", "").matches(REGEXP_PRODUCT_LIST),
				"Product list by environment - just promoted product");
		
		// repo list --environment (2 entries).
		res = clienttasks.run_cliCmd(String.format(IKatelloRepo.LIST_BY_ENVIRONMENT,this.org_name,envName));
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (repo list by product)");
		String REGEXP_PRODUCT_LIST_I386 = ".*Id:\\s+"+this.org_name+"-"+envName+"-"+prodName+"-"+prodName+"_.*_i386.*Name:\\s+"+prodName+"_.*_i386.*Package Count:\\s+0.*";
		Assert.assertTrue(res.getStdout().replaceAll("\n", "").matches(REGEXP_PRODUCT_LIST_I386),
				"Repo list should contain info about just created repo (requested by: org, product - i386)");
		String REGEXP_PRODUCT_LIST_X86_64 = ".*Id:\\s+"+this.org_name+"-"+envName+"-"+prodName+"-"+prodName+"_.*_x86_64.*Name:\\s+"+prodName+"_.*_x86_64.*Package Count:\\s+0.*";
		Assert.assertTrue(res.getStdout().replaceAll("\n", "").matches(REGEXP_PRODUCT_LIST_X86_64),
				"Repo list should contain info about just created repo (requested by: org, product - x86_64)");
	}

	@Test(description="sync product - single repo", groups = {"cli-products"}, enabled=true)
	public void test_syncronizeProduct_SingleRepo(){
		String uid = KatelloTestScript.getUniqueID();
		String prodName = "sync1Repo-"+uid;
		SSHCommandResult res;

		// create product
		res = clienttasks.run_cliCmd(String.format(IKatelloProduct.CREATE,this.org_name,this.prov_name,prodName,PULP_F15_i386_REPO));
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product create)");
		Assert.assertTrue(res.getStdout().trim().contains(String.format(IKatelloProduct.OUT_CREATED,prodName)), "Check - returned output string (product create)");
		assert_productExists(this.org_name, this.prov_name, prodName);
		
		// sync product
		res = clienttasks.run_cliCmd(String.format(IKatelloProduct.SYNCHRONIZE,this.org_name,prodName));
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product synchronize)");
		Assert.assertTrue(res.getStdout().trim().contains(String.format(IKatelloProduct.OUT_SYNCHRONIZED,prodName)), "Check - returned output string (product synchronize)");
		
		// get packages count for the repo - !=0
		res = clienttasks.run_cliCmd(String.format(IKatelloRepo.LIST_BY_PRODUCT,this.org_name,prodName));
		String REGEXP_REPO_LIST = ".*Package Count:\\s+0.*";
		Assert.assertFalse(res.getStdout().replaceAll("\n", "").matches(REGEXP_REPO_LIST),
				"Repo list of the product - should not contain package count 0 (after product synchronize)");
	}

	@Test(description="sync product - multiple repos", groups = {"cli-products"}, enabled=true)
	public void test_syncronizeProduct_MultipleRepos(){
		String uid = KatelloTestScript.getUniqueID();
		String prodName = "syncManyRepos-"+uid;
		SSHCommandResult res;

		// create product
		res = clienttasks.run_cliCmd(String.format(IKatelloProduct.CREATE,this.org_name,this.prov_name,prodName,PULP_F15_REPO));
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product create)");
		Assert.assertTrue(res.getStdout().trim().contains(String.format(IKatelloProduct.OUT_CREATED,prodName)), "Check - returned output string (product create)");
		assert_productExists(this.org_name, this.prov_name, prodName);
		
		// sync product
		res = clienttasks.run_cliCmd(String.format(IKatelloProduct.SYNCHRONIZE,this.org_name,prodName));
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product synchronize)");
		Assert.assertTrue(res.getStdout().trim().contains(String.format(IKatelloProduct.OUT_SYNCHRONIZED,prodName)), "Check - returned output string (product synchronize)");
		
		// get packages count for the repo - !=0
		res = clienttasks.run_cliCmd(String.format(IKatelloRepo.LIST_BY_PRODUCT,this.org_name,prodName));
		String REGEXP_PACKAGE_CNT = ".*Package Count:\\s+0.*";
		
		String[] lines = res.getStdout().trim().split("\n");String line;
		for(int i=0;i<lines.length;i++){
			line = lines[i];
			if(line.startsWith("Package Count:")){
				// our line to analyze - should not contain: 0
				Assert.assertFalse(line.matches(REGEXP_PACKAGE_CNT),"Repo list of the product - should not contain package count 0 (after product synchronize)");
			}
		}
	}
	
	// TODO - implement: https://bugzilla.redhat.com/show_bug.cgi?id=749517
	// Duplicate product names - creating same product name for different orgs (even provider could have the same name)
	
	@Test(description="delete product - included in some changeset", groups = {"cli-products"}, enabled=true)
	public void test_deleteProduct_InChangeset(){
		String uid = KatelloTestScript.getUniqueID();
		String prodName = "delProd1-"+uid;
		String envName_dev = "dev-"+uid;
		String csName = "cs-"+uid;
		SSHCommandResult res;
		
		// create product
		res = clienttasks.run_cliCmd(String.format(IKatelloProduct.CREATE,this.org_name,this.prov_name,prodName,PULP_F15_x86_64_REPO));
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product create)");
		// sync product
		res = clienttasks.run_cliCmd(String.format(IKatelloProduct.SYNCHRONIZE,this.org_name,prodName));
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product synchronize)");
		// create env. - dev
		res = clienttasks.run_cliCmd(String.format(IKatelloEnvironment.CREATE_NODESC,this.org_name,envName_dev,IKatelloEnvironment.LOCKER));
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (environment create)");
		// create changeset
		res = clienttasks.run_cliCmd(String.format(IKatelloChangeset.CREATE,this.org_name,envName_dev,csName));
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (changeset create)");
		// add product to the changeset
		res = clienttasks.run_cliCmd(String.format(IKatelloChangeset.UPDATE_ADD_PRODUCT,prodName,this.org_name,envName_dev,csName));
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (changeset update --add_product)");
		// promote changeset (dev)
		res = clienttasks.run_cliCmd(String.format(IKatelloChangeset.PROMOTE,this.org_name,envName_dev,csName));
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (changeset promote)");
		
		// Assertions - repo list by env
		res = clienttasks.run_cliCmd(String.format(IKatelloRepo.LIST_BY_ENVIRONMENT,this.org_name,envName_dev));
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (repo list --environment)");
		String REGEXP_PRODUCT_LIST_X86_64 = ".*Id:\\s+"+this.org_name+"-"+envName_dev+"-"+prodName+"-"+prodName+"_.*_x86_64.*Name:\\s+"+prodName+"_.*_x86_64.*Package Count:.*";
		Assert.assertTrue(res.getStdout().replaceAll("\n", "").matches(REGEXP_PRODUCT_LIST_X86_64),
				"Repo list by environment - should contain info");
		// Assertions - product list by env
		res = clienttasks.run_cliCmd(String.format(IKatelloProduct.LIST_BY_ENV,this.org_name,envName_dev));
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product list --environment)");
		String REGEXP_PRODUCT_LIST = ".*Name:\\s+"+prodName+".*Provider Name:\\s+"+this.prov_name+".*";
		Assert.assertTrue(res.getStdout().replaceAll("\n", "").matches(REGEXP_PRODUCT_LIST), 
				"List should contain info about product (requested by: environment)");
		
		// Final action - DELETE the product
		// ... but get its id first. To check the output string.
		res = clienttasks.run_cliCmd(String.format(IKatelloProduct.STATUS,this.org_name,prodName));
		String prodId = KatelloCliTasks.grepCLIOutput("Id", res.getStdout());
		res = clienttasks.run_cliCmd(String.format(IKatelloProduct.DELETE,this.org_name,prodName));
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product create)");
		Assert.assertTrue(res.getStdout().trim().contains(String.format(IKatelloProduct.OUT_DELETED,prodId)), "Check - returned output string (product delete)");
		
		// Assertions - product list of the org
		res = clienttasks.run_cliCmd(String.format(IKatelloProduct.LIST_BY_PROVIDER,this.org_name,this.prov_name));
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product list --provider)");
		REGEXP_PRODUCT_LIST = ".*Name:\\s+"+prodName+".*Provider Name:\\s+"+this.prov_name+".*";
		Assert.assertFalse(res.getStdout().replaceAll("\n", "").matches(REGEXP_PRODUCT_LIST), 
				"Check - list should NOT contain info about product (deleted already)");
		
		// Assertions - repo list by product
		res = clienttasks.run_cliCmd(String.format(IKatelloRepo.LIST_BY_PRODUCT,this.org_name,prodName));
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (repo list --product)"); // Bug#750464
		Assert.assertTrue(res.getStdout().trim().equals(String.format(IKatelloProduct.ERR_COULD_NOT_FIND_PRODUCT, prodName,org_name)), "Check - `repo list --product` output string");
		
		// Assertions - repo list by env.
		res = clienttasks.run_cliCmd(String.format(IKatelloRepo.LIST_BY_ENVIRONMENT,this.org_name,envName_dev));
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (repo list --environment)");
		String REGEXP_NOREPO = ".*Id:\\s+.*Name:\\s+.*Package Count:\\s+\\d+.*";
		Assert.assertFalse(res.getStdout().replaceAll("\n", "").matches(REGEXP_NOREPO), "Check - `repo list --environment` output string");
		
		// Assertions - product list of env.
		res = clienttasks.run_cliCmd(String.format(IKatelloProduct.LIST_BY_ENV,this.org_name,envName_dev));
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (product list --environment)");
		REGEXP_PRODUCT_LIST = ".*Name:\\s+"+prodName+".*Provider Name:\\s+"+this.prov_name+".*";
		Assert.assertFalse(res.getStdout().replaceAll("\n", "").matches(REGEXP_PRODUCT_LIST), 
				"Check - list should NOT contain info about product (deleted already)");		
	}
}
