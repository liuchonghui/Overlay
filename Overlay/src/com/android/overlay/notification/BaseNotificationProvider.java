package com.android.overlay.notification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;

import com.android.overlay.manager.NotificationManager;
import com.android.overlay.manager.SettingsManager;

/**
 * Base provider for the notifications to be displayed.
 */
public class BaseNotificationProvider<T extends NotificationItem> implements
		NotificationProvider<T> {

	protected final Collection<T> items;
	private final int icon;
	private boolean canClearNotifications;

	public BaseNotificationProvider(int icon) {
		super();
		this.items = new ArrayList<T>();
		this.icon = icon;
		canClearNotifications = true;
	}

	public void add(T item, Boolean notify) {
		boolean exists = items.remove(item);
		if (notify == null) {
			notify = !exists;
		}
		items.add(item);
		NotificationManager.getInstance().updateNotifications(this,
				notify ? item : null);
	}

	public boolean remove(T item) {
		boolean result = items.remove(item);
		if (result) {
			NotificationManager.getInstance().updateNotifications(this, null);
		}
		return result;
	}

	public void setCanClearNotifications(boolean canClearNotifications) {
		this.canClearNotifications = canClearNotifications;
	}

	@Override
	public Collection<T> getNotifications() {
		return Collections.unmodifiableCollection(items);
	}

	@Override
	public boolean canClearNotifications() {
		return canClearNotifications;
	}

	@Override
	public void clearNotifications() {
		items.clear();
	}

	@Override
	public Uri getSound() {
		return SettingsManager.eventsSound();
	}

	@Override
	public int getStreamType() {
		return AudioManager.STREAM_NOTIFICATION;
	}

	@Override
	public int getIcon() {
		return icon;
	}

	@Override
	public Intent getIntent(NotificationItem item) {
		return null;
	}

}
