package com.android.overlay.notification;

import java.util.Iterator;

public class BaseAccountNotificationProvider<T extends AccountNotificationItem>
		extends BaseNotificationProvider<T> implements
		AccountNotificationProvider<T> {

	public BaseAccountNotificationProvider(int icon) {
		super(icon);
	}

	public T get(String account) {
		for (T item : items) {
			if (item.getAccount().equals(account)) {
				return item;
			}
		}
		return null;
	}

	public boolean remove(String account) {
		return remove(get(account));
	}

	@Override
	public void clearAccountNotifications(String account) {
		for (Iterator<T> iterator = items.iterator(); iterator.hasNext();) {
			if (account.equals(iterator.next().getAccount())) {
				iterator.remove();
			}
		}
	}

}
