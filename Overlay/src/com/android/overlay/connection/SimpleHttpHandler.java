package com.android.overlay.connection;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.protocol.HttpContext;

import com.android.overlay.manager.LogManager;

import android.util.Log;

public class SimpleHttpHandler<T> extends HttpHandler<T> {

	public SimpleHttpHandler(AbstractHttpClient client, HttpContext context,
			AjaxCallBack<T> callback, String charset) {
		super(client, context, callback, charset);
	}

	public SimpleHttpHandler(AbstractHttpClient client, AjaxCallBack<T> callback) {
		super(client, callback);
	}

	protected void makeRequestWithRetries(HttpUriRequest request)
			throws IOException {
		if (isResume && targetUrl != null) {
			Log.d("HTTP", "set File:" + targetUrl);
			File downloadFile = new File(targetUrl);
			long fileLen = 0;
			if (downloadFile.isFile() && downloadFile.exists()) {
				fileLen = downloadFile.length();
			}
			if (fileLen > 0)
				request.setHeader("RANGE", "bytes=" + fileLen + "-");
		}

		boolean retry = true;
		IOException cause = null;
		HttpRequestRetryHandler retryHandler = client
				.getHttpRequestRetryHandler();
		while (retry) {
			try {
				if (!isCancelled()) {
					if (LogManager.isDebugable()) {
						Log.d("HTTP", "execute:" + request.getURI());
					}
					HttpResponse response = client.execute(request);
					if (!isCancelled()) {
						handleResponse(response);
					}
				}
				return;
			} catch (UnknownHostException e) {
				publishProgress(UPDATE_FAILURE, e, 0,
						"unknownHostException：can't resolve host");
				return;
			} catch (IOException e) {
				cause = e;
				retry = retryHandler.retryRequest(cause, ++executionCount,
						context);
			} catch (NullPointerException e) {
				// HttpClient 4.0.x 之前的一个bug
				// http://code.google.com/p/android/issues/detail?id=5255
				String message = null;
				if (e != null && e.getMessage() != null) {
					message = e.getMessage();
				}
				cause = new IOException("NPE in HttpClient" + message);
				retry = retryHandler.retryRequest(cause, ++executionCount,
						context);
			} catch (Exception e) {
				String message = null;
				if (e != null && e.getMessage() != null) {
					message = e.getMessage();
				}
				cause = new IOException("Exception" + message);
				retry = retryHandler.retryRequest(cause, ++executionCount,
						context);
			}
		}
		if (cause != null)
			throw cause;
		else
			throw new IOException("未知网络错误");
	}
}
