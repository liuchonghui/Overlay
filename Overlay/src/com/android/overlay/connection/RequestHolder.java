package com.android.overlay.connection;

import java.util.Date;

/**
 * 
 * @author liu_chonghui
 * 
 */
public class RequestHolder<T> {

	private final long DEFAULT_TIMEOUT_VALUE = 5000L;
	private final long timeout;
	private long configTimeout = DEFAULT_TIMEOUT_VALUE;
	private boolean timerEnable = false;
	private final T listener;

	public RequestHolder(T listener) {
		super();
		this.timeout = new Date().getTime() + configTimeout;
		this.listener = listener;
	}

	public void configTimeout(long value) {
		this.configTimeout = value;
	}

	public void setTimerEnable(boolean enable) {
		timerEnable = enable;
	}

	public boolean timerEnable() {
		return timerEnable;
	}

	public boolean isExpired(long now) {
		return now > timeout;
	}

	public T getListener() {
		return listener;
	}

}
