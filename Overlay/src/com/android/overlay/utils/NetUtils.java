package com.android.overlay.utils;

import org.apache.http.HttpHost;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;

import com.android.overlay.connection.ApnInfo;

@SuppressLint("DefaultLocale")
public final class NetUtils {

	/**
	 * 获取客户端代理
	 * 
	 * @param context
	 * @return
	 */
	public static HttpHost getCurrHttpProxy(Context context) {
		HttpHost proxy = null;
		try {
			ConnectivityManager connectManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo wifi = connectManager
					.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			NetworkInfo mobile = connectManager
					.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
			if (wifi != null && mobile != null && !wifi.isAvailable()
					&& mobile.isAvailable()) {
				ApnInfo apn = null;
				NetworkInfo active = connectManager.getActiveNetworkInfo();
				if (active != null) {
					apn = getCurrentApnInfo(context, active.getExtraInfo());
				}
				if (apn == null) {
					apn = getDefaultApnInfo(context);
				}
				if (apn != null && apn.hasProxy()) {
					proxy = new HttpHost(apn.proxy, Integer.valueOf(apn.port),
							HttpHost.DEFAULT_SCHEME_NAME);
				}
			}
		} catch (Exception e) {
			proxy = null;
		}

		return proxy;
	}

	/**
	 * 检查网络状态是否连通
	 * 
	 * @param context
	 * @return
	 */
	public static boolean checkNetworkState(Context context) {
		try {
			ConnectivityManager connectivity = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			if (connectivity != null) {
				NetworkInfo info = connectivity.getActiveNetworkInfo();
				if (info != null && info.isConnected()) {
					if (info.getState() == NetworkInfo.State.CONNECTED) {
						return true;
					}
				}
			}
		} catch (Exception e) {
		}
		return false;
	}

	/**
	 * 检查数据业务是否连通 Indicates whether mobile network connectivity is possible.
	 * 
	 * @param context
	 * @return true if the mobile network is available, false otherwise
	 */
	public static boolean checkMobileState(Context context) {
		try {
			ConnectivityManager mgr = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mobile = mgr
					.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
			if (mobile != null
					&& (mobile.isAvailable() && mobile.isConnected())) {
				return true;
			}
		} catch (Exception e) {
		}
		return false;
	}

	/**
	 * 检查wifi业务是否连通
	 * 
	 * @param context
	 * @return
	 */
	public static boolean checkWifiState(Context context) {
		try {
			ConnectivityManager connectManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo wifi = connectManager
					.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			if (wifi.isAvailable() && wifi.isConnected()) {
				return true;
			}
		} catch (Exception e) {
		}
		return false;
	}

	/**
	 * 检查网络设备是否可用 Indicates whether network connectivity is possible, or it is
	 * possible to establish connections and pass data.
	 * 
	 * @param context
	 * @return true if network is available or connectivity exists, false
	 *         otherwise.
	 */
	public static boolean isNetworkAvailable(Context context) {
		if (isWifiAvailable(context) || isMobileAvailable(context)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 检查wifi设备是否可用
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isWifiAvailable(Context context) {
		try {
			ConnectivityManager connectManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);

			NetworkInfo wifi = connectManager
					.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			if (wifi != null && wifi.isAvailable()) {
				return true;
			}
		} catch (Exception e) {
		}
		return false;
	}

	/**
	 * 检查数据设备是否可用
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isMobileAvailable(Context context) {
		try {
			ConnectivityManager connectManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);

			NetworkInfo mobile = connectManager
					.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
			if (mobile != null && mobile.isAvailable()) {
				return true;
			}
		} catch (Exception e) {
		}
		return false;
	}

	private static final Uri DEFAULT_APN_URI = Uri
			.parse("content://telephony/carriers/preferapn");
	private static final Uri CURRENT_APN_URI = Uri
			.parse("content://telephony/carriers/current");

	private static ApnInfo getDefaultApnInfo(Context context) {
		ApnInfo info = null;
		Cursor cursor = null;
		try {
			cursor = context.getContentResolver().query(DEFAULT_APN_URI, null,
					null, null, null);
			if (cursor != null && cursor.moveToFirst()) {

				info = new ApnInfo();
				info.id = cursor.getString(cursor.getColumnIndex("_id"));
				info.name = cursor.getString(cursor.getColumnIndex("name"));
				info.apn = cursor.getString(cursor.getColumnIndex("apn"))
						.toLowerCase();
				info.type = cursor.getString(cursor.getColumnIndex("type"));
				info.proxy = cursor.getString(cursor.getColumnIndex("proxy"));
				info.port = cursor.getString(cursor.getColumnIndex("port"));
				info.mcc = cursor.getString(cursor.getColumnIndex("mcc"));
				info.mnc = cursor.getString(cursor.getColumnIndex("mnc"));
				info.numeric = cursor.getString(cursor
						.getColumnIndex("numeric"));

				info.current = cursor.getString(cursor
						.getColumnIndex("current"));

				cursor.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
			info = null;
			if (cursor != null) {
				cursor.close();
			}
		}

		return info;
	}

	private static ApnInfo getCurrentApnInfo(Context context, String apn) {
		ApnInfo info = null;
		Cursor cursor = null;

		if (apn == null) {
			return null;
		}

		try {
			cursor = context.getContentResolver().query(CURRENT_APN_URI, null,
					null, null, null);
			if (cursor != null && cursor.moveToFirst()) {

				String apnName = null;
				do {
					apnName = cursor.getString(cursor.getColumnIndex("apn"));
					if (apnName != null && apn.equalsIgnoreCase(apnName)) {
						info = new ApnInfo();
						info.id = cursor
								.getString(cursor.getColumnIndex("_id"));
						info.name = cursor.getString(cursor
								.getColumnIndex("name"));
						info.apn = cursor.getString(
								cursor.getColumnIndex("apn")).toLowerCase();
						info.type = cursor.getString(cursor
								.getColumnIndex("type"));
						info.proxy = cursor.getString(cursor
								.getColumnIndex("proxy"));
						info.port = cursor.getString(cursor
								.getColumnIndex("port"));
						info.mcc = cursor.getString(cursor
								.getColumnIndex("mcc"));
						info.mnc = cursor.getString(cursor
								.getColumnIndex("mnc"));
						info.numeric = cursor.getString(cursor
								.getColumnIndex("numeric"));

						info.current = cursor.getString(cursor
								.getColumnIndex("current"));

						break;
					}
				} while (cursor.moveToNext());

				cursor.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
			info = null;
			if (cursor != null) {
				cursor.close();
			}
		}

		return info;
	}
}
