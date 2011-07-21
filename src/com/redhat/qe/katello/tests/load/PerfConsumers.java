package com.redhat.qe.katello.tests.load;

import org.testng.annotations.Test;

import com.redhat.qe.katello.api.tests.A_ConsumersTest;
import com.redhat.qe.katello.base.KatelloTestScript;
import com.redhat.qe.katello.common.KatelloDBCleaner;

public class PerfConsumers  extends KatelloTestScript{

	@Test(description="")
	public static void exhaustedPool(){
		KatelloDBCleaner.main(null); // Cleanup the DB before the execution.
		A_ConsumersTest test = new A_ConsumersTest();
		test.test_importManifest();
		for(int i=0;i<100;i++){
			test.test_createConsumer();
			test.test_subscribeConsumer();		
		}
	}
}
