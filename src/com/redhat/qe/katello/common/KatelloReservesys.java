package com.redhat.qe.katello.common;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.redhat.qe.auto.testng.TestScript;
import com.redhat.qe.tools.ExecCommands;

public class KatelloReservesys {
	private Logger log = Logger.getLogger(KatelloReservesys.class.getName());
	private ExecCommands localCommandRunner = null;
	
	public static final String BKR_RESERVED = 
		"path=\"/distribution/reservesys\" result=\"Pass\"";
	public static final String HUDSON_BUFFER = 
		"/tmp/"+System.getenv("BUILD_TAG")+".beaker.info";
	public static final int WAIT_TIMEOUT = 60;
		
	public KatelloReservesys(){
		try {
			localCommandRunner = new ExecCommands();
		}
		catch (Exception e) {
			log.severe(e.getMessage());
			System.exit(1);
		}
	}

	/**
	 * <b>INPUT:</b><br>
	 * bkr workflow-simple<br> 
	 * --username=`System.getenv("BKR_USER")`<br> 
	 * --password=`System.getenv("BKR_PASS")` <br>
	 * --distro=Fedora-14 <br>
	 * --whiteboard="Katello system by Jenkins - [`System.getenv("BUILD_TAG")`]"<br> 
	 * --arch=x86_64 <br>
	 * --keyvalue="MEMORY>2300"<br> 
	 * --keyvalue="DISK>7999" <br>
	 * --task=/distribution/reservesys<br>
	 * <br>
	 * <b>OUTPUT:</b><br>
	 * Submitted: ['j:&lt;JOB_ID&gt;']
	 */
	public void doReserveBeakerSystem(){
		try{
			int sleepInt = 60000; // sleep 1 min
			String cmd_reservesys = 
				"bkr workflow-simple " +
				"--username="+System.getenv("BKR_USER")+"  " +
				"--password=\"*****\" " +
				"--distro=Fedora-14  " +
				"--whiteboard=\"Katello system by Jenkins - ["+System.getenv("BUILD_TAG")+"]\" " +
				"--arch=x86_64 " +
				"--keyvalue=\"MEMORY>2300\" " +
				"--keyvalue=\"DISK>7999\" " +
				"--task=/distribution/reservesys";
			log.finest("Requested: ["+cmd_reservesys+"]");
			cmd_reservesys = cmd_reservesys.replace(
					"*****", System.getenv("BKR_PASS"));
			String out = this.execute_local(false, cmd_reservesys);
			log.finest("Response: ["+out+"]");
			
			if(! out.startsWith("Submitted: ['j:")){
				log.severe("Failed to apply bkr workflow-simple <...>");
				System.exit(1);
			}
			
			String job_id = out.substring(
					out.indexOf("Submitted: ['j:")+"Submitted: ['j:".length(), 
					out.lastIndexOf("']"));
			int tick = 0;
			log.finest("Going to check status in each ["+(sleepInt/1000)+"] sec. max for ["+WAIT_TIMEOUT+"] tries");
			while(tick<WAIT_TIMEOUT){
				try{Thread.sleep(sleepInt);}catch(InterruptedException iex){}
				tick++;
				log.finest("Check status of reservation after: ["+tick+"] try(ies).");
				out = this.execute_local(false, "bkr job-results J:"+job_id);
				log.finest("Return from Beaker: ["+out+"]");
				// We are looking for:
				// ==> path="/distribution/reservesys" result="Pass" <==
				if(out.contains(BKR_RESERVED)){// Found - (happy)
					// let's grub the reserved hostname
					int ib = out.indexOf("system=\"");
					try{
						String bkr_system = out.substring(
								ib+"system=\"".length(),
								out.indexOf("\" variant=\"", ib));
						log.fine("Beaker system get reserved with:");
						// open a file and write down the info:
						BufferedWriter bw = new BufferedWriter(new FileWriter(HUDSON_BUFFER));
						log.fine("\tbeaker_job_id="+job_id);
						bw.write("beaker_job_id="+job_id+"\n");
						log.fine("\tbeaker_hostname="+bkr_system);
						bw.write("beaker_hostname="+bkr_system+"\n");
						log.fine("\tbeaker_distro_prep="+tick+"sec.");
						bw.write("beaker_distro_prep="+tick+"sec.\n");
						bw.close();
						log.fine("Put the info in: ["+HUDSON_BUFFER+"]");
						return;
					}catch(Exception ex){
						log.severe("Unable to parse Beaker system hostname: see logs to analyze the issue.");
						log.finest("Returning [J:"+job_id+"] to Beaker tool");
						execute_local(false, "bkr job-cancel J:"+job_id);
						System.exit(2);
					}
				}
			}
			if(tick>=WAIT_TIMEOUT){
				log.finest("Beaker was unable to provide us a system in ["+(WAIT_TIMEOUT*(sleepInt/1000))+"] sec.");
				log.finest("Returning [J:"+job_id+"] to Beaker tool");
				execute_local(false, "bkr job-cancel J:"+job_id);
				System.exit(3);
			}
			System.exit(4); // just in case  :) not to return "success"
		}catch(Exception ex){
			log.severe(ex.getMessage());
			System.exit(9);
		}		
	}
	
	private String execute_local(boolean showLogResults, String command){
		String out = null; String tmp_cmdFile = "/tmp/katello-json.sh";
		try{
			// cleanup the running buffer file.
			this.localCommandRunner.submitCommandToLocalWithReturn(false, 
					"rm -f "+tmp_cmdFile,"");

			FileOutputStream fout = 
				new FileOutputStream(tmp_cmdFile);
			fout.write((command+"\n").getBytes());fout.flush();fout.close();
			if(showLogResults)
				log.finest(String.format("Executing local: [%s]",command));
			out = this.localCommandRunner.submitCommandToLocalWithReturn(
					false, "sh "+tmp_cmdFile, ""); // HERE is the run
			
			if(showLogResults){ // log output if specified so.
				// split the lines and out each line.
				String[] split = out.split("\\n");
				for(int i=0;i<split.length;i++){
					log.info("Output: "+split[i]);
				}
			}
			
			// cleanup the running buffer file.
			this.localCommandRunner.submitCommandToLocalWithReturn(false, 
					"rm -f "+tmp_cmdFile,"");
		}catch(IOException iex){
			log.log(Level.SEVERE, iex.getMessage(), iex);
		}
		return out;
	}
		
	public static void main(String[] args) {
		new TestScript(); // should initialize the ~/automation.properties
		KatelloReservesys bkrReserve = new KatelloReservesys();
		bkrReserve.doReserveBeakerSystem();
	}
}
