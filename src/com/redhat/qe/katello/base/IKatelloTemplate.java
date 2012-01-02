package com.redhat.qe.katello.base;

public interface IKatelloTemplate {

	public static final String CREATE = "template create --org \"%s\" --name \"%s\"";	
	public static final String INFO_FOR_ENV = "template info --org \"%s\" --name \"%s\" --environment \"%s\" -v";
}
