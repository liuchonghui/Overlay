package com.android.overlay.notification;

import android.content.Intent;

/**
 * @author liu_chonghui
 * 
 */
public interface NotificationItem {

	/**
	 * Title for notification.
	 */
	String getTitle();

	/**
	 * Show Text for notification.
	 */
	String getText();

	/**
	 * Intent to launch activity.
	 */
	Intent getIntent();

}
