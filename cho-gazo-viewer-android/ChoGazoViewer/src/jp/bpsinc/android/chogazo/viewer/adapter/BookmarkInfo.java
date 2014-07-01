
package jp.bpsinc.android.chogazo.viewer.adapter;

import java.io.Serializable;

@SuppressWarnings("serial")
public class BookmarkInfo implements Serializable {
    private final String mContentsKey;
    private final int mBookmarkPage;

    public BookmarkInfo(String contentsKey, int bookmarkPage) {
        mContentsKey = contentsKey;
        mBookmarkPage = bookmarkPage;
    }

    public String getContentsKey() {
        return mContentsKey;
    }

    public int getBookmarkPage() {
        return mBookmarkPage;
    }
}
