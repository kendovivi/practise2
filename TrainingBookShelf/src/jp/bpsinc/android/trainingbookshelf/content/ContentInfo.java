
package jp.bpsinc.android.trainingbookshelf.content;

public class ContentInfo {
    /** コンテンツ専用ID ISSN番号？ */
    private String mUid;
    /** コンテンツタイトル */
    private String mTitle;
    /** コンテンツ著者 */
    private String mAuthor;
    /** コンテンツEPUBファイルパス */
    private String mEpubPath;

    public ContentInfo(String uid, String title, String author, String epubPath) {
        this.mUid = uid;
        this.mTitle = title;
        this.mAuthor = author;
        this.setmEpubPath(epubPath);
    };

    public String getContentTitle() {
        return mTitle;
    }

    public void setContentTitle(String title) {
        this.mTitle = title;
    }

    public String getContentAuthor() {
        return mAuthor;
    }

    public void setContentAuthor(String author) {
        this.mAuthor = author;
    }

    public String getUid() {
        return mUid;
    }

    public void setUid(String uid) {
        this.mUid = uid;
    }

    public String getEpubPath() {
        return mEpubPath;
    }

    public void setmEpubPath(String epubPath) {
        this.mEpubPath = epubPath;
    }

}
