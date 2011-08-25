package com.redhat.qe.katello.tests.api;

import java.util.logging.Logger;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.testng.annotations.Test;

import com.redhat.qe.auto.testng.Assert;
import com.redhat.qe.katello.base.KatelloTestScript;

public class UsersTest extends KatelloTestScript {
	protected static Logger log = 
		Logger.getLogger(UsersTest.class.getName());

	public UsersTest() {
		super();
	}
	
	private String username_disabled;
	private String username_enabled;

	@Test(groups = { "testUsers" }, description = "Create user (disabled)")
	public void test_createUserDisabled(){
		String pid = KatelloTestScript.getUniqueID();
		this.username_disabled = "user_"+pid;
		String s = servertasks.createUser(this.username_disabled, "redhat", true);
		JSONObject juser = KatelloTestScript.toJSONObj(s);
		Assert.assertNotNull(juser.get("id"), "Check: not null returned: id");
		Boolean disabled = (Boolean)juser.get("disabled");
		Assert.assertTrue(disabled.booleanValue(), "Check: returned value: disabled=true");
		
		log.info("Preparing disabled user: ["+this.username_disabled+"]");
	}
	
	@Test(groups = { "testUsers" }, description = "Create user (enabled)")
	public void test_createUserEnabled(){
		String pid = KatelloTestScript.getUniqueID();
		this.username_enabled = "user_"+pid;
		String s = servertasks.createUser(this.username_enabled, "redhat", false);
		JSONObject juser = KatelloTestScript.toJSONObj(s);
		Assert.assertNotNull(juser.get("id"), "Check: not null returned: id");
		Boolean disabled = (Boolean)juser.get("disabled");
		Assert.assertFalse(disabled.booleanValue(), "Check: returned value: disabled=false");
		
		log.info("Preparing enabled user: ["+this.username_enabled+"]");
	}
	
	@Test(dependsOnMethods={"test_createUserDisabled","test_createUserEnabled"},
			groups = { "testUsers" }, description = "Get all users")
	public void test_getUsers(){
		String _ret = servertasks.apiKatello_GET("/users");
		Assert.assertTrue(_ret.contains("\"username\":\"admin\""), "Check: \"admin\" user exists");
		JSONArray users = KatelloTestScript.toJSONArr(_ret);
		JSONObject tmpUsr;
		boolean userFound_D=false, userFound_E=false;
		for(int i=0;i<users.size();i++){
			tmpUsr = (JSONObject)users.get(i);
			if(tmpUsr.get("username").equals(this.username_enabled)){
				userFound_E = true;
				Assert.assertFalse(((Boolean)tmpUsr.get("disabled")).booleanValue(), "Check: enabled user's disabled flag.");
			}
			if(tmpUsr.get("username").equals(this.username_disabled)){
				userFound_D = true;
				Assert.assertTrue(((Boolean)tmpUsr.get("disabled")).booleanValue(), "Check: disabled user's disabled flag.");
			}				
		}
		Assert.assertTrue((userFound_D && userFound_E), "Check: both users should be found in the list");
	}
	
}
