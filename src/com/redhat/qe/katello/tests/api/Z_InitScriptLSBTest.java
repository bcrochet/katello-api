package com.redhat.qe.katello.tests.api;

import java.util.logging.Logger;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.redhat.qe.auto.testng.Assert;
import com.redhat.qe.katello.base.KatelloTestScript;
import com.redhat.qe.tools.SCPTools;
import com.redhat.qe.tools.SSHCommandResult;

public class Z_InitScriptLSBTest extends KatelloTestScript{
	private static final String KATELLO_SERVICENAME = "katello";
	private static final String DAEMON_PID = "/usr/lib/katello/tmp/pids/server.pid";

	protected static Logger log = Logger.getLogger(Z_InitScriptLSBTest.class.getName());
	
	@BeforeClass
	public void setUp(){
		SCPTools scpRunner = new SCPTools(katelloInfo.getServername(),
				katelloInfo.getUsernameSSH(), 
				katelloInfo.getSshKeyPrivate(),
				katelloInfo.getPasswordSSH());

		scpRunner.sendFile("scripts/other/helper-katello.sh", "/tmp/");
		servertasks.execute_remote(String.format("service %s stop",KATELLO_SERVICENAME));
		servertasks.execute_remote("useradd testuserqa");
		servertasks.execute_remote(String.format("export KATELLO_HOME=/usr/lib/katello; service %s start",KATELLO_SERVICENAME));
		servertasks.execute_remote("pushd /tmp; chmod +x *.sh; " +
		". helper-katello.sh; waitfor_katello; popd");
	}
	
	@AfterClass(alwaysRun=true)
	public void tearDown(){
		servertasks.execute_remote("userdel -fr testuserqa");
		servertasks.execute_remote(String.format("service %s restart",KATELLO_SERVICENAME));
		servertasks.execute_remote("pushd /tmp; chmod +x *.sh; " +
		". helper-katello.sh; waitfor_katello; popd"); // the helper file should be there, see @BeforeClass
	}

	@Test(description="Katello service start", enabled=false) // BZ: https://bugzilla.redhat.com/show_bug.cgi?id=678090#c11
	public void test_serviceStart(){
		SSHCommandResult res;
		servertasks.execute_remote(String.format("service %s stop",KATELLO_SERVICENAME));
		res = servertasks.execute_remote(String.format("export KATELLO_HOME=/usr/lib/katello; service %s start",KATELLO_SERVICENAME));
		Assert.assertEquals(res.getExitCode(), new Integer(0),"Service must start without problem");
		res = servertasks.execute_remote(String.format("service %s status",KATELLO_SERVICENAME));
		Assert.assertEquals(res.getExitCode(), new Integer(0),"Then Status command");
		res = servertasks.execute_remote(String.format("export KATELLO_HOME=/usr/lib/katello; service %s start",KATELLO_SERVICENAME));
		Assert.assertEquals(res.getExitCode(), new Integer(0),"Already started service");
		res = servertasks.execute_remote(String.format("service %s status",KATELLO_SERVICENAME));
		Assert.assertEquals(res.getExitCode(), new Integer(0),"Again status command");		
	}
	
	@Test(description="Katello service restart")
	public void test_serviceRestart(){
		SSHCommandResult res;
		// restart service from the stopped mode
		servertasks.execute_remote(String.format("service %s stop",KATELLO_SERVICENAME));
		res = servertasks.execute_remote(String.format("service %s restart",KATELLO_SERVICENAME));
		Assert.assertEquals(res.getExitCode(), new Integer(0),"Restarting of service (from stopped mode)");
		res = servertasks.execute_remote(String.format("service %s status",KATELLO_SERVICENAME));
		Assert.assertEquals(res.getExitCode(), new Integer(0),"Status command");
		// restart service from the started mode
		servertasks.execute_remote(String.format("service %s stop",KATELLO_SERVICENAME));
		servertasks.execute_remote(String.format("export KATELLO_HOME=/usr/lib/katello; service %s start",KATELLO_SERVICENAME));
		res = servertasks.execute_remote(String.format("service %s restart",KATELLO_SERVICENAME));
		Assert.assertEquals(res.getExitCode(), new Integer(0),"Restarting of service (from started mode)");
		res = servertasks.execute_remote(String.format("service %s status",KATELLO_SERVICENAME));
		Assert.assertEquals(res.getExitCode(), new Integer(0),"Status command");
	}
	
	@Test(description="Katello service stop")
	public void test_serviceStop(){
		SSHCommandResult res;
		servertasks.execute_remote(String.format("service %s restart",KATELLO_SERVICENAME));
		
		res = servertasks.execute_remote(String.format("service %s stop",KATELLO_SERVICENAME));
		Assert.assertEquals(res.getExitCode(), new Integer(0),"Stopping service)");
		res = servertasks.execute_remote(String.format("service %s status",KATELLO_SERVICENAME));
		Assert.assertEquals(res.getExitCode(), new Integer(3),"Status of stopped service");
		res = servertasks.execute_remote(String.format("service %s stop",KATELLO_SERVICENAME));
		Assert.assertEquals(res.getExitCode(), new Integer(0),"Stopping service again)");
		res = servertasks.execute_remote(String.format("service %s status",KATELLO_SERVICENAME));
		Assert.assertEquals(res.getExitCode(), new Integer(3),"Status of stopped service");
	}
	
	@Test(description="Katello pid file", enabled=false) // BZ: https://bugzilla.redhat.com/show_bug.cgi?id=678090#c11
	public void test_pidFile(){
		SSHCommandResult res;
		servertasks.execute_remote(String.format("service %s restart",KATELLO_SERVICENAME));
		
		res = servertasks.execute_remote(String.format("ls %s",DAEMON_PID));
		Assert.assertEquals(res.getExitCode(), new Integer(0),String.format("Pid file [%s] must exist",DAEMON_PID));
		servertasks.execute_remote("killall -9 ruby");
		res = servertasks.execute_remote(String.format("service %s status",KATELLO_SERVICENAME));
		Assert.assertEquals(res.getExitCode(), new Integer(1),"Existing pid file, but service not started");
		servertasks.execute_remote(String.format("rm -fv %s",DAEMON_PID)); // remove the pid we left there.
	}
	
	@Test(description="Katello insufficient rights", enabled=false) // BZ: https://bugzilla.redhat.com/show_bug.cgi?id=678090#c11
	public void test_insuffRights(){
		SSHCommandResult res;
		servertasks.execute_remote(String.format("service %s restart",KATELLO_SERVICENAME));

		res = servertasks.execute_remote(String.format("su testuserqa -c 'service %s stop'",KATELLO_SERVICENAME));
		Assert.assertEquals(res.getExitCode(), new Integer(4),"Insufficient rights, stopping service under nonprivileged user must fail");
		
	}
	
	
}
