package com.redhat.qe.katello.tasks;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.redhat.qe.katello.base.KatelloTestScript;
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
public class KatelloCliTasks {
	protected static Logger log = 
		Logger.getLogger(KatelloCliTasks.class.getName());
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
	
	public static String grepCLIOutput(String prop, String output){
		String[] split1 = output.split(prop+":\\s+");
		if(split1.length<2){
			log.severe("ERROR: Output can not be extracted for the property: ["+prop+"]");
			return null;
		}
		String[] split2 = split1[1].trim().split("\n");
		return split2[0].trim();
	}
}
