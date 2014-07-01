
package jp.bpsinc.android.chogazo.viewer.view;

import java.util.List;

import jp.bpsinc.android.chogazo.viewer.Config;
import jp.bpsinc.android.chogazo.viewer.support.MyViewCompat;
import jp.bpsinc.android.chogazo.viewer.util.LogUtil;
import jp.bpsinc.android.chogazo.viewer.activity.ViewerActivity;
import jp.bpsinc.android.chogazo.viewer.content.Page;
import jp.bpsinc.android.chogazo.viewer.content.PageAccess;
import jp.bpsinc.android.chogazo.viewer.content.PageInfo;
import jp.bpsinc.android.chogazo.viewer.content.PageLoadManager;
import jp.bpsinc.android.chogazo.viewer.content.RetentionPageHelper;
import jp.bpsinc.android.chogazo.viewer.content.PageLoadThread.PageLoadCompleteListener;
import jp.bpsinc.android.chogazo.viewer.listener.FxlViewerOnGestureListener;
import jp.bpsinc.android.chogazo.viewer.listener.OmfViewerOnGestureListener;
import jp.bpsinc.android.chogazo.viewer.listener.ScaleGestureDetector;
import jp.bpsinc.android.chogazo.viewer.listener.ViewerOnGestureListener;
import jp.bpsinc.android.chogazo.viewer.listener.ViewerOnScaleGestureListener;
import jp.bpsinc.android.chogazo.viewer.view.EbookMode.FitMode;
import jp.bpsinc.android.chogazo.viewer.view.EbookMode.Mode;
import jp.bpsinc.android.chogazo.viewer.view.EbookMode.PageAnimation;
import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.os.Build;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

@SuppressLint("ViewConstructor")
public class EbookView extends View {
    private final ViewerActivity mViewerActivity;
    /** コンテンツのページへアクセスを行うオブジェクト */
    private final PageAccess mPageAccess;
    /** 画像読み込みマネージャ */
    private final PageLoadManager mPageLoadManager;
    /** 画像の読み込み、保持、描画などを行う */
    private RetentionPageHelper mRetentionPageHelper;
    /** スクロール状態を制御する */
    private final FinishDetectableScroller mScroller;
    /** タップジェスチャー用のリスナー */
    private final ViewerOnGestureListener mGestureListener;
    /** タップジェスチャー用のデテクター */
    private final GestureDetector mGestureDetector;
    /** スケールジェスチャー用のリスナー */
    private final ViewerOnScaleGestureListener mScaleGestureListener;
    /** スケールジェスチャー用のデテクター */
    private final ScaleGestureDetector mScaleGestureDetector;

    /** ビューアの各種設定の取得などに使用 */
    private final EbookMode mEbookMode;
    /** ビューの描画開始位置 */
    private final PointF mPosition;
    /** 現在ページ */
    private int mCurrentPageIndex;
    /** 単ページ、または、見開きのページリスト */
    private List<PageInfo> mPageInfoList;

    /** ズーム処理中を表すフラグ */
    private boolean mZoomAnimating;
    /** ズーム処理中に描画を続けるための切り替えフラグ */
    private boolean mPendingZoomAnimating;

    /** 拡大時や横スクロール＋横フィット時にページ遷移するために必要な最低スクロール量 */
    private static float VIEW_JUMP_MIN_SCROLL_AMOUNT = 60;
    /** ページとページの隙間サイズ */
    public static final int PAGE_PADDING = 30;

