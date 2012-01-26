package com.redhat.qe.katello.base;

public interface IKatelloProduct {

	public static final String CREATE_NOURL = "product create --org \"%s\" --provider \"%s\" --name \"%s\"";
	public static final String CREATE = "product create --org \"%s\" --provider \"%s\" --name \"%s\" --url \"%s\" --assumeyes";
	public static final String STATUS = "product status --org \"%s\" --name \"%s\"";
	public static final String PROMOTE = "product promote --org \"%s\" --name \"%s\" --environment \"%s\"";
	public static final String LIST_BY_PROVIDER = "product list --org \"%s\" --provider \"%s\" -v";
	public static final String LIST_BY_ENV = "product list --org \"%s\" --environment \"%s\" -v";
	public static final String SYNCHRONIZE = "product synchronize --org \"%s\" --name \"%s\"";
	public static final String DELETE = "product delete --org \"%s\" --name \"%s\"";
	
	
	/** Parameters:<BR>1: product_name<BR>2: org_name */
	public static final String ERR_COULD_NOT_FIND_PRODUCT = 
		"Could not find product [ %s ] within organization [ %s ]";
	/** Parameters:<BR>1: product_name */
	public static final String OUT_CREATED = 
		"Successfully created product [ %s ]";
	/** Parameters:<BR>1: product_name<BR>2: env_name */
	public static final String OUT_PROMOTED = 
		"Product [ %s ] promoted to environment [ %s ]";
	/** Parameters:<BR>1: product_name */
	public static final String OUT_SYNCHRONIZED = 
		"Product [ %s ] synchronized";
	public static final String OUT_DELETED = 
		"Deleted product '%s'";
	public static final String OUT_NOT_SYNCHRONIZED_YET = 
			"Product '%s' was not synchronized yet";
	public static final String ERR_PROMOTE_NOREPOS = 
			"Product '%s' hasn't any repositories";
	
}
