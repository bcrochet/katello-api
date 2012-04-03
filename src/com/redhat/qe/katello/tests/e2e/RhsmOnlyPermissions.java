package com.redhat.qe.katello.tests.e2e;

import java.util.logging.Logger;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import com.redhat.qe.auto.testng.Assert;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.base.KatelloTestScript;
import com.redhat.qe.katello.base.cli.KatelloChangeset;
import com.redhat.qe.katello.base.cli.KatelloEnvironment;
import com.redhat.qe.katello.base.cli.KatelloOrg;
import com.redhat.qe.katello.base.cli.KatelloPermission;
import com.redhat.qe.katello.base.cli.KatelloProduct;
import com.redhat.qe.katello.base.cli.KatelloProvider;
import com.redhat.qe.katello.base.cli.KatelloRepo;
import com.redhat.qe.katello.base.cli.KatelloSystem;
import com.redhat.qe.katello.base.cli.KatelloUser;
import com.redhat.qe.katello.base.cli.KatelloUserRole;
import com.redhat.qe.katello.tasks.KatelloCliTasks;
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
	private String prod;
	private String repo;

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
		
		log.info("Create RHSM full access permission and assign it to the user.");
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
		
		log.info("Register the system");
		rhsm_clean();
		String cmd = String.format(
				"subscription-manager register --username %s --password %s --org \"%s\" --environment \"%s\" --name \"%s\"",
				this.user,KatelloUser.DEFAULT_USER_PASS,org,this.env_dev,this.system);
		SSHCommandResult res = clienttasks.execute_remote(cmd);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (rhsm register)");
		Assert.assertTrue(res.getStdout().trim().contains("The system has been registered"), "Check - message (registered)");
	}
	
	@Test(description="Sync Zoo3 repo", dependsOnMethods={"test_rhsmRegisterSystem"}, enabled=true)
	public void test_syncZoo3(){
		String uid = KatelloTestScript.getUniqueID();
		String providerName = "Zoo3_"+uid; 
		this.prod = "Zoo 3 - "+uid;
		this.repo = "Zoo3-"+uid;

		log.info("Sync and promote Zoo3 to the dev env. Subscribe the system to it.");
		KatelloProvider prov = new KatelloProvider(clienttasks, providerName, this.org, null, null);
		prov.create();
		KatelloProduct prod = new KatelloProduct(clienttasks, this.prod, this.org, providerName, null, null, null, null, null);
		prod.create();
		KatelloRepo repo = new KatelloRepo(clienttasks, this.repo, this.org, this.prod, REPO_INECAS_ZOO3, null, null);
		repo.create();
		repo.synchronize();
		KatelloChangeset cs1 = new KatelloChangeset(clienttasks, "cs"+uid, this.org, this.env_dev);
		cs1.create();
		cs1.update_addProduct(this.prod);
		SSHCommandResult res = cs1.promote();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (changeset promote)");
	}
	
	@Test(description="Subscribe system to Zoo3", dependsOnMethods={"test_syncZoo3"}, enabled=true)
	public void test_subscribeSystemToZoo3(){
		
		log.info("Subscribing system to the pool of: Zoo3");
		KatelloSystem sys = new KatelloSystem(clienttasks, this.system, this.org, null);
		SSHCommandResult res = sys.subscriptions_available();
		String pool = KatelloCliTasks.grepCLIOutput("PoolId", res.getStdout().trim(),1);
		
		String cmd = "subscription-manager subscribe --pool "+pool;
		res = clienttasks.execute_remote(cmd);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (rhsm subscribe)");
		String MATCH_SUBSCRIBED = "Successfully.*"+pool+".*";
		Assert.assertTrue(res.getStdout().trim().matches(MATCH_SUBSCRIBED), "Check - message (subscribed)");
	}
	
	@Test(description="Yum operations", dependsOnMethods={"test_subscribeSystemToZoo3"}, enabled=true)
	public void test_yumOperations(){
		
		log.info("Checks on: yum repolist, packages count");
		SSHCommandResult res = clienttasks.execute_remote("yum repolist | grep \""+this.repo+"\"");
		Assert.assertFalse(res.getStdout().trim().equals(""), "Yum repolist contains the repo just subscribed");
		
		String sRev = new StringBuffer(res.getStdout().trim()).reverse().toString();
		String pkgCountRev = sRev.substring(0, sRev.indexOf(" ")+1);
		int pkgFromYum = Integer.parseInt(new StringBuffer(pkgCountRev).reverse().toString().trim());
		
		KatelloRepo repo = new KatelloRepo(clienttasks, this.repo, this.org, this.prod, null, null, null);
		res = repo.info(this.env_dev);
		int pkgFromKatello = Integer.parseInt(KatelloCliTasks.grepCLIOutput("Package Count", res.getStdout().trim()));
		
		Assert.assertTrue((pkgFromYum==pkgFromKatello), "Check: package counts for both yum and katello repo");
	}
}
