package com.android.overlay.manager;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import android.util.Log;

import com.android.overlay.OnTimerListener;
import com.android.overlay.RunningEnvironment;
import com.android.overlay.connection.AjaxCallBack;
import com.android.overlay.connection.AjaxParams;
import com.android.overlay.connection.ConnectionType;
import com.android.overlay.connection.FinalHttp;
import com.android.overlay.connection.JsonHttpHandler;
import com.android.overlay.connection.NetworkState;
import com.android.overlay.connection.OnConnectedListener;
import com.android.overlay.connection.SimpleHttpHandler;
import com.android.overlay.connection.SyncRequestHandler;
import com.android.overlay.utils.NetUtils;

public class ConnectionManager implements OnConnectedListener, OnTimerListener {

	private final static ConnectionManager instance;
	private ExecutorService singleThreadExecutor;
	private ExecutorService operatorExecutor;
	private ExecutorService requestExecutor;
	private List<Runnable> runnables;

	private HttpHost mProxy = null;
	private HttpContext httpContext;

	static {
		instance = new ConnectionManager();
	}

	public static ConnectionManager getInstance() {
		return instance;
	}

	private ConnectionManager() {
		httpContext = new BasicHttpContext();

		runnables = new ArrayList<Runnable>();
		singleThreadExecutor = Executors
				.newSingleThreadExecutor(new ThreadFactory() {

					public Thread newThread(Runnable runnable) {
						Thread thread = new Thread(runnable, "CM-sT Processor");
						thread.setDaemon(true);
						return thread;
					}
				});
		operatorExecutor = Executors
				.newSingleThreadExecutor(new ThreadFactory() {

					public Thread newThread(Runnable runnable) {
						Thread thread = new Thread(runnable, "CM-oP Processor");
						thread.setDaemon(true);
						return thread;
					}
				});
		requestExecutor = Executors.newFixedThreadPool(10, new ThreadFactory() {

			public Thread newThread(Runnable runnable) {
				Thread thread = new Thread(runnable, "CM-worker");
				thread.setPriority(Thread.MAX_PRIORITY);
				return thread;
			}
		});
	}

	public void doInBackground(Runnable task) {
		singleThreadExecutor.submit(task);
	}

	@Override
	public void onConnected(ConnectionType type) {
		Log.d("HTTP", "execute(onConnected)");
		if (RunningEnvironment.getInstance().isInitialized()) {
			execute();
		}
	}

	public void executeWhenConnected(Runnable runnable) {
		Log.d("HTTP", "executeWhenConnected addone");
		runnables.add(runnable);
		if (NetworkState.available == NetworkManager.getInstance().getState()) {
			Log.d("HTTP", "execute(addRunnable)");
			execute();
		}
	}

	int localTimer = 0;
	final int LOCALPERIOD = 10 * 60;

	@Override
	public void onTimer() {
		localTimer++;
		if (localTimer > LOCALPERIOD) {
			localTimer = 0;
			Log.d("HTTP", "execute(onTimer)");
			execute();
		}
	}

	protected synchronized void execute() {
		for (int i = 0; i < runnables.size();) {
			operatorExecutor.submit(runnables.get(i));
			runnables.remove(i);
		}
	}

	public Object syncRequest(String url) {
		return syncRequest(url, null);
	}

	public void asyncRequest(String url, AjaxCallBack<? extends Object> callBack) {
		asyncRequest(url, null, callBack);
	}

	public Object syncRequest(String url, AjaxParams params) {
		mProxy = NetUtils.getCurrHttpProxy(RunningEnvironment.getInstance()
				.getApplicationContext());
		HttpUriRequest request = new HttpGet(FinalHttp.getUrlWithQueryString(
				url, params));
		return sendSyncRequest(buildHttpClient(url), httpContext, request, null);
	}

	public void asyncRequest(String url, AjaxParams params,
			AjaxCallBack<? extends Object> callBack) {
		mProxy = NetUtils.getCurrHttpProxy(RunningEnvironment.getInstance()
				.getApplicationContext());
		sendRequest(buildHttpClient(url), httpContext,
				new HttpGet(FinalHttp.getUrlWithQueryString(url, params)),
				null, callBack);
	}

	public <T> void asyncJsonRequest(String url, AjaxParams params,
			AjaxCallBack<T> callBack) {
		mProxy = NetUtils.getCurrHttpProxy(RunningEnvironment.getInstance()
				.getApplicationContext());
		sendJsonRequest(buildHttpClient(url), httpContext,
				new HttpGet(FinalHttp.getUrlWithQueryString(url, params)),
				null, callBack);
	}

	protected DefaultHttpClient buildHttpClient(String url) {
		HttpParams httpParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParams, 15000); // 连接超时15秒
		HttpConnectionParams.setSoTimeout(httpParams, 15000); // 数据超时15秒
		DefaultHttpClient httpClient = new DefaultHttpClient(httpParams);
		// no retry
		// httpClient.setHttpRequestRetryHandler(new RetryHandler(2));

		if (mProxy != null) {
			httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY,
					mProxy);

