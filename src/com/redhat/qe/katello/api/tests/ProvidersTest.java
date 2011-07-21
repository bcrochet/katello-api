package com.redhat.qe.katello.api.tests;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Logger;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.redhat.qe.auto.testng.Assert;
import com.redhat.qe.katello.base.KatelloTestScript;
import com.redhat.qe.katello.common.KatelloConstants;

public class ProvidersTest extends KatelloTestScript {
	private String org_name;
	private String provider_name;
	protected static Logger log = 
		Logger.getLogger(EnvironmentsTest.class.getName());

	public ProvidersTest() {
		super();
	}

	@BeforeClass(description="Prepare an organization to work with")
	public void setUp_createOrg(){
//		String uid = KatelloTestScript.getUniqueID();
//		this.org_name = "auto-org-"+uid; 
//		String org_descr = String.format("Test Organization %s for ProvidersTest",uid);
//		servertasks.createOrganization(this.org_name, org_descr);
		this.org_name = default_org;
	}

	@Test(groups = { "testProviders" }, description = "Create provider")
	public void test_createProvider() {
		
		String uid = getUniqueID();
		this.provider_name = "auto-provider-"+uid;
		String str_json = servertasks.createProvider(
				this.org_name,provider_name, "Provider in test - "+uid,"Custom");
		JSONObject json_prov = KatelloTestScript.toJSONObj(str_json);
		Assert.assertNotNull(json_prov, "Returned string in katello is JSON-formatted");
		
		// ASSERTIONS - katello
		Assert.assertEquals(json_prov.get("name"), 
				provider_name, 
				"Katello - Check provider: name");
		Assert.assertEquals(json_prov.get("description"), 
				"Provider in test - "+uid, 
				"Katello - Check provider: description");
		Assert.assertEquals(json_prov.get("provider_type"), 
				"Custom",
				"Katello - Check provider: provider_type");
		JSONObject json_org = servertasks.getOrganization(org_name);
		Assert.assertEquals(json_prov.get("organization_id"), 
				json_org.get("id"),
				"Katello - Check provider: organization_id");
		
//		// ASSERTIONS - candlepin
//		String repoUrl_Candlepin = 
//			KatelloConstants.KATELLO_SMALL_REPO.substring(
//					KatelloConstants.KATELLO_SMALL_REPO.indexOf(":80/")+3);
//		String candlepin_json = servertasks.apiCandlepin_GET("/products/"+provider_name);
//		JSONObject json_cpProduct = KatelloTestScript.toJSONObj(candlepin_json);
//		Assert.assertNotNull(json_cpProduct, "Returned string in candlepin is JSON-formatted");
//		JSONObject cp_prodContent = (JSONObject)((JSONArray)json_cpProduct.get("productContent")).get(0);
//		Assert.assertEquals(((JSONObject)cp_prodContent.get("content")).get("name"), 
//				provider_name, 
//				"Candlepin - Check product content: name");
//		Assert.assertEquals(((JSONObject)cp_prodContent.get("content")).get("label"), 
//				provider_name, 
//				"Candlepin - Check product content: label");
//		Assert.assertEquals(((JSONObject)cp_prodContent.get("content")).get("contentUrl"), 
//				repoUrl_Candlepin, 
//				"Candlepin - Check product content: contentUrl");
//		Assert.assertEquals(((JSONObject)cp_prodContent.get("content")).get("type"), 
//				"yum", 
//				"Candlepin - Check product content: type");
//		Assert.assertEquals(((JSONObject)cp_prodContent.get("content")).get("vendor"), 
//				"Custom", 
//				"Candlepin - Check product content: vendor");
//		Assert.assertEquals(json_cpProduct.get("name"), 
//				provider_name, 
//				"Candlepin - Check product: name");
//		Assert.assertEquals(json_cpProduct.get("href"), 
//				"/products/"+provider_name, 
//				"Candlepin - Check product: href");
//		Assert.assertEquals(json_cpProduct.get("id"), 
//				provider_name, 
//				"Candlepin - Check product: id");
//		
//		// ASSERTIONS - Pulp
////		String pulp_url = String.format("/repositories/%s-%s-%s/", 
////				provider_name, json_prov.get("id"), this.org_name);
//		String pulp_url = String.format("/repositories/%s-%s-%s/", 
//				provider_name, provider_name, this.org_name);
//		String pulp_json = servertasks.apiPulp_GET(pulp_url);
//		JSONObject json_pulpProduct = KatelloTestScript.toJSONObj(pulp_json);
//		Assert.assertEquals(json_pulpProduct.get("name"), 
//				provider_name, 
//				"Pulp - Check repository: name");
//		Assert.assertEquals(json_pulpProduct.get("_id"), 
//				String.format("%s-%s-%s",
//						provider_name, provider_name, this.org_name), 
//				"Pulp - Check repository: _id");
//		Assert.assertEquals(json_pulpProduct.get("id"), 
//				String.format("%s-%s-%s",
//						provider_name, provider_name, this.org_name), 
//				"Pulp - Check repository: id");
//		Assert.assertEquals(json_pulpProduct.get("last_sync"), 
//				null, 
//				"Pulp - Check repository: last_sync");
//		Assert.assertEquals(json_pulpProduct.get("sync_schedule"), 
//				null, 
//				"Pulp - Check repository: sync_schedule");
//		Assert.assertEquals(((JSONObject)json_pulpProduct.get("source")).get("url"), 
//				KatelloConstants.KATELLO_SMALL_REPO, 
//				"Pulp - Check repository: source->url");
//		Assert.assertEquals(((JSONObject)json_pulpProduct.get("source")).get("type"), 
//				"yum", 
//				"Pulp - Check repository: source->type");
	}
	
