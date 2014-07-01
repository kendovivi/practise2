
package jp.bpsinc.android.chogazo.viewer.content;

import java.util.ArrayList;

import jp.bpsinc.android.chogazo.viewer.exception.ContentsOtherException;
import jp.bpsinc.android.chogazo.viewer.exception.ContentsParseException;
import jp.bpsinc.android.chogazo.viewer.exception.UnexpectedException;

public abstract class Book {
    /** 書誌情報：タイトル */
    protected String mTitle = "";
    /** 書誌情報：タイトル(かな) */
    protected String mTitleKana = "";
    /** 書誌情報：著者名 */
    protected String mAuthor = "";
    /** 書誌情報：著者名(かな) */
    protected String mAuthorKana = "";
    /** ページめくり方向、デフォルト値は右から左へ */
    protected boolean mIsRtl = true;
    /** 単ページ時のページリスト */
    private ArrayList<PageInfo> mSinglePageInfo = new ArrayList<PageInfo>();
    /** 見開きページ時のページリスト */
    private ArrayList<PageInfo> mSpreadPageInfo = new ArrayList<PageInfo>();
    /** 目次情報リスト */
    protected ArrayList<NavListItem> mNavList = new ArrayList<NavListItem>();

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
        mTitle = title;
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
        mTitleKana = titleKana;
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
        mAuthor = author;
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
        mAuthorKana = authorKana;
    }

    /**
     * ページめくり方向を判定する
     * 
     * @return ページめくり方向が右から左の場合はtrue、左から右の場合はfalse
     */
    public boolean isRtl() {
        return mIsRtl;
    }

    /**
     * ページめくり方向を設定する
     * 
     * @param isRtl ページめくり方向
     */
    public void setIsRtl(boolean isRtl) {
        mIsRtl = isRtl;
    }

    /**
     * コンテンツの総ページ数(単ページ表示時のページ数)を取得する
     * 
     * @return コンテンツの総ページ数
     */
    public int getPageCount() {
        return mSinglePageInfo.size();
    }

    /**
     * 単ページ表示時のページ情報リストを取得する
     * 
     * @return 単ページ表示時のページ情報リスト
     */
    public ArrayList<PageInfo> getSinglePageInfo() {
        return mSinglePageInfo;
    }

    /**
     * 見開きページ表示時のページ情報リストを取得する
     * 
     * @return 見開きページ表示時のページ情報リスト
     */
    public ArrayList<PageInfo> getSpreadPageInfo() {
        return mSpreadPageInfo;
    }

    /**
     * 目次情報リストを取得する
     * 
     * @return 目次情報リスト
     */
    public ArrayList<NavListItem> getNavList() {
        return mNavList;
    }

    /**
     * 単ページ用のページリストにページ情報を追加する、ページアイテムがnullの場合は何もしない
     * 
     * @param item ページアイテム
     * @param currentPage 対象ページのページ数
     * @return 次のページのページ数
     */
    protected int addSinglePageInfo(PageItem item, int currentPage) {
        if (item != null) {
            mSinglePageInfo.add(new PageInfo(item, currentPage));
            currentPage++;
        }
        return currentPage;
    }

    /**
     * 見開きページ用のページリストにページ情報を追加する、ページアイテムがnullの場合は何もしない
     * 
     * @param item センターページアイテム
     * @param currentPage 対象ページのページ数
     * @return 次のページのページ数
     */
    protected int addSpreadPageInfo(PageItem item, int currentPage) {
        if (item != null) {
            mSpreadPageInfo.add(new PageInfo(item, currentPage));
            currentPage++;
        }
        return currentPage;
    }

    /**
     * 見開きページ用のページリストにページ情報を追加する、ページアイテムが全てnullの場合は何もしない
     * 
     * @param leftItem レフトページアイテム
     * @param rightItem ライトページアイテム
     * @param currentPage 対象ページのページ数
     * @return 次のページのページ数
     */
    protected int addSpreadPageInfo(PageItem leftItem, PageItem rightItem, int currentPage) {
        if (leftItem != null || rightItem != null) {
            mSpreadPageInfo.add(new PageInfo(leftItem, rightItem, currentPage));
        }
        if (leftItem != null) {
            currentPage++;
        }
        if (rightItem != null) {
            currentPage++;
        }
        return currentPage;
    }

    /**
     * コンテンツを解析して書誌情報の設定、ページリストの作成を行う
     * 
     * @param reader コンテンツをオープン済みの入力クラス
     * @throws ContentsParseException コンテンツの解析エラー
     * @throws ContentsOtherException 解析処理以外でエラー
     * @throws UnexpectedException その他例外
     */
    public abstract void parse(ContentsReader reader) throws
            ContentsParseException, ContentsOtherException, UnexpectedException;

    /**
     * コンテンツを解析した情報を使用して書誌情報を初期化する
     */
    protected abstract void bibliographicInit();
}
