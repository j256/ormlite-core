package com.j256.ormlite.examples.common;

import com.j256.ormlite.dao.BaseDaoImpl;

/**
 * Implementation of the Account DAO which is used to read/write Account to/from the database.
 */
public class AccountDaoImpl extends BaseDaoImpl<Account, Integer> implements AccountDao {

	// used by Spring which injects the DatabaseType afterwards
	public AccountDaoImpl() {
		super(Account.class);
	}

	// no additional methods necessary unless you have per-Account specific DAO methods here
}
