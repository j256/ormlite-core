-------------------------------------------------------------------------------------------------------

BACKGROUND:

This package provides a lightweight Object Relational Mapping between Java classes and SQL databases.
There is a JDBC implementation which talks to JDBC databases as well as one that makes calls to the
Android OS database API.

For more information, see the online documentation on the home page:

http://ormlite.sourceforge.net/

This package is provided for those who are interested in providing additional backends for the
package.  Contact the author for more information.

Enjoy,
Gray Watson
http://256.com/gray/

-------------------------------------------------------------------------------------------------------

REQUIREMENTS:

- Java 5 or above (http://www.sun.com/java/)

-------------------------------------------------------------------------------------------------------

GETTING STARTED:

See the Getting Started section from the online documentation:

http://ormlite.sourceforge.net/

-------------------------------------------------------------------------------------------------------

MAVEN DEPENDENCIES:

ORMLite has no direct dependencies.  It has logging classes that use reflection to call out to
log4j and other logging classes but these will not be called unless they exist in the classpath.
Package versions can be tuned as necessary.

Test Dependencies:

	commons-logging -- 1.1.1
	log4j -- 1.2.15
	junit -- 4.8.1
	org.easymock -- 2.3
	javax-persistence -- 1.0

-------------------------------------------------------------------------------------------------------
