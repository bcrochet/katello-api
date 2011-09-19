package com.redhat.qe.katello.tests.cli;

import org.testng.annotations.Test;
import com.redhat.qe.auto.testng.Assert;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.base.KatelloTestScript;
import com.redhat.qe.tools.SSHCommandResult;

public class BPMTests extends KatelloCliTestScript{
static{
	/*
	 *  Setup in your Eclipse IDE:
	 *  	-Dkatello.cli.reuseSystem=true 
	 *  
	 *  This will give you chance not to execute the:
	 *  	KatelloCliTestScript.setUpKatelloCli();
	 *  
	 *	Or if you are lazy: uncomment the line below: but please push it back again.   
	 */
	// System.setProperty("katello.cli.reuseSystem", "true");  // TODO - /me needs to be commented.
}
	
	@Test(description="Create a new Org and create a user who can manage providers, systems and environments.")
	public void test_createOrgUser(){
		SSHCommandResult exec_result;
		String uid = KatelloTestScript.getUniqueID();
		String org_name = "orgBPM_"+uid;
		String user_name = "userBPMAdmin_"+uid;
		
		// Create org:
		exec_result = clienttasks.run_cliCmd("org create --name "+org_name+" --description \"BPM tests\"");
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		Assert.assertEquals(exec_result.getStdout().trim(), "Successfully created org [ "+org_name+" ]");
		// Create user:
		exec_result = clienttasks.run_cliCmd("user create --username "+user_name+" --password "+KATELLO_CLI_USER_DEFAULT_PASSWORD);
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code");
		Assert.assertEquals(exec_result.getStdout().trim(), "Successfully created user [ "+user_name+" ]");
	}
	
}
