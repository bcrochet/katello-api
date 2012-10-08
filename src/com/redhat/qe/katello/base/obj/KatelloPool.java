package com.redhat.qe.katello.base.obj;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown=true)
public class KatelloPool {
    private String id;
    private KatelloOwner owner;
    private String productName;
//    private Boolean activeSubscription = Boolean.TRUE;
//    private String subscriptionId;
//    private String subscriptionSubKey;
//    private KatelloEntitlement sourceEntitlement;
//    private Long quantity;
//    private Date startDate;
//    private Date endDate;
//    private String productId;
//    private String restrictedToUsername;
//    private String contractNumber;
//    private String accountNumber;
//    private Long consumed;
//    private Long exported;

    public KatelloPool() {}
    
//    public KatelloPool(JSONObject json) {
//        this.id = (String)json.get("id");
//        JSONObject jown = (JSONObject)json.get("owner");
//        this.owner = new KatelloOwner((String)jown.get("id"), (String)jown.get("key"), (String)jown.get("displayName"), (String)jown.get("href"));
//    }
    
    @JsonProperty("id")
    public String getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }
    
    @JsonProperty("owner")
    public KatelloOwner getOwner() {
        return owner;
    }
    
    @JsonProperty("owner")
    public void setOwner(KatelloOwner owner) {
        this.owner = owner;
    }
    
    public String getProductName() {
        return productName;
    }
    
    public void setProductName(String productName) {
        this.productName = productName;
    }
}
