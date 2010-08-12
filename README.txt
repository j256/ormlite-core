-------------------------------------------------------------------------------------------------------

BACKGROUND:

This package provides a lightweight Object Relational Mapping between Java classes and SQL databases.
There are certainly some much more mature packages which provide this functionality including Hibernate
and iBatis.  However, I wanted a simple wrapper around the JDBC functions from the Spring package and
Hibernate and iBatis are significantly more complicated.   The package supports natively MySQL,
Postgres, Microsoft SQL Server, H2, Derby, HSQLDB, and Sqlite.  It also contains initial, untested
support for DB2 and Oracle and can be extended to additional ones relatively easily.  Contact the author if
your database type is not supported.

This code is 97% my own.  Probably 2% is PD stuff that I've cherry picked from the web and
another 1% that that I've copied from other developers I've worked with -- all with their approval.

Hope it helps.

Gray Watson

http://ormlite.sourceforge.net/
http://256.com/gray/

-------------------------------------------------------------------------------------------------------

REQUIREMENTS:

- Java 5 or above (http://www.sun.com/java/)

-------------------------------------------------------------------------------------------------------

GETTING STARTED:

The src/main/javadoc/doc-files/details.html file has the getting started information.  See the
home page for more information.

-------------------------------------------------------------------------------------------------------

MAVEN DEPENDENCIES:

The following packages are defined as dependencies in the pom.xml maven configuration file.  The
versions can be tuned as necessary.

Main Dependencies:

	javax-persistence -- 1.0

	The javax.persistence classes are centralized in the JavaxPersistence misc class and can easily
	be removed there.
	
Optional Dependencies:

	The commons-logging and the log4j packages are referenced in the Logger classes but ORMLite will
	not use the classes unless they exist in the classpath.

	commons-logging -- 1.1.1
	log4j -- 1.2.15

Test Dependencies:

	junit -- 4.8.1
	org.easymock -- 2.3
	com.h2database -- 1.2.134

-------------------------------------------------------------------------------------------------------
