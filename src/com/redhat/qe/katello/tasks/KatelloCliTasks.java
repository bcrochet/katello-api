package com.redhat.qe.katello.tasks;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.testng.Assert;

import com.redhat.qe.katello.base.KatelloTestScript;
import com.redhat.qe.katello.common.KatelloConstants;
import com.redhat.qe.katello.common.KatelloInfo;
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

		// Disable the rhsm redhat.repo from being recreated - it fails yum to work properly.
		execute_remote("rm -f /etc/yum.repos.d/redhat.repo");
		disableRhsmYumPlugin();

		String reuseSystem = System.getProperty("katello.cli.reuseSystem", "false");
		if(reuseSystem.equalsIgnoreCase("true")){
			return;
		}
				
		execute_remote("yum clean all"); // cleanup the caches
		execute_remote("yum -y install wget");

		if(CLIENT_PLATFORMS_ALLOWED[platform_id][0].contains("fedora")){ // Fedora
			execute_remote(String.format("" +
					"wget -O /etc/yum.repos.d/fedora-katello.repo %s",YUM_REPO_FEDORA_KATELLO));
		}
		if(CLIENT_PLATFORMS_ALLOWED[platform_id][0].contains("redhat")){ // RHEL
			execute_remote(String.format("" +
					"wget -O /etc/yum.repos.d/epel-katello.repo %s",YUM_REPO_RHEL_KATELLO));
		}

		execute_remote("yum repolist && yum -y erase katello-cli*"); // listing repos is needed, gets the repodata
		execute_remote("rm -f /etc/katello/client*"); // all kinda katello client configs
		
		ssh_res = execute_remote("yum -y install katello-cli*");
		Assert.assertEquals(ssh_res.getExitCode(), new Integer(0), 
				"Check: return code is 0");
		ssh_res = execute_remote("rpm -q katello-cli");
		Assert.assertEquals(ssh_res.getExitCode(), new Integer(0), 
				"Check: return code of `rpm -q katello-cli`");
		
		execute_remote("sed -i \"s/localhost.localdomain/"+
				System.getProperty("katello.server.hostname", "localhost")+
				"/g\" "+KATELLO_CLI_CLIENT_CONFIG);
	}
	
	public void config_rhsm(String servername, String candlepin_ca_crt){
		SSHCommandResult exec_result;
		execute_remote("subscription-manager clean"); // cleanup previous registration craps
		execute_remote("wget -O /etc/yum.repos.d/epel-subscription-manager.repo http://repos.fedorapeople.org/repos/candlepin/subscription-manager/epel-subscription-manager.repo");
		execute_remote("yum -y erase python-rhsm subscription-manager; rm -rf /etc/rhsm/* /etc/yum.repos.d/redhat.repo");
		exec_result = execute_remote("yum -y install python-rhsm subscription-manager");
		Assert.assertEquals(exec_result.getExitCode().intValue(), 0, "RHSM packages should get installed");

		// some assertions - we need to assure we are running the version we need!
		exec_result = execute_remote("subscription-manager register --help");
		Assert.assertTrue(exec_result.getStdout().contains("--org=ORG"), "Check: subscription-manager --org");
		Assert.assertTrue(exec_result.getStdout().contains("--environment=ENVIRONMENT"), "Check: subscription-manager --environment");
		
		execute_remote("sed -i \"s/hostname = subscription.rhn.redhat.com/" +
				"hostname = "+servername+"/g\" /etc/rhsm/rhsm.conf");
		execute_remote("sed -i \"s/prefix = \\/subscription/prefix = \\/katello\\/api/g\" /etc/rhsm/rhsm.conf");
		execute_remote("sed -i \"s/baseurl= https:\\/\\/cdn.redhat.com/" +
				"baseurl=https:\\/\\/"+servername+"\\/pulp\\/repos\\//g\" /etc/rhsm/rhsm.conf");
		execute_remote("sed -i \"s/repo_ca_cert = %(ca_cert_dir)sredhat-uep.pem/" +
				"repo_ca_cert = %(ca_cert_dir)scandlepin-local.pem/g\" /etc/rhsm/rhsm.conf");
		execute_remote("touch /etc/rhsm/ca/candlepin-local.pem; echo -e \""+candlepin_ca_crt+"\" > /etc/rhsm/ca/candlepin-local.pem");		
	}

	protected void disableRhsmYumPlugin(){
		execute_remote("echo -e \"[main]\nenabled=0\" > /etc/yum/pluginconf.d/subscription-manager.conf");
	}
	protected void enableRhsmYumPlugin(){
		execute_remote("echo -e \"[main]\nenabled=1\" > /etc/yum/pluginconf.d/subscription-manager.conf");
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
	
}
