package com.redhat.qe.katello.guice;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.client.AuthCache;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.jboss.resteasy.client.ClientExecutor;
import org.jboss.resteasy.client.ClientRequestFactory;
import org.jboss.resteasy.client.core.ClientInterceptorRepository;
import org.jboss.resteasy.spi.interception.ClientExecutionInterceptor;

import com.google.inject.PrivateModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
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
import com.redhat.qe.katello.ssl.PEMx509KeyManager;
import com.redhat.qe.katello.tasks.KatelloTasks;
import com.redhat.qe.katello.tasks.impl.KatelloApiTasks;

public abstract class ClientFactoryModule extends PrivateModule {
    private final Class<? extends Annotation> annotation;
    
    ClientFactoryModule(Class<? extends Annotation> annotation) {
        this.annotation = annotation;
    }
    
    @Override
    protected void configure() {
        bind(AuthCache.class).to(BasicAuthCache.class);
        bind(HttpParams.class).to(BasicHttpParams.class);
        bind(HttpContext.class).to(BasicHttpContext.class);

        // Load properties from automation.properties
        TestScript.loadProperties();
        Names.bindProperties(binder(), System.getProperties());

        bind(KatelloTasks.class).annotatedWith(annotation).to(KatelloApiTasks.class);
        expose(KatelloTasks.class).annotatedWith(annotation);
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
    OrganizationResource provideOrganizationResource(ClientRequestFactory clientRequestFactory) {
        return clientRequestFactory.createProxy(OrganizationResource.class);
    }
    
    @Provides
    RepositoryResource provideRepositoryResource(ClientRequestFactory clientRequestFactory) {
        return clientRequestFactory.createProxy(RepositoryResource.class);
    }

    @Provides
    ConsumerResource provideConsumerResource(ClientRequestFactory clientRequestFactory) {
        return clientRequestFactory.createProxy(ConsumerResource.class);
    }

    @Provides
    UserResource provideUserResource(ClientRequestFactory clientRequestFactory) {
        return clientRequestFactory.createProxy(UserResource.class);
    }

    @Provides
    PoolResource providePoolResource(ClientRequestFactory clientRequestFactory) {
        return clientRequestFactory.createProxy(PoolResource.class);
    }

    @Provides
    SystemResource provideSystemResource(ClientRequestFactory clientRequestFactory) {
        return clientRequestFactory.createProxy(SystemResource.class);
    }

    @Provides
    ProviderResource provideProviderResource(ClientRequestFactory clientRequestFactory) {
        return clientRequestFactory.createProxy(ProviderResource.class);
    }        
    
    @Provides @Singleton
    PEMx509KeyManager[] provideKeyManagers() {
        PEMx509KeyManager[] managers = new PEMx509KeyManager[1];
        managers[0] = new PEMx509KeyManager();

        return managers;        
    }

    @Provides @Singleton
    TrustManager[] provideTrustManagers() {
        return new TrustManager[] { new X509TrustManager() {
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
        } };
    }

    @Provides
    SSLSocketFactory provideSSLSocketFactory(SSLContext sslContext) throws Exception {
        return new SSLSocketFactory(sslContext);
    }

    @Provides
    SchemeRegistry provideSchemeRegistry(SSLSocketFactory sslSocketFactory) {
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", 80, PlainSocketFactory.getSocketFactory()));

        try {;
            Scheme httpsScheme = new Scheme("https", 443, sslSocketFactory);

            schemeRegistry.register(httpsScheme);            
        } catch (Exception e) {
            System.err.println("HttpClient: Scheme not initialized properly");
            e.printStackTrace();
        }
        
        return schemeRegistry;
    }

    @Provides
    ClientConnectionManager provideClientConnectionManager(SchemeRegistry schemeRegistry) {
        PoolingClientConnectionManager cm = new PoolingClientConnectionManager(schemeRegistry);
        cm.setDefaultMaxPerRoute(20);
//        BasicClientConnectionManager cm = new BasicClientConnectionManager(schemeRegistry);
        return cm;
    }
    
    @Provides
    HttpResponseInterceptor[] provideHttpResponseInterceptors() {
        return new HttpResponseInterceptor[] {
                new HttpResponseInterceptor() {            
                    @Override
                    public void process(HttpResponse response, HttpContext context)
                            throws HttpException, IOException {
                        if ( response.getStatusLine().getStatusCode() == 401 && !response.containsHeader("WWW-Authenticate")) {
                            response.addHeader("WWW-Authenticate", "realm='Basic security'");
                        }
                    }            
                },
                new HttpResponseInterceptor() {
                    @Override
                    public void process(HttpResponse response, HttpContext context)
                            throws HttpException, IOException {
                        Header[] contentTypes = response.getHeaders("Content-Type");
                        for (int i = 0; i < contentTypes.length; ++i) {
                            if (contentTypes[i].getValue().startsWith("json")) {
                                String fixed = contentTypes[i].getValue().replaceFirst("json", "application/json");
                                response.removeHeader(contentTypes[i]);
                                response.addHeader("Content-Type", fixed);
                            }
                        }                
                    }
                }
        };
    }
    
    @Provides
    HttpClient provideHttpClient(ClientConnectionManager clientConnectionManager, HttpParams params, HttpResponseInterceptor[] responseInterceptors) {
        DefaultHttpClient httpClient = new DefaultHttpClient(clientConnectionManager, params);
        for (int i = 0; i < responseInterceptors.length; ++i) {
            httpClient.addResponseInterceptor(responseInterceptors[i]);
        }
        return httpClient;
    }
    
    @Provides
    ClientRequestFactory provideClientRequestFactory(ClientExecutor clientExecutor, List<ClientExecutionInterceptor> clientExecutionInterceptors, @Named("katello.url") String url) {
        ClientRequestFactory clientRequestFactory = null;
        try {
            clientRequestFactory = new ClientRequestFactory(clientExecutor, new URI(url));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        ClientInterceptorRepository interceptors = clientRequestFactory.getPrefixInterceptors();
        for (ClientExecutionInterceptor interceptor : clientExecutionInterceptors) {
            interceptors.registerInterceptor(interceptor);
        }
        return clientRequestFactory;
    }    
    
}
