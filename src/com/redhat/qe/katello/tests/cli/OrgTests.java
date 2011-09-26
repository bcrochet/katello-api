package com.redhat.qe.katello.tests.cli;

import java.util.Vector;

import com.redhat.qe.auto.testng.Assert;
import org.testng.annotations.Test;

import com.redhat.qe.katello.base.KatelloCliDataProvider;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.tools.SSHCommandResult;

public class OrgTests extends KatelloCliTestScript{
	Vector<String[]> orgs;
//	@Test(groups = {"cli-org"}, 
//			description = "List all orgs - ACME_Corporation should be there")
//	public void test_listOrgs(){
//		SSHCommandResult res = clienttasks.run_cliCmd("org list");
//		Assert.assertEquals(res.getExitCode().intValue(), 0, "Check - return code");
//		Assert.assertTrue(res.getStdout().contains(ACME_ORG), "Check - contains: ["+ACME_ORG+"]");
//	}
//	
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
		String REG_ORG_INFO = ".*Id:\\s+\\d+.*Name:\\s+%s.*Description:.*%s.*";
		String cmd_list = "org list";
		String cmd_info = "org info --name %s";
		
		SSHCommandResult res = clienttasks.run_cliCmd(cmd_list);
		Assert.assertEquals(res.getExitCode().intValue(), 0, "Check - return code");
		
		String[] lines = res.getStdout().trim().split("\n");
		String name, descr, match_list, match_info;
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
			
			// Check - org info --name %s
			match_info = String.format(REG_ORG_INFO,name,descr).replaceAll("\"", "");
			res = clienttasks.run_cliCmd(String.format(cmd_info,name));
			Assert.assertEquals(res.getExitCode().intValue(), 0, "Check - return code");
			log.finest(String.format("Org (info) match regex: [%s]",match_info));
			// we could replace the \n there - make regex work there.
			// Not interested in new lines at all :)
			Assert.assertTrue(res.getStdout().replaceAll("\n", "").matches(match_info), 
					String.format("Org [%s] should be found in the result info",name));
		}
	}
	
	
	
}
