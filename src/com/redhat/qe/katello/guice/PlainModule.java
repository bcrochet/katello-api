package com.redhat.qe.katello.guice;

import java.lang.annotation.Annotation;
import java.security.SecureRandom;
import java.util.LinkedList;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.HttpClient;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HttpContext;
import org.jboss.resteasy.client.ClientExecutor;
import org.jboss.resteasy.client.core.executors.ApacheHttpClient4Executor;
import org.jboss.resteasy.spi.interception.ClientExecutionInterceptor;

import com.google.inject.Provides;
import com.google.inject.name.Named;

public class PlainModule extends ClientFactoryModule {
    public PlainModule(Class<? extends Annotation> annotation) {
        super(annotation);
    }
    
    @Provides
    SSLContext provideSSLContext(TrustManager[] trustManagers, SecureRandom secureRandom) {
        SSLContext clientContext = null;

        // Initialize the SSLContext to work with our key managers.
        try {
            clientContext = SSLContext.getInstance(SSLSocketFactory.TLS);
            clientContext.init(null, trustManagers, secureRandom);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return clientContext;
    }

    @Provides
    ClientExecutor provideClientExecutor(HttpClient httpClient, HttpContext context,
            AuthCache authCache,
            @Named("katello.server.hostname") String hostname,
            @Named("katello.server.port") int port,
            @Named("katello.api.user") String username,
            @Named("katello.api.password") String password) {
        ApacheHttpClient4Executor executor = new ApacheHttpClient4Executor(httpClient, context);
        HttpHost targetHost = new HttpHost(hostname, port, "https");
        ((DefaultHttpClient)httpClient).getCredentialsProvider().setCredentials(
                new AuthScope(targetHost.getHostName(), targetHost.getPort()), 
                new UsernamePasswordCredentials(username, password));

        // Generate BASIC scheme object and add it to the local auth cache
        BasicScheme basicAuth = new BasicScheme();
        authCache.put(targetHost, basicAuth);

        // Add AuthCache to the execution context
        executor.getHttpContext().setAttribute(ClientContext.AUTH_CACHE, authCache); 
        
        return executor;
    }
   
    @Provides
    List<ClientExecutionInterceptor> provideClientExecutionInterceptors() {
        List<ClientExecutionInterceptor> interceptors = new LinkedList<ClientExecutionInterceptor>();
        return interceptors;
    }
}
