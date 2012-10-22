package com.redhat.qe.katello.resource.cli;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.management.Attribute;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.jboss.resteasy.client.ClientResponse;

import com.google.inject.Inject;
import com.redhat.qe.katello.base.KatelloCli;
import com.redhat.qe.katello.base.obj.KatelloEnvironment;
import com.redhat.qe.katello.base.obj.KatelloOrg;
import com.redhat.qe.katello.base.obj.KatelloProduct;
import com.redhat.qe.katello.base.obj.KatelloProvider;
import com.redhat.qe.katello.base.obj.KatelloUser;
import com.redhat.qe.katello.resource.OrganizationResource;
import com.redhat.qe.tools.SSHCommandResult;

public class CliOrganizationResource implements OrganizationResource {
    protected KatelloUser user;
    protected ArrayList<Attribute> opts;

    @Inject
    CliOrganizationResource(KatelloUser user) {
        this.user = user;
    }
    
    @Override
    @GET
    @Produces("application/json")
    public ClientResponse<List<KatelloOrg>> list() {
        String CLI_CMD_LIST = "org list";
        KatelloCli cli;
        if(user == null) 
            cli = new KatelloCli(CLI_CMD_LIST, opts); // as default admin
        else 
            cli = new KatelloCli(CLI_CMD_LIST, opts, user); // as the user specified
        SSHCommandResult response = cli.run();
        
        List<KatelloOrg> orgs = new ArrayList<KatelloOrg>();
        
        return new CliClientResponse<List<KatelloOrg>>();
    }

    @Override
    @GET
    @Path("/{id}")
    @Produces("application/json")
    public ClientResponse<KatelloOrg> getOrganization(@PathParam("id") String id) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    @GET
    @Path("/{id}/environments")
    @Produces("application/json")
    public ClientResponse<List<KatelloEnvironment>> listEnvironments(@PathParam("id") String orgId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    @DELETE
    @Path("/{id}/environments/{envId}")
    public ClientResponse<String> deleteEnvironment(@PathParam("id") String orgId,
            @PathParam("envId") Long envId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    @PUT
    @Path("/{id}/environments/{envId}")
    @Produces("application/json")
    @Consumes("application/json")
    public ClientResponse<KatelloEnvironment> updateEnvironment(@PathParam("id") String id,
            @PathParam("envId") Long envId, Map<String, Object> updEnv) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    @POST
    @Consumes("application/json")
    @Produces("application/json")
    public ClientResponse<KatelloOrg> create(Map<String, Object> orgPost) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    @POST
    @Path("/{id}/environments/")
    @Consumes("application/json")
    @Produces("application/json")
    public ClientResponse<KatelloEnvironment> createEnvironment(@PathParam("id") String cpKey,
            Map<String, Object> env) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    @GET
    @Path("/{id}/products/")
    @Produces("application/json")
    public ClientResponse<List<KatelloProduct>> listProducts(@PathParam("id") String cpKey) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    @GET
    @Path("/{id}/providers/")
    @Produces("application/json")
    public ClientResponse<List<KatelloProvider>> listProviders(@PathParam("id") String cpKey) {
        // TODO Auto-generated method stub
        return null;
    }

}
