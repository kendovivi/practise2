
package jp.bpsinc.android.chogazo.viewer.content;

import jp.bpsinc.android.chogazo.viewer.util.LogUtil;
import jp.bpsinc.android.chogazo.viewer.content.Page.HorizontalAlign;
import jp.bpsinc.android.chogazo.viewer.content.Page.LoadQuality;
import jp.bpsinc.android.chogazo.viewer.content.Page.Size;
import jp.bpsinc.android.chogazo.viewer.content.Page.VerticalAlign;
import jp.bpsinc.android.chogazo.viewer.view.EbookView;
import jp.bpsinc.android.chogazo.viewer.view.EbookMode;
import jp.bpsinc.android.chogazo.viewer.view.EbookMode.Mode;
import android.graphics.Canvas;

public class RetentionPageHelper {
    /** 電子書籍を表示するビュー */
    private final EbookView mEbookView;
    /** コンテンツのページへアクセスを行うオブジェクト */
    private final PageAccess mPageAccess;
    /** ページごとに画像読み込みスレッドを作成、制御 */
    private final PageLoadManager mPageLoadManager;
    /** ビューアの各種設定の取得などに使用 */
    private final EbookMode mEbookMode;
    /** 電子書籍を表示するビューのサイズ */
    private Size mDisplaySize;
    /** カレントページに表示するページ */
    private Page mCurrentPage;
    /** カレントページの右側に表示するページ(縦スクロールの場合は上側に表示するページ) */
    private Page mRightPage;
    /** カレントページの左側に表示するページ(縦スクロールの場合は下側に表示するページ) */
    private Page mLeftPage;

    /**
     * このクラスオブジェクトを生成後、必ず一番初めにdisplayInitを実行すること
     * 
     * @param ebookView 電子書籍を表示するビュー
     * @param pageAccess コンテンツのページへアクセスを行うオブジェクト
     * @param pageLoadManager 画像読み込みマネージャ
     */
    public RetentionPageHelper(EbookView ebookView, PageAccess pageAccess,
            PageLoadManager pageLoadManager) {
        LogUtil.v();
        mEbookView = ebookView;
        mPageAccess = pageAccess;
        mPageLoadManager = pageLoadManager;
        mEbookMode = mEbookView.getEbookMode();
    }

    /**
     * 初期化済みか判定
     * 
     * @return 初期化済みならtrue
     */
    public boolean initialized() {
        return mDisplaySize != null;
    }

    /**
     * ビューのサイズを設定して初期化する<br>
     * 注意：このメソッドを1度も実行せずに他のメソッドを実行してはいけない
     * 
     * @param displayWidth viewの横幅
     * @param displayHeight viewの縦幅
     */
    public void displayInit(int displayWidth, int displayHeight) {
        stopAllPageLoadAndReset();
        mDisplaySize = new Size(displayWidth, displayHeight);
        mCurrentPage = new Page(mPageAccess, mDisplaySize, mEbookMode);
        mRightPage = new Page(mPageAccess, mDisplaySize, mEbookMode);
        mLeftPage = new Page(mPageAccess, mDisplaySize, mEbookMode);
    }

    public Page getCurrentPage() {
        return mCurrentPage;
    }

    public void stopAllPageLoadAndReset() {
        mPageLoadManager.stopAllTasks();
        if (mCurrentPage != null) {
            mCurrentPage.reset();
        }
        if (mRightPage != null) {
            mRightPage.reset();
        }
        if (mLeftPage != null) {
            mLeftPage.reset();
        }
    }

    private void setLoadDefaultVerticalAlign(Page page) {
        switch (mEbookMode.getMode()) {
            case PORTRAIT_STANDARD:
            case PORTRAIT_SPREAD:
            case LANDSCAPE_SPREAD:
                page.setVerticalAlign(VerticalAlign.MIDDLE);
                break;
            case LANDSCAPE_STANDARD:
                page.setVerticalAlign(VerticalAlign.TOP);
                break;
        }
    }

    private void loadCurrentPage() {
        setLoadDefaultVerticalAlign(mCurrentPage);
        if (mEbookMode.isVerticalScroll()) {
            mCurrentPage.setVerticalAlign(VerticalAlign.TOP);
        } else if (mEbookMode.getMode() == Mode.PORTRAIT_SPREAD) {
            // 右開き、左開きでデフォルトポジションを変更する＋単ページから見開きに切り替わった場合にデフォルトポジションを単ページ時の方に寄せる
            if ((mPageAccess.isRtl() && mEbookMode.isSubsequentPagePositionFlag() == false)
                    || (mPageAccess.isRtl() == false && mEbookMode.isSubsequentPagePositionFlag())) {
                mCurrentPage.setHorizontalAlign(HorizontalAlign.RIGHT);
            } else {
                mCurrentPage.setHorizontalAlign(HorizontalAlign.LEFT);
            }
        }
        mEbookMode.setIsSubsequentPagePositionFlag(false);
        if (mCurrentPage.hasBitmap() == false) {
            mCurrentPage.setPage(mEbookView.getCurrentPageInfo());
            mPageLoadManager.addPageLoadThread(mCurrentPage, LoadQuality.NEUTRAL);
        }
    }

    private void loadRightPage() {
        if (mEbookView.convertedPage(mEbookView.getCurrentPageIndex()) > 0) {
            setLoadDefaultVerticalAlign(mRightPage);
            if (mEbookMode.isVerticalScroll()) {
                mRightPage.setVerticalAlign(VerticalAlign.BOTTOM);
            } else if (mEbookMode.getMode() == Mode.PORTRAIT_SPREAD) {
                mRightPage.setHorizontalAlign(HorizontalAlign.LEFT);
            }
            if (mRightPage.hasBitmap()) {
                setDefaultPageValue(mRightPage);
                replaceToNormalQuality(mRightPage);
            } else {
                mRightPage.setPage(mEbookView.getRightPageInfo());
                mPageLoadManager.addPageLoadThread(mRightPage, LoadQuality.NEUTRAL);
            }
        }
    }

