package com.redhat.qe.katello.tests.cli;

import com.redhat.qe.auto.testng.Assert;
import org.testng.annotations.Test;

import com.redhat.qe.katello.base.KatelloCliDataProvider;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.tools.SSHCommandResult;

public class OrgTests extends KatelloCliTestScript{

	@Test(groups = {"cli-org"}, 
			description = "List all orgs - ACME_Corporation should be there")
	public void test_listOrgs(){
		SSHCommandResult res = clienttasks.run_cliCmd("org list");
		Assert.assertEquals(res.getExitCode().intValue(), 0, "Check - return code");
		Assert.assertTrue(res.getStdout().contains(ACME_ORG), "Check - contains: ["+ACME_ORG+"]");
	}
	
	@Test(groups = {"cli-org"}, 
			description = "Create an org - different variations",
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
	}
	
	
	
}
