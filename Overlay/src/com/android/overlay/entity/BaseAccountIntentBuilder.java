package com.android.overlay.entity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

public class BaseAccountIntentBuilder<T extends BaseAccountIntentBuilder<?>> extends
		SegmentIntentBuilder<T> {

	private String account;

	public BaseAccountIntentBuilder(Context context, ComponentName component) {
		super(context, component);
	}

	@SuppressWarnings("unchecked")
	public T setAccount(String account) {
		this.account = account;
		return (T) this;
	}

	@Override
	protected void preBuild() {
		super.preBuild();
		if (account == null) {
			return;
		}
		if (getSegmentCount() != 0) {
			throw new IllegalStateException();
		}
		addSegment(account);
	}

	public static String getAccount(Intent intent) {
		return getSegment(intent, 0);
	}

}