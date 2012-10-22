package com.redhat.qe.katello.resource.cli;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;

import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.spi.Link;
import org.jboss.resteasy.spi.LinkHeader;
import org.jboss.resteasy.util.GenericType;

@SuppressWarnings("unchecked")
public class CliClientResponse<T> extends ClientResponse<T> {
    private Object entity;
    
    // Not supported, but we just want empty value. May add other metadata here
    @Override
    public MultivaluedMap<String, String> getHeaders() {        
        return null;
    }

    @Override
    public Status getResponseStatus() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public T getEntity() {
        return (T)this.entity;
    }

    @Override
    public <T2> T2 getEntity(Class<T2> type) {
        return (T2)this.entity.getClass().asSubclass(type);
    }

    @Override
    public <T2> T2 getEntity(Class<T2> type, Type genericType) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T2> T2 getEntity(Class<T2> type, Type genericType, Annotation[] annotations) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T2> T2 getEntity(GenericType<T2> type) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T2> T2 getEntity(GenericType<T2> type, Annotation[] annotations) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public LinkHeader getLinkHeader() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Link getLocation() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Link getHeaderAsLink(String headerName) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void resetStream() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void releaseConnection() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public Map getAttributes() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getStatus() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public MultivaluedMap<String, Object> getMetadata() {
        // TODO Auto-generated method stub
        return null;
    }


}
