package com.redhat.qe.katello.tests.e2e;

import java.io.File;
import java.util.ArrayList;
import java.util.logging.Logger;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import com.redhat.qe.auto.testng.Assert;
import com.redhat.qe.katello.base.KatelloCliTestScript;
import com.redhat.qe.katello.base.KatelloTestScript;
import com.redhat.qe.katello.base.cli.KatelloChangeset;
import com.redhat.qe.katello.base.cli.KatelloDistribution;
import com.redhat.qe.katello.base.cli.KatelloEnvironment;
import com.redhat.qe.katello.base.cli.KatelloOrg;
import com.redhat.qe.katello.base.cli.KatelloProduct;
import com.redhat.qe.katello.base.cli.KatelloProvider;
import com.redhat.qe.katello.base.cli.KatelloRepo;
import com.redhat.qe.katello.base.cli.KatelloTemplate;
import com.redhat.qe.katello.tasks.KatelloCliTasks;
import com.redhat.qe.tools.SCPTools;
import com.redhat.qe.tools.SSHCommandResult;

/**
 * Implementation of one of V1 scenarios:<BR>
 * Scenario: href='https://tcms.engineering.redhat.com/run/35162/?from_plan=4785#caserun_927353'<BR>
 * Description:<BR>
 * TDL export from RHEL repo 
 * @author gkhachik
 */
public class TdlExportRhel extends KatelloCliTestScript{
	protected static Logger log = Logger.getLogger(TdlExportRhel.class.getName());

	String org;
	private String env_prod;
	private String cs_prod;
	private String template_prod;
	private String distribution;

	@BeforeTest(description="Init unique names", alwaysRun=true)
	public void setUp(){
		String uid = KatelloTestScript.getUniqueID();
		this.env_prod = "Prod-"+uid;
		this.template_prod = "template_Prod_"+uid;
		this.cs_prod = "cs_"+this.env_prod;

		ArrayList<String> orgs = getOrgsWithImportedManifest();
		log.info("E2E - check org to use/create for importing manifest");
		if(orgs.size()==0){
			log.info("Seems there is no org with imported stage manifest. Doing it now");
			SCPTools scp = new SCPTools(
					System.getProperty("katello.client.hostname", "localhost"), 
					System.getProperty("katello.client.ssh.user", "root"), 
					System.getProperty("katello.client.sshkey.private", ".ssh/id_hudson_dsa"), 
					System.getProperty("katello.client.sshkey.passphrase", "secret"));
			Assert.assertTrue(scp.sendFile("data"+File.separator+"export.zip", "/tmp"),
					"export.zip sent successfully");			
			this.org = "TDL-RHEL-"+uid; // TODO - do org with space. Now there is a bug.
			KatelloOrg org = new KatelloOrg(clienttasks, this.org, null);
			org.create();
			KatelloProvider prov = new KatelloProvider(clienttasks, KatelloProvider.PROVIDER_REDHAT, this.org, null, null);
			SSHCommandResult res = prov.import_manifest("/tmp"+File.separator+"export.zip", new Boolean(true));
			Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (provider import_manifest)");
			Assert.assertTrue(res.getStdout().trim().contains("Manifest imported"),"Message - (provider import_manifest)");
		}else{
			this.org = orgs.get(0);
			log.info("There is an org having manifest. Using: ["+this.org+"]");
		}		
		log.info("Enable repo: ["+KatelloRepo.RH_REPO_RHEL6_SERVER_RPMS_64BIT+"]");
		KatelloRepo repo = new KatelloRepo(clienttasks, KatelloRepo.RH_REPO_RHEL6_SERVER_RPMS_64BIT, this.org, KatelloProduct.RHEL_SERVER, null, null, null);
		SSHCommandResult res = repo.enable();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (repo enable)");
		Assert.assertTrue(res.getStdout().trim().contains("enabled."),"Message - (repo enable)");
	}
	
