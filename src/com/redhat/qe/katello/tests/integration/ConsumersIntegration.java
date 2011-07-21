package com.redhat.qe.katello.tests.integration;

import java.util.logging.Level;

import org.json.simple.JSONObject;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import com.redhat.qe.katello.base.KatelloTestScript;
import com.redhat.qe.katello.common.KatelloDBCleaner;

public class ConsumersIntegration extends KatelloTestScript{
	
	public static final String REPO_FEDORA_14_64BIT = 
		"http://download.fedoraproject.org/pub/fedora/linux/releases/14/Fedora/x86_64/os/";
	public static final long FEDORA_14_64BIT_SIZE = 5000000l; // ~5GB needed, df returns in KBytes

	@BeforeMethod(description="Cleanup database + Pulp repository structure on HDD", enabled=false) // TODO - open the db cleanup
	public void cleanup(){
		log.info("Cleanup DBs + Pulp repositories in disk ...");
		KatelloDBCleaner.main(null);
	}

	/**
	 * With the cli:<br>
	 * 1. Crate an org<br>
	 * 2. Create a provider<br>
	 * 3. Create a product<br>
	 * 4. Sync the provider (or sync the product)<br>
	 * <br>
	 * With RHSM:<br>
	 * 1. register with rhsm<br>
	 * 2. subscribe to the product.<br>
	 * <br>
	 * All using the same user.<br>
	 */
	@Test(description=
		"As a cli user, I would like to create a custom product from Fedora 14 and " +
		"then susbcribe to it via RHSM (use same user in both tools)")	
	public void integration_rhsm_F14(){
		String uid = KatelloTestScript.getUniqueID();

		String org_name = "org_Fedora14_"+uid;
		String provider_name = "provider_Fedora14_"+uid;
		String product_name="product_Fedora14_"+uid;
		String repo_name = "repo_Fedora14_"+uid;
		
		// == Create an org ==
		servertasks.createOrganization(org_name, "");
		
		// == Create a provider ==
		KatelloTestScript.toJSONObj(
				servertasks.createProvider(org_name, provider_name, "", "Custom"));
		
		// == Create a product ==
		JSONObject prod = KatelloTestScript.toJSONObj(
				servertasks.createProduct(provider_name, product_name, "", "http://download.fedoraproject.org"));
		
		// == Create a repo ==
		String product_id = (String)prod.get("cp_id"); // it's actually Candlepin id stored in cp_id field of /product_create call result.
		String ret = servertasks.createRepository(provider_name, product_id,repo_name, 
				REPO_FEDORA_14_64BIT);
		
		boolean canSyncRepo = canRepoBeSynced(FEDORA_14_64BIT_SIZE);
		if(canSyncRepo){
			
		}
		
	}
	
	private boolean canRepoBeSynced(long spaceNeeded){
		boolean _return = true;
		
		long df = servertasks.getDiskFreeForPulpRepos();
		_return = (df > spaceNeeded);
		if(!_return){
			String warn = "Not enough space for syncing the repository";
			log.log(Level.WARNING, warn);
		}
		return _return;
	}
}
