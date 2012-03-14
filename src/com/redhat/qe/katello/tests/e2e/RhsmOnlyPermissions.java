package com.redhat.qe.katello.tests.e2e;

import java.util.logging.Logger;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import com.redhat.qe.auto.testng.Assert;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.base.KatelloTestScript;
import com.redhat.qe.katello.base.cli.KatelloEnvironment;
import com.redhat.qe.katello.base.cli.KatelloOrg;
import com.redhat.qe.katello.base.cli.KatelloPermission;
import com.redhat.qe.katello.base.cli.KatelloUser;
import com.redhat.qe.katello.base.cli.KatelloUserRole;
import com.redhat.qe.tools.SSHCommandResult;

/**
 * Implementing E2E scenario for the case when:<br>
 * Admin registers a user called: rhsm-only giving RHSM systems full permission for specific environment only.<br>
 * This user should be able to:<br>
 * 1. register systems for that environment<br>
 * 2. Request systems related calls (list, info, delete) for that env.<br>
 * 3. everything else should be prohibited.
 * @author gkhachik
 *
 */
public class RhsmOnlyPermissions extends KatelloCliTestScript{
	private static Logger log = Logger.getLogger(RhsmOnlyPermissions.class.getName());

	String org;
	private String env_dev;
	private String env_test;
	private String user;
	private String user_role;
	private String system;

	@BeforeTest(description="Init org/env", alwaysRun=true)
	public void setUp(){
		String uid = KatelloTestScript.getUniqueID();
		this.env_dev = "Dev-"+uid;
		this.env_test = "Test-"+uid;
		this.user = "usr"+uid;
		this.user_role = "Full RHSM "+uid;
		this.system = "systemof-"+this.user;

		this.org = "org-RHSM-only-"+uid;
		KatelloOrg org = new KatelloOrg(clienttasks, this.org, null);
		org.create();
		KatelloEnvironment env = new KatelloEnvironment(clienttasks, this.env_dev, null, this.org, KatelloEnvironment.LIBRARY);
		env.create();
		env = new KatelloEnvironment(clienttasks, this.env_test, null, this.org, KatelloEnvironment.LIBRARY);
		env.create();		
	}
	
	@Test(description="Create user & user_role", enabled=true)
	public void test_createUserAndRole(){
		log.info("Preparing: user, user_role");
		
		KatelloUser user = new KatelloUser(clienttasks, this.user, "root@localhost", KatelloUser.DEFAULT_USER_PASS, false);
		user.create();
		user.asserts_create();
		KatelloUserRole role = new KatelloUserRole(clienttasks, this.user_role, "Full RHSM access for an env. scope");
		SSHCommandResult res = role.create();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (user_role create)");
	}
	
	@Test(description="Create permission and assign to user", dependsOnMethods={"test_createUserAndRole"}, enabled=true)
	public void test_permissionAssign(){
		KatelloPermission perm = new KatelloPermission(clienttasks, this.user_role, this.org, "environments", this.env_dev, 
				"update_systems,read_contents,read_systems,register_systems,delete_systems", this.user_role);
		perm.create();
		KatelloUser user = new KatelloUser(clienttasks, this.user, null, null, false);
		SSHCommandResult res = user.assign_role(this.user_role);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (user assign_role)");
		Assert.assertTrue(res.getStdout().trim().equals(
				String.format("User '%s' assigned to role '%s'",this.user, this.user_role)), 
				"Check - return code (user assign_role)");
	}
	
	@Test(description="Register user", dependsOnMethods={"test_permissionAssign"}, enabled=true)
	public void test_rhsmRegisterSystem(){
		rhsm_clean();
		String cmd = String.format(
				"subscription-manager register --username %s --password %s --org \"%s\" --environment \"%s\" --name \"%s\"",
				this.user,KatelloUser.DEFAULT_USER_PASS,org,this.env_dev,this.system);
		SSHCommandResult res = clienttasks.execute_remote(cmd);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (rhsm register)");
		Assert.assertTrue(res.getStdout().trim().contains("The system has been registered"), "Check - message (registered)");
	}
}
