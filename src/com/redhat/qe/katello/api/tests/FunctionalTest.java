package com.redhat.qe.katello.api.tests;

import java.io.IOException;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import com.redhat.qe.auto.testng.Assert;
import com.redhat.qe.katello.base.KatelloTestScript;
import com.redhat.qe.katello.common.KatelloDBCleaner;

public class FunctionalTest  extends KatelloTestScript{

	public static final String EXPORT_ZIP_PATH = 
		System.getProperty("user.dir") + "/data/export.zip";

	public FunctionalTest(){
		super();
	}
	
	@BeforeClass(description="Cleanup DBs before run", alwaysRun=true, enabled=false) // <--- Change to 
	public void setup() throws Exception{
		KatelloDBCleaner.main(null);
	}
		
	@Test(description="Import subscriptions/entitlements", enabled=true)
	public void import_subscriptions() throws IOException{
		servertasks.createProvider(
				default_org, "Export_Manifest1", "Provider for importing export.zip",
				"Red Hat","http://localhost");
		String ret = servertasks.apiKatello_POST_manifest(EXPORT_ZIP_PATH, "/providers/Export_Manifest/import_manifest");
		Assert.assertEquals(ret, "Manifest imported","Output should be: \"Manifest imported\"");
	}
	
}