	@Test (groups={"testProviders"}, description="Import Products", 
			dependsOnMethods="test_createProvider", enabled = false) // Seems moved to another controller.
	public void test_importProducts(){
		// Read data/products.json file. Needs to get replaced by actual values. 
		String sProducts="{}";
		try{
			BufferedReader br = new BufferedReader(new FileReader("data/product.json"));
			sProducts=br.readLine();
			br.close();
		}catch(IOException iex){
			log.severe(iex.getMessage());
			throw new RuntimeException(iex);
		}
		// Replace the values in products.json
		String pid = KatelloTestScript.getUniqueID();
		try{Thread.sleep(1000);}catch(InterruptedException ex){}
		String cid = KatelloTestScript.getUniqueID();
		String repoUrl=KatelloConstants.KATELLO_SMALL_REPO;
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'.000+0000'");
		String sTS = df.format(Calendar.getInstance().getTime());
		sProducts = sProducts.replaceAll("\\$\\{product_id\\}", pid);
		sProducts = sProducts.replaceAll("\\$\\{content_id\\}", cid);
		sProducts = sProducts.replaceAll("\\$\\{content_url\\}", repoUrl);
		sProducts = sProducts.replaceAll("\\$\\{product_create_ts\\}", sTS);
		log.finest("Replaced data/products.json: ["+sProducts+"]");
		JSONObject json_prov=servertasks.getProvider(this.provider_name);
		String prov_id = ((Long)json_prov.get("id")).toString();
		String s = servertasks.import_products(prov_id, sProducts);
//		Assert.assertEquals(s.startsWith("{\"name\":"), true,"Returned output should start with: {\"name\":");
		Assert.assertEquals(s.equals("[true]"), true,"Returned output should be: [true]");
	}
	
