package com.redhat.qe.katello.resource.cli;

import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Variant;

public class CliResponseBuilder extends ResponseBuilder {
    private int status;
    private Object entity;
    private Locale locale;
    private Date lastModified;
    
    @Override
    public Response build() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ResponseBuilder clone() {
        return new CliResponseBuilder().status(status).entity(entity).language(locale).lastModified(lastModified);
    }

    @Override
    public ResponseBuilder status(int status) {
        this.status = status;
        return this;
    }

    @Override
    public ResponseBuilder entity(Object entity) {
        this.entity = entity;
        return this;
    }

    // No concept of MediaType for CLI
    @Override
    public ResponseBuilder type(MediaType type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ResponseBuilder type(String type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ResponseBuilder variant(Variant variant) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ResponseBuilder variants(List<Variant> variants) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ResponseBuilder language(String language) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ResponseBuilder language(Locale locale) {
        this.locale = locale;
        return this;
    }

    @Override
    public ResponseBuilder location(URI location) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ResponseBuilder contentLocation(URI location) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ResponseBuilder tag(EntityTag tag) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ResponseBuilder tag(String tag) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ResponseBuilder lastModified(Date lastModified) {
        this.lastModified = lastModified;
        return this;
    }

    @Override
    public ResponseBuilder cacheControl(CacheControl cacheControl) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ResponseBuilder expires(Date expires) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ResponseBuilder header(String name, Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ResponseBuilder cookie(NewCookie... cookies) {
        throw new UnsupportedOperationException();
    }

}
