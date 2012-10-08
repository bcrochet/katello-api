package com.redhat.qe.katello.guice;

import java.lang.annotation.Annotation;
import java.security.SecureRandom;
import java.util.LinkedList;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.protocol.HttpContext;
import org.jboss.resteasy.client.ClientExecutor;
import org.jboss.resteasy.client.core.executors.ApacheHttpClient4Executor;
import org.jboss.resteasy.spi.interception.ClientExecutionInterceptor;

import com.google.inject.Inject;
import com.google.inject.Provides;
import com.redhat.qe.katello.resteasy.interceptors.KatelloClientExecutionInterceptor;
import com.redhat.qe.katello.ssl.PEMx509KeyManager;

public class CertModule extends ClientFactoryModule {
    @Inject private PEMx509KeyManager[] keyManagers;
    @Inject private TrustManager[] trustManagers;
    @Inject private SecureRandom secureRandom;
    
    public CertModule(Class<? extends Annotation> annotation) {
        super(annotation);
    }

    @Provides
    SSLContext provideCertSSLContext(PEMx509KeyManager[] keyManagers, TrustManager[] trustManagers, SecureRandom secureRandom) {
        SSLContext clientContext = null;

        // Initialize the SSLContext to work with our key managers.
        try {
            clientContext = SSLContext.getInstance(SSLSocketFactory.TLS);
            clientContext.init(keyManagers, trustManagers, secureRandom);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return clientContext;
    }
    
    @Provides
    ClientExecutor provideClientExecutor(HttpClient httpClient, HttpContext context) {
        ApacheHttpClient4Executor executor = new ApacheHttpClient4Executor(httpClient, context);
        
        return executor;
    }
    
    @Provides
    List<ClientExecutionInterceptor> provideClientExecutionInterceptors(KatelloClientExecutionInterceptor katelloClientExecutionInterceptor) {
        List<ClientExecutionInterceptor> interceptors = new LinkedList<ClientExecutionInterceptor>();
        interceptors.add(katelloClientExecutionInterceptor);
        return interceptors;
    }
}
