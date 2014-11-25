package com.android.overlay.notification;

import java.util.Collection;

import android.net.Uri;

public interface NotificationProvider<T extends NotificationItem> {

	/**
	 * List of notifications.
	 */
	Collection<T> getNotifications();

	/**
	 * Whether notification can be cleared.
	 */
	boolean canClearNotifications();

	/**
	 * Clear notifications.
	 */
	void clearNotifications();

	/**
	 * Sound for notification.
	 */
	Uri getSound();

	/**
	 * Audio stream type for notification.
	 */
	int getStreamType();

	/**
	 * Resource id for notification.
	 */
	int getIcon();

}
