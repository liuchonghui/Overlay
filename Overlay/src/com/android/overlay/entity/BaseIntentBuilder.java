package com.android.overlay.entity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.android.overlay.utils.IntentUtil;

public class BaseIntentBuilder<T extends BaseIntentBuilder<?>> {

	private final Context context;
	private final ComponentName comp;

	public BaseIntentBuilder(Context ctx, ComponentName component) {
		super();
		this.context = ctx;
		this.comp = component;
	}

	public Intent build() {
		if (context != null && comp != null) {
			return IntentUtil.createActivityInitValue(context, comp);
		} else {
			return new Intent();
		}
	}

}