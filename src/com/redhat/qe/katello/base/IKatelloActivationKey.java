package com.redhat.qe.katello.base;

public interface IKatelloActivationKey {

	public static final String CREATE_NODESC = "activation_key create --org \"%s\" --environment \"%s\" --name \"%s\"";
	public static final String CREATE_NODESC_TEMPLATE = "activation_key create --org \"%s\" --environment \"%s\" --name \"%s\" --template \"%s\"";
	
	public static final String ERR_TEMPLATE_NOTFOUND = 
			"Could not find template [ %s ]";
	
}
