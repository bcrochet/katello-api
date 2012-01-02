package com.redhat.qe.katello.base;

public interface IKatelloEnvironment {

	public static final String LOCKER = "Locker";
	public static final String CREATE_NODESC = "environment create --org \"%s\" --name \"%s\" --prior \"%s\"";
	public static final String INFO = "environment info --org \"%s\" --name \"%s\" -v";
}