    /**
     * @param viewerActivity 設定済みのViewerActivityインスタンス
     * @param pageAccess 設定済みのPageAccessインスタンス
     * @param ebookMode 設定済みのEbookModeインスタンス
     * @param currentPageIndex 初期表示するページ数
     */
    public EbookView(ViewerActivity viewerActivity, PageAccess pageAccess, EbookMode ebookMode,
            int currentPageIndex) {
        super(viewerActivity);
        mViewerActivity = viewerActivity;
        mPageAccess = pageAccess;
        mEbookMode = ebookMode;
        mCurrentPageIndex = currentPageIndex;
        mPosition = new PointF();
        mScroller = new FinishDetectableScroller(mViewerActivity, new DecelerateInterpolator(2f));
        switch (mEbookMode.getContentMode()) {
            case FXL:
                mGestureListener = new FxlViewerOnGestureListener(mViewerActivity, this);
                break;
            case OMF:
                mGestureListener = new OmfViewerOnGestureListener(mViewerActivity, this);
                break;
            default:
                mGestureListener = new FxlViewerOnGestureListener(mViewerActivity, this);
                break;
        }
        mGestureDetector = new GestureDetector(mViewerActivity, mGestureListener);
        mGestureDetector.setIsLongpressEnabled(false);
        mScaleGestureListener = new ViewerOnScaleGestureListener(this);
        mScaleGestureDetector = new ScaleGestureDetector(mViewerActivity, mScaleGestureListener);
        mZoomAnimating = false;
        mPendingZoomAnimating = false;
        setPageList();

        PageLoadCompleteListener pageLoadCompleteListener = new PageLoadCompleteListener() {
            @Override
            public void onLoadComplete(Page page) {
                Page currentPage = mRetentionPageHelper.getCurrentPage();

                // 横フィットや縦フィット時に画面より画像のサイズの方が小さい場合、デフォルト表示位置が画面の端によってしまうのでポジションをセットし直す
                // この処理は画像のサイズを取得できる状況(画像読み込み後)でないと無理なため、ここで行っている
                // 拡大処理で再読み込みが発生した場合はポジション変更したらまずい
                if (currentPage != page || page.isZoomed() == false) {
                    page.setDefaultAlign();
                    page.setDefaultPosition();
                }
                if (currentPage == page) {
                    drawPostInvalidate();
                }
            }
        };
        mPageLoadManager = new PageLoadManager(mViewerActivity, pageLoadCompleteListener);
        mRetentionPageHelper = new RetentionPageHelper(this, mPageAccess, mPageLoadManager);
    }

    public void stopAllPageLoadAndReset() {
        mRetentionPageHelper.stopAllPageLoadAndReset();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        LogUtil.v("action=%d", action);

        // 自動再生の経過秒数をリセット
        mViewerActivity.resetAutoPlayInterval();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                LogUtil.v("MotionEvent DOWN");

                // ページ内のスクロール中にタップしたらスクロール停止
                if (mScroller.scrollingTargetIsPage()) {
                    scrollerAbortAnimation();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                LogUtil.v("MotionEvent MOVE");
                break;
            case MotionEvent.ACTION_UP:
                LogUtil.v("MotionEvent UP");
            case MotionEvent.ACTION_CANCEL:
                mGestureListener.setIsPageMoving(false);

                if (mScroller.isFinishedAndFinishEventProcessed()) {
                    int startX = 0;
                    int startY = 0;
                    int destX = 0;
                    int destY = 0;
                    float diffX = 0;
                    float diffY = 0;
                    int currentPageIndex = convertedPage(mCurrentPageIndex);
                    if (mEbookMode.isVerticalScroll()) {
                        diffY = mPosition.y + getPagePositionY(currentPageIndex);
                        // 次・前ページへ遷移可能か判定
                        if (isViewJumpMinScrollAmount(diffX, diffY)) {
                            if (diffY > 0) {
                                // 上から下にスクロール
                                currentPageIndex--;
                            } else if (diffY < 0) {
                                currentPageIndex++;
                            }
                        }
                        startY = (int) mPosition.y;
                        destY = (int) -getPagePositionY(currentPageIndex);
                    } else {
                        diffX = mPosition.x + getPagePositionX(currentPageIndex);
                        // 次・前ページへ遷移可能か判定
                        if (isViewJumpMinScrollAmount(diffX, diffY)) {
                            if (diffX > 0) {
                                // 左から右にスクロール
                                currentPageIndex++;
                            } else if (diffX < 0) {
                                // 右から左にスクロール
                                currentPageIndex--;
                            }
                        }
                        startX = (int) mPosition.x;
                        destX = (int) -getPagePositionX(currentPageIndex);
                    }
                    // ページ遷移やスクロール量が少なくて元のページに戻る場合などにスクロール処理を実行
                    if ((startX != destX) || (startY != destY)) {
                        LogUtil.v("ACTION_UP startX=%d, startY=%d, destX=%d, destY=%d",
                                startX, startY, destX, destY);
                        mCurrentPageIndex = convertedPage(currentPageIndex);
                        scrollTo(false, startX, startY, destX - startX, destY - startY,
                                FinishDetectableScroller.DURATION_DRAG);
                    }
                }
                break;
            default:
                break;
        }
        if (mScroller.isFinishedAndFinishEventProcessed()
                && mGestureListener.isPageMoving() == false) {
            try {
                // スクロール中でなければスケールジェスチャー処理を実行
                mScaleGestureDetector.onTouchEvent(event);
            } catch (RuntimeException e) {
                // バージョンによるスクロール速度などの操作感統一のために特定バージョン(2.3)のScaleGestureDetectorを
                // 使用しているため？端末によっては実行時例外が発生することがある、発生した場合は無視する
                LogUtil.e("ScaleGestureDetector.onTouchEvent unexpected error", e);
            }
        }
        boolean ret = true;
        if (mScaleGestureListener.isScalling() == false && event.getPointerId(0) == 0) {
            // この条件分岐はジェスチャーの挙動がバージョンによって異なるために行っている
            // 具体的にはピンチ系操作が2.3だとうまくいくが、4.1だと正常に動作しないなど
            ret = mGestureDetector.onTouchEvent(event);
        }

        return ret;
    }

