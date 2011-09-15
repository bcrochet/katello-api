package com.redhat.qe.katello.tests.cli;

import com.redhat.qe.auto.testng.Assert;
import org.testng.annotations.Test;

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
}
