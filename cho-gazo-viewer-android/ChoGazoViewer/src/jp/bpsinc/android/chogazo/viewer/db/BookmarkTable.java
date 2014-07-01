
package jp.bpsinc.android.chogazo.viewer.db;

import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

public class BookmarkTable extends AbstractTable {
    public static final String TABLE_NAME = "bookmark";
    public static final String COLUMN_CONTENTS_KEY = "contents_key";
    public static final String COLUMN_BOOKMARK_PAGE = "bookmark_page";
    public static final String COLUMN_LABEL = "label";
    public static final String COLUMN_DATE = "date";
    public static final String SQL_CREATE = "CREATE TABLE " + TABLE_NAME + " ("
            + COLUMN_CONTENTS_KEY + " TEXT,"
            + COLUMN_BOOKMARK_PAGE + " INTEGER NOT NULL,"
            + COLUMN_LABEL + " TEXT NOT NULL,"
            + COLUMN_DATE + " TEXT NOT NULL,"
            + "CONSTRAINT bookmark_pkey PRIMARY KEY ("
            + COLUMN_CONTENTS_KEY + "," + COLUMN_BOOKMARK_PAGE + ")" + ");";
    public static final String SQL_WHERE_PRIMARY = COLUMN_CONTENTS_KEY + " = ? AND "
            +  COLUMN_BOOKMARK_PAGE + " = ?";
    public static final String SQL_WHERE_USER_BOOK = COLUMN_CONTENTS_KEY + " = ?";

    public BookmarkTable(Context context) {
        super(context);
    }

    public List<AbstractRow> getBookmark(String contentsKey) {
        return findAll(SQL_WHERE_USER_BOOK, new String[] {contentsKey}, null, null, COLUMN_DATE);
    }

    public List<AbstractRow> getBookmark(String contentsKey, int pageIndex) {
        return findAll(SQL_WHERE_PRIMARY, new String[] {contentsKey, String.valueOf(pageIndex)});
    }

    public void insertBookmark(String contentsKey, int pageIndex, String label, String date) {
        Row row = new Row();
        row.mContentsKey = contentsKey;
        row.mBookmarkPage = pageIndex;
        row.mLabel = label;
        row.mDate = date;
        insert(row);
    }

    public void updateBookmarkLabel(String contentsKey, int pageIndex, String label) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_LABEL, label);
        update(values, SQL_WHERE_PRIMARY, new String[] {contentsKey, String.valueOf(pageIndex)});
    }

    public void deleteBookmark(String contentsKey, int pageIndex) {
        delete(SQL_WHERE_PRIMARY, new String[] {contentsKey, String.valueOf(pageIndex)});
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
        private int mBookmarkPage;
        private String mLabel;
        private String mDate;

        public static Row build(Cursor cursor) {
            Row row = new Row();
            row.mContentsKey = cursor.getString(cursor.getColumnIndex(COLUMN_CONTENTS_KEY));
            row.mBookmarkPage = cursor.getInt(cursor.getColumnIndex(COLUMN_BOOKMARK_PAGE));
            row.mLabel = cursor.getString(cursor.getColumnIndex(COLUMN_LABEL));
            row.mDate = cursor.getString(cursor.getColumnIndex(COLUMN_DATE));
            return row;
        }

        @Override
        public ContentValues toContentValues() {
            ContentValues values = new ContentValues();
            values.put(COLUMN_CONTENTS_KEY, mContentsKey);
            values.put(COLUMN_BOOKMARK_PAGE, mBookmarkPage);
            values.put(COLUMN_LABEL, mLabel);
            values.put(COLUMN_DATE, mDate);
            return values;
        }

        public String getContentsKey() {
            return mContentsKey;
        }

        public int getBookmarkPage() {
            return mBookmarkPage;
        }

        public String getLabel() {
            return mLabel;
        }

        public String getDate() {
            return mDate;
        }
    }
}
