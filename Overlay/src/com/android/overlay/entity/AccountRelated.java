package com.android.overlay.entity;

/**
 * Object with relation to the account.
 * 
 * @author liu_chonghui
 */
public abstract class AccountRelated {

	protected String account;

	public AccountRelated(String account) {
		super();
		this.account = account;
	}

	public String getAccount() {
		return account;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((account == null) ? 0 : account.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		AccountRelated other = (AccountRelated) obj;
		if (account == null) {
			if (other.account != null) {
				return false;
			}

		} else if (!account.equals(other.account)) {
			return false;
		}
		return true;
	}

}
