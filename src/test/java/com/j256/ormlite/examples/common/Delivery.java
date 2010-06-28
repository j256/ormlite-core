package com.j256.ormlite.examples.common;

import java.util.Date;

import com.j256.ormlite.examples.simple.FieldConfigMain;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.DatabaseFieldConfig;

/**
 * Example delivery object that does not use any {@link DatabaseField} annotations but uses direct wiring of the field
 * configurations using {@link DatabaseFieldConfig} in Java or Spring configurations.
 * 
 * See {@link FieldConfigMain} or {@link SpringFieldConfigMain}.
 */
public class Delivery {

	private int id;
	private Date when;
	private String signedBy;
	private Account account;

	Delivery() {
		// all persisted classes must define a no-arg constructor with at least package visibility
	}

	public Delivery(Date when, String signedBy, Account account) {
		this.when = when;
		this.signedBy = signedBy;
		this.account = account;
	}

	public int getId() {
		return id;
	}

	public Date getWhen() {
		return when;
	}

	public String getSignedBy() {
		return signedBy;
	}

	public Account getAccount() {
		return account;
	}
}
