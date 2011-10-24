package com.redhat.qe.katello.common;

/**
 * Public interface for storing the general Katello constants.
 * @author gkhachik
 * @since 14.Feb.2011
 *
 */
public interface KatelloConstants {
	/** Masked string for passwords */
	public static final String SYSOUT_PWD_MASK = "********";

	public static final String KATELLO_SMALL_REPO = 
		"http://repos.fedorapeople.org/repos/katello/katello/fedora-14/x86_64/";
	public static final String PULP_F15_x86_64_REPO = 
		"http://repos.fedorapeople.org/repos/pulp/pulp/fedora-15/x86_64/";
	public static final String PULP_F15_i386_REPO = 
		"http://repos.fedorapeople.org/repos/pulp/pulp/fedora-15/i386/";
	public static final String EXPORT_ZIP_PATH = 
		System.getProperty("user.dir") + "/data/export.zip";
	public static final int PRODUCTS_IN_EXPORT_ZIP = 6;
	public static final String AWESOME_SERVER_BASIC = 
		"Awesome OS Server Basic";
	
	/** curl -s -u {username}:{password} 
	 * http://${servername}:${port}/api${call} */
	public static final String KATELLO_HTTP_GET =
		"curl -s -u {0}:{1} http://{2}/katello/api{4}";
		//"curl -s -u {0}:{1} http://{2}:{3}/api{4}";
	
	/** curl -s -u ${username}:${password} 
	 * -H \"Accept: application/json\" -H \"content-type: application/json\" 
	 * -d \"${content}\" -X PUT http://${servername}:${port}/api${call}*/
	public static final String KATELLO_HTTP_PUT = 
		"curl -s -u {0}:{1} -H \"Accept: application/json\" " +
		"-H \"content-type: application/json\" -d \"{2}\" " +
		"-X PUT http://{3}/katello/api{5}";
		//"-X PUT http://{3}:{4}/api{5}";
	
	/** curl -s -u ${username}:${password} -H \"Accept: application/json\" 
	 * -H \"content-type: application/json\" -d \"${content}\" 
	 * -X POST http://${servername}:${port}/api${call}*/
	public static final String KATELLO_HTTP_POST = 
		"curl -s -u{0}:{1} -H \"Accept: application/json\" " +
		"-H \"content-type: application/json\" -d \"{2}\" " +
		"-X POST http://{3}/katello/api{5}";
		//"-X POST http://{3}:{4}/api{5}";

	public static final String KATELLO_HTTP_POST_MANIFEST = 
		"curl -s -u{0}:{1} -H \"Accept: application/json\" -# " +
		"-X POST -F import=@{2} http://{3}/katello/api{5}";
		//"-X POST -F import=@{2} http://{3}:{4}/api{5}";	
	
	/** curl -s -u ${username}:${password} -H \"Accept: application/json\" 
	 * -X DELETE http://${servername}:${port}/api${call}*/
	public static final String KATELLO_HTTP_DELETE = 
		"curl -s -u {0}:{1} -H \"Accept: application/json\" " +
		"-X DELETE http://{2}/katello/api{4}";
		//"-X DELETE http://{2}:{3}/api{4}";

	/** curl -s -k -u {username}:{password} 
	 * https://${servername}:${port}/candlepin${call} */
	public static final String CANDLEPIN_HTTP_GET = 
		"curl -s -k -u {0}:{1} https://{2}:{3}/candlepin{4}";
	
	/** curl -s -k -u {username}:{password} 
	 * https://${servername}:${port}/pulp/api${call} */ 
	public static final String PULP_HTTP_GET =
		"curl -s -k -u {0}:{1} https://{2}:{3}/pulp/api{4}";
		
	/**
	 * arguments are:<br>
	 * 0 - name<br>
	 * 1 - description<br>
	 * 2 - provider_type
	 */
	public static final String JSON_CREATE_PROVIDER =
		"{'organization_id':'%s', " +
		"'provider':{" +
		"'name':'%s', " +
		"'description':'%s', " +
		"'provider_type': '%s'}}";
	
	/**
	 * arguments are:<br>
	 * 0 - name<br>
	 * 1 - description<br>
	 * 2 - provider_type
	 */
	public static final String JSON_CREATE_PROVIDER_WITH_URL =
		"{'organization_id':'%s', " +
		"'provider':{" +
		"'name':'%s', " +
		"'description':'%s', " +
		"'provider_type': '%s', " +
		"'repository_url':'%s'}}";

	public static final String JSON_CREATE_PROVIDER_BYORG =
		"{'provider':{" +
		"'name':'%s', " +
		"'description':'%s', " +
		"'provider_type': '%s'}}";

