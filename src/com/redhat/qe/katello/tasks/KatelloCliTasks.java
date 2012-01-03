package com.redhat.qe.katello.tasks;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.testng.Assert;
import com.redhat.qe.katello.base.KatelloTestScript;
import com.redhat.qe.katello.common.KatelloConstants;
import com.redhat.qe.tools.ExecCommands;
import com.redhat.qe.tools.SSHCommandResult;
import com.redhat.qe.tools.SSHCommandRunner;

/**
 * Various utility tasks regarding Katello CLI.
 * @author gkhachik
 * @since 02.Sep.2011
 *
 */
public class KatelloCliTasks implements KatelloConstants{
	protected static Logger log = Logger.getLogger(KatelloCliTasks.class.getName());
	private SSHCommandRunner sshCommandRunner = null;
	
// # ************************************************************************* #
// # PUBLIC section                                                            #
// # ************************************************************************* #	
	public KatelloCliTasks(SSHCommandRunner sshRunner, ExecCommands localRunner) {
		setSSHCommandRunner(sshRunner);
	}

	public void setSSHCommandRunner(SSHCommandRunner runner) {
		sshCommandRunner = runner;
	}
		
	public SSHCommandResult execute_remote(String command){
		SSHCommandResult cmd_res = this.sshCommandRunner.runCommandAndWait(command);
		return cmd_res;
	}
	
	public SSHCommandResult run_cliCmd(String katelloCliCommand){
		return this.sshCommandRunner.runCommandAndWait(
				"katello --username admin --password admin "+katelloCliCommand);
	}
	
	public void run_cliCmd_nowait(String katelloCliCommand){
		this.sshCommandRunner.runCommand("katello --username admin --password admin "+katelloCliCommand+"&");
	}
	
	
	public void config_cli(String servername){
		SSHCommandResult ssh_res;

		// Exit if we want to reuse the system. For DEBUG only.
		String reuseSystem = System.getProperty("katello.cli.reuseSystem", "false");
		if(reuseSystem.equalsIgnoreCase("true")){
			return;
		}
				
		// Install Katello yum repo under /etc/yum.repos.d/
		installKatelloRepo();

		// Clean up yum caches, install wget (if not installed)
		execute_remote("yum clean all"); // cleanup the caches
		if(execute_remote("rpm -q wget").getExitCode().intValue()!=0)
			execute_remote("yum -y install wget");

		execute_remote("yum repolist && yum -y erase katello-cli* katello-agent"); // listing repos is needed, gets the repodata
		execute_remote("rm -f /etc/katello/client* /etc/gofer/plugins/katelloplugin*"); // remove config files
		
		ssh_res = execute_remote("yum -y install katello-cli* katello-agent");
		Assert.assertTrue(ssh_res.getExitCode().intValue()==0, 
				"Check: return code is 0");
		ssh_res = execute_remote("rpm -q katello-cli katello-agent");
		Assert.assertEquals(ssh_res.getExitCode(), new Integer(0), 
				"Check: return code of `rpm -q katello-cli katello-agent`");
		
		execute_remote("sed -i \"s/url\\s*=.*/"+
				"url=tcp:\\/\\/\\$\\(host):5672"+
				"/g\" "+KATELLO_AGENT_CONFIG);
	}
	
	public void config_rhsm(String servername, String candlepin_ca_crt){
		SSHCommandResult exec_result;

		// Cleanup the previous registration
		execute_remote("subscription-manager clean"); // cleanup previous registration craps
		
		// Exit if we want to reuse the system. For DEBUG only.
		String reuseSystem = System.getProperty("katello.cli.reuseSystem", "false");
		if(reuseSystem.equalsIgnoreCase("true")){
			return;
		}
		
		// Remove possible old version of RHSM, install new
		execute_remote("wget -O /etc/yum.repos.d/epel-subscription-manager.repo http://repos.fedorapeople.org/repos/candlepin/subscription-manager/epel-subscription-manager.repo");
		execute_remote("yum -y erase python-rhsm subscription-manager; rm -rf /etc/rhsm/* /etc/yum.repos.d/redhat.repo");
		exec_result = execute_remote("yum -y install python-rhsm subscription-manager");
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "RHSM packages should get installed");