			HttpHost target = parseHost(url);
			if (target != null) {
				httpClient.getParams().setParameter("X-Online-Host",
						target.toHostString());
			}
		}
		return httpClient;
	}

	protected HttpHost parseHost(String url) {
		HttpHost host = null;
		URL _url;
		try {
			_url = new URL(url);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			_url = null;
		}
		if (_url != null) {
			int port = _url.getPort();
			host = new HttpHost(_url.getHost(),
					-1 == port ? _url.getDefaultPort() : port,
					HttpHost.DEFAULT_SCHEME_NAME);
		}

		return host;
	}

	protected <T> void sendRequest(DefaultHttpClient client,
			HttpContext httpContext, HttpUriRequest uriRequest,
			String contentType, AjaxCallBack<T> ajaxCallBack) {
		if (contentType != null) {
			uriRequest.addHeader("Content-Type", contentType);
		}

		new SimpleHttpHandler<T>(client, ajaxCallBack).executeOnExecutor(
				requestExecutor, uriRequest);

	}

	protected <T> void sendJsonRequest(DefaultHttpClient client,
			HttpContext httpContext, HttpUriRequest uriRequest,
			String contentType, AjaxCallBack<T> ajaxCallBack) {
		if (contentType != null) {
			uriRequest.addHeader("Content-Type", contentType);
		}

		new JsonHttpHandler<T>(client, ajaxCallBack).executeOnExecutor(
				requestExecutor, uriRequest);

	}

	protected Object sendSyncRequest(DefaultHttpClient client,
			HttpContext httpContext, HttpUriRequest uriRequest,
			String contentType) {
		if (contentType != null) {
			uriRequest.addHeader("Content-Type", contentType);
		}
		return new SyncRequestHandler(client, httpContext, "UTF-8")
				.sendRequest(uriRequest);
	}

	protected HttpEntity paramsToEntity(AjaxParams params) {
		HttpEntity entity = null;
		if (params != null) {
			entity = params.getEntity();
		}
		return entity;
	}

	protected HttpEntityEnclosingRequestBase addEntityToRequestBase(
			HttpEntityEnclosingRequestBase requestBase, HttpEntity entity) {
		if (entity != null) {
			requestBase.setEntity(entity);
		}
		return requestBase;
	}

	public void asyncUpdate(String url, AjaxCallBack<? extends Object> callBack) {
		// asyncUpdate(url, null, callBack);
		new FinalHttp().post(url, callBack);
	}

	public void asyncUpdate(String url, AjaxParams params,
			AjaxCallBack<? extends Object> callBack) {
		// asyncUpdate(url, paramsToEntity(params), null, callBack);
		new FinalHttp().post(url, params, callBack);
	}

	public void asyncUpdate(String url, HttpEntity entity, String contentType,
			AjaxCallBack<? extends Object> callBack) {
		// sendRequest(buildHttpClient(url), httpContext,
		// addEntityToRequestBase(new HttpPost(url), entity), contentType,
		// callBack);
		new FinalHttp().post(url, entity, contentType, callBack);
	}

	public Object syncUpdate(String url) {
		// return syncUpdate(url, null);
		return new FinalHttp().postSync(url);
	}

	public Object syncUpdate(String url, AjaxParams params) {
		// return syncUpdate(url, paramsToEntity(params), null);
		return new FinalHttp().postSync(url, params);
	}

	public Object syncUpdate(String url, HttpEntity entity, String contentType) {
		// return sendSyncRequest(buildHttpClient(url), httpContext,
		// addEntityToRequestBase(new HttpPost(url), entity), contentType);
		return new FinalHttp().postSync(url, entity, contentType);
	}

	public byte[] RequestByteArray(String url) throws Exception {
		long totalLength = 0;
		int readLength = 0;

		byte[] resultBuffer = null;
		HttpClient httpClient = null;
		HttpHost host = null;

		try {
			httpClient = buildHttpClient(url);
			host = parseHost(url);

			HttpGet httpRequest = new HttpGet(url);
			HttpResponse httpResponse = httpClient.execute(host, httpRequest);
			if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				HttpEntity entity = httpResponse.getEntity();

				if (entity != null) {
					InputStream is = entity.getContent();
					try {
						totalLength = httpResponse.getEntity()
								.getContentLength();
						if (totalLength > 0) {
							int tempBufferSize = 4096;
							readLength = 0;
							resultBuffer = new byte[(int) totalLength];
							byte[] tempBuffer = new byte[tempBufferSize];

							int tempReadLength = is.read(tempBuffer);
							while (tempReadLength != -1) {
								System.arraycopy(tempBuffer, 0, resultBuffer,
										readLength, tempReadLength);
								readLength += tempReadLength;
								tempReadLength = is.read(tempBuffer);
							}
						}

					} catch (IOException e) {
						e.printStackTrace();
					} catch (RuntimeException e) {
						httpRequest.abort();
						e.printStackTrace();
					} finally {
						try {
							is.close();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
		} catch (NoHttpResponseException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (httpClient != null) {
				try {
					httpClient.getConnectionManager().shutdown();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		if (readLength == totalLength && resultBuffer != null) {
			return resultBuffer;
		} else {
			return null;
		}

	}
}
