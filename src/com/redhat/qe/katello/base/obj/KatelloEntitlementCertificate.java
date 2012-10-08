package com.redhat.qe.katello.base.obj;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown=true)
public class KatelloEntitlementCertificate {
    private String id;
    private String key;
    private String cert;
    
    public KatelloEntitlementCertificate() {        
    }
        
    @JsonProperty("id")
    public String getId() {
        return id;
    }
    
    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    @JsonProperty("key")
    public String getKey() {
        return key;
    }

    @JsonProperty("key")
    public void setKey(String key) {
        this.key = key;
    }

    @JsonProperty("cert")
    public String getCert() {
        return cert;
    }

    @JsonProperty("cert")
    public void setCert(String cert) {
        this.cert = cert;
    }
    
}
