package com.redhat.qe.katello.tasks;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
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
