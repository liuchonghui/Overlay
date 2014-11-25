package com.android.overlay.entity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

public class EntityIntentBuilder extends
		BaseAccountIntentBuilder<EntityIntentBuilder> {

	public EntityIntentBuilder(Context context, ComponentName component) {
		super(context, component);
	}

	private String user;

	public EntityIntentBuilder setUser(String user) {
		this.user = user;
		return this;
	}

	@Override
	protected void preBuild() {
		super.preBuild();
		if (user == null) {
			return;
		}
		if (getSegmentCount() == 0) {
			throw new IllegalStateException();
		}
		addSegment(user);
	}

	public static String getUser(Intent intent) {
		return getSegment(intent, 1);
	}

}