    @Override
    public void computeScroll() {
        LogUtil.v();
        if (mScroller.scrollingTargetIsPage()) {
            // ページ内スクロール(ページのポジション変更)
            if (mScroller.computeScrollOffset()) {
                mRetentionPageHelper.getCurrentPage().setPosition(
                        mScroller.getCurrX(), mScroller.getCurrY());
                MyViewCompat.postInvalidateOnAnimation(this);
            }
        } else {
            // ページ移動(ビューのポジション変更)
            if (mScroller.computeScrollOffset()) {
                viewMoveTo(mScroller.getCurrX(), mScroller.getCurrY());

                // アニメーション終了時に再描画を行ってしまうと、ダブルタップによる見開き切り替えのタイミングによっては
                // 画像読み込み中にonDrawが走り一瞬空白が表示されてしまうため最後は再描画を行わない
                if (mScroller.isFinished() == false) {
                    drawInvalidate();
                }
            }
        }
        if (mScroller.isFinishedNow()) {
            LogUtil.v("isFinished");
            if (mScroller.scrollingTargetIsPage() == false) {
                mRetentionPageHelper.updatePages();
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        LogUtil.v();
        super.onDraw(canvas);

        if (mRetentionPageHelper.initialized() == false) {
            return;
        }

        boolean isClearBitmap = true;
        // TODO
        // 自動スクロール系もOFFにした方が良い？2系の端末だと多少速度差があるけどスクロール停止タイミングでアンチエイリアスがかかる動きも微妙に気になる
        if ((mScroller.isFinishedAndFinishEventProcessed() == false || mZoomAnimating)
                && isViewHardwareAccelerated() == false) {
//        if (mZoomAnimating && isViewHardwareAccelerated() == false) {
            // GPUレンダリング無効環境では、高速化のためにアニメーション中アンチエイリアスをOFFにする
            isClearBitmap = false;
        }

        switch (mEbookMode.getContentMode()) {
            case FXL:
                canvas.drawColor(Config.BACK_COLOR_FXL);
                break;
            case OMF:
                canvas.drawColor(Config.BACK_COLOR_OMF);
                break;
        }
        float tx = mPosition.x;
        float ty = mPosition.y;
        switch (mEbookMode.getPageAnimation()) {
            case NONE:
                // ページアニメーションなしに設定されている場合、現在のビューポジション無視して常にページごとの初期位置を固定値として使う
                if (mEbookMode.isVerticalScroll()) {
                    ty = getPagePositionY(convertedPage(mCurrentPageIndex)) * -1;
                } else {
                    tx = getPagePositionX(convertedPage(mCurrentPageIndex)) * -1;
                }
                break;
            default:
                break;
        }
        mRetentionPageHelper.drawAllPage(canvas, tx, ty, isClearBitmap,
                mZoomAnimating || mScroller.isFinished() == false);

        // ズーム処理時にフラグを切り替えながら描画を続ける
        if (mPendingZoomAnimating != mZoomAnimating) {
            mZoomAnimating = mPendingZoomAnimating;
            drawInvalidate();
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        LogUtil.v("changed=%b", changed);
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            // ビューのサイズを設定し直す
            mRetentionPageHelper.displayInit(getWidth(), getHeight());
            viewDefaultUpdatePage();
        }
    }

    /**
     * UIスレッド用:Bitmapを1ページ以上読み込み済みなら再描画<br>
     * ※画像読み込み前に再描画を行ってしまうと読み込み終了まで白画面が表示されてしまう(ここで再描画されなくても読み込み終了時に再描画をする)
     */
    public void drawInvalidate() {
        boolean hasBitmap = mRetentionPageHelper.getCurrentPage().hasBitmap();
        LogUtil.v("hasBitmap=%b", hasBitmap);
        if (hasBitmap) {
            invalidate();
        }
    }

    /**
     * 非UIスレッド用：Bitmapを1ページ以上読み込み済みなら再描画<br>
     * ※画像読み込み前に再描画を行ってしまうと読み込み終了まで白画面が表示されてしまう(ここで再描画されなくても読み込み終了時に再描画をする)
     */
    public void drawPostInvalidate() {
        boolean hasBitmap = mRetentionPageHelper.getCurrentPage().hasBitmap();
        LogUtil.v("hasBitmap=%b", hasBitmap);
        if (hasBitmap) {
            postInvalidate();
        }
    }

    /**
     * 現在のページインデックスを、単ページ⇔見開きページを切り替える
     * <pre>
     * ・見開きページ→単ページの場合はポジションによって見開きの前後ページも考慮されたページ数となる
     * ・単ページ→見開きページの場合はデフォルトポジション単ページ側に寄せるためのフラグ設定を行う
     * </pre>
     * 
     * @param spreadChange true=見開き設定を切り替える、false=見開き設定は変更しない
     */
    public void spreadChange(boolean spreadChange) {
        if (mEbookMode.isSpreadView()) {
            mCurrentPageIndex = mPageAccess.getSingleIndexFromSpreadIndex(mCurrentPageIndex);
            if (isSubsequentPagePosition()) {
                // 見開き表示状態で後続ページ側にポジションが寄っている場合は単ページインデックスを+1する
                mCurrentPageIndex++;
            }
        } else {
            int singlePageIndex = mCurrentPageIndex;
            mCurrentPageIndex = mPageAccess.getSpreadIndexFromSingleIndex(mCurrentPageIndex);
            mViewerActivity.spreadSubsequentPageJudgment(singlePageIndex, mCurrentPageIndex);
        }
        if (spreadChange) {
            mEbookMode.setIsSpread(!mEbookMode.isSpread());
        }
    }

    /**
     * 現在の設定に基づいてページリストの更新、ビューのポジションをカレントページに対するデフォルト値に更新してページのアップデート
     */
    public void pageListInit() {
        setPageList();
        viewDefaultUpdatePage();
    }

    /**
     * ビューのポジションをカレントページに対するデフォルト値に更新してページのアップデート
     */
    private void viewDefaultUpdatePage() {
        if (mRetentionPageHelper.initialized()) {
            setViewDefaultPosition();
            mRetentionPageHelper.updatePages();
        }
    }

    /**
     * 現在表示中のページが先頭ページか判定する
     * 
     * @return 先頭ページならture、それ以外ならfalse
     */
    public boolean isFirstPage() {
        return mCurrentPageIndex == 0;
    }

    /**
     * 現在表示中のページが最終ページか判定する
     * 
     * @return 最終ページならture、それ以外ならfalse
     */
    public boolean isLastPage() {
        return mCurrentPageIndex + 1 == getPageCount();
    }

    /**
     * 進行方向へページ内スクロールをする余地があるか判定する
     * 
     * @return 進行方向へページ内スクロールをする余地があったらtrue、余地がなかったらfalse
     */
    public boolean isScrollableForward() {
        boolean isScrollable = true;
        Page page = mRetentionPageHelper.getCurrentPage();
        int startX = (int) page.getPosition().x;
        int startY = (int) page.getPosition().y;
        int canScaleMinX = (int) page.getCanScaleMinX();
        int canScaleMinY = (int) page.getCanScaleMinY();

        if (mEbookMode.getMode() == Mode.LANDSCAPE_STANDARD) {
            if (startY <= canScaleMinY) {
                isScrollable = false;
            }
        } else {
            if (mPageAccess.isRtl()) {
                if (startX >= 0) {
                    isScrollable = false;
                }
            } else {
                if (startX <= canScaleMinX) {
                    isScrollable = false;
                }
            }
        }
        return isScrollable;
    }

    /**
     * 進行方向とは逆へページ内スクロールをする余地があるか判定する
     * 
     * @return 進行方向とは逆へページ内スクロールをする余地があったらtrue、余地がなかったらfalse
     */
    public boolean isScrollableBackward() {
        boolean isScrollable = true;
        Page page = mRetentionPageHelper.getCurrentPage();
        int startX = (int) page.getPosition().x;
        int startY = (int) page.getPosition().y;
        int canScaleMinX = (int) page.getCanScaleMinX();

        if (mEbookMode.getMode() == Mode.LANDSCAPE_STANDARD) {
            if (startY >= 0) {
                isScrollable = false;
            }
        } else {
            if (mPageAccess.isRtl()) {
                if (startX <= canScaleMinX) {
                    isScrollable = false;
                }
            } else {
                if (startX >= 0) {
                    isScrollable = false;
                }
            }
        }
        return isScrollable;
    }

    /**
     * 進行方向へページ内スクロールをする余地があったら画面サイズ分ページ内スクロール、ページの端なら次のページへ移動
     */
    public void pageScrollOrNextPage() {
        Page page = mRetentionPageHelper.getCurrentPage();
        int startX = (int) page.getPosition().x;
        int startY = (int) page.getPosition().y;
        int destX = 0; // X方向への移動量
        int destY = 0; // Y方向への移動量
        int canScaleMinX = (int) page.getCanScaleMinX();
        int canScaleMinY = (int) page.getCanScaleMinY();
        LogUtil.v("currentPage=%d, startX=%d, startY=%d, canScaleMinX=%d, canScaleMinY=%d",
                mCurrentPageIndex, startX, startY, canScaleMinX, canScaleMinY);

        if (mEbookMode.getMode() == Mode.LANDSCAPE_STANDARD) {
            destY = getVerticalScrollDistance(startY, canScaleMinY, true);
        } else {
            destX = getHorizontalScrollDistance(startX, canScaleMinX, true);
        }
        if (destX == 0 && destY == 0) {
            nextPageScroll();
        } else {
            // ページ内移動(スクロール)
            scrollTo(true, startX, startY, destX, destY, FinishDetectableScroller.DURATION_TAP);
        }
    }

    /**
     * 進行方向とは逆へページ内スクロールをする余地があったら画面サイズ分ページ内スクロール、ページの端なら前のページへ移動
     */
    public void pageScrollOrPrevPage() {
        Page page = mRetentionPageHelper.getCurrentPage();
        int startX = (int) page.getPosition().x;
        int startY = (int) page.getPosition().y;
        int destX = 0; // X方向への移動量
        int destY = 0; // Y方向への移動量
        int canScaleMinX = (int) page.getCanScaleMinX();
        int canScaleMinY = (int) page.getCanScaleMinY();
        LogUtil.v("currentPage=%d, startX=%d, startY=%d, canScaleMinX=%d, canScaleMinY=%d",
                mCurrentPageIndex, startX, startY, canScaleMinX, canScaleMinY);

        if (mEbookMode.getMode() == Mode.LANDSCAPE_STANDARD) {
            destY = getVerticalScrollDistance(startY, canScaleMinY, false);
        } else {
            destX = getHorizontalScrollDistance(startX, canScaleMinX, false);
        }
        if (destX == 0 && destY == 0) {
            prevPageScroll();
        } else {
            // ページ内移動スクロール)
            scrollTo(true, startX, startY, destX, destY, FinishDetectableScroller.DURATION_TAP);
        }
    }

    /**
     * 水平方向へスクロール可能な距離を取得する
     * 
     * @param startX ページ内の現在ポジションX
     * @param minX ページ内ポジションXの最少値
     * @param isForward 進行方向へスクロール可能な距離を取得したい場合はtrueを指定する、逆方向の場合はfalseを指定する
     * @return isForwardで指定した方向へスクロール可能な距離
     */
    private int getHorizontalScrollDistance(int startX, int minX, boolean isForward) {
        int destX;
        if (mPageAccess.isRtl() == isForward) {
            if (startX >= 0) {
                destX = 0;
            } else {
                int viewWidth = getWidth();
                destX = -startX;
                if (destX > viewWidth) {
                    destX = viewWidth;
                }
            }
        } else {
            if (startX <= minX) {
                destX = 0;
            } else {
                int viewWidth = getWidth();
                destX = minX - startX;
                if (destX < -viewWidth) {
                    destX = -viewWidth;
                }
            }
        }
        return destX;
    }

    /**
     * 垂直方向へスクロール可能な距離を取得する
     * 
     * @param startY ページ内の現在ポジションY
     * @param minY ページ内ポジションYの最少値
     * @param isForward 進行方向へスクロール可能な距離を取得したい場合はtrueを指定する、逆方向の場合はfalseを指定する
     * @return isForwardで指定した方向へスクロール可能な距離
     */
    private int getVerticalScrollDistance(int startY, int minY, boolean isForward) {
        int destY;
        if (isForward) {
            if (startY <= minY) {
                destY = 0;
            } else {
                int viewHeight = getHeight();
                destY = minY - startY;
                if (destY < -viewHeight) {
                    destY = -viewHeight;
                }
            }
        } else {
            if (startY >= 0) {
                destY = 0;
            } else {
                destY = -startY;
                if (destY > getHeight()) {
                    destY = getHeight();
                }
            }
        }
        return destY;
    }

    /**
     * 次のページヘビューをスクロールする
     */
    public void nextPageScroll() {
        if (!isLastPage()) {
            mCurrentPageIndex++;
            int viewStartX = 0;
            int viewStartY = 0;
            int viewDestX = 0;
            int viewDestY = 0;
            if (mEbookMode.isVerticalScroll()) {
                viewStartY = (int) mPosition.y;
                viewDestY = (int) -getPagePositionY(mCurrentPageIndex);
            } else {
                viewStartX = (int) mPosition.x;
                viewDestX = (int) -getPagePositionX(convertedPage(mCurrentPageIndex));
            }
            scrollTo(false, viewStartX, viewStartY, viewDestX - viewStartX, viewDestY - viewStartY,
                    FinishDetectableScroller.DURATION_TAP);
        }
    }

    /**
     * 前のページヘビューをスクロールする
     */
    public void prevPageScroll() {
        if (!isFirstPage()) {
            mCurrentPageIndex--;
            int viewStartX = 0;
            int viewStartY = 0;
            int viewDestX = 0;
            int viewDestY = 0;
            if (mEbookMode.isVerticalScroll()) {
                viewStartY = (int) mPosition.y;
                viewDestY = (int) -getPagePositionY(mCurrentPageIndex);
            } else {
                viewStartX = (int) mPosition.x;
                viewDestX = (int) -getPagePositionX(convertedPage(mCurrentPageIndex));
            }
            scrollTo(false, viewStartX, viewStartY, viewDestX - viewStartX, viewDestY - viewStartY,
                    FinishDetectableScroller.DURATION_TAP);
        }
    }

    /**
     * ビュー(ページ遷移)、または、ページ内スクロールをする。
     * 
     * @param isPage trueの場合ページ内スクロール、falseの場合ビューのスクロール(ページ遷移)
     * @param startX スクロール開始座標X
     * @param startY スクロール開始座標Y
     * @param destX X方向へのスクロール量
     * @param destY Y方向へのスクロール量
     * @param duration スクロールにかかる時間
     */
    public void scrollTo(boolean isPage, int startX, int startY, int destX, int destY,
            int duration) {
        // ビュー(ページ遷移)スクロール時にページめくりアニメーションがNONEの場合、
        // ページ遷移速度とアンチエイリアスの無効解除(Android2系の場合)のためにdurationを0にする
        if (isPage == false && mEbookMode.getPageAnimation() == PageAnimation.NONE) {
            duration = 0;
        }
        // スクロールアニメーションありでビューのスクロール開始(ここでいうビューのスクロールはページ内の移動)
        mScroller.setScrollingTargetIsPage(isPage);
        mScroller.startScroll(startX, startY, destX, destY, duration);
        drawInvalidate();
    }

    public void jumpToPage(int pageIndex) {
        mCurrentPageIndex = pageIndex;
        // onLayout前の場合は以下のメソッドでは何もしない(あとでonLayout動いた時に動くのでカレントページだけ更新しとけば良い)
        viewDefaultUpdatePage();
    }

    /**
     * ページ移動が横スクロール時はビューのX座標、縦スクロール時はビューのY座標を更新<br>
     * (横スクロール時のY座標と縦スクロール時のX座標は使用されない)<br>
     * 指定した座標が範囲外だった場合、範囲内に収まるように補正する
     * 
     * @param posX スクロール先のX座標
     * @param posY スクロール先のY座標
     */
    public void viewMoveTo(float posX, float posY) {
        if (mEbookMode.isVerticalScroll()) {
            mPosition.y = getScrollTo(posY);
        } else {
            mPosition.x = getScrollTo(posX);
        }
        // TODO スクロール操作で最初・最終ページより先に進もうとした場合に何か表示させたい場合、ここで判定？
        LogUtil.v("mPosition.x=%f, mPosition.y=%f", mPosition.x, mPosition.y);
    }

    /**
     * 指定した座標が範囲外だった場合、範囲内に収まるように補正した値を返す<br>
     * 範囲内の場合は値は変化しない
     * 
     * @param base ページ移動が横スクロール時は座標X、縦スクロール時は座標Y
     * @return
     */
    private float getScrollTo(float base) {
        float move = base;
        float diff = 0;
        if (mEbookMode.isVerticalScroll()) {
            diff = (getViewHeight() - getHeight()) * -1;
        } else {
            diff = (getViewWidth() - getWidth()) * -1;
        }
        if (base > 0) {
            move = 0;
        }
        if (base < diff) {
            move = diff;
        }
        LogUtil.v("base=%f, diff=%f", base, diff);
        return move;
    }

    /**
     * 設定に対応したページの表示X座標を返す、返される表示座標はキャンバスに対する位置
     * 
     * @param pageIndex 表示座標を求めるページ数
     * @return ページ数に対応したX座標
     */
    public float getPagePositionX(int pageIndex) {
        // ページの表示X座標を返す 返される表示座標はキャンバスに対する位置
        float x;
        x = getViewWidth();
        x -= (getWidth() + PAGE_PADDING) * pageIndex;
        x -= getWidth();
        return x;
    }

    /**
     * 設定に対応したページの表示Y座標を返す、返される表示座標はキャンバスに対する位置
     * 
     * @param pageIndex 表示座標を求めるページ数
     * @return ページ数に対応したY座標
     */
    public float getPagePositionY(int pageIndex) {
        return (getHeight() + PAGE_PADDING) * pageIndex;
    }

    /**
     * 設定に対応したビューの横幅を返す
     * 
     * @return ・ページ移動が縦スクロール：ビューの横幅<br>
     *         ・ページ移動が横スクロール：(ビューの横幅*ページ数)＋(パディング*(ページ数-1))
     */
    private float getViewWidth() {
        if (mEbookMode.isVerticalScroll()) {
            return getWidth();
        } else {
            return getWidth() * getPageCount() + PAGE_PADDING * (getPageCount() - 1);
        }
    }

    /**
     * 設定に対応したビューの縦幅を返す>
     * 
     * @return ・ページ移動が縦スクロール：(ビューの縦幅*ページ数)＋(パディング*(ページ数-1))<br>
     *         ・ページ移動が横スクロール：ビューの縦幅
     */
    private float getViewHeight() {
        if (mEbookMode.isVerticalScroll()) {
            return getHeight() * getPageCount() + PAGE_PADDING * (getPageCount() - 1);
        } else {
            return getHeight();
        }
    }

    /**
     * 使用するページリストを現在の設定にあったものに変更する
     */
    public void setPageList() {
        if (mEbookMode.isSpreadView()) {
            mPageInfoList = mPageAccess.getSpreadPageInfoList();
        } else {
            mPageInfoList = mPageAccess.getSinglePageInfoList();
        }
    }

    /**
     * カレントページインデックスを使用してビューのポジションを初期化する
     */
    private void setViewDefaultPosition() {
        if (mEbookMode.isVerticalScroll()) {
            mPosition.set(0, -getPagePositionY(mCurrentPageIndex));
        } else {
            mPosition.set(-getPagePositionX(convertedPage(mCurrentPageIndex)), 0);
        }
    }

    public PointF getPosition() {
        return mPosition;
    }

    public EbookMode getEbookMode() {
        return mEbookMode;
    }

    /**
     * 現在保持しているページリスト(単ページ、または、見開きページ)のサイズを取得する
     * 
     * @return 現在保持しているページリスト(単ページ、または、見開きページ)のサイズ
     */
    public int getPageCount() {
        return mPageInfoList.size();
    }

    public PageInfo getCurrentPageInfo() {
        return mPageInfoList.get(convertedPage(mCurrentPageIndex));
    }

    public PageInfo getRightPageInfo() {
        return mPageInfoList.get(convertedPage(mCurrentPageIndex) - 1);
    }

    public PageInfo getLeftPageInfo() {
        return mPageInfoList.get(convertedPage(mCurrentPageIndex) + 1);
    }

    public int getCurrentPageIndex() {
        return mCurrentPageIndex;
    }

    /**
     * 現在のページインデックスを常に単ページインデックスとして返す(ポジションによって見開きの前後ページも考慮されたページ数となる)
     * 
     * @return 現在表示中の単ページインデックス
     */
    public int getCurrentSinglePageIndex() {
        int currentPageIndex = mCurrentPageIndex;
        if (mEbookMode.isSpreadView()) {
            currentPageIndex = mPageAccess.getSingleIndexFromSpreadIndex(currentPageIndex);
            if (isSubsequentPagePosition()) {
                // 見開き表示状態で後続ページ側にポジションが寄っている場合は単ページインデックスを+1する
                currentPageIndex++;
            }
        }
        return currentPageIndex;
    }

    /**
     * 渡したページインデックスを、横スクロール＋LTRの時に逆順にする
     * 
     * @param pageIndex ページインデックス
     * @return 横スクロール＋LTRの場合：総ページ数 - pageIndex - 1、それ以外の場合：pageIndex
     */
    public int convertedPage(int pageIndex) {
        if (mViewerActivity.isReversePage()) {
            return mPageInfoList.size() - pageIndex - 1;
        } else {
            return pageIndex;
        }
    }

    public boolean isReversePage() {
        return mViewerActivity.isReversePage();
    }

    public RetentionPageHelper getRetentionPageHelper() {
        return mRetentionPageHelper;
    }

    public PageAccess getPageAccess() {
        return mPageAccess;
    }

    public FinishDetectableScroller getScroller() {
        return mScroller;
    }

    /**
     * PageInfoオブジェクトを元に、ページ数を取得する
     * 
     * @param pageInfo ページ数を確かめたいPageInfoオブジェクト
     * @return ページ数
     */
    public int getPageIndex(PageInfo pageInfo) {
        return mPageInfoList.indexOf(pageInfo);
    }

    /**
     * 次のページへ遷移するのに必要なスクロール量を満たしているか判定
     * 
     * @param diffX X方向へのスクロール量
     * @param diffY Y方向へのスクロール量
     * @return 横スクロール＋横フィット、または、ズーム中にスクロール量が一定以下の場合false、それ以外ならtrue
     */
    private boolean isViewJumpMinScrollAmount(float diffX, float diffY) {
        return !(((!mEbookMode.isVerticalScroll() && mEbookMode.getFitMode() == FitMode.WIDTH_FIT)
                || mRetentionPageHelper.getCurrentPage().isZoomed())
                && diffX < VIEW_JUMP_MIN_SCROLL_AMOUNT && diffX > -VIEW_JUMP_MIN_SCROLL_AMOUNT
                && diffY < VIEW_JUMP_MIN_SCROLL_AMOUNT && diffY > -VIEW_JUMP_MIN_SCROLL_AMOUNT);
    }

    /**
     * 見開きページの表示位置がスクロールなどにより後続ページよりになっているか判定
     * 
     * @return 画面の中央が後続ページ側に寄っている場合はtrue、それ以外ならfalse
     */
    public boolean isSubsequentPagePosition() {
        if (mRetentionPageHelper.initialized() == false) {
            return false;
        }
        boolean ret = false;
        Page currentPage = mRetentionPageHelper.getCurrentPage();

        synchronized (currentPage) {
            // 左右ページ読み込み済みか判定、読み込み前ならポジション変わってるはずないので常にfalse
            if (currentPage.getLeftBitmap() != null && currentPage.getRightBitmap() != null) {
                float spreadCenterPosition; // 見開きページの中央位置

                switch (mEbookMode.getMode()) {
                    case PORTRAIT_SPREAD:
                        // 縦見開きの場合は全体ページサイズの中央が見開きの中央とは限らないので、
                        // 左画像の横幅を取得することにより見開きの中央位置を取得
                        spreadCenterPosition =
                                currentPage.getLeftBitmapFitScaleWidth() * currentPage.getScale();
                        break;
                    case LANDSCAPE_SPREAD:
                        // 横見開きの場合は全体ページサイズを2で割って中央位置を取得
                        spreadCenterPosition =
                                (currentPage.getBitmapWidth() * currentPage.getScale()) / 2;
                        break;
                    default:
                        return false;
                }
                if (mPageAccess.isRtl()) {
                    if (currentPage.getPosition().x + spreadCenterPosition > getWidth() / 2) {
                        ret = true;
                    } else {
                        ret = false;
                    }
                } else {
                    if (currentPage.getPosition().x + spreadCenterPosition < getWidth() / 2) {
                        ret = true;
                    } else {
                        ret = false;
                    }
                }
            }
        }
        return ret;
    }

    public void scrollerAbortAnimation() {
        if (!mScroller.isFinished()) {
            mScroller.abortAnimation();
            mScroller.setScrollingTargetIsPage(false);
        }
    }

    public void setZoomAnimating(boolean anim) {
        mPendingZoomAnimating = anim;
    }

    /**
     * GPUレンダリングの有無を調査、本来はisHardwareAccelerated()を実行すべきだが、高速化のために簡易実装
     * 
     * @return ハニカム以降ならtrue、それ以前ならfalse
     */
    private boolean isViewHardwareAccelerated() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    }
}
