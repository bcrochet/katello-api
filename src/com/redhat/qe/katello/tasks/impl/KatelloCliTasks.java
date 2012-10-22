package com.redhat.qe.katello.tasks.impl;

import java.util.List;
import java.util.Map;

import org.jboss.resteasy.client.ClientResponse;

import com.google.inject.Inject;
import com.redhat.qe.katello.base.KatelloApiException;
import com.redhat.qe.katello.base.obj.KatelloEntitlement;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloPool;
import com.redhat.qe.katello.base.obj.KatelloProduct;
import com.redhat.qe.katello.base.obj.KatelloProvider;
import com.redhat.qe.katello.base.obj.KatelloRepo;
import com.redhat.qe.katello.base.obj.KatelloSystem;
import com.redhat.qe.katello.base.obj.KatelloUser;
import com.redhat.qe.katello.resource.OrganizationResource;
import com.redhat.qe.katello.tasks.KatelloTasks;

public class KatelloCliTasks implements KatelloTasks {
    protected OrganizationResource orgResource;
    
    @Inject
    KatelloCliTasks(OrganizationResource orgResource) {
        this.orgResource = orgResource;
    }
    
    @Override
    public String uploadManifest(Long providerId, String exportZipPath) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public KatelloEnvironment getEnvironment(String orgName, String envName) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String deleteEnvironment(String orgName, String envName)
            throws KatelloApiException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public KatelloEnvironment getEnvFromOrgList(String orgName, String envName) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<KatelloOrg> getOrganizations() throws KatelloApiException {
        ClientResponse<List<KatelloOrg>> _return = orgResource.list();
        if ( _return.getStatus() > 0 ) throw new KatelloApiException(_return);
        return _return.getEntity();
    }

    @Override
    public KatelloOrg getOrganization(String organizationKey) throws KatelloApiException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public KatelloSystem getConsumer(String consumer_id) throws KatelloApiException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<KatelloProduct> getProductsByOrg(String org_name)
            throws KatelloApiException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Long> getSerials(String consumerId) throws KatelloApiException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<KatelloEnvironment> getEnvironments(String org_name)
            throws KatelloApiException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<KatelloProvider> listProviders(String org_name) throws KatelloApiException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public KatelloUser createUser(String username, String email, String password,
            boolean disabled) throws KatelloApiException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<KatelloUser> listUsers() throws KatelloApiException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public KatelloUser getUser(Long userId) throws KatelloApiException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public KatelloOrg createOrganization(String org_name, String org_description)
            throws KatelloApiException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public KatelloEnvironment createEnvironment(String orgKey, String env_name,
            String env_descr, String prior) throws KatelloApiException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public KatelloProvider createProvider(String orgName, String providerName,
            String description, String type, String url) throws KatelloApiException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public KatelloProduct createProduct(String org_name, String provider_name,
            String productName, String productDescription, String productUrl)
            throws KatelloApiException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public KatelloRepo createRepository(String providerName, String candlepin_id,
            String repo_name, String repo_url) throws KatelloApiException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<KatelloProduct> import_products(String orgName, String providerName,
            Map<String, Object> products) throws KatelloApiException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<KatelloProduct> listProducts(String orgName) throws KatelloApiException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public KatelloProduct getProductByOrg(String orgName, String productName)
            throws KatelloApiException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String subscribeConsumer(String consumerId) throws KatelloApiException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<KatelloEntitlement> subscribeConsumerWithPool(String consumerId,
            String poolId) throws KatelloApiException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public KatelloSystem subscribeConsumerViaSystem(String consumerId, String poolId)
            throws KatelloApiException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public KatelloProvider getProvider(String org_name, String byName)
            throws KatelloApiException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getPool(String poolName) throws KatelloApiException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<KatelloPool> getPools() throws KatelloApiException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String deleteProvider(KatelloProvider provider) throws KatelloApiException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public KatelloSystem createConsumer(String orgName, String hostname, String uuid)
            throws KatelloApiException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public KatelloSystem updateFacts(String consumerId, String component, String updValue)
            throws KatelloApiException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public KatelloEnvironment updateEnvProperty(String organizationName,
            String environmentName, String component, Object updValue)
            throws KatelloApiException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public KatelloProvider updateProviderProperty(String organizationName,
            String providerName, String component, Object updValue)
            throws KatelloApiException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String updateUser(Long userId, String component, Object updValue)
            throws KatelloApiException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public KatelloSystem updatePackages(KatelloSystem consumer) throws KatelloApiException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String deleteUser(Long userId) throws KatelloApiException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String deleteConsumer(String consumerId) throws KatelloApiException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public KatelloSystem unsubscribeConsumer(String consumerId, String serial)
            throws KatelloApiException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public KatelloSystem unsubscribeConsumer(String consumerId) throws KatelloApiException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Long getEnvironmentPriorId(KatelloEnvironment env) {
        // TODO Auto-generated method stub
        return null;
    }

}
