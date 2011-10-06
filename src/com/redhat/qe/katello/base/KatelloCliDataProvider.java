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
				{ "aa", null, null, new Integer(0), "Successfully created provider [ aa ]"},
				{ "11", null, null, new Integer(0), "Successfully created provider [ 11 ]"},
				{ "1a", null, null, new Integer(0), "Successfully created provider [ 1a ]"},
				{ "a1", null, null, new Integer(0), "Successfully created provider [ a1 ]"},
				{ strRepeat("0123456789", 12)+"abcdefgh", null, null, new Integer(0), "Successfully created provider [ "+strRepeat("0123456789", 12)+"abcdefgh"+" ]"},
				{ "prov-"+uid, null, null, new Integer(0), "Successfully created provider [ prov-"+uid+" ]"},
				{ "prov "+uid, "Provider with space in name", null, new Integer(0), "Successfully created provider [ prov "+uid+" ]"},
				{ null, null, null, new Integer(2), "katello: error: Option --name is required; please see --help"},
				{ " ", null, null, new Integer(144), "Validation failed: Name can't be blank"},
				{ " a", null, null, new Integer(144), "Validation failed: Name must not contain leading or trailing white spaces."},
				{ "a ", null, null, new Integer(144), "Validation failed: Name must not contain leading or trailing white spaces."},
				{ "a", null, null, new Integer(144), "Validation failed: Name must contain at least 2 characters"},
				{ "?1", null, null, new Integer(144), "Validation failed: Name cannot contain characters other than alpha numerals, space,'_', '-'."},
				{ strRepeat("0123456789", 12)+"abcdefghi", null, null, new Integer(144), "Validation failed: Name cannot contain more than 128 characters"},
				// description
				{ "desc-specChars"+uid, "\\!@%^&*(<_-~+=//\\||,.>)", null, new Integer(0), "Successfully created provider [ desc-specChars"+uid+" ]"},
				{ "desc-255Chars"+uid, strRepeat("0123456789", 25)+"abcde", null, new Integer(0), "Successfully created provider [ desc-255Chars"+uid+" ]"},
				{ "desc-256Chars"+uid, strRepeat("0123456789", 25)+"abcdef", null, new Integer(144), "Validation failed: Description cannot contain more than 255 characters"},
				// url
				{ "url-httpOnly"+uid, null, "http://", new Integer(0), "Successfully created provider [ url-httpOnly"+uid+" ]"},
				{ "url-httpsOnly"+uid, null, "https://", new Integer(0), "Successfully created provider [ url-httpsOnly"+uid+" ]"},
				{ "url-redhatcom"+uid, null, "http://redhat.com/", new Integer(0), "Successfully created provider [ url-redhatcom"+uid+" ]"},
				{ "url-with_space"+uid, null, "http://url with space/", new Integer(0), "Successfully created provider [ url-with_space"+uid+" ]"},
				// misc
				{ "duplicate"+uid, null, null, new Integer(0), "Successfully created provider [ duplicate"+uid+" ]"},
				{ "duplicate"+uid, null, null, new Integer(144), "Validation failed: Name has already been taken"}
		};		
	}
	
	@DataProvider(name="provider_create_diffType")
	public static Object[][] provider_create_diffType(){
		return new Object[][] {
				{ "C", new Integer(2), "katello: error: option --type: invalid choice: 'C' (choose from 'redhat', 'custom')"},
				{ "Custom", new Integer(2), "katello: error: option --type: invalid choice: 'Custom' (choose from 'redhat', 'custom')"},
				{ "CUSTOM", new Integer(2), "katello: error: option --type: invalid choice: 'CUSTOM' (choose from 'redhat', 'custom')"},
				{ "rh", new Integer(2), "katello: error: option --type: invalid choice: 'rh' (choose from 'redhat', 'custom')"},
				{ "RedHat", new Integer(2), "katello: error: option --type: invalid choice: 'RedHat' (choose from 'redhat', 'custom')"},
				{ "REDHAT", new Integer(2), "katello: error: option --type: invalid choice: 'REDHAT' (choose from 'redhat', 'custom')"},
				{ "^custom", new Integer(2), "katello: error: option --type: invalid choice: '^custom' (choose from 'redhat', 'custom')"},
				{ " custom", new Integer(2), "katello: error: option --type: invalid choice: ' custom' (choose from 'redhat', 'custom')"},
				{ "custom ", new Integer(2), "katello: error: option --type: invalid choice: 'custom ' (choose from 'redhat', 'custom')"}
				
		};		
	}

	public static String strRepeat(String src, int times){
		String res = "";
		for(int i=0;i<times; i++)
			res = res + src; 
		return res;
	}
}
