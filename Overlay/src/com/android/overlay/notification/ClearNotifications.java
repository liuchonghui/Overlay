package com.android.overlay.notification;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.android.overlay.RunningEnvironment;
import com.android.overlay.manager.NotificationManager;

/**
 * Clear all notifications.(reserved:DO NOT MOVE)
 */
public class ClearNotifications extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (RunningEnvironment.getInstance().isInitialized()) {
			NotificationManager.getInstance().onClearNotifications();
		}
		finish();
	}

	public static Intent createIntent(Context context) {
		Intent intent = new Intent(context, ClearNotifications.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION
				| Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP
				| Intent.FLAG_ACTIVITY_NEW_TASK);
		return intent;
	}

}