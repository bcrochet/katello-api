package com.redhat.qe.katello.common;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Logger;
import com.redhat.qe.auto.testng.TestScript;
import com.redhat.qe.katello.tasks.KatelloTasks;
import com.redhat.qe.tools.SSHCommandRunner;

public class KatelloInBeaker implements KatelloConstants {
	private enum BKR_STATUS {RUNNING, RESERVED, FAILED, CANCELLED};

	private Logger log = Logger.getLogger(KatelloInBeaker.class.getName());
	private HashMap<String, String> beaker_info;
	private String pk_file;
	private SSHCommandRunner sshRunner;
	
	public static final String BKR_RESERVED = 
		"path=\"/distribution/reservesys\" result=\"Pass\"";
	public static final String HUDSON_BUFFER = 
		"/tmp/"+System.getenv("BUILD_TAG")+".beaker.info";
	public static final int WAIT_TIMEOUT = Integer.parseInt(System.getenv("BKR_MAXWAIT"));	
	
	public KatelloInBeaker(){
		try {
			beaker_info = new HashMap<String, String>();
			beaker_info.put("beaker_job_id", "");
			beaker_info.put("beaker_hostname", "");
			beaker_info.put("beaker_reserve_time", "");
			//beaker_info.put("", ""); // Maybe distro_arch as well ?
			
			String hostname = KatelloTasks.run_local(false, "hostname");
			// CHECK & fail if there is no private ssh key file.
			String sshKeysDir = System.getenv("HOME")+"/.ssh";   
			File f_priv = new File(sshKeysDir+"/id_rsa");
			File f_pub = new File(sshKeysDir+"/id_rsa.pub");
			if (!f_priv.exists() || !f_pub.exists()){
				throw new RuntimeException(String.format(
						"ERROR: There are missing private/public key pairs on your running server: [%s]",
						hostname));
			}
			this.pk_file = KatelloTasks.run_local(false, "cat "+sshKeysDir+"/id_rsa.pub");
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
	 * --keyvalue="MEMORY>2047"<br> 
	 * --keyvalue="DISK>7999" <br>
	 * --task=/distribution/reservesys<br>
	 * <br>
	 * <b>OUTPUT:</b><br>
	 * Submitted: ['j:&lt;JOB_ID&gt;']
	 */
	public void reserveBeakerSystems(){
		try{
			String distrArchList = System.getenv("BKR_DISTRO_ARCH");
			
			String[] distros = distrArchList.split(",");
			/** 
			 * [i][0] = distro
			 * [i][1] = arch
			 * [i][2] = job_id
			 * [i][3] = BKR_STATUS 
			 */
			String[][] hmDA = new String[distros.length][4];
			for(int i=0;i<distros.length;i++){
				String distro = distros[i].substring(0, distros[i].indexOf("(")).trim();
				String arch = distros[i].substring(distros[i].indexOf("(")+1,distros[i].length()-1).trim();
				hmDA[i][0] = distro;
				hmDA[i][1] = arch;
				hmDA[i][3] = BKR_STATUS.RUNNING.toString();
			}
			
			// STEP 1. init beaker reservesys
			for(int i=0;i<hmDA.length;i++){
				hmDA[i][2] = this.bkr_initReserveSys(hmDA[i][0],hmDA[i][1]);
			}
			
			// STEP 2. wait for WAIT_TIMEOUT for reservations completion
			int tik = 0; boolean allDone = false;
			while(tik<WAIT_TIMEOUT && !allDone){
				try{Thread.sleep(60000);}catch(InterruptedException iex){} // sleep 1 min
				tik++; allDone = true; //on each round suppose - all done 
				for(int i=0;i<hmDA.length;i++){
					if(hmDA[i][3].toString().equals(BKR_STATUS.RUNNING.toString())){
						allDone=false;
						hmDA[i][3] = this.bkr_checkReservation(hmDA[i][2], tik).toString();
					}
				}
			}
			
			// STEP 3. put public key of "hudson" user into that server(s)
			boolean atLeastOneDone = false; // we are pessimistic
			String[] servers = beaker_info.get("beaker_hostname").split(",");
			for(int i=0;i<hmDA.length;i++){
				if(hmDA[i][3].toString().equals(BKR_STATUS.RESERVED.toString())){
					atLeastOneDone = true;
					bkr_putHudsonPubKey(servers[i]);
				}
			}
			
			// STEP 4. log the progress we have
			log.info("# ======================================================");
			log.info("# Report of Beaker reservation for:");
			log.info("# "+distrArchList);
			log.info("# ======================================================");
			if(!atLeastOneDone){
				// ALL is bad - exit with non-zero status
				log.info("# ERROR: Unable to provision any platform there.");
				log.info("# ======================================================");
				System.exit(99);
			}
			// prepare the Jenkins buffer file.
			BufferedWriter bw = new BufferedWriter(new FileWriter(HUDSON_BUFFER));
			bw.write("beaker_hostname="+beaker_info.get("beaker_hostname")+"\n");
			bw.write("beaker_job_id="+beaker_info.get("beaker_job_id")+"\n");
			bw.write("beaker_reserve_time="+beaker_info.get("beaker_reserve_time")+"\n");
			// Sysout the content.
			log.info("# Output of: ["+HUDSON_BUFFER+"]:");
			log.info("# beaker_hostname="+beaker_info.get("beaker_hostname"));
			log.info("# beaker_job_id="+beaker_info.get("beaker_job_id"));
			log.info("# beaker_reserve_time="+beaker_info.get("beaker_reserve_time"));
			log.info("# ======================================================");
			System.exit(0);
		}catch(Exception ex){
			log.severe(ex.getMessage());
			System.exit(9);
		}		
	}
	
	/**
	 * Initiates `bkr workflow-simple` command for specified distro/arch.
	 * @param distro Beaker distro name, like: Fedora-14
	 * @param arch Beaker arch name, like: x86_64
	 * @return Job id as a string (without "J:" in the beginning).
	 */
	private String bkr_initReserveSys(String distro, String arch){
		log.info(String.format("Requesting system reservation in Beaker: [%s (%s)]",distro,arch));
		String bkr_reserveCmd = String.format(BKR_RESERVESYS_CMD, 
				System.getenv("BKR_USER"),
				System.getenv("BKR_PASS"),
				distro,
				"\"Katello Installer by Jenkins - ["+System.getenv("BUILD_TAG")+"]\"",
				arch);
		String out = KatelloTasks.run_local(false, bkr_reserveCmd);
		if(! out.startsWith("Submitted: ['J:")){
			log.severe(String.format("Failed to apply `bkr workflow-simple` for: [%s (%s)]",distro,arch));
			log.severe("Output: "+out);
			System.exit(1);
		}
		return out.substring(
				out.indexOf("Submitted: ['J:")+"Submitted: ['J:".length(), 
				out.lastIndexOf("']"));
	}
	
	private BKR_STATUS bkr_checkReservation(String job_id, int waitingAlready){
		BKR_STATUS ret = BKR_STATUS.RUNNING;
		
		String out = KatelloTasks.run_local(false, "bkr job-results J:"+job_id);
		if(out.contains(BKR_RESERVED)){// Found - (happy)
			// let's grub the reserved hostname
			ret = BKR_STATUS.RESERVED;
			int ib = out.indexOf("system=\"");
			try{
				String bkr_system = out.substring(
						ib+"system=\"".length(),out.indexOf("\" variant=\"", ib));
				if(beaker_info.get("beaker_job_id").equals("")){
					beaker_info.put("beaker_job_id", job_id);					
					beaker_info.put("beaker_hostname", bkr_system);
					beaker_info.put("beaker_reserve_time", String.valueOf(waitingAlready));
				}else{ // Does not exist - create file and put the info 
					beaker_info.put("beaker_job_id", beaker_info.get("beaker_job_id")+","+job_id);					
					beaker_info.put("beaker_hostname", beaker_info.get("beaker_hostname")+","+bkr_system);
					beaker_info.put("beaker_reserve_time", beaker_info.get("beaker_reserve_time")+","+String.valueOf(waitingAlready));					
				}
			}catch(Exception ex){
				log.severe("Unable to parse Beaker system hostname: see logs to analyze the issue.");
				log.finest("Returning [J:"+job_id+"] to Beaker tool");
				KatelloTasks.run_local(false, "bkr job-cancel J:"+job_id);
				ret = BKR_STATUS.FAILED;
			}
		}else{
			if(waitingAlready<WAIT_TIMEOUT){
				log.finest("Check status of reservation for job: ["+job_id+"] after: ["+waitingAlready+"] try(ies).");
			}else{
				// Waiting timeout expired. cancel the job, return to pool. Exit.
				log.finest("Beaker was unable to provide us a system in ["+WAIT_TIMEOUT+"] min.");
				log.finest("Returning [J:"+job_id+"] to Beaker tool");
				KatelloTasks.run_local(false, "bkr job-cancel J:"+job_id);
				ret = BKR_STATUS.CANCELLED;
			}
		}
		return ret;
	}
	
	private void bkr_putHudsonPubKey(String servername){
		try{
			sshRunner = new SSHCommandRunner(
					servername, "root", System.getenv("BKR_SYS_PWD"),"","", null);
			sshRunner.runCommandAndWait("mkdir ~/.ssh/; touch ~/.ssh/authorized_keys; echo -e \""+this.pk_file+"\" >> ~/.ssh/authorized_keys");
		}catch(IOException iex){
			log.finest("Unable to import public key of Hudson to: ["+servername+"]");
			log.finest("ERROR: "+iex.getMessage());
		}
	}
	
	public static void main(String[] args) {
		new TestScript(); // should initialize the ~/automation.properties
		KatelloInBeaker bkrReserve = new KatelloInBeaker();
		bkrReserve.reserveBeakerSystems();
	}
}

