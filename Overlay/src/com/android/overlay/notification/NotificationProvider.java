package com.android.overlay.notification;

import java.util.Collection;

import android.content.Intent;
import android.net.Uri;

public interface NotificationProvider<T extends NotificationItem> {

	/**
	 * List of notifications.
	 */
	public Collection<T> getNotifications();

	/**
	 * Whether notification can be cleared.
	 */
	public boolean canClearNotifications();

	/**
	 * Clear notifications.
	 */
	public void clearNotifications();

	/**
	 * Sound for notification.
	 */
	public Uri getSound();

	/**
	 * Audio stream type for notification.
	 */
	public int getStreamType();

	/**
	 * Resource id for notification.
	 */
	public int getIcon();

	/**
	 * Intent for notification (If NotificationItem getIntent() == null).
	 */
	public Intent getIntent(NotificationItem item);

}
