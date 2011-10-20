package com.redhat.qe.katello.base;

public interface IKatelloProvider {

	public static final String CREATE_NODESCRIPTION_NOURL = "provider create --type custom --org \"%s\" --name \"%s\"";
	public static final String CREATE_NODESCRIPTION = "provider create --type custom --org \"%s\" --name \"%s\" --url \"%s\"";
	public static final String CREATE = "provider create --type custom --org \"%s\" --name \"%s\" --url \"%s\" --description \"%s\"";
	public static final String DELETE = "provider delete --org \"%s\" --name \"%s\"";
	public static final String LIST_VMODE = "provider list --org \"%s\" -v";
	public static final String INFO = "provider info --org \"%s\" --name \"%s\"";
	public static final String SYNCHRONIZE = "provider synchronize --org \"%s\" --name \"%s\"";
	
	/** Parameters:<BR>1: provider_name*/
	public static final String OUT_DELETE = 
		"Deleted provider [ %s ]";
	/** Parameters:<BR>1: provider_name*/
	public static final String OUT_SYNCHRONIZE = 
		"Provider [ %s ] synchronized";
	
}
