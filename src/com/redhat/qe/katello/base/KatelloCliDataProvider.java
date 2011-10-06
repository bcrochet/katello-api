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
	
	/**
	 * Object[] contains of:<BR>
	 * provider:<BR>
	 * &nbsp;&nbsp;name<br>
	 * &nbsp;&nbsp;description<br>
	 * &nbsp;&nbsp;url<br>
	 * &nbsp;&nbsp;exit_code<br>
	 * &nbsp;&nbsp;output
	 */
	@DataProvider(name="provider_create")
	public static Object[][] provider_create(){
		// TODO - the cases with unicode characters still missing - there 
		// is a bug: to display that characters.
		String uid = KatelloTestScript.getUniqueID();
		return new Object[][] {
				// name
				{ "aa", null, null, new Integer(0), "Successfully created provider [ prov-"+uid+" ]"},
				{ "11", null, null, new Integer(0), "Successfully created provider [ prov-"+uid+" ]"},
				{ "1a", null, null, new Integer(0), "Successfully created provider [ prov-"+uid+" ]"},
				{ "a1", null, null, new Integer(0), "Successfully created provider [ prov-"+uid+" ]"},
				{ strRepeat("0123456789", 12)+"abcdefgh", null, null, new Integer(0), "Successfully created provider [ prov-"+uid+" ]"},
				{ "prov-"+uid, null, null, new Integer(0), "Successfully created provider [ prov-"+uid+" ]"},
				{ "prov "+uid, "Provider with space in name", null, new Integer(0), "Successfully created provider [ prov "+uid+" ]"},
				{ null, null, null, new Integer(2), "katello: error: Option --name is required; please see --help"},
				{ " ", null, null, new Integer(144), "Validation failed: Name can't be blank, Name must not contain leading or trailing white spaces., Name must contain at least 2 characters"},
				{ " a", null, null, new Integer(144), "Validation failed: Name must not contain leading or trailing white spaces."},
				{ "a ", null, null, new Integer(144), "Validation failed: Name must not contain leading or trailing white spaces."},
				{ "a", null, null, new Integer(144), "Validation failed: Name must contain at least 2 characters"},
				{ "?1", null, null, new Integer(144), "Validation failed: Name cannot contain characters other than alpha numerals, space,'_', '-'."},
				{ strRepeat("0123456789", 12)+"abcdefghi", null, null, new Integer(144), "Validation failed: Name cannot contain more than 128 characters"},
				// Description
				{ "desc-specChars"+uid, "\\!@%^&*(<_-~+=//\\||,.>)", null, new Integer(0), "Successfully created provider [ prov-"+uid+" ]"},
				{ "desc-255Chars"+uid, strRepeat("0123456789", 25)+"abcde", null, new Integer(0), "Successfully created provider [ prov-"+uid+" ]"},
				{ "desc-256Chars"+uid, strRepeat("0123456789", 25)+"abcdef", null, new Integer(144), "Validation failed: Description cannot contain more than 255 characters"}
				
		};		
	}
	
	public static String strRepeat(String src, int times){
		String res = "";
		for(int i=0;i<times; i++)
			res = res + src; 
		return res;
	}
}
