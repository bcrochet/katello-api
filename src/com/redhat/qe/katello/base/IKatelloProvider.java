package com.redhat.qe.katello.base;

public interface IKatelloProvider {

	public static final String CREATE_NODESCRIPTION = "provider create --type custom --org \"%s\" --name \"%s\"";
	public static final String DELETE = "provider delete --org \"%s\" --name \"%s\"";
	
	public static final String OUT_DELETE = 
		"Deleted provider [ %s ]";
}
