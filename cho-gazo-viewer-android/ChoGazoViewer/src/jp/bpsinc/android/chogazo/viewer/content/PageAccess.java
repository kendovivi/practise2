
package jp.bpsinc.android.chogazo.viewer.content;

import java.io.InputStream;
import java.util.ArrayList;

import jp.bpsinc.android.chogazo.viewer.exception.ContentsOtherException;
import jp.bpsinc.android.chogazo.viewer.exception.ContentsParseException;

public abstract class PageAccess {
    /** コンテンツのファイルアクセスに使用 */
    private ContentsReader mReader;
    /** 解析したコンテンツの各種情報 */
    private Book mBook;
    /** 単一ページ時のページ情報(ページ順) */
    protected ArrayList<PageInfo> mSinglePageInfo;
    /** 見開きページ時のページ情報(ページ順) */
    protected ArrayList<PageInfo> mSpreadPageInfo;

    public PageAccess(ContentsReader reader, Book book) throws ContentsOtherException {
        if (reader == null || reader.isClosed()) {
            throw new ContentsOtherException("reader is not open");
        }
        if (book == null) {
            throw new ContentsOtherException("book is not null");
        }
        mReader = reader;
        mBook = book;
        mSinglePageInfo = book.getSinglePageInfo();
        mSpreadPageInfo = book.getSpreadPageInfo();
    }

    public void close() {
        mReader.close();
    }

    public byte[] getPageData(PageItem item) throws ContentsParseException,
            ContentsOtherException {
        return mReader.getFileContents(item.getKey());
    }

    public InputStream getPageStream(PageItem item) throws ContentsParseException,
            ContentsOtherException {
        return mReader.getInputStream(item.getKey());
    }

    /**
     * 設定に対応して正しい並び順に変更した単ページ情報リストを返す。
     * 
     * @return 設定に対応したページ順に並び替えられた単ページ情報リスト
     */
    public abstract ArrayList<PageInfo> getSinglePageInfoList();

    /**
     * 設定に対応して正しい並び順に変更した見開きページ情報リストを返す。
     * 
     * @return 設定に対応したページ順に並び替えられた見開きページ情報リスト
     */
    public abstract ArrayList<PageInfo> getSpreadPageInfoList();

    /**
     * 見開きページインデックスを単一ページインデックスに変換
     * 
     * @param index 見開きページインデックスを単一ページインデックスに変換した値、index < 0の場合は0、
     *            index >= mSpreadPages.size()の場合はmSinglePages.size() - 1
     * @return
     */
    public int getSingleIndexFromSpreadIndex(int index) {
        if (index < 0) {
            return 0;
        } else if (index >= mSpreadPageInfo.size()) {
            return mSinglePageInfo.size() - 1;
        }
        return mSpreadPageInfo.get(index).getThisPage();
    }

    /**
     * 単一ページインデックスを見開きページインデックスに変換
     * 
     * @param index 単一ページインデックスを見開きページインデックスに変換した値、 index < 0の場合は0、 index >=
     *            mSinglePages.size()の場合はmSpreadPages.size() - 1、<br>
     *            mSpreadPages内に存在するPageInfoオブジェクトを全て調べてもindex以上の値が見つからない場合は
     *            mSpreadPages.size() - 1
     * @return
     */
    public int getSpreadIndexFromSingleIndex(int index) {
        if (index < 0) {
            return 0;
        } else if (index >= mSinglePageInfo.size()) {
            return mSpreadPageInfo.size() - 1;
        }
        int spreadIndex = 0;
        for (PageInfo pageInfo : mSpreadPageInfo) {
            if (pageInfo.getLeftPageItem() != null && pageInfo.getRightPageItem() != null) {
                if (index <= pageInfo.getThisPage() + 1) {
                    return spreadIndex;
                }
            } else {
                if (index <= pageInfo.getThisPage()) {
                    return spreadIndex;
                }
            }
            spreadIndex++;
        }
        // 存在しない場合は最終ページのインデックスを返す
        return mSpreadPageInfo.size() - 1;
    }

    public boolean isRtl() {
        return mBook.isRtl();
    }
}
