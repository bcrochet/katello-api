package com.redhat.qe.katello.common;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.logging.Logger;
import com.redhat.qe.katello.base.KatelloCliDataProvider;
import com.redhat.qe.katello.tasks.KatelloTasks;

public class KatelloInBeaker implements KatelloConstants {	
	private static enum BKR_STATUS {RUNNING, RESERVED, FAILED, CANCELLED};

	private static Logger log = Logger.getLogger(KatelloInBeaker.class.getName());
	
	public static final String BKR_RESERVED = 
		"path=\"/distribution/reservesys\" result=\"Pass\"";
	public static final String HUDSON_BUFFER = 
		"/tmp/"+System.getenv("BUILD_TAG")+".%s.beaker.info";
	public static int WAIT_TIMEOUT = 24*60; // default - 24 hours. (in minutes)

	
	public static void bkrReservesys_RHEL6(){
		String filename = String.format(HUDSON_BUFFER, "RHEL6");
		String job_id = reservesys(BKR_KATELLO_RESERVESYS_RHEL6);
		int tik = 0; BKR_STATUS jobStatus = BKR_STATUS.RUNNING;
		while(jobStatus == BKR_STATUS.RUNNING && tik<WAIT_TIMEOUT){
			try{Thread.sleep(60000);}catch(InterruptedException iex){} tik++; // sleep 1 min
			jobStatus = checkReservation(job_id,tik,filename);
		}
		if(jobStatus != BKR_STATUS.RESERVED){ // if not success - cancel the job.
			log.severe("ERROR: Unable to get [J:"+job_id+"] done. Return system to the Beaker tool");
			KatelloTasks.run_local(false, "bkr job-cancel J:"+job_id);
			System.exit(1);
		}
	}
	
	private static String reservesys(String cmdTemplate){

		// overwrite the value from Jenkins (if provided).
		if(System.getenv("BKR_MAXWAIT")!=null)
			try{
				WAIT_TIMEOUT = Integer.parseInt(System.getenv("BKR_MAXWAIT"));
			}catch(Exception ex){} // wrong formatted "number". Take the default.
		
		String bkrUser = System.getenv("BKR_USER");
		String bkrPass = System.getenv("BKR_PASS");
		String cmd_reservesys = String.format(cmdTemplate, bkrUser, bkrPass);
		String out = KatelloTasks.run_local(false, cmd_reservesys);
		if(! out.startsWith("Submitted: ['J:")){
			final StackTraceElement[] ste = new Throwable().getStackTrace();
			String methodname = ste[0].getMethodName();
			log.severe("Failed to apply `bkr workflow-simple` for: ["+methodname+"]");
			log.severe("Output: "+out);
			System.exit(1);
		}
		return out.substring(
				out.indexOf("Submitted: ['J:")+"Submitted: ['J:".length(), 
				out.lastIndexOf("']")); 
	}
	
	private static BKR_STATUS checkReservation(String job_id, int waitingAlready, String filename){
		BKR_STATUS ret = BKR_STATUS.RUNNING;
		String reportContent = "";
		
		String out = KatelloTasks.run_local(false, "bkr job-results J:"+job_id);		
		if(out.contains(BKR_RESERVED)){// Found - (happy)
			// let's grub the reserved hostname
			ret = BKR_STATUS.RESERVED;
			try{
				int ib = out.indexOf("system=\"");
				String bkr_system = out.substring(
						ib+"system=\"".length(),out.indexOf("\" variant=\"", ib));
				reportContent = reportContent+"beaker_job_id="+job_id+"\n";
				reportContent = reportContent+"beaker_hostname="+bkr_system+"\n";
				reportContent = reportContent+"beaker_reserve_time"+String.valueOf(waitingAlready);
				log.fine("Reservesys done successfully. Below are the details:\n"+
						"\t"+KatelloCliDataProvider.strRepeat("=", 80)+"\n"+
						"\t"+"beaker_job_id="+job_id+"\n"+
						"\t"+"beaker_hostname="+bkr_system+"\n"+
						"\t"+"beaker_reserve_time"+String.valueOf(waitingAlready)+"\n"+
						"\t"+KatelloCliDataProvider.strRepeat("=", 80));
				// put to the file.
				BufferedWriter bw = new BufferedWriter(new FileWriter(filename));
				bw.write(reportContent); bw.flush(); bw.close();
				log.fine("Content report is put in: ["+filename+"]");
			}catch(Exception ex){
				log.severe("Unable to parse Beaker system hostname: see logs to analyze the issue.");
				ret = BKR_STATUS.FAILED;
			}
		}else{
			if(waitingAlready<WAIT_TIMEOUT)
				log.finest("Check status of job: [J:"+job_id+"] after: ["+waitingAlready+"] try(ies).");
		}
		return ret;
	}
	
	public static void main(String[] args) {
		String bkrDistro = System.getenv("BKR_DISTRO");
		if(bkrDistro == null){
			log.severe("ERROR: Please specify $BKR_DISTRO with value of: {RHEL6, F15, F14}");
			System.exit(2);
		}
		if(bkrDistro.equals("RHEL6")){
			bkrReservesys_RHEL6();
		}else if (bkrDistro.equals("F15")){
			// TODO - add support for F15
		}else if (bkrDistro.equals("F14")){
			// TODO - add support for F14
		}else{
			log.severe("Unsupported platform: ["+bkrDistro+"]. Should be one of: {RHEL6, F15, F14}");
			System.exit(3);
		}
	}
}