		// Configure
		execute_remote("sed -i \"s/hostname = subscription.rhn.redhat.com/" +
				"hostname = "+servername+"/g\" /etc/rhsm/rhsm.conf");
		execute_remote("sed -i \"s/prefix = \\/subscription/prefix = \\/katello\\/api/g\" /etc/rhsm/rhsm.conf");
		execute_remote("sed -i \"s/baseurl= https:\\/\\/cdn.redhat.com/" +
				"baseurl=https:\\/\\/"+servername+"\\/pulp\\/repos\\//g\" /etc/rhsm/rhsm.conf");
		execute_remote("sed -i \"s/repo_ca_cert = %(ca_cert_dir)sredhat-uep.pem/" +
				"repo_ca_cert = %(ca_cert_dir)scandlepin-local.pem/g\" /etc/rhsm/rhsm.conf");
		execute_remote("touch /etc/rhsm/ca/candlepin-local.pem; echo -e \""+candlepin_ca_crt+"\" > /etc/rhsm/ca/candlepin-local.pem");		
	}

	public static String run_local(boolean showLogResults, String command){
		String out = null; String tmp_cmdFile = "/tmp/katello-"+KatelloTestScript.getUniqueID()+".sh";
		ExecCommands localRunner = new ExecCommands();
		try{
			// cleanup the running buffer file - in case it would exist
			localRunner.submitCommandToLocalWithReturn(false, 
					"rm -f "+tmp_cmdFile,"");
			FileOutputStream fout = 
				new FileOutputStream(tmp_cmdFile);
			fout.write((command+"\n").getBytes());fout.flush();fout.close();
			log.finest(String.format("Executing local: [%s]",command));
			out = localRunner.submitCommandToLocalWithReturn(
					false, "sh "+tmp_cmdFile, ""); // HERE is the run
			
			if(showLogResults){ // log output if specified so.
				// split the lines and out each line.
				String[] split = out.split("\\n");
				for(int i=0;i<split.length;i++){
					log.info("Output: "+split[i]);
				}
			}
		}catch(IOException iex){
			log.log(Level.SEVERE, iex.getMessage(), iex);
		}finally{
			// cleanup the running buffer file.
			try{localRunner.submitCommandToLocalWithReturn(false, 
					"rm -f "+tmp_cmdFile,"");
			}catch(IOException ie){log.log(Level.SEVERE, ie.getMessage(), ie);}
		}
		return out;
	}
	
	public static String grepCLIOutput(String property, String output){
		return grepCLIOutput(property, output, 1);
	}
	
	public static String grepCLIOutput(String property, String output, int occurence){
		int meet_cnt = 0;
		String[] lines = output.split("\\n");
		for(int i=0;i<lines.length;i++){
			if(lines[i].startsWith(property)){ // our line
				meet_cnt++;
				if(meet_cnt == occurence){
					String[] split = lines[i].split(":\\s+");
					if(split.length<2){
						return lines[i+1].trim();
					}else{
						return split[1].trim();
					}
				}
			}
		}
		log.severe("ERROR: Output can not be extracted for the property: ["+property+"]");
		return null;
	}
	
	private int getClientPlatformID(){		
		String platform = execute_remote("python -c 'from platform import platform; print platform();'").getStdout();
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
	
	private void installKatelloRepo(){
		int platform_id = getClientPlatformID(); // or exit if unsupported.
		if(CLIENT_PLATFORMS_ALLOWED[platform_id][0].contains("fedora")){ // Fedora
			execute_remote(String.format("" +
					"wget -O /etc/yum.repos.d/fedora-katello.repo %s",YUM_REPO_FEDORA_KATELLO));
		}
		if(CLIENT_PLATFORMS_ALLOWED[platform_id][0].contains("redhat")){ // RHEL
			execute_remote(String.format("" +
					"wget -O /etc/yum.repos.d/epel-katello.repo %s",YUM_REPO_RHEL_KATELLO));
		}
	}
	
}
