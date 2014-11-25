package com.android.overlay.connection;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.json.JSONObject;

public class JsonEntityHandler<T> {

	public Object handleEntity(HttpEntity entity, EntityCallBack callback,
			String charset) throws IOException {
		if (entity == null)
			return null;

		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];

		long count = entity.getContentLength();
		long curCount = 0;
		int len = -1;
		InputStream is = entity.getContent();
		while ((len = is.read(buffer)) != -1) {
			outStream.write(buffer, 0, len);
			curCount += len;
			if (callback != null)
				callback.callBack(count, curCount, false);
		}
		if (callback != null)
			callback.callBack(count, curCount, true);
		byte[] data = outStream.toByteArray();
		outStream.close();
		is.close();
		JSONObject js = null;
		try {
			js = new JSONObject(new String(data, charset));
		} catch (Exception e) {
			js = null;
		}
		if (js == null) {
			return null;
		}

		// Type type = this.getClass();
		// type = this.getClass().getGenericSuperclass();
		//
		// Class<T> entityClass = (Class<T>) ((ParameterizedType)
		// JsonEntityHandler.class
		// .getGenericSuperclass()).getActualTypeArguments()[0];
		// T model = JSON.parseObject(js.toString(), entityClass);
		return js;
	}
}
