package com.android.overlay.connection;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpResponseException;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.protocol.HttpContext;

import android.os.SystemClock;
import android.util.Log;

import com.android.overlay.manager.LogManager;

public class JsonHttpHandler<T> extends SimpleHttpHandler<T> {

	protected final JsonEntityHandler<T> mJsonEntityHandler = new JsonEntityHandler<T>();

	public JsonHttpHandler(AbstractHttpClient client, HttpContext context,
			AjaxCallBack<T> callback, String charset) {
		super(client, context, callback, charset);
	}

	public JsonHttpHandler(AbstractHttpClient client, AjaxCallBack<T> callback) {
		super(client, callback);
	}

	protected void handleResponse(HttpResponse response) {
		StatusLine status = response.getStatusLine();
		if (LogManager.isDebugable()) {
			Log.d("HTTP", "getResponse:" + status.getStatusCode());
		}
		if (status.getStatusCode() >= 300) {
			String errorMsg = "response status error code:"
					+ status.getStatusCode();
			if (status.getStatusCode() == 416 && isResume) {
				errorMsg += " \n maybe you have download complete.";
			}
			publishProgress(
					UPDATE_FAILURE,
					new HttpResponseException(status.getStatusCode(), status
							.getReasonPhrase()), status.getStatusCode(),
					errorMsg);
		} else {
			try {
				HttpEntity entity = response.getEntity();
				Object responseBody = null;
				if (entity != null) {
					time = SystemClock.uptimeMillis();
					if (targetUrl != null) {
						responseBody = mFileEntityHandler.handleEntity(entity,
								this, targetUrl, isResume);
					} else {
						responseBody = mJsonEntityHandler.handleEntity(entity,
								this, charset);
					}

				}
				publishProgress(UPDATE_SUCCESS, responseBody);

			} catch (IOException e) {
				String message = null;
				if (e != null && e.getMessage() != null) {
					message = e.getMessage();
				}
				publishProgress(UPDATE_FAILURE, e, 0, message);
			}

		}
	}
}
