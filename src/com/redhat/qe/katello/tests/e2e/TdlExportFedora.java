package com.redhat.qe.katello.tests.e2e;

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
 * Scenario: https://tcms.engineering.redhat.com/case/123749/?from_plan=4785<BR>
 * Description:<BR>
 * TDL export from Fedora repo 
 * @author gkhachik
 */
public class TdlExportFedora extends KatelloCliTestScript{
	protected static Logger log = Logger.getLogger(TdlExportFedora.class.getName());
	public static final String FEDORA_VER = "16";
	public static final String TDL_AEOLUS_VALIDATOR = "https://raw.github.com/aeolusproject/conductor/master/src/app/util/template-rng.xml";

	String org;
	String provider;
	String product;
	String repo;
	private String env_prod;
	private String cs_prod;
	private String template_prod;
	private String distribution;
	
	private String templateName;

	@BeforeTest(description="Init unique names", alwaysRun=true)
	public void setUp(){
		String uid = KatelloTestScript.getUniqueID();
		this.provider = "Fedora_"+uid;
		this.product = "Fedora_"+uid;
		this.repo = "Fedora"+FEDORA_VER+"_"+uid;
		
		this.env_prod = "Prod-"+uid;
		this.template_prod = "template_Fedora_"+uid;
		this.cs_prod = "cs_"+this.env_prod;

		this.org = "TDL-Fedora-"+uid;
		KatelloOrg org = new KatelloOrg(clienttasks, this.org, null);
		org.create();
		KatelloProvider prov = new KatelloProvider(clienttasks, this.provider, this.org, null, null);
		prov.create();
		KatelloProduct prod = new KatelloProduct(clienttasks, this.product, this.org, this.provider, null, null, null, null, null);
		prod.create();
		String fedoraUrl = getFedoraUrl(); // <--- here goes URL
		KatelloRepo repo = new KatelloRepo(clienttasks, this.repo, this.org, this.product, fedoraUrl, null, null);
		repo.create();
	}
	
	@Test(description="Sync Fedora repo", enabled=true)
	public void test_syncRepo(){
		log.info("E2E - sync Fedora "+FEDORA_VER+" repo");
		KatelloRepo repo = new KatelloRepo(clienttasks, this.repo, this.org, this.product, null, null, null);
		SSHCommandResult res = repo.synchronize();
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (repo synchronize)");
		res = repo.info();
		int pkgCount = Integer.parseInt(KatelloCliTasks.grepCLIOutput("Package Count", res.getStdout()));
		String progress = KatelloCliTasks.grepCLIOutput("Progress", res.getStdout());
		Assert.assertTrue(pkgCount>0, "Check - Packages >0");
		Assert.assertTrue(progress.equals("Finished"), "Check: status of repo sync - Finished");
	}
	
	@Test(description="Prepare changeset", dependsOnMethods={"test_syncRepo"}, enabled=true)
	public void test_prepChangeset(){
		log.info("E2E - add environment, prepare changeset");
		KatelloEnvironment env = new KatelloEnvironment(clienttasks, this.env_prod, null, this.org, KatelloEnvironment.LIBRARY);
		env.create();
		KatelloChangeset cs = new KatelloChangeset(clienttasks, this.cs_prod, this.org, this.env_prod);
		cs.create();
		SSHCommandResult res = cs.update_addProduct(this.product);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (changeset add_product)");
	}
	
	@Test(description="Prepare template", dependsOnMethods={"test_prepChangeset"}, enabled=true)
	public void test_prepareTemplate(){
		KatelloTemplate tpl = new KatelloTemplate(clienttasks, this.template_prod, null, this.org, null);
		tpl.create();
		KatelloDistribution dist = new KatelloDistribution(
				clienttasks, this.org, this.product,this.repo, null);
		SSHCommandResult res = dist.list();
		this.distribution = KatelloCliTasks.grepCLIOutput("Id", res.getStdout());
		Assert.assertTrue((this.distribution!=null), "Check - distribution exists in repo");
		tpl.update_add_repo(this.product, this.repo);
		res = tpl.update_add_distribution(this.product, this.distribution);
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
		this.templateName = this.template_prod+".tdl";
		res = tpl.export(this.env_prod, "/tmp/"+this.templateName, KatelloTemplate.FORMAT_TDL);
		Assert.assertTrue(res.getExitCode().intValue()==0, "Check - return code (template export - for env)");
		Assert.assertTrue(getOutput(res).contains(
				"Template was exported successfully to file /tmp/"+this.templateName), 
				"Check - return message (template export)");
	}

