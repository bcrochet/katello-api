package com.redhat.qe.katello.base;

public interface IKatelloChangeset {
	/** Params are:<BR>1. org_name<BR>2. env_name<BR>3. name */
	public static final String CREATE = "changeset create --org \"%s\" --environment \"%s\" --name \"%s\"";
	/** Add product to changeset<BR>Params are:<BR>1. product_name<BR>2. org_name<BR>3. env_name<BR>4. name */
	public static final String UPDATE_ADD_PRODUCT = "changeset update --add_product \"%s\" --org \"%s\" --environment \"%s\" --name \"%s\"";
	public static final String PROMOTE = "changeset promote --org \"%s\" --environment \"%s\" --name \"%s\"";
	public static final String UPDATE_ADD_TEMPLATE = "changeset update --add_template \"%s\" --org \"%s\" --environment \"%s\" --name \"%s\"";
}