    private void loadLeftPage() {
        int convertedAfterPageIndex = mEbookView.isReversePage()
                ? mEbookView.convertedPage(mEbookView.getCurrentPageIndex() - 1)
                : mEbookView.getCurrentPageIndex() + 1;
        if (convertedAfterPageIndex < mEbookView.getPageCount()) {
            setLoadDefaultVerticalAlign(mLeftPage);
            if (mEbookMode.isVerticalScroll()) {
                mLeftPage.setVerticalAlign(VerticalAlign.TOP);
            } else if (mEbookMode.getMode() == Mode.PORTRAIT_SPREAD) {
                mLeftPage.setHorizontalAlign(HorizontalAlign.RIGHT);
            }
            if (mLeftPage.hasBitmap()) {
                setDefaultPageValue(mLeftPage);
                replaceToNormalQuality(mLeftPage);
            } else {
                mLeftPage.setPage(mEbookView.getLeftPageInfo());
                mPageLoadManager.addPageLoadThread(mLeftPage, LoadQuality.NEUTRAL);
            }
        }
    }

    private void setDefaultPageValue(Page page) {
        page.setScale(Page.PAGE_DEFAULT_SCALE);
        page.setDefaultAlign();
        page.setDefaultPosition();
    }

    public void replaceToHighQuality() {
        if (mCurrentPage.canReplaceToQuality(LoadQuality.HIGH)) {
            mPageLoadManager.addPageLoadThread(mCurrentPage, LoadQuality.HIGH);
        }
    }

    private void replaceToNormalQuality(Page page) {
        if (page.canReplaceToQuality(LoadQuality.NEUTRAL)) {
            mPageLoadManager.addPageLoadThread(page, LoadQuality.NEUTRAL);
        }
    }

    public void updatePages() {
        int convertedCurrentPageIndex = mEbookView.convertedPage(mEbookView.getCurrentPageIndex());
        int drawCurrentPageIndex = mEbookView.getPageIndex(mCurrentPage.getPageInfo());

        if (drawCurrentPageIndex == -1 || convertedCurrentPageIndex >= drawCurrentPageIndex + 3
                || convertedCurrentPageIndex <= drawCurrentPageIndex - 3) {
            // getPageInfoがnullだったり、見開き切り替え時などでgetPageInfoしたPageInfoがページリスト内に無かったり、3ページ以上ずれてたら全とっかえ
            // stopAllPageLoadAndResetだとwaitが発生するのでひとまず3回シフトしとく、スレッド終了を待たない分メモリ食うかも？
            shiftPageLeft();
            shiftPageLeft();
            shiftPageLeft();
        } else if (convertedCurrentPageIndex > drawCurrentPageIndex) {
            // 左(下)のページ方向へ遷移
            for (int i = drawCurrentPageIndex; i < convertedCurrentPageIndex; i++) {
                shiftPageRight();
            }
        } else if (convertedCurrentPageIndex < drawCurrentPageIndex) {
            // 右(上)のページ方向へ遷移
            for (int i = drawCurrentPageIndex; i > convertedCurrentPageIndex; i--) {
                shiftPageLeft();
            }
        }
        loadCurrentPage();
        loadRightPage();
        loadLeftPage();
        mEbookView.drawInvalidate();
    }

    private void shiftPageLeft() {
        mPageLoadManager.stopPageLoadThread(mLeftPage);
        mLeftPage.reset();

        Page pageWork = mLeftPage;
        mLeftPage = mCurrentPage;
        mCurrentPage = mRightPage;
        mRightPage = pageWork;
    }

    private void shiftPageRight() {
        mPageLoadManager.stopPageLoadThread(mRightPage);
        mRightPage.reset();

        Page pageWork = mRightPage;
        mRightPage = mCurrentPage;
        mCurrentPage = mLeftPage;
        mLeftPage = pageWork;
    }

    public void drawAllPage(Canvas canvas, float translateX, float translateY,
            boolean isClearBitmap, boolean isAnimating) {
        float drawX = 0;
        float drawY = 0;
        float pageDiffX = 0;
        float pageDiffY = 0;

        // 縦・横スクロールを判定し、PageInfoオブジェクトを元にしてビュー上の描画ポジション取得
        if (mEbookMode.isVerticalScroll()) {
            drawY = translateY + mEbookView.getPagePositionY(
                    mEbookView.getPageIndex(mCurrentPage.getPageInfo()));
            pageDiffY = mDisplaySize.height + EbookView.PAGE_PADDING;
        } else {
            drawX = translateX + mEbookView.getPagePositionX(
                    mEbookView.getPageIndex(mCurrentPage.getPageInfo()));
            pageDiffX = mDisplaySize.width + EbookView.PAGE_PADDING;
        }
        LogUtil.v("drawX=%f, drawY=%f, pageDiffX=%f, pageDiffY=%f",
                drawX, drawY, pageDiffX, pageDiffY);
        mCurrentPage.drawPage(canvas, drawX, drawY, isClearBitmap, isAnimating);
        mRightPage.drawPage(canvas, drawX + pageDiffX, drawY - pageDiffY, isClearBitmap,
                isAnimating);
        mLeftPage.drawPage(canvas, drawX - pageDiffX, drawY + pageDiffY, isClearBitmap,
                isAnimating);
    }
}
