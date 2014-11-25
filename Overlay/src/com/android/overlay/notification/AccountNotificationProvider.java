package com.android.overlay.notification;


public interface AccountNotificationProvider<T extends AccountNotificationItem>
		extends NotificationProvider<T> {

	void clearAccountNotifications(String account);

}
