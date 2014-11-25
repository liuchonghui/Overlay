package com.android.overlay.table;

import android.database.Cursor;

/**
 * Table with entity related information.
 */
public abstract class AbstractEntityTable extends AbstractAccountTable {

	public static interface Fields extends AbstractAccountTable.Fields {

		public static final String USER = "user";

	}

	public static String getUser(Cursor cursor) {
		return cursor.getString(cursor.getColumnIndex(Fields.USER));
	}

}