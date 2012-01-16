package com.redhat.qe.katello.tests.cli;

import java.util.logging.Logger;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.redhat.qe.auto.testng.Assert;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.base.KatelloTestScript;
import com.redhat.qe.katello.base.cli.KatelloEnvironment;
import com.redhat.qe.katello.base.cli.KatelloOrg;
import com.redhat.qe.katello.base.cli.KatelloSystem;
import com.redhat.qe.tools.SSHCommandResult;

public class SystemTests extends KatelloCliTestScript{
	static{
			 System.setProperty("katello.cli.reuseSystem", "true");  // TODO - /me needs to be commented.
	}
	
	protected static Logger log = 
		Logger.getLogger(SystemTests.class.getName());
	
	private SSHCommandResult exec_result;
	private String orgName;
	private String envName_Dev;
	private String envName_Test;
	
	@BeforeTest(description="Generate unique names")
	public void setUp(){
		String uid = KatelloTestScript.getUniqueID();
		this.orgName = "org-rhsm-"+uid;
		this.envName_Dev = "Dev-"+uid;
		this.envName_Test = "Test-"+uid;
		
		KatelloOrg org = new KatelloOrg(clienttasks, this.orgName, null);
		exec_result = org.create();
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "Check - return code (org create)");
		Assert.assertTrue(exec_result.getStdout().trim().equals(String.format(KatelloOrg.OUT_CREATE,this.orgName)),
				"Check - returned message");
	}
	
	@Test(description = "RHSM register - org have no environment but Locker only", enabled=true)
	public void test_rhsm_RegLockerOnly(){
		KatelloSystem sys = new KatelloSystem(clienttasks, "localhost.localadmin", this.orgName, null);
		exec_result = sys.rhsm_register(); 
		Assert.assertTrue(exec_result.getExitCode().intValue() == 255, "Check - return code");
		Assert.assertEquals(exec_result.getStderr().trim(), 
				String.format(KatelloSystem.ERR_RHSM_LOCKER_ONLY,this.orgName, KatelloEnvironment.LOCKER),
				"Check - please create an env.");
	}
	
	@Test(description = "RHSM register - one environment only", 
			dependsOnMethods = {"test_rhsm_RegLockerOnly"}, enabled=true)
	public void test_rhsm_RegOneEnvOnly(){
		String uid = KatelloTestScript.getUniqueID();
		String system = "rhsm-reg-"+uid;
		
		// Create the env.
		KatelloEnvironment env = new KatelloEnvironment(
				clienttasks, this.envName_Dev, null, this.orgName, KatelloEnvironment.LOCKER);
		env.create();		
		KatelloSystem sys = new KatelloSystem(clienttasks, system, this.orgName, null);
		exec_result = sys.rhsm_register(); 
		Assert.assertTrue(exec_result.getExitCode().intValue() == 0, "Check - return code");
		Assert.assertTrue(exec_result.getStdout().trim().contains("The system has been registered with id:"),
				"Check - output (success)");
	}
	
	
	@AfterTest(description="erase registration made; cleanup",alwaysRun=true)
	public void tearDown(){
		clienttasks.execute_remote("subscription-manager clean");
	}
}
