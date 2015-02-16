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
	public String getTitle();

	/**
	 * Show Text for notification.
	 */
	public String getText();

	/**
	 * Intent to launch activity.
	 */
	public Intent getIntent();

}
