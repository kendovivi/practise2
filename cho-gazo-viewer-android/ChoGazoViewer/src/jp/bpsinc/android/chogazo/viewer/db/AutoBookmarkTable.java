
package jp.bpsinc.android.chogazo.viewer.db;

import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

public class AutoBookmarkTable extends AbstractTable {
    public static final String TABLE_NAME = "auto_bookmark";
    public static final String COLUMN_CONTENTS_KEY = "contents_key";
    public static final String COLUMN_LAST_PAGE = "last_page";
    public static final String SQL_CREATE = "CREATE TABLE " + TABLE_NAME + " ("
            + COLUMN_CONTENTS_KEY + " TEXT PRIMARY KEY,"
            + COLUMN_LAST_PAGE + " INTEGER NOT NULL);";
    public static final String SQL_WHERE_PRIMARY = COLUMN_CONTENTS_KEY + " = ?";

    private final String mContentsKey;
    private final String[] mSelectionArgsPrimary;

    public AutoBookmarkTable(Context context, String contentsKey) {
        super(context);
        mContentsKey = contentsKey;
        mSelectionArgsPrimary = new String[] {
                mContentsKey
        };
    }

    public Row getAutoBookmark() {
        Row row = null;
        List<AbstractRow> list = findAll(SQL_WHERE_PRIMARY, mSelectionArgsPrimary);
        if (list.size() > 0) {
            row = (Row) list.get(0);
        }
        return row;
    }

    public void setAutoBookmark(int pageIndex) {
        Row row = new Row();
        row.mContentsKey = mContentsKey;
        row.mLastPage = pageIndex;
        replace(row);
    }

    @Override
    protected String getTableName() {
        return TABLE_NAME;
    }

    @Override
    protected AbstractRow buildRow(Cursor cursor) {
        return Row.build(cursor);
    }

    /**
     * TABLEの1行を表現
     */
    public static class Row extends AbstractRow {
        private String mContentsKey;
        private int mLastPage;

        public static Row build(Cursor cursor) {
            Row row = new Row();
            row.mContentsKey = cursor.getString(cursor.getColumnIndex(COLUMN_CONTENTS_KEY));
            row.mLastPage = cursor.getInt(cursor.getColumnIndex(COLUMN_LAST_PAGE));
            return row;
        }

        @Override
        public ContentValues toContentValues() {
            ContentValues values = new ContentValues();
            values.put(COLUMN_CONTENTS_KEY, mContentsKey);
            values.put(COLUMN_LAST_PAGE, mLastPage);
            return values;
        }

        public String getContentsKey() {
            return mContentsKey;
        }

        public int getLastPage() {
            return mLastPage;
        }
    }
}
