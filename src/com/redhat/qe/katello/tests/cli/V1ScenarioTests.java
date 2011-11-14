package com.redhat.qe.katello.tests.cli;

import java.util.logging.Logger;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import com.redhat.qe.auto.testng.Assert;
import com.redhat.qe.katello.base.IKatelloOrg;
import com.redhat.qe.katello.base.IKatelloProduct;
import com.redhat.qe.katello.base.IKatelloProvider;
import com.redhat.qe.katello.base.IKatelloRepo;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.base.KatelloTestScript;
import com.redhat.qe.katello.common.KatelloInfo;
import com.redhat.qe.tools.SSHCommandResult;

public class V1ScenarioTests extends KatelloCliTestScript{
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
//	 System.setProperty("katello.cli.reuseSystem", "true");  // TODO - /me needs to be commented.
}
	protected static Logger log = 
		Logger.getLogger(V1ScenarioTests.class.getName());
	
	private SSHCommandResult exec_result;

	@BeforeSuite(description="Prepare rhsm")
	public void init_rhsm(){
		/* 
		 * Once tried on F15 - there was some error on exit code:
		 * --- 
		 * subscription-manager register --username admin --password admin --org
		 * <org> --environment <env> --name `hostname` --force
		 * The system has been registered with id:
		 * bf624394-4ce1-4792-8b0e-de858c920c0c
		 * org.freedesktop.DBus.Error.Spawn.ChildExited: Launch helper exited with 
		 * unknown return code 1
		 * ---
		 *   
		 */		
		// we need to run on RHEL6 now.
		if(!CLIENT_PLATFORMS_ALLOWED[getClientPlatformID()][0].contains("redhat-6")){
			String err = "RHSM tasks need in having RHEL6. " +
					"Please adjust: [katello.client.hostname] properly.";
			log.severe(err);
			Assert.assertTrue(false, "RHSM client running on proper platform"); // raise error
		}
		
		String reuseSystem = System.getProperty("katello.cli.reuseSystem", "false");
		if(reuseSystem.equalsIgnoreCase("true")){
			enableRhsmYumPlugin();// Enable the rhsm yum plugin.
			return;
		}

		String servername = KatelloInfo.getInstance().getServername();
		clienttasks.execute_remote("yum -y erase python-rhsm subscription-manager; rm -rf /etc/rhsm/*;");
		exec_result = clienttasks.execute_remote("yum -y install python-rhsm subscription-manager");
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "RHSM packages should get installed");

		// some assertions - we need to assure we are running the version we need!
		exec_result = clienttasks.execute_remote("subscription-manager register --help");
		Assert.assertTrue(exec_result.getStdout().contains("--org=ORG"), "Check: subscription-manager --org");
		Assert.assertTrue(exec_result.getStdout().contains("--environment=ENVIRONMENT"), "Check: subscription-manager --environment");
		
		clienttasks.execute_remote("sed -i \"s/hostname = subscription.rhn.redhat.com/" +
				"hostname = "+servername+"/g\" /etc/rhsm/rhsm.conf");
		clienttasks.execute_remote("sed -i \"s/prefix = \\/subscription/prefix = \\/katello\\/api/g\" /etc/rhsm/rhsm.conf");
		clienttasks.execute_remote("sed -i \"s/baseurl= https:\\/\\/cdn.redhat.com/" +
				"baseurl=https:\\/\\/"+servername+"\\/pulp\\/repos\\//g\" /etc/rhsm/rhsm.conf");
		clienttasks.execute_remote("sed -i \"s/repo_ca_cert = %(ca_cert_dir)sredhat-uep.pem/" +
				"repo_ca_cert = %(ca_cert_dir)scandlepin-ca.pem/g\" /etc/rhsm/rhsm.conf");
		clienttasks.execute_remote("sed -i '/sslcacert=\\/etc\\/pki\\/CA\\/certs/ d' /etc/yum.conf");
		clienttasks.execute_remote("echo \"sslcacert=/etc/pki/CA/certs\" >> /etc/yum.conf");
		
		String candlepin_ca_crt;
		if(KATELLO_SERVERS_RHQE_CA_CRT.contains(servername)){
			exec_result = clienttasks.execute_remote("wget "+RHQE_CA_CERT+" -O /tmp/candlepin-ca.crt;");
			Assert.assertEquals(exec_result.getExitCode(), new Integer(0),"Check - return code");
			exec_result = clienttasks.execute_remote("cat /tmp/candlepin-ca.crt");
			candlepin_ca_crt = exec_result.getStdout().trim(); 
		}else{
			exec_result = servertasks.execute_remote("cat /etc/candlepin/certs/candlepin-ca.crt");
			candlepin_ca_crt = exec_result.getStdout().trim();
		}
		clienttasks.execute_remote("touch /etc/rhsm/ca/candlepin-ca.crt; echo -e \""+candlepin_ca_crt+"\" > /etc/rhsm/ca/candlepin-ca.crt");
		clienttasks.execute_remote("pushd /etc/rhsm/ca/; " +
				"openssl x509 -in candlepin-ca.crt -out candlepin-ca.der -outform DER; " +
				"openssl x509 -in candlepin-ca.der -inform DER -out candlepin-ca.pem -outform PEM; " +
				"popd;");		
	}

	@BeforeTest(description="Prepare client for RHSM - \"unregister it\"")
	public void setUp(){
		/* 
		 * Cleanup the files that make system registered through RHSM:
		 * ATTENTION: do this *only* if you know what you are doing there.
		 * 
		 * for further changes good to follow/adjust the progress of:
		 * strace -o /tmp/rhsm-unregister.log subscription-manager unregister
		 * grep "unlink(" in there to find out what are the files being removed.
		 * 
		 */
		clienttasks.execute_remote("rm -f /etc/pki/consumer/key.pem " +
				"/etc/pki/consumer/cert.pem " +
				"/var/lib/rhsm/packages/packages.json " +
				"/var/lib/rhsm/facts/facts.json " +
				"/var/lib/rhsm/cache/installed_products.json " +
				"/var/run/rhsm/cert.pid");
		clienttasks.execute_remote("service messagebus restart");
	}
	
	/**
	 * Scenario: Fetch Fedora15 content<BR>
	 *  - check repo is created<br>
	 *  - packages count >0<br>
	 *  - 
	 */
	@Test(description="V1 simple scenario")
	public void test_syncF15(){
		SSHCommandResult res;
		String uniqueID = KatelloTestScript.getUniqueID();
		String orgName = "orgF15-"+uniqueID;
		String provName = "provF15-"+uniqueID;
		String prodName = "prodF15-"+uniqueID;
		String repoName = "repoF15_x86_64-"+uniqueID;

		// create org
		res = clienttasks.run_cliCmd(String.format(IKatelloOrg.CREATE_NODESC,orgName));
		Assert.assertEquals(res.getExitCode().intValue(), 0, "Check - return code");
		// create provider
		res = clienttasks.run_cliCmd(String.format(IKatelloProvider.CREATE_NODESCRIPTION_NOURL, orgName,provName));
		Assert.assertEquals(res.getExitCode().intValue(), 0, "Check - return code");
		// create product
		res = clienttasks.run_cliCmd(String.format(IKatelloProduct.CREATE_NOURL, orgName,provName,prodName));
		Assert.assertEquals(res.getExitCode().intValue(), 0, "Check - return code");
		// create repo
		res = clienttasks.run_cliCmd(String.format(IKatelloRepo.CREATE, orgName,prodName,repoName,
				System.getProperty("katello.yumrepo.f15.x86_64",
						"http://download.eng.brq.redhat.com/pub/fedora/linux/releases/15/Fedora/x86_64/os/")));
		Assert.assertEquals(res.getExitCode().intValue(), 0, "Check - return code");
		log.fine("Initialize `repo synchronize` for repo: ["+repoName+"]");
		log.fine("Follow the process by: ["+String.format(IKatelloRepo.STATUS, orgName,prodName,repoName)+"]");
		clienttasks.run_cliCmd_nowait(String.format(IKatelloRepo.SYNCHRONIZE, orgName,prodName,repoName)); // initiate the sync.
		
		waitfor_reposync(orgName, prodName, repoName, 30);
	}
}