	@Test (groups={"testProviders"}, description="Update Provider Properties", dependsOnMethods="test_createProvider")
	public void test_updateProvider(){
		Date dupBefore, dupAfter;
		JSONObject json_updProv = servertasks.getProvider(provider_name);
		String upd_repo_url = "https://localhost";
		try{
			dupBefore = parseKatelloDate((String)json_updProv.get("updated_at"));
			// update - name
			json_updProv = updateProviderProperty("name", "modified-"+provider_name);
			dupAfter = parseKatelloDate((String)json_updProv.get("updated_at"));
			Assert.assertEquals(json_updProv.get("name"), "modified-"+this.provider_name,"Check updated: name");
			Assert.assertMore(dupAfter.getTime(), dupBefore.getTime(), "Check the timestamp updated");
			this.provider_name = "modified-"+this.provider_name;
			
			//update - repository_url
			dupBefore = dupAfter;
			json_updProv = updateProviderProperty("repository_url", upd_repo_url);
			dupAfter = parseKatelloDate((String)json_updProv.get("updated_at"));
			Assert.assertEquals(json_updProv.get("repository_url"), upd_repo_url,"Check updated: repository_url");
			Assert.assertMore(dupAfter.getTime(), dupBefore.getTime(), "Check the timestamp updated");

			//update - description
			dupBefore = dupAfter;
			json_updProv = updateProviderProperty("description", "Updated: provider ["+provider_name+"]");
			dupAfter = parseKatelloDate((String)json_updProv.get("updated_at"));
			Assert.assertEquals(json_updProv.get("description"), "Updated: provider ["+provider_name+"]","Check updated: description");
			Assert.assertMore(dupAfter.getTime(), dupBefore.getTime(), "Check the timestamp updated");
			
//			//update - provider_type
//			dupBefore = dupAfter;
//			json_updProv = updateProviderProperty("provider_type", "Red Hat");
//			dupAfter = parseKatelloDate((String)json_updProv.get("updated_at"));
//			Assert.assertEquals(json_updProv.get("provider_type"), "Red Hat","Check updated: provider_type");
//			Assert.assertMore(dupAfter.getTime(), dupBefore.getTime(), "Check the timestamp updated");
			// TODO - needs to be applied with additional tests for provider type-RedHat, as only 1 provider of RedHat type could be in org.
		}catch(ParseException pex){
			log.severe(pex.getMessage());
		}
	}
	
	@Test (groups={"testProviders"}, description="List all providers", dependsOnMethods="test_updateProvider")
	public void test_listProviders(){
		// Get providers json string
		String s_json_provs = servertasks.getProviders();
		JSONArray arr_provs = KatelloTestScript.toJSONArr(s_json_provs) ;
		Assert.assertMore(arr_provs.size(), 0, "Check: providers count >0");
		JSONObject json_prov;
		// we need to find our provider modified (see the test dependency)
		boolean findOurProvider = false;
		for(int i=0;i<arr_provs.size();i++){
			json_prov = (JSONObject)arr_provs.get(i);
			if(json_prov.get("name").equals(this.provider_name))
				findOurProvider = true;
		}
		// We have to have the provider found, else: error.
		Assert.assertTrue(findOurProvider, "Check: we found our provider");
	}
	
	@Test (groups={"testProviders"}, description="Delete provider",
			enabled=true) // BZ: https://bugzilla.redhat.com/show_bug.cgi?id=700423
	public void test_deleteProvider(){
		// Create separate provider to be removed 
		String uid = getUniqueID();
		String providerName = "auto-deleteMe-"+uid;
		String str_json = servertasks.createProvider(
				this.org_name,providerName, "Provider in test - "+uid,"Custom");		
		JSONObject json_prov = KatelloTestScript.toJSONObj(str_json);
		Assert.assertNotNull(json_prov, "Returned string in katello is JSON-formatted");
		
		String provider_id = ((Long)servertasks.getProvider(providerName).get("id")).toString();
		String sout = servertasks.deleteProvider(providerName);
		Assert.assertEquals(sout, "Deleted provider '"+provider_id+"'","Check: message returned by the API call");
		JSONObject obj_del = servertasks.getProvider(providerName);
		Assert.assertNull(obj_del, "Check: returned getProvider() is null");
	}
	
	private JSONObject updateProviderProperty(String component, String updValue){
		JSONObject _return = null; String retStr;
		String updProv = String.format("'provider':{'%s':'%s'}",
				component,updValue);
		String provider_id = ((Long)servertasks.getProvider(this.provider_name).get("id")).toString();
		try{Thread.sleep(1000);}catch(Exception ex){} // for the "update_at" checks
		try{
			retStr = servertasks.apiKatello_PUT(updProv,String.format(
					"/providers/%s",provider_id));
			_return = KatelloTestScript.toJSONObj(retStr);
		}catch(IOException ie){
			log.severe(ie.getMessage());
		}
		return _return;
	}

	
}
