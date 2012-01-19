package com.redhat.qe.katello.tests.config;

import org.testng.Assert;
import org.testng.annotations.BeforeSuite;
import com.redhat.qe.katello.common.KatelloConstants;
import com.redhat.qe.katello.tasks.KatelloCliTasks;
import com.redhat.qe.katello.tasks.KatelloTasks;
import com.redhat.qe.tools.ExecCommands;
import com.redhat.qe.tools.SSHCommandResult;
import com.redhat.qe.tools.SSHCommandRunner;

public class InitKatelloCli extends com.redhat.qe.auto.testng.TestScript implements KatelloConstants{

	protected KatelloTasks servertasks	= null;
	protected KatelloCliTasks clienttasks = null;

	public InitKatelloCli() {
		super();
		try {
			SSHCommandRunner server_sshRunner = null;
			SSHCommandRunner client_sshRunner = null;
			try{
				server_sshRunner = new SSHCommandRunner(
						System.getProperty("katello.server.hostname", "localhost"), 
						System.getProperty("katello.ssh.user", "root"), 
						System.getProperty("katello.ssh.passphrase", "secret"), 
						System.getProperty("katello.sshkey.private", ".ssh/id_auto_dsa"), 
						System.getProperty("katello.sshkey.passphrase", "secret"), null);				
			}catch(Throwable t){
				log.warning("Warning: Could not initialize server's SSHCommandRunner.");
			}
			try{
				client_sshRunner = new SSHCommandRunner(
						System.getProperty("katello.client.hostname", "localhost"), 
						System.getProperty("katello.client.ssh.user", "root"), 
						System.getProperty("katello.client.ssh.passphrase", "secret"), 
						System.getProperty("katello.client.sshkey.private", ".ssh/id_auto_dsa"), 
						System.getProperty("katello.client.sshkey.passphrase", "secret"), null);				
			}catch(Throwable t){
				log.warning("Warning: Could not initialize client's SSHCommandRunner.");
			}

			ExecCommands localRunner = new ExecCommands();
			servertasks = new KatelloTasks(server_sshRunner, localRunner);
			clienttasks = new KatelloCliTasks(client_sshRunner, localRunner);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@BeforeSuite(description="Prepare katello client", alwaysRun = true)
	public void setUp(){
		SSHCommandResult exec_result;
		String kt_servername = System.getProperty("katello.server.hostname", "localhost");
		String candlepin_ca_crt;
		exec_result = servertasks.execute_remote("cat /etc/candlepin/certs/candlepin-ca.crt");
		candlepin_ca_crt = exec_result.getStdout().trim();
		
		// Ordering is important. First setup of RHSM should come.
		log.info("Setup / configure subscription-manager");
		config_rhsm(kt_servername, candlepin_ca_crt);
		log.info("Setup / configure katello cli");
		config_cli(kt_servername);
	}
	
	private void config_cli(String servername){
		SSHCommandResult ssh_res;

		// Exit if we want to reuse the system. For DEBUG only.
		String reuseSystem = System.getProperty("katello.cli.reuseSystem", "false");
		if(reuseSystem.equalsIgnoreCase("true")){
			return;
		}
				
		// Install Katello yum repo under /etc/yum.repos.d/
		installRepo_Katello();

		// Clean up yum caches, install wget (if not installed)
		clienttasks.execute_remote("yum clean all"); // cleanup the caches
		if(clienttasks.execute_remote("rpm -q wget > /dev/null").getExitCode().intValue()!=0)
			clienttasks.execute_remote("yum -y install wget");

		clienttasks.execute_remote("yum repolist && yum -y erase katello-cli katello-cli-common katello-agent gofer gofer-package"); // listing repos is needed, gets the repodata
		clienttasks.execute_remote("rm -f /etc/katello/client* /etc/gofer/plugins/katelloplugin*"); // remove config files
		
		ssh_res = clienttasks.execute_remote("yum -y install katello-cli katello-cli-common katello-agent");
		Assert.assertTrue(ssh_res.getExitCode().intValue()==0, 
				"Check: return code is 0");
		ssh_res = clienttasks.execute_remote("rpm -q katello-cli katello-agent");
		Assert.assertEquals(ssh_res.getExitCode(), new Integer(0), 
				"Check: return code of `rpm -q katello-cli katello-agent`");
		
		clienttasks.execute_remote("sed -i \"s/host\\s*=.*/" +
				"host = "+servername+"/g\" "+KATELLO_CLI_CLIENT_CONFIG);
		clienttasks.execute_remote("sed -i \"s/url\\s*=.*/"+
				"url=tcp:\\/\\/\\$\\(host):5672"+
				"/g\" "+KATELLO_AGENT_CONFIG);
	}
	
	private void config_rhsm(String servername, String candlepin_ca_crt){
		SSHCommandResult exec_result;

		// Cleanup the previous registration
		clienttasks.execute_remote("subscription-manager clean"); // cleanup previous registration craps
		
		// Exit if we want to reuse the system. For DEBUG only.
		String reuseSystem = System.getProperty("katello.cli.reuseSystem", "false");
		if(reuseSystem.equalsIgnoreCase("true")){
			return;
		}
		
		// Remove possible old version of RHSM, install new
		installRepo_RHSM();
		clienttasks.execute_remote("yum -y erase python-rhsm subscription-manager; rm -rf /etc/rhsm/* /etc/yum.repos.d/redhat.repo");
		exec_result = clienttasks.execute_remote("yum repolist; yum -y install python-rhsm subscription-manager");
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "RHSM packages should get installed");

		// Configure
		clienttasks.execute_remote("sed -i \"s/hostname = subscription.rhn.redhat.com/" +
				"hostname = "+servername+"/g\" /etc/rhsm/rhsm.conf");
		clienttasks.execute_remote("sed -i \"s/prefix = \\/subscription/prefix = \\/katello\\/api/g\" /etc/rhsm/rhsm.conf");
		clienttasks.execute_remote("sed -i \"s/baseurl= https:\\/\\/cdn.redhat.com/" +
				"baseurl=https:\\/\\/"+servername+"\\/pulp\\/repos\\//g\" /etc/rhsm/rhsm.conf");
		clienttasks.execute_remote("sed -i \"s/repo_ca_cert = %(ca_cert_dir)sredhat-uep.pem/" +
				"repo_ca_cert = %(ca_cert_dir)scandlepin-local.pem/g\" /etc/rhsm/rhsm.conf");
		clienttasks.execute_remote("touch /etc/rhsm/ca/candlepin-local.pem; echo -e \""+candlepin_ca_crt+"\" > /etc/rhsm/ca/candlepin-local.pem");		
	}
	
	private void installRepo_Katello(){
		int platform_id = getClientPlatformID(); // or exit if unsupported.
		if(CLIENT_PLATFORMS_ALLOWED[platform_id][0].contains("fedora")){ // Fedora
			clienttasks.execute_remote(String.format("" +
					"wget -O /etc/yum.repos.d/fedora-katello.repo %s",YUM_REPO_FEDORA_KATELLO));
		}
		if(CLIENT_PLATFORMS_ALLOWED[platform_id][0].contains("redhat")){ // RHEL
			clienttasks.execute_remote(String.format("" +
					"wget -O /etc/yum.repos.d/epel-katello.repo %s",YUM_REPO_RHEL_KATELLO));
		}
	}
	
	private void installRepo_RHSM(){
		int platform_id = getClientPlatformID(); // or exit if unsupported.
		if(CLIENT_PLATFORMS_ALLOWED[platform_id][0].contains("fedora")){ // Fedora
			clienttasks.execute_remote(String.format("" +
					"wget -O /etc/yum.repos.d/fedora-subscription-manager.repo %s",YUM_REPO_FEDORA_RHSM));
		}
		if(CLIENT_PLATFORMS_ALLOWED[platform_id][0].contains("redhat")){ // RHEL
			clienttasks.execute_remote(String.format("" +
					"wget -O /etc/yum.repos.d/epel-subscription-manager.repo %s",YUM_REPO_RHEL_RHSM));
		}		
	}

	private int getClientPlatformID(){		
		String platform = clienttasks.execute_remote("python -c 'from platform import platform; print platform();'").getStdout();
		int platform_id = -1;
		
		for(int i=0;i<CLIENT_PLATFORMS_ALLOWED.length;i++){
			if(platform.contains(CLIENT_PLATFORMS_ALLOWED[i][0])){
				platform_id = i;
				log.info(String.format("The client is running on platform: [%s]",CLIENT_PLATFORMS_ALLOWED[i][1]));
			}
		}
		if(platform_id == -1){
			log.severe(String.format("ERROR: Unsupported platform for katello client: [%s]",platform));
			System.exit(1);
		}
		return platform_id;
	}
}
