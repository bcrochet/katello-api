package com.redhat.qe.katello.tests.cli;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.testng.annotations.Test;

import com.google.inject.Inject;
import com.redhat.qe.Assert;
import com.redhat.qe.katello.base.KatelloApiException;
import com.redhat.qe.katello.base.KatelloCliDataProvider;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloProduct;
import com.redhat.qe.katello.base.obj.KatelloProvider;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.tools.SSHCommandResult;

public class OrgTests extends KatelloCliTestScript{
	List<KatelloOrg> orgs;
	
	@Inject
	@Test(description = "List all orgs - ACME_Corporation should be there",groups={"cfse-cli","headpin-cli"})	
	public void test_listOrgs_ACME_Corp(KatelloOrg acmeCorpOrg){
	    try {
            List<KatelloOrg> orgs = katelloTasks.getOrganizations();
            Assert.assertContains(orgs, acmeCorpOrg);
        } catch (KatelloApiException e) {
            e.printStackTrace();
            Assert.fail("Organization list could not be retrieved", e);
        }
	}
	
	@Test(description = "Create org - different variations",
			dataProviderClass = KatelloCliDataProvider.class,
			dataProvider = "org_create",groups={"cfse-cli","headpin-cli"})
	public void test_createOrg(String name, String descr){		
		KatelloOrg org = new KatelloOrg(name, descr);
		SSHCommandResult res = org.cli_create();
		
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code");
		if(this.orgs ==null){
			this.orgs = Collections.synchronizedList(new ArrayList<KatelloOrg>());
		}
		this.orgs.add(org);
	}
	
	@Test(description = "List orgs - created", 
			dependsOnMethods={"test_createOrg"},groups={"cfse-cli","headpin-cli"})
	public void test_infoListOrg(){
		String uniqueID = KatelloUtils.getUniqueID();
		KatelloOrg list_org = new KatelloOrg("orgUpd"+uniqueID, "Simple description");		
		list_org.cli_create();
		
		orgs.add(list_org);
		
		list_org = new KatelloOrg(null,null);
		SSHCommandResult res = list_org.cli_list();
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code (org list)");
		
		for(KatelloOrg org : orgs){
			if(org.description ==null) org.description = "None";
			String match_list = String.format(KatelloOrg.REG_ORG_LIST, org.name, org.description).replaceAll("\"", ""); // output not have '"' signs
			Assert.assertTrue(getOutput(res).replaceAll("\n", "").matches(match_list), "Check - org matches ["+org.name+"]");
			assert_orgInfo(org); // Assertions - `org info --name %s` 
		}
	}
	
	@Test(description="Update org's description",groups={"cfse-cli","headpin-cli"})
	public void test_updateOrg(){
		SSHCommandResult res;
		String uniqueID = KatelloUtils.getUniqueID();
		KatelloOrg org = new KatelloOrg("orgUpd"+uniqueID, "Simple description");		
		
		res = org.cli_create();
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code");
		String new_desc = String.format("Updated %s",org.description);
		res = org.update(new_desc);
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code");
		Assert.assertEquals(getOutput(res).trim(), String.format("Successfully updated org [ %s ]",org.name));
		
		org.description = "您好" + org.description;
		res = org.update(org.description);
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code");
		Assert.assertEquals(getOutput(res).trim(), String.format("Successfully updated org [ %s ]",org.name));
		
		res = org.cli_list();
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code (org list)");

		assert_orgInfo(org);
	}
	
	@Test(description="Delete an organization",groups={"cfse-cli","headpin-cli"})
	public void test_deleteOrg(){
		String uniqueID = KatelloUtils.getUniqueID();
		KatelloOrg org = new KatelloOrg("orgDel"+uniqueID, null);
		
		org.cli_create();
		SSHCommandResult res = org.delete();
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code");
		Assert.assertTrue(getOutput(res).contains(String.format("Successfully deleted org [ %s ]",org.name)),"Check - return string");
		
		res = org.cli_info();
		Assert.assertTrue(res.getExitCode() == 148, "Check - return code [148]");
		Assert.assertEquals(getOutput(res).trim(), 
				String.format(KatelloOrg.ERR_ORG_NOTFOUND,org.name));
	}
	
