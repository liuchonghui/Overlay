package com.android.overlay.connection;

import java.io.IOException;
import java.net.UnknownHostException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.protocol.HttpContext;

public class SyncRequestHandler {

	private final AbstractHttpClient client;
	private final HttpContext context;
	private final StringEntityHandler entityHandler = new StringEntityHandler();

	private int executionCount = 0;
	private String charset;

	public SyncRequestHandler(AbstractHttpClient client, HttpContext context,
			String charset) {
		this.client = client;
		this.context = context;
		this.charset = charset;
	}

	private Object makeRequestWithRetries(HttpUriRequest request)
			throws IOException {

		boolean retry = true;
		IOException cause = null;
		HttpRequestRetryHandler retryHandler = client
				.getHttpRequestRetryHandler();
		while (retry) {
			try {
				HttpResponse response = client.execute(request, context);
				return entityHandler.handleEntity(response.getEntity(), null,
						charset);
			} catch (UnknownHostException e) {
				cause = e;
				retry = retryHandler.retryRequest(cause, ++executionCount,
						context);
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

	public Object sendRequest(HttpUriRequest... params) {
		try {
			return makeRequestWithRetries(params[0]);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

}
