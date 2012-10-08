package com.redhat.qe.katello.resteasy.interceptors;

import java.util.logging.Logger;

import javax.ws.rs.ext.Provider;

import net.oauth.signature.pem.PEMReader;

import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HttpContext;
import org.jboss.resteasy.annotations.interception.ClientInterceptor;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.client.core.executors.ApacheHttpClient4Executor;
import org.jboss.resteasy.spi.interception.ClientExecutionContext;
import org.jboss.resteasy.spi.interception.ClientExecutionInterceptor;

import com.google.inject.Inject;
import com.redhat.qe.katello.ssl.KatelloPemThreadLocal;
import com.redhat.qe.katello.ssl.PEMx509KeyManager;

@Provider
@ClientInterceptor
public class KatelloClientExecutionInterceptor implements ClientExecutionInterceptor {
    private final PEMx509KeyManager keyManager;
    @Inject Logger log;
    private String pem = null;
    
    @Inject 
    KatelloClientExecutionInterceptor(PEMx509KeyManager[] keyManagers) {
        this.keyManager = keyManagers[0];
    }
    
    @Override
    public ClientResponse<?> execute(ClientExecutionContext ctx) throws Exception {
        ApacheHttpClient4Executor executor = (ApacheHttpClient4Executor)ctx.getRequest().getExecutor();
        DefaultHttpClient client = (DefaultHttpClient)executor.getHttpClient();
        
        if ( pem == null ) {
            pem = KatelloPemThreadLocal.get();
            if ( pem != null ) {
                log.fine("Cert is: \n" + pem);
                int endOfFirstPart = pem.indexOf("\n", pem.indexOf("END"));
                if (endOfFirstPart == -1) {
                    throw new IllegalArgumentException("unable to parse PEM data");
                }
                String certificate = pem.substring(0, endOfFirstPart);
                String privateKey = pem.substring(endOfFirstPart);
                if (!certificate.startsWith(PEMReader.CERTIFICATE_X509_MARKER)) {
                    String tmp = privateKey;
                    privateKey = certificate;
                    certificate = tmp;
                }

                keyManager.addPEM(certificate, privateKey);
            }
        }
        
//        if ( pem != null ) {           
        HttpContext context = executor.getHttpContext();
        context.removeAttribute(ClientContext.AUTH_CACHE);
        context.removeAttribute(ClientContext.USER_TOKEN);
        context.setAttribute(ClientContext.USER_TOKEN, keyManager.chooseClientAlias(null, null, null));
        log.fine("HttpContext: " + context.toString());
        client.getCredentialsProvider().clear();
//        }
//        
        return ctx.proceed();
    }

}
