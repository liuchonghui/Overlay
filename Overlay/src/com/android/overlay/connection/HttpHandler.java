package com.android.overlay.connection;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import com.android.overlay.manager.LogManager;

import android.os.SystemClock;
import android.util.Log;

public class HttpHandler<T> extends AsyncTask<Object, Object, Object> implements
		EntityCallBack {

	protected final AbstractHttpClient client;
	protected final HttpContext context;

	protected final StringEntityHandler mStrEntityHandler = new StringEntityHandler();
	protected final FileEntityHandler mFileEntityHandler = new FileEntityHandler();

	protected final AjaxCallBack<T> callback;

	protected int executionCount = 0;
	protected String targetUrl = null; // 下载的路径
	protected boolean isResume = false; // 是否断点续传
	protected String charset;

	public HttpHandler(AbstractHttpClient client, AjaxCallBack<T> callback) {
		this(client, new BasicHttpContext(), callback, "UTF-8");
	}

	public HttpHandler(AbstractHttpClient client, HttpContext context,
			AjaxCallBack<T> callback, String charset) {
		this.client = client;
		this.context = context;
		this.callback = callback;
		this.charset = charset;
	}

	protected void makeRequestWithRetries(HttpUriRequest request)
			throws IOException {
		if (isResume && targetUrl != null) {
			if (LogManager.isDebugable()) {
				Log.d("HTTP", "set File:" + targetUrl);
			}
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
					HttpResponse response = client.execute(request, context);
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

	@Override
	protected Object doInBackground(Object... params) {
		if (params != null && params.length == 3) {
			targetUrl = String.valueOf(params[1]);
			isResume = (Boolean) params[2];
		}
		try {
			if (LogManager.isDebugable()) {
				Log.d("HTTP", "start.");
			}
			publishProgress(UPDATE_START); // 开始
			makeRequestWithRetries((HttpUriRequest) params[0]);

		} catch (IOException e) {
			String message = null;
			if (e != null && e.getMessage() != null) {
				message = e.getMessage();
			}
			publishProgress(UPDATE_FAILURE, e, 0, message); // 结束
		}

		return null;
	}

	protected final static int UPDATE_START = 1;
	protected final static int UPDATE_LOADING = 2;
	protected final static int UPDATE_FAILURE = 3;
	protected final static int UPDATE_SUCCESS = 4;

	@SuppressWarnings("unchecked")
	@Override
	protected void onProgressUpdate(Object... values) {
		int update = Integer.valueOf(String.valueOf(values[0]));
		switch (update) {
		case UPDATE_START:
			if (callback != null)
				callback.onStart();
			break;
		case UPDATE_LOADING:
			if (callback != null)
				callback.onLoading(Long.valueOf(String.valueOf(values[1])),
						Long.valueOf(String.valueOf(values[2])));
			break;
		case UPDATE_FAILURE:
			if (callback != null)
				callback.onFailure((Throwable) values[1], (Integer) values[2],
						(String) values[3]);
			break;
		case UPDATE_SUCCESS:
			if (callback != null)
				callback.onSuccess((T) values[1]);
			break;
		default:
			break;
		}
		super.onProgressUpdate(values);
	}

	public boolean isStop() {
		return mFileEntityHandler.isStop();
	}

	/**
	 * @param stop
	 *            停止下载任务
	 */
	public void stop() {
		mFileEntityHandler.setStop(true);
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
						responseBody = mStrEntityHandler.handleEntity(entity,
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

	protected long time;

	@Override
	public void callBack(long count, long current, boolean mustNoticeUI) {
		if (callback != null && callback.isProgress()) {
			if (mustNoticeUI) {
				publishProgress(UPDATE_LOADING, count, current);
			} else {
				long thisTime = SystemClock.uptimeMillis();
				if (thisTime - time >= callback.getRate()) {
					time = thisTime;
					publishProgress(UPDATE_LOADING, count, current);
				}
			}
		}
	}

}
