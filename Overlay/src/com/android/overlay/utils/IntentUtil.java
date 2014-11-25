package com.android.overlay.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

/**
 * get intents.
 * 
 * @author liu_chonghui
 * 
 */
public class IntentUtil {
	public static Intent createActivityInitValue(Context ctx,
			ComponentName component) {
		Intent intent = null;
		boolean find = true;
		if (component == null) {
			return intent;
		}
		try {
			intent = new Intent();
			if (component != null) {
				intent.setComponent(component);
			}
			if (ctx.getPackageManager().resolveActivity(intent,
					PackageManager.MATCH_DEFAULT_ONLY) == null) {
				if (ctx.getPackageManager().resolveService(intent,
						PackageManager.MATCH_DEFAULT_ONLY) == null) {
					find = false;
				}
			}
			if (!find) {
				intent = null;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return intent;
	}
}
