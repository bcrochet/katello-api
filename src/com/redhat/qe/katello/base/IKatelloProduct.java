package com.redhat.qe.katello.base;

public interface IKatelloProduct {

	public static final String CREATE = "product create --org \"%s\" --provider \"%s\" --name \"%s\"";
	public static final String STATUS = "product status --org \"%s\" --name \"%s\"";
	
	/** Parameters:<BR>1: product_name<BR>2: org_name */
	public static final String ERR_COULD_NOT_FIND_PRODUCT = 
		"Could not find product [ %s ] within organization [ %s ]";
}
