package com.redhat.qe.katello.tests.cli;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.redhat.qe.auto.testng.Assert;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.base.KatelloTestScript;
import com.redhat.qe.katello.base.cli.KatelloEnvironment;
import com.redhat.qe.katello.base.cli.KatelloFilter;
import com.redhat.qe.katello.base.cli.KatelloOrg;
import com.redhat.qe.tools.SSHCommandResult;

public class FilterTests extends KatelloCliTestScript{

	String org;
	String environment;
	
	@BeforeClass(description="init: create org stuff", groups = {"cli-filter"})
	public void setUp(){
		SSHCommandResult res;
		String uid = KatelloTestScript.getUniqueID();
		this.org = "filter-"+uid;
		this.environment = "Dev-"+uid;
		KatelloOrg org = new KatelloOrg(clienttasks, this.org, null);
		res = org.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (org create)");
		KatelloEnvironment env = new KatelloEnvironment(clienttasks, this.environment, null, this.org, KatelloEnvironment.LIBRARY);
		res = env.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (env create)");
	}

	@Test(description="Create filter: no packages", groups = {"cli-filter"})
	public void test_createWithoutPackages(){
		String filterName = "filter"+KatelloTestScript.getUniqueID();
		KatelloFilter filter = new KatelloFilter(clienttasks, filterName, this.org, null, null);
		SSHCommandResult res = filter.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (filter create)");
		Assert.assertTrue(res.getStdout().trim().equals(String.format(KatelloFilter.OUT_CREATE, filterName)), 
				"Check - output string (filter create)");
	}
}
