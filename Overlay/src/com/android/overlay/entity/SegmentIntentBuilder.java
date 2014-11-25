package com.android.overlay.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class SegmentIntentBuilder<T extends SegmentIntentBuilder<?>> extends
		BaseIntentBuilder<T> {

	private final List<String> segments;

	public SegmentIntentBuilder(Context context, ComponentName component) {
		super(context, component);
		segments = new ArrayList<String>();
	}

	protected int getSegmentCount() {
		return segments.size();
	}

	@SuppressWarnings("unchecked")
	public T addSegment(String segment) {
		segments.add(segment);
		return (T) this;
	}

	protected void preBuild() {
		// TODO
	}

	@Override
	public Intent build() {
		preBuild();
		Intent intent = super.build();
		Uri.Builder builder = new Uri.Builder();
		for (String segment : segments) {
			builder.appendPath(segment);
		}
		Uri uri = builder.build();
		uri = Uri.parse(uri.toString());
		if (intent != null) {
			intent.setData(uri);
		}
		return intent;
	}

	public static List<String> getSegments(Intent intent) {
		Uri uri = intent.getData();
		if (uri == null) {
			List<String> emptyList = Collections.emptyList();
			return emptyList;
		}
		return uri.getPathSegments();
	}

	public static String getSegment(Intent intent, int index) {
		Uri uri = intent.getData();
		if (uri == null) {
			return null;
		}
		List<String> list = uri.getPathSegments();
		if (list.size() <= index) {
			return null;
		}
		return list.get(index);
	}

}