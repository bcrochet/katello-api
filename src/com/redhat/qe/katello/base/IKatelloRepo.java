package com.redhat.qe.katello.base;

public interface IKatelloRepo {

	public static final String CREATE = "repo create --org \"%s\" --product \"%s\" --name \"%s\" --url \"%s\"";
	public static final String INFO = "repo info --org \"%s\" --product \"%s\" --name \"%s\"";
	public static final String LIST_BY_PRODUCT = "repo list --org \"%s\" --product \"%s\" -v";
	public static final String LIST_BY_ENVIRONMENT = "repo list --org \"%s\" --environment \"%s\" -v";
}