	@Test(description="Check TDL format against Aeolus TDL validator", dependsOnMethods={"test_genUebercertExportTdl"}, enabled=true)
	public void test_tdlFormatChecks(){
		SCPTools scp = new SCPTools(
				System.getProperty("katello.client.hostname", "localhost"), 
				System.getProperty("katello.client.ssh.user", "root"), 
				System.getProperty("katello.client.sshkey.private", ".ssh/id_hudson_dsa"), 
				System.getProperty("katello.client.sshkey.passphrase", "secret"));
		
		Assert.assertTrue(scp.getFile("/tmp/"+this.templateName, "data/"),
				"TDL export file get - sucessfully");
		KatelloCliTasks.run_local(false, "rm -rf /tmp/template-rng.xml; wget "+TDL_AEOLUS_VALIDATOR+" -O /tmp/template-rng.xml");
		String out = KatelloCliTasks.run_local(true, "xmllint --noout --relaxng /tmp/template-rng.xml data/"+this.templateName);
		Assert.assertTrue(out.endsWith("validates"), "export file passes TDL validation");
	}
	
	@Test(description="Uebercert - should be able to access repomd.xml using ueber cert key/cert pairs", 
			dependsOnMethods={"test_tdlFormatChecks"}, enabled=true)
	public void test_ueberCertAccess(){
		String pemKey = System.getProperty("user.dir")+"/data/key-"+this.org+".pem";
		String pemCert = System.getProperty("user.dir")+"/data/cert-"+this.org+".pem";
		KatelloCliTasks.run_local(true, "scripts/katello-utils.py --method tdl_extract_certs --args " +
				"\"filename=data/"+this.templateName+",reponame="+this.repo+",certname="+pemCert+",keyname="+pemKey+"\"");
		String url = KatelloCliTasks.run_local(true, "scripts/katello-utils.py --method tdl_fromTag --args " +
				"\"filename=data/"+this.templateName+",tag=os/install/url\"");
		String res = KatelloCliTasks.run_local(true, "curl -sk --cert "+pemCert+" --key "+pemKey+" \""+url+"/repodata/repomd.xml\" | grep \"<location href=\\\"repodata/.*filelists.xml.gz\\\"/>\" | wc -l");
		Assert.assertTrue(res.equals("1"), "Check - could be able to get repomd.xml content unlocked.");
	}
	
	private String getFedoraUrl(){
		String domain="";
		
		String lab_controller = servertasks.execute_remote("echo ${LAB_CONTROLLER}").getStdout().trim();
		if(lab_controller.equals("")) lab_controller = "lab.rhts.englab.brq.redhat.com";
		
		if(lab_controller.equals("lab2.rhts.eng.bos.redhat.com"))
			domain = "download.bos.redhat.com";
		if(lab_controller.equals("lab.rhts.eng.nay.redhat.com"))
			domain = "download.eng.nay.redhat.com";
		if(lab_controller.equals("lab.rhts.eng.pnq.redhat.com"))
			domain = "download.eng.pnq.redhat.com";
		if(lab_controller.equals("lab-01.eng.tlv.redhat.com"))
			domain = "download.eng.tlv.redhat.com";
		if(lab_controller.equals("lab.rhts.englab.brq.redhat.com"))
			domain = "download.eng.brq.redhat.com";

		String _url = "http://"+domain+"/pub/fedora/linux/releases/"+FEDORA_VER+"/Fedora/x86_64/os/";
		return _url;
	}
}
