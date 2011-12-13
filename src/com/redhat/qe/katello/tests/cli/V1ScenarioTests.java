package com.redhat.qe.katello.tests.cli;

import java.util.logging.Logger;
import org.testng.annotations.Test;
import com.redhat.qe.auto.testng.Assert;
import com.redhat.qe.katello.base.IKatelloOrg;
import com.redhat.qe.katello.base.IKatelloProduct;
import com.redhat.qe.katello.base.IKatelloProvider;
import com.redhat.qe.katello.base.IKatelloRepo;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.base.KatelloTestScript;
import com.redhat.qe.katello.common.KatelloConstants;
import com.redhat.qe.katello.tasks.KatelloCliTasks;
import com.redhat.qe.katello.tasks.KatelloTasks;
import com.redhat.qe.tools.SSHCommandResult;

public class V1ScenarioTests extends KatelloCliTestScript implements KatelloConstants{
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
	
	protected KatelloTasks servertasks	= null;
	protected KatelloCliTasks clienttasks = null;

	/**
	 * Scenario: Fetch Fedora15 content<BR>
	 *  - check repo is created<br>
	 *  - packages count >0<br>
	 *  - 
	 */
	@Test(description="Scenario: synchronize Fedora 15 repository", enabled = false)
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
		res = clienttasks.run_cliCmd(String.format(IKatelloRepo.CREATE, orgName,prodName,repoName,getFedora15RepoUrl()));
		Assert.assertEquals(res.getExitCode().intValue(), 0, "Check - return code");
		log.fine("Initialize `repo synchronize` for repo: ["+repoName+"]");
		log.fine("Follow the process by: ["+String.format(IKatelloRepo.STATUS, orgName,prodName,repoName)+"]");
		clienttasks.run_cliCmd_nowait(String.format(IKatelloRepo.SYNCHRONIZE, orgName,prodName,repoName)); // initiate the sync.
		
		waitfor_reposync(orgName, prodName, repoName, 30);
	}
	
	
	
	
	
	private String getFedora15RepoUrl(){
		String ret;
		String bkr_lab = System.getProperty("LAB_CONTROLLER", labs.get(BKR_LAB_CONTROLLER.BRQ));
		if(bkr_lab.equals(labs.get(BKR_LAB_CONTROLLER.BOS)))
			ret = "http://download.bos.redhat.com/pub/fedora/linux/releases/15/Fedora/x86_64/os/";
		else if(bkr_lab.equals(labs.get(BKR_LAB_CONTROLLER.PNQ)))
			ret = "http://download.eng.pnq.redhat.com/pub/fedora/linux/releases/15/Fedora/x86_64/os/";
		else if(bkr_lab.equals(labs.get(BKR_LAB_CONTROLLER.NAY)))
			ret = "http://download.eng.nay.redhat.com/pub/fedora/linux/releases/15/Fedora/x86_64/os/";
		else if(bkr_lab.equals(labs.get(BKR_LAB_CONTROLLER.TLV)))
			ret = "http://download.eng.tlv.redhat.com/pub/fedora/linux/releases/15/Fedora/x86_64/os/";
		// RDU ? -> BRQ (refer to BRQ)
		else
			ret = "http://download.eng.brq.redhat.com/pub/fedora/linux/releases/15/Fedora/x86_64/os/";
		
		return ret;
	}
}
