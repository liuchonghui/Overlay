package com.android.overlay;

import android.app.Application;

/**
 * Base application.
 * 
 * @author liu_chonghui
 */
public class DefaultApplication extends Application {

	RunningEnvironment overlayEnvironment;

	@Override
	public void onCreate() {
		super.onCreate();
		if (overlayEnvironment == null) {
			overlayEnvironment = new RunningEnvironment(
					"R.array.overlay_managers", "R.array.overlay_tables");
		} else {
			overlayEnvironment = RunningEnvironment.getInstance();
		}
		overlayEnvironment.onCreate(this);
	}

	@Override
	public void onTerminate() {
		overlayEnvironment.onTerminate();
		super.onTerminate();
	}

	@Override
	public void onLowMemory() {
		overlayEnvironment.onLowMemory();
		super.onLowMemory();
	}
}
