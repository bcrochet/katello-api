package com.redhat.qe.katello.base;

import java.util.Random;

import org.testng.annotations.DataProvider;

public class KatelloCliDataProvider {

	
	@DataProvider(name = "org_create")
	public static Object[][] org_create(){
		String uniqueID1 = KatelloTestScript.getUniqueID();
		try{Thread.sleep(1000+Math.abs(new Random().nextInt(500)));}catch(InterruptedException iex){};
		String uniqueID2 = KatelloTestScript.getUniqueID();
		return new Object[][] {
				{ "orgNoDescr_"+uniqueID1, null },
				{ "\"org "+uniqueID2+"\"", "\"Org with space\""}
		};
	}
}
