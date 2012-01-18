# Generated by Buildr 1.4.4, change to your liking
# Version number for this release
VERSION_NUMBER = "1.0.0"
# Group identifier for your projects
GROUP = "katello-api"
COPYRIGHT = ""

# Specify Maven 2.0 remote repositories here, like this:
repositories.remote << "http://www.ibiblio.org/maven2/"
repositories.remote << "http://clojars.org/repo" # webui-framework*.jar
repositories.remote << "http://repo1.maven.org/maven2/"
repositories.remote << "http://mvnrepository.com/artifact/"
repositories.remote << "http://download.java.net/maven/2/"
# repositories.remote << "http://repository.jboss.org/maven2/" # for testng-5.9-jdk15.jar

WEBUI_FRAMEWORK = ['webui-framework:webui-framework:jar:1.0.2-SNAPSHOT'] # <--- (v1.0.2) works with (testng v6.0)
SIMPLE_JSON = ['com.googlecode.json-simple:json-simple:jar:1.1']
TESTNG = ['org.testng:testng:jar:6.0','com.beust:jcommander:jar:1.13','com.mycila.com.google.inject:guice:jar:3.0-20100907','javax.inject:javax.inject:jar:1','bsh:bsh:jar:1.3.0','org.uncommons:reportng:jar:1.1.2','velocity:velocity:jar:1.4','commons-collections:commons-collections:jar:3.2','logkit:logkit:jar:1.0.1']
SELENIUM = ['org.seleniumhq.selenium.client-drivers:selenium-java-client-driver:jar:1.0.2']
SSH2 = ['com.trilead:trilead-ssh2:jar:build213-svnkit-1.3-patch']

TESTNG_XML = 'testng-suites/katello-tests.xml'
JAVAC_SRC = 'src' # location of java source files
JAVAC_CLASSES = 'classes' # compiled class files location
CP_ALL = [ WEBUI_FRAMEWORK, TESTNG, SIMPLE_JSON, SELENIUM, SSH2 ]
TESTNG_RUN_LIST = ENV['KATELLO_API_TESTNAMES']

Project.local_task :update_katello
Project.local_task :install_katello
Project.local_task :db_cleanup_katello
Project.local_task :beaker_reservesys_new

desc "The Katello-api project"
define "katello-api" do
  project.version = VERSION_NUMBER
  project.group = GROUP
  manifest["Implementation-Vendor"] = COPYRIGHT
  
  task(:testng) do
     cmd_args = ['-testnames',TESTNG_RUN_LIST,'-listener','com.redhat.qe.auto.testng.TestNGListener,org.uncommons.reportng.HTMLReporter,org.uncommons.reportng.JUnitXMLReporter,org.testng.reporters.XMLReporter','-configfailurepolicy','continue','-parallel','classes','-threadcount','2']
     Java::Commands.java "org.testng.TestNG", TESTNG_XML, cmd_args,
     :classpath => [ CP_ALL, JAVAC_CLASSES ]
  end
  task :test => :testng

  task :update_katello => :compile do
     cmd_args = []
     Java::Commands.java "com.redhat.qe.katello.common.KatelloUpdater", cmd_args,
     :classpath => [ CP_ALL, JAVAC_CLASSES ]
  end

  task :install_katello => :compile do
     cmd_args = [ENV['BUILDR_KATELLO_SERVER'],ENV['BUILDR_KATELLO_SSHPASS'],'scripts/katello-install/',ENV['BUILDR_KATELLO_DB'],ENV['RAILS_ENV']]
     Java::Commands.java "com.redhat.qe.katello.common.KatelloInstaller", cmd_args,
     :classpath => [ CP_ALL, JAVAC_CLASSES ]
  end
 
  task :db_cleanup_katello => :compile do
     cmd_args = []
     Java::Commands.java "com.redhat.qe.katello.common.KatelloDBCleaner", cmd_args,
     :classpath => [ CP_ALL, JAVAC_CLASSES ]
  end
  
  task :beaker_reservesys_new => :compile do
     cmd_args = []
     Java::Commands.java "com.redhat.qe.katello.common.KatelloInBeaker", cmd_args,
     :classpath => [ CP_ALL, JAVAC_CLASSES ]
  end

  
  compile.using(:javac)
  compile.from(JAVAC_SRC)
  compile.into(JAVAC_CLASSES)
#  compile.with transitive(SIMPLE_JSON, WEBUI_FRAMEWORK, SELENIUM, SSH2) # <--- transitive resolves the deps in the m2 jars themselves
  compile.with(CP_ALL)
end

