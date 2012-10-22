package com.redhat.qe.katello.guice;

import com.google.inject.AbstractModule;
import com.redhat.qe.katello.resource.OrganizationResource;
import com.redhat.qe.katello.resource.cli.CliOrganizationResource;
import com.redhat.qe.katello.tasks.KatelloTasks;
import com.redhat.qe.katello.tasks.impl.KatelloCliTasks;

public class KatelloCliModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(KatelloTasks.class).to(KatelloCliTasks.class);
        bind(OrganizationResource.class).to(CliOrganizationResource.class);
    }

}