	@Test(description="Sync RHEL repo - if it's not", enabled=true)
	public void test_syncRepo(){
		log.info("E2E - sync repo (if needed");
		KatelloRepo repo = new KatelloRepo(clienttasks, KatelloRepo.RH_REPO_RHEL6_SERVER_RPMS_64BIT, this.org, KatelloProduct.RHEL_SERVER, null, null, null);
		SSHCommandResult res = repo.info();
		int pkgCount = Integer.parseInt(KatelloCliTasks.grepCLIOutput("Package Count", res.getStdout()));
		if(pkgCount==0){
			log.info("Seems repo is not synchronized yet. Doing now ...");
			res = repo.synchronize();
			Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (repo synchronize)");
			res = repo.info();
			pkgCount = Integer.parseInt(KatelloCliTasks.grepCLIOutput("Package Count", res.getStdout()));
			String progress = KatelloCliTasks.grepCLIOutput("Progress", res.getStdout());
			Assert.assertTrue(pkgCount>0, "Check - Packages >0");
			Assert.assertTrue(progress.equals("Finished"), "Check: status of repo sync - Finished");
		}else{
			log.info("Seems repo is synchronized. Found ["+pkgCount+"] there");
		}
	}
	
	@Test(description="Prepare changeset", dependsOnMethods={"test_syncRepo"}, enabled=true)
	public void test_prepChangeset(){
		log.info("E2E - add environment, prepare changeset");
		KatelloEnvironment env = new KatelloEnvironment(clienttasks, this.env_prod, null, this.org, KatelloEnvironment.LIBRARY);
		env.create();
		KatelloChangeset cs = new KatelloChangeset(clienttasks, this.cs_prod, this.org, this.env_prod);
		cs.create();
		SSHCommandResult res = cs.update_addProduct(KatelloProduct.RHEL_SERVER);
		//SSHCommandResult res = cs.update_fromProduct_addRepo(KatelloProduct.RHEL_SERVER, KatelloRepo.RH_REPO_RHEL6_SERVER_RPMS_64BIT);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (changeset add_product)");
	}
	
	@Test(description="Prepare template", dependsOnMethods={"test_prepChangeset"}, enabled=true)
	public void test_prepareTemplate(){
		KatelloTemplate tpl = new KatelloTemplate(clienttasks, this.template_prod, null, this.org, null);
		tpl.create();
		KatelloDistribution dist = new KatelloDistribution(clienttasks, this.org, KatelloProduct.RHEL_SERVER, 
				KatelloRepo.RH_REPO_RHEL6_SERVER_RPMS_64BIT, null);
		SSHCommandResult res = dist.list();
		this.distribution = KatelloCliTasks.grepCLIOutput("Id", res.getStdout());
		Assert.assertTrue((this.distribution!=null), "Check - distribution exists in repo");
		tpl.update_add_repo(KatelloProduct.RHEL_SERVER, KatelloRepo.RH_REPO_RHEL6_SERVER_RPMS_64BIT);
		res = tpl.update_add_distribution(KatelloProduct.RHEL_SERVER, this.distribution);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (template add_distribution)");
		tpl.update_add_package("vim-enhanced");
		tpl.update_add_package_group("Desktop");
	}
	
	@Test(description="Promote changeset with template", dependsOnMethods={"test_prepareTemplate"}, enabled=true)
	public void test_promoteChangesetWithTemplate(){
		KatelloChangeset cs = new KatelloChangeset(clienttasks, this.cs_prod, this.org, this.env_prod);
		SSHCommandResult res = cs.update_addTemplate(this.template_prod);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (changeset add_template)");
		res = cs.promote();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (changeset promote)");
	}
	
	@Test(description="Generate uebercert & export TDL", dependsOnMethods={"test_promoteChangesetWithTemplate"}, enabled=true)
	public void test_genUebercertExportTdl(){
		KatelloOrg org = new KatelloOrg(clienttasks, this.org, null);
		SSHCommandResult res = org.uebercert();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (org uebercert)");
		KatelloTemplate tpl = new KatelloTemplate(clienttasks, this.template_prod, null, this.org, null);
		res = tpl.export(this.env_prod, "/tmp/"+this.template_prod+".tdl", KatelloTemplate.FORMAT_TDL);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (template export - for env)");
		Assert.assertTrue(res.getStdout().trim().contains(
				"Template was exported successfully to file "+"/tmp/"+this.template_prod+".tdl"), 
				"Check - return message (template export)");
	}	
}
