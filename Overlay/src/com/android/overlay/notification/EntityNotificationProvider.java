package com.android.overlay.notification;

public class EntityNotificationProvider<T extends EntityNotificationItem>
		extends BaseAccountNotificationProvider<T> {

	public EntityNotificationProvider(int icon) {
		super(icon);
	}

	@Override
	public T get(String account) {
		throw new UnsupportedOperationException();
	}

	public T get(String account, String user) {
		for (T item : items) {
			if (item.getAccount().equals(account)
					&& item.getUser().equals(user)) {
				return item;
			}
		}
		return null;
	}

	public boolean remove(String account, String user) {
		return remove(get(account, user));
	}
}
