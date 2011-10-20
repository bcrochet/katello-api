package com.redhat.qe.katello.base;

public interface IKatelloRepo {

	public static final String CREATE = "repo create --org \"%s\" --product \"%s\" --name \"%s\" --url \"%s\"";
	public static final String INFO = "repo info --org \"%s\" --product \"%s\" --name \"%s\"";
}
