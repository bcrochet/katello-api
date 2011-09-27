package com.redhat.qe.katello.tests.cli;

import java.util.Vector;

import com.redhat.qe.auto.testng.Assert;
import org.testng.annotations.Test;

import com.redhat.qe.katello.base.KatelloCliDataProvider;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.base.KatelloTestScript;
import com.redhat.qe.tools.SSHCommandResult;

public class OrgTests extends KatelloCliTestScript{
	Vector<String[]> orgs;
	@Test(groups = {"cli-org"}, 
			description = "List all orgs - ACME_Corporation should be there")
	public void test_listOrgs_ACME_Corp(){
		SSHCommandResult res = clienttasks.run_cliCmd("org list");
		Assert.assertEquals(res.getExitCode().intValue(), 0, "Check - return code");
		Assert.assertTrue(res.getStdout().contains(ACME_ORG), "Check - contains: ["+ACME_ORG+"]");
	}
	
	@Test(groups = {"cli-org"}, 
			description = "Create org - different variations",
			dataProviderClass = KatelloCliDataProvider.class,
			dataProvider = "org_create")
	public void test_createOrg(String name, String descr){
		String cmd = "org create";
		if(name != null || descr != null){
			if (name != null)
				cmd = cmd +" --name "+name;
			if (descr != null)
				cmd = cmd +" --description "+descr;
		}
		SSHCommandResult res = clienttasks.run_cliCmd(cmd);
		Assert.assertEquals(res.getExitCode().intValue(), 0, "Check - return code");
		if(this.orgs ==null){
			this.orgs = new Vector<String[]>();
		}
		String[]org = new String[2];org[0]=name;org[1]=descr;
		this.orgs.add(org);
	}
	
	@Test(groups = {"cli-org"}, description = "List orgs - created", 
			dependsOnMethods={"test_createOrg"})
	public void test_infoListOrg(){
		String REG_ORG_LIST = "^\\s\\d+\\s+%s\\s+%s\\s*";
		String cmd_list = "org list";
		
		SSHCommandResult res = clienttasks.run_cliCmd(cmd_list);
		Assert.assertEquals(res.getExitCode().intValue(), 0, "Check - return code");
		
		String[] lines = res.getStdout().trim().split("\n");
		String name, descr, match_list;
		for(int i=0;i<this.orgs.size();i++){
			name = this.orgs.elementAt(i)[0];
			descr = this.orgs.elementAt(i)[1];
			if(descr ==null) descr = "None";
			boolean found = false; int j=0;
			match_list = String.format(REG_ORG_LIST,name,descr).replaceAll("\"", "");
			log.finest(String.format("Org (list) match regex: [%s]",match_list));
			while (!found && j<lines.length){
				found = lines[j].matches(match_list);
				j++;
			}
			Assert.assertTrue(found, String.format("Org [%s] should be found in the result list",name));
			
			assert_orgInfo(name, descr); // Assertions - `org info --name %s` 
		}
	}
	
	@Test(description="Update org's description", groups = {"cli-org"})
	public void test_updateOrg(){
		String uniqueID = KatelloTestScript.getUniqueID();
		String org_name = "orgUpd"+uniqueID;
		String org_descr = "Simple description";
		SSHCommandResult res = clienttasks.run_cliCmd(
				String.format("org create --name %s --description \"%s\"",org_name, org_descr));
		Assert.assertEquals(res.getExitCode().intValue(), 0, "Check - return code");
		org_descr = String.format("Updated %s",org_descr);
		res = clienttasks.run_cliCmd(String.format("org update --name %s --description \"%s\"",
				org_name, org_descr));
		Assert.assertEquals(res.getExitCode().intValue(), 0, "Check - return code");
		Assert.assertEquals(res.getStdout().trim(), String.format("Successfully updated org [ %s ]",org_name));
		
		// TODO - Enter special characters - check it works. 您好
		// BZ: https://bugzilla.redhat.com/show_bug.cgi?id=741274
		assert_orgInfo(org_name, org_descr);
	}
	
	@Test(description="Delete an organization", groups = {"cli-org"})
	public void test_deleteOrg(){
		String uniqueID = KatelloTestScript.getUniqueID();
		String orgName = "orgDel"+uniqueID;
		clienttasks.run_cliCmd(String.format("org create --name %s",orgName));
		SSHCommandResult res = clienttasks.run_cliCmd(String.format(
				"org delete --name %s",orgName));
		Assert.assertEquals(res.getExitCode().intValue(), 0, "Check - return code");
		Assert.assertEquals(res.getStdout().trim(), String.format("Successfully deleted org [ %s ]",orgName));
		
		res = clienttasks.run_cliCmd(String.format("org info --name %s",orgName));
		Assert.assertEquals(res.getExitCode(), new Integer(148),"Check - return code [148]");
		Assert.assertEquals(res.getStderr().trim(), 
				String.format("Couldn't find organization '%s',",orgName));
	}
	
	/**
	 * Assertions on org to check its existence in the `org info --name %s` call.   
	 * @param orgName Organization name 
	 * @param orgDescription Organization description
	 */
	private void assert_orgInfo(String orgName, String orgDescription){
		String REG_ORG_INFO = ".*Id:\\s+\\d+.*Name:\\s+%s.*Description:.*%s.*";
		SSHCommandResult res;
		// Check - org info --name %s
		String match_info = String.format(REG_ORG_INFO,orgName,orgDescription).replaceAll("\"", "");
		res = clienttasks.run_cliCmd(String.format("org info --name %s",orgName));
		Assert.assertEquals(res.getExitCode().intValue(), 0, "Check - return code");
		log.finest(String.format("Org (info) match regex: [%s]",match_info));
		// we could replace the \n there - make regex work there.
		// Not interested in new lines at all :)
		Assert.assertTrue(res.getStdout().replaceAll("\n", "").matches(match_info), 
				String.format("Org [%s] should be found in the result info",orgName));		
	}
}
