package examples;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.GeneralSecurityException;
import java.util.List;

import org.testng.annotations.Test;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.grapher.GrapherModule;
import com.google.inject.grapher.InjectorGrapher;
import com.google.inject.grapher.graphviz.GraphvizModule;
import com.google.inject.grapher.graphviz.GraphvizRenderer;
import com.redhat.qe.katello.base.KatelloApiException;
import com.redhat.qe.katello.base.KatelloTestScript;
import com.redhat.qe.katello.base.obj.KatelloEntitlement;
import com.redhat.qe.katello.base.obj.KatelloSystem;
import com.redhat.qe.katello.common.KatelloUtils;
import com.redhat.qe.katello.guice.CertSSLContext;
import com.redhat.qe.katello.guice.KatelloApiModule;
import com.redhat.qe.katello.guice.PlainSSLContext;
import com.redhat.qe.katello.ssl.KatelloPemThreadLocal;
import com.redhat.qe.katello.ssl.PEMx509KeyManager;
import com.redhat.qe.katello.tasks.KatelloTasks;

public class DemoKatelloApi extends KatelloTestScript {
    @Inject Injector injector;
    @Inject PEMx509KeyManager keyManager;
    
    @Test(description="demo new RestEasy API")
	public void test_resteasy_api() {
        String hostname = "host" + KatelloUtils.getUniqueID() + ".example.com";
//        String organizationName = "org" + KatelloUtils.getUniqueID();
//        String environmentName = "env" + KatelloUtils.getUniqueID();
        String uuid = KatelloUtils.getUUID();
        KatelloSystem consumer;
        try {            
//            KatelloOrg org = servertasks.createOrganization(organizationName, "Org Description - " + organizationName);
//            servertasks.createEnvironment(org.getCpKey(), environmentName, "Env Description - " + environmentName, KatelloEnvironment.LIBRARY);
            for ( int i = 0; i < 2; ++i ) {
                KatelloTasks tasks = injector.getInstance(Key.get(KatelloTasks.class, PlainSSLContext.class));
                KatelloTasks tasksWithCert = injector.getInstance(Key.get(KatelloTasks.class, CertSSLContext.class));
                consumer = tasks.createConsumer("ACME_Corporation", hostname, uuid);
                KatelloPemThreadLocal.set(consumer.getIdCert().getCert() + consumer.getIdCert().getKey());
                try {
                    keyManager.addPEM(consumer.getIdCert().getCert(), consumer.getIdCert().getKey());
                } catch (GeneralSecurityException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                KatelloSystem _return = tasksWithCert.updatePackages(consumer);
                log.info("Return cert is: " + _return.getIdCert().getCert());
                String[] aliases = keyManager.getClientAliases("", null);
                for (int j = 0; j < aliases.length; ++j) {
                    log.fine(aliases[j]);
                }
                List<KatelloEntitlement> subret = tasksWithCert.subscribeConsumerWithProduct(_return.getUuid(), new String[] { "rhel6-server" } );
//            log.info(subret);
                KatelloPemThreadLocal.unset();
            }
        } catch (KatelloApiException e) {
            e.printStackTrace();
        }
    }    
    
    private static void graph(String filename, Injector demoInjector) throws IOException {
        PrintWriter out = new PrintWriter(new File(filename), "UTF-8");
        
        Injector injector = Guice.createInjector(new GrapherModule(), new GraphvizModule());
        GraphvizRenderer renderer = injector.getInstance(GraphvizRenderer.class);
        renderer.setOut(out).setRankdir("TB");
        
        injector.getInstance(InjectorGrapher.class)
            .of(demoInjector)
            .graph();
    }
    
    public static void main(String[] args) {
        Injector injector = Guice.createInjector(new KatelloApiModule());
        KatelloTasks tasks = injector.getInstance(Key.get(KatelloTasks.class, CertSSLContext.class));
        try {
            tasks.getOrganizations();
        } catch (KatelloApiException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
