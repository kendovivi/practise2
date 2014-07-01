
package jp.bpsinc.android.chogazo.viewer.content;

import java.io.Serializable;

@SuppressWarnings("serial")
public class ViewerContents implements Serializable {
    /** コンテンツを一意に特定するためのキー */
    private String mContentsKey;
    /** コンテンツのファイルパス */
    private String mPath;
    /** コンテンツを読み込むクラスの名称 */
    private String mReaderClassName;
    /** コンテンツを解析するクラスの名称 */
    private String mBookClassName;
    /** コンテンツのタイトル */
    private String mTitle;
    /** コンテンツのタイトル(かな) */
    private String mTitleKana;
    /** コンテンツの著者名 */
    private String mAuthor;
    /** コンテンツの著者名(かな) */
    private String mAuthorKana;
    /** コンテンツを読み込む時に使用するデータ */
    private Serializable mReaderOption;

    /**
     * コンテンツを一意に特定するためのキーを取得する
     * 
     * @return コンテンツを一意に特定するためのキー
     */
    public String getContentsKey() {
        return mContentsKey;
    }

    /**
     * コンテンツを一意に特定するためのキーをセットする
     * 
     * @param contentsKey コンテンツを一意に特定するためのキー
     */
    public void setContentsKey(String contentsKey) {
        this.mContentsKey = contentsKey;
    }

    /**
     * コンテンツのファイルパスを取得する
     * 
     * @return コンテンツのファイルパス
     */
    public String getPath() {
        return mPath;
    }

    /**
     * コンテンツのファイルパスをセットする
     * 
     * @param path コンテンツのファイルパス
     */
    public void setPath(String path) {
        this.mPath = path;
    }

    /**
     * コンテンツを読み込むクラスの名称を取得する
     * 
     * @return コンテンツを読み込むクラスの名称
     */
    public String getReaderClassName() {
        return mReaderClassName;
    }

    /**
     * コンテンツを読み込むクラスの名称をセットする
     * 
     * @param readerClassName コンテンツを読み込むクラスの名称
     */
    public void setReaderClassName(String readerClassName) {
        this.mReaderClassName = readerClassName;
    }

    /**
     * コンテンツを解析するクラスの名称を取得する
     * 
     * @return コンテンツを解析するクラスの名称
     */
    public String getBookClassName() {
        return mBookClassName;
    }

    /**
     * コンテンツを解析するクラスの名称をセットする
     * 
     * @param bookClassName コンテンツを解析するクラスの名称
     */
    public void setBookClassName(String bookClassName) {
        this.mBookClassName = bookClassName;
    }

    /**
     * コンテンツのタイトルを取得する
     * 
     * @return コンテンツのタイトル
     */
    public String getTitle() {
        return mTitle;
    }

    /**
     * コンテンツのタイトルをセットする
     * 
     * @param title コンテンツのタイトル
     */
    public void setTitle(String title) {
        this.mTitle = title;
    }

    /**
     * コンテンツのタイトル(かな)を取得する
     * 
     * @return コンテンツのタイトル(かな)
     */
    public String getTitleKana() {
        return mTitleKana;
    }

    /**
     * コンテンツのタイトル(かな)をセットする
     * 
     * @param titleKana コンテンツのタイトル(かな)
     */
    public void setTitleKana(String titleKana) {
        this.mTitleKana = titleKana;
    }

    /**
     * コンテンツの著者名を取得する
     * 
     * @return コンテンツの著者名
     */
    public String getAuthor() {
        return mAuthor;
    }

    /**
     * コンテンツの著者名をセットする
     * 
     * @param author コンテンツの著者名
     */
    public void setAuthor(String author) {
        this.mAuthor = author;
    }

    /**
     * コンテンツの著者名(かな)を取得する
     * 
     * @return コンテンツの著者名(かな)
     */
    public String getAuthorKana() {
        return mAuthorKana;
    }

    /**
     * コンテンツの著者名(かな)をセットする
     * 
     * @param authorKana コンテンツの著者名(かな)
     */
    public void setAuthorKana(String authorKana) {
        this.mAuthorKana = authorKana;
    }

    /**
     * コンテンツを読み込む時に使用するデータを取得する
     * 
     * @return コンテンツを読み込む時に使用するデータ
     */
    public Serializable getReaderOption() {
        return mReaderOption;
    }

    /**
     * コンテンツを読み込む時に使用するデータをセットする
     * 
     * @param readerOption コンテンツを読み込む時に使用するデータ
     */
    public void setReaderOption(Serializable readerOption) {
        this.mReaderOption = readerOption;
    }
}
