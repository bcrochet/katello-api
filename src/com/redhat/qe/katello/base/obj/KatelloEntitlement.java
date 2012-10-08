package com.redhat.qe.katello.base.obj;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown=true)
public class KatelloEntitlement {
    private String id;
    private KatelloPool pool;
    private List<KatelloEntitlementCertificate> certificates = new ArrayList<KatelloEntitlementCertificate>();
    private KatelloSystem consumer;
//    private Date startDate;
//    private Date endDate;
//    private Integer quantity;
//    private String accountNumber;
//    private String contractNumber;

    public KatelloEntitlement() {}
//    public KatelloEntitlement(JSONObject json) {
//        this.id = (String)json.get("id");
//        this.pool = new KatelloPool((JSONObject)json.get("pool"));
//        JSONArray certs = (JSONArray)json.get("certificates");
//        for (int i = 0; i < certs.size(); ++i) {
//            JSONObject cert = (JSONObject)certs.get(i);
//            certificates.add(new KatelloEntitlementCertificate((String)cert.get("id")));
//        }
//    }
    
    @JsonProperty("id")
    public String getId() {
        return id;
    }
    
    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }
    
    @JsonProperty("pool")
    public KatelloPool getPool() {
        return pool;
    }
    
    @JsonProperty("pool")
    public void setPool(KatelloPool pool) {
        this.pool = pool;
    }
    
    @JsonProperty("certificates")
    public List<KatelloEntitlementCertificate> getCertificates() {
        return certificates;
    }
    
    @JsonProperty("certificates")
    public void setCertificates(List<KatelloEntitlementCertificate> certificates) {
        this.certificates = certificates;
    }

    @JsonProperty("consumer")
    public KatelloSystem getConsumer() {
        return consumer;
    }

    @JsonProperty("consumer")
    public void setConsumer(KatelloSystem consumer) {
        this.consumer = consumer;
    }
}
