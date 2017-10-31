ORMLite Core
============

This package provides the core functionality for the JDBC and Android packages.  Users that are connecting to SQL
databases via JDBC will need to download the [ormlite-jdbc](https://github.com/j256/ormlite-jdbc) package as well. Android
users should download the [ormlite-android](https://github.com/j256/ormlite-android) package as well as this core package.

* For more information, visit the [ORMLite home page](http://ormlite.com/).	
* Online documentation can be found off the home page.  Here are the [code Javadocs](http://ormlite.com/javadoc/ormlite-core/).
* Browse the code on the [git repository](https://github.com/j256/ormlite-core).  [![CircleCI](https://circleci.com/gh/j256/ormlite-core.svg?style=svg)](https://circleci.com/gh/j256/ormlite-core) [![CodeCov](https://img.shields.io/codecov/c/github/j256/ormlite-core.svg)](https://codecov.io/github/j256/ormlite-core/)
* Maven packages are published via [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.j256.ormlite/ormlite-core/badge.svg?style=flat-square)](https://maven-badges.herokuapp.com/maven-central/com.j256.ormlite/ormlite-core/)

Enjoy, Gray Watson

## Little Sample Program

I've checked in a [number of example programs](http://ormlite.com/docs/examples).

## Getting Started

The following is a quick code example to give you a taste on how to use the package.

    // this uses h2 but you can change it to match your database
    String databaseUrl = "jdbc:h2:mem:account";
    // create a connection source to our database
    ConnectionSource connectionSource =
         new JdbcConnectionSource(databaseUrl);
    
    // instantiate the DAO to handle Account with String id
    Dao<Account,String> accountDao =
         DaoManager.createDao(connectionSource, Account.class);
    
    // if you need to create the 'accounts' table make this call
    TableUtils.createTable(connectionSource, Account.class);
    
    // create an instance of Account
    String name = "Jim Smith";
    Account account = new Account(name, "_secret");
    
    // persist the account object to the database
    accountDao.create(account);
    
    // retrieve the account
    Account account2 = accountDao.queryForId(name);
    // show its password
    System.out.println("Account: " + account2.getPassword());
    
    // close the connection source
    connectionSource.close();

# ChangeLog Release Notes

See the [ChangeLog.txt file](src/main/javadoc/doc-files/changelog.txt).