	@Test(description="Delete an organization which does not exist",groups={"cfse-cli","headpin-cli"})
	public void test_deleteOrgNotExist(){
		String uniqueID = KatelloUtils.getUniqueID();
		KatelloOrg org = new KatelloOrg("orgDel"+uniqueID, null);
		
		SSHCommandResult res = org.delete();
		Assert.assertTrue(res.getExitCode() == 148, "Check - return code [148]");
		Assert.assertEquals(getOutput(res).trim(), 
				String.format(KatelloOrg.ERR_ORG_NOTFOUND,org.name));
	}
	
	@Test(description="List org subscriptions.",groups={"cfse-cli"})
	public void test_orgSubscriptions(){
		String uniqueID = KatelloUtils.getUniqueID();
		String orgName = "subscriptions-" + uniqueID;
		KatelloOrg org = new KatelloOrg(orgName, null); // or you can provide null -> "some simple description here"
		SSHCommandResult res = org.cli_create();
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code (org create)");
		
		String  providerName = "provider" + uniqueID;
		KatelloProvider prov = new KatelloProvider(providerName, orgName, "Fedora provider", null);
		res = prov.create();
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code");
		
		String productName = "product" + uniqueID;
		KatelloProduct prod = new KatelloProduct(productName, orgName, providerName, null, null, null, null, null);
		res = prod.create();
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code");
		
		String productName1 = "product1" + uniqueID;
		prod = new KatelloProduct(productName1, orgName, providerName, null, null, null, null, null);
		res = prod.create();
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code");
		
		res = org.subscriptions();
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code (org subscriptions)"); // check: ($? is 0)
		
		Assert.assertTrue(getOutput(res).replaceAll("\n", "").matches(String.format(KatelloOrg.OUT_ORG_SUBSCR, productName)), "Check - Subscriptions contains " + productName);
		Assert.assertTrue(getOutput(res).replaceAll("\n", "").matches(String.format(KatelloOrg.OUT_ORG_SUBSCR, productName1)), "Check - Subscriptions contains " + productName1);
	}
	
	@Test(description = "Create org - existing",groups={"cfse-cli","headpin-cli"})
	public void test_createOrgExists(){
		String uniqueID = KatelloUtils.getUniqueID();
		KatelloOrg org = new KatelloOrg("orgCrt"+uniqueID, "Simple description");	
		org.cli_create();

		KatelloOrg org2 = new KatelloOrg("orgCrt"+uniqueID, "Simple description");	
		SSHCommandResult res = org2.cli_create();
		
		Assert.assertTrue(res.getExitCode() == 144, "Check - return code [144]");
		Assert.assertEquals(getOutput(res).trim(), 
				KatelloOrg.ERR_ORG_EXISTS);
	}
	
	@Test(description = "Create org - name is invalid",groups={"cfse-cli","headpin-cli"})
	public void test_createOrgInvalidName(){
		String uniqueID = KatelloUtils.getUniqueID();
		KatelloOrg org = new KatelloOrg("orgCrt"+uniqueID + " very ++== invalid name", "Simple description");	
		SSHCommandResult res = org.cli_create();
		
		Assert.assertTrue(res.getExitCode() == 144, "Check - return code [144]");
		Assert.assertEquals(getOutput(res).trim(), 
				KatelloOrg.ERR_NAME_INVALID);
	}
	
	private void assert_orgInfo(KatelloOrg org){
		
		SSHCommandResult res;
		res = org.cli_info();
		String match_info = String.format(KatelloOrg.REG_ORG_INFO,org.name,org.description).replaceAll("\"", "");
		Assert.assertTrue(res.getExitCode() == 0, "Check - return code");
		log.finest(String.format("Org (info) match regex: [%s]",match_info));
		Assert.assertTrue(getOutput(res).replaceAll("\n", "").matches(match_info), 
				String.format("Org [%s] should be found in the result info",org.name));		
	}
}
