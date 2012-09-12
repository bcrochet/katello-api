package com.redhat.qe.katello.guice;

import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.jboss.resteasy.client.ClientExecutor;
import org.jboss.resteasy.client.ProxyFactory;
import org.jboss.resteasy.client.core.executors.ApacheHttpClient4Executor;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.redhat.qe.auto.testng.TestScript;
import com.redhat.qe.katello.resource.ConsumerResource;
import com.redhat.qe.katello.resource.OrganizationResource;
import com.redhat.qe.katello.resource.PoolResource;
import com.redhat.qe.katello.resource.ProviderResource;
import com.redhat.qe.katello.resource.RepositoryResource;
import com.redhat.qe.katello.resource.SystemResource;
import com.redhat.qe.katello.resource.UserResource;
import com.redhat.qe.katello.tasks.KatelloTasks;
import com.redhat.qe.katello.tasks.impl.KatelloApiTasks;

public class KatelloApiModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(AuthCache.class).to(BasicAuthCache.class);
        bind(KatelloTasks.class).to(KatelloApiTasks.class);
        // Load properties from automation.properties
        TestScript.loadProperties();
        Names.bindProperties(binder(), System.getProperties());
    }
    
    @Provides @Named("katello.url")
    String provideKatelloUrl(
            @Named("katello.server.protocol") String protocol,
            @Named("katello.server.hostname") String hostname,
            @Named("katello.server.port") int port,
            @Named("katello.product") String product
        ) {
        return String.format("%s://%s:%d/%s/api", protocol, hostname, port, product);
    }
    
    @Provides
    OrganizationResource provideOrganizationResource(
            ClientExecutor clientExecutor, @Named("katello.url") String url) {
        return ProxyFactory.create(OrganizationResource.class, url, clientExecutor);
    }
    
    @Provides
    RepositoryResource provideRepositoryResource(
            ClientExecutor clientExecutor, @Named("katello.url") String url) {
        return ProxyFactory.create(RepositoryResource.class, url, clientExecutor);
    }

    @Provides
    ConsumerResource provideConsumerResource(
            ClientExecutor clientExecutor, @Named("katello.url") String url) {
        return ProxyFactory.create(ConsumerResource.class, url, clientExecutor);
    }

    @Provides
    UserResource provideUserResource(
            ClientExecutor clientExecutor, @Named("katello.url") String url) {
        return ProxyFactory.create(UserResource.class, url, clientExecutor);
    }

    @Provides
    PoolResource providePoolResource(
            ClientExecutor clientExecutor, @Named("katello.url") String url) {
        return ProxyFactory.create(PoolResource.class, url, clientExecutor);
    }

    @Provides
    SystemResource provideSystemResource(
            ClientExecutor clientExecutor, @Named("katello.url") String url) {
        return ProxyFactory.create(SystemResource.class, url, clientExecutor);
    }

    @Provides
    ProviderResource provideProviderResource(
            ClientExecutor clientExecutor, @Named("katello.url") String url) {
        return ProxyFactory.create(ProviderResource.class, url, clientExecutor);
    }

    @Provides
    SchemeRegistry provideSchemeRegistry() {
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));

        try {
            SSLContext sslContext = SSLContext.getInstance("SSL");

            // set up a TrustManager that trusts everything
            sslContext.init(null, new TrustManager[] { new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                @Override
                public void checkClientTrusted(java.security.cert.X509Certificate[] arg0, String arg1)
                        throws CertificateException {
                }

                @Override
                public void checkServerTrusted(java.security.cert.X509Certificate[] arg0, String arg1)
                        throws CertificateException {
                }
            } }, new SecureRandom());
            SSLSocketFactory sf = new SSLSocketFactory(sslContext);
            Scheme httpsScheme = new Scheme("https", 443, sf);

            schemeRegistry.register(httpsScheme);            
        } catch (Exception e) {
            System.err.println("HttpClient: Scheme not initialized properly");
            e.printStackTrace();
        }
        
        return schemeRegistry;
    }

    @Provides
    ClientExecutor provideClientExecutor(AuthCache authCache, SchemeRegistry schemeRegistry, @Named("katello.api.user") String username, @Named("katello.api.password") String password,
            @Named("katello.server.hostname") String hostname, @Named("katello.server.port") int port) {
        HttpParams params = new BasicHttpParams();

        PoolingClientConnectionManager cm = new PoolingClientConnectionManager(schemeRegistry);
        cm.setDefaultMaxPerRoute(10);
        HttpHost targetHost = new HttpHost(hostname, port, "https");
        DefaultHttpClient httpClient = new DefaultHttpClient(cm, params);
        httpClient.getCredentialsProvider().setCredentials(
                new AuthScope(targetHost.getHostName(), targetHost.getPort()), 
                new UsernamePasswordCredentials(username, password));

        // Generate BASIC scheme object and add it to the local auth cache
        BasicScheme basicAuth = new BasicScheme();
        authCache.put(targetHost, basicAuth);

        // Add AuthCache to the execution context
        BasicHttpContext localcontext = new BasicHttpContext();
        localcontext.setAttribute(ClientContext.AUTH_CACHE, authCache); 
            
        return new ApacheHttpClient4Executor(httpClient, localcontext);     
    }

}