	public static final String JSON_CREATE_PRODUCT_WITH_URL =
		"{'product':{" +
		"'name':'%s', " +
		"'description':'%s', " +
		"'url':'%s'}}";

	public static final String JSON_CREATE_REPO_WITH_URL =
		"{'name':'%s', " +
		"'product_id':'%s', " +
		"'url':'%s'}";
	
	public static final String JSON_CREATE_USER = 
		"{'username':'%s', 'password':'%s', 'disabled':'%s'}";

// # Katello-CLI constants #
// # ===================== #
	public static final String YUM_REPO_FEDORA_KATELLO=
		"http://repos.fedorapeople.org/repos/katello/katello/fedora-katello.repo";
	public static final String YUM_REPO_RHEL_KATELLO=
		"http://repos.fedorapeople.org/repos/katello/katello/epel-katello.repo";
	/**
	 * positions:<BR>
	 * &nbsp;&nbsp;[0] - String to match from `python -c "platofrm()"` string - indicates platform<br>
	 * &nbsp;&nbsp;[1] - Human readable name of the platform to display<br>
	 * &nbsp;&nbsp;[2] - Katello yum repo url 
	 */
	public static final String[][] CLIENT_PLATFORMS_ALLOWED = {
		{"x86_64-x86_64-with-fedora-15","Fedora 15 (64 bit)",YUM_REPO_FEDORA_KATELLO},
		{"x86_64-x86_64-with-fedora-14","Fedora 14 (64 bit))",YUM_REPO_FEDORA_KATELLO},
		{"x86_64-x86_64-with-redhat-6","RHEL 6.x (64 bit)",YUM_REPO_RHEL_KATELLO}
	};
	
	public static final String BKR_RESERVESYS_CMD =
			"bkr workflow-simple " +
			"--username=%s " +
			"--password=\"%s\" " +
			"--distro=%s " +
			"--whiteboard=\"%s\" " +
			"--arch=%s " +
			"--keyvalue=\"MEMORY>2047\" " +
			"--keyvalue=\"DISK>7999\" " +
			"--task=/distribution/reservesys";
	
	public static final String BKR_KATELLO_RESERVESYS_RHEL6 = 
		"bkr workflow-simple --username=%s --password=\"%s\" "+
		"--distro=RHEL6-6.1 --variant=Server --arch=x86_64 "+
		"--keyvalue=\"MEMORY>2047\" --keyvalue=\"DISK>19999\" "+
		"--whiteboard=\"Katello install [Jenkins] - RHEL6\" "+
		"--task=/Kalpana/Sanity/ImportKeys --taskparam='PUBKEYS=id_auto_dsa' "+
		"--task=/distribution/reservesys";
	
	public static final String BKR_KATELLO_RESERVESYS_F15 = 
		"bkr workflow-simple --username=%s --password=\"%s\" "+
		"--distro=Fedora-15 --arch=x86_64 "+
		"--keyvalue=\"MEMORY>2047\" --keyvalue=\"DISK>19999\" "+
		"--whiteboard=\"Katello install [Jenkins] - F15\" "+
		"--task=/Kalpana/Sanity/ImportKeys --taskparam='PUBKEYS=id_auto_dsa' "+
		"--task=/distribution/reservesys";

	public static final String BKR_KATELLO_RESERVESYS_F14 = 
		"bkr workflow-simple --username=%s --password=\"%s\" "+
		"--distro=Fedora-14 --arch=x86_64 "+
		"--keyvalue=\"MEMORY>2047\" --keyvalue=\"DISK>19999\" "+
		"--whiteboard=\"Katello install [Jenkins] - F14\" "+
		"--task=/Kalpana/Sanity/ImportKeys --taskparam='PUBKEYS=id_auto_dsa' "+
		"--task=/distribution/reservesys";
	
	public static final String JENKINS_SSH_PUBKEY = 
		System.getenv("HOME")+"/.ssh/id_dsa.pub";
	public static final String JENKINS_SSH_PRIVKEY =
		System.getenv("HOME")+"/.ssh/id_dsa";
	public static final String KATELLO_CLI_CLIENT_CONFIG = 
		"/etc/katello/client.conf";
	
	public static final String ACME_ORG = "ACME_Corporation";
	public static final String KATELLO_CLI_USER_DEFAULT_PASSWORD = "testing123";
	public static final String KATELLO_SERVERS_RHQE_CA_CRT = 
		"katello-ci.usersys.redhat.com"; // Only katello-ci for now uses that cert.
	public static final String RHQE_CA_CERT = 
		"https://github.com/RedHatQE/hudson-config/raw/master/rhqe-ca.crt";
	
	public static final String ENV_LOCKER = "Locker";
}
