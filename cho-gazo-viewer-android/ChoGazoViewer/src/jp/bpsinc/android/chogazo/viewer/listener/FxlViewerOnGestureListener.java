
package jp.bpsinc.android.chogazo.viewer.listener;

import android.graphics.PointF;
import android.view.MotionEvent;
import jp.bpsinc.android.chogazo.viewer.support.MyViewCompat;
import jp.bpsinc.android.chogazo.viewer.util.LogUtil;
import jp.bpsinc.android.chogazo.viewer.activity.ViewerActivity;
import jp.bpsinc.android.chogazo.viewer.content.Page;
import jp.bpsinc.android.chogazo.viewer.dialog.ViewerErrorDialog;
import jp.bpsinc.android.chogazo.viewer.view.EbookView;
import jp.bpsinc.android.chogazo.viewer.view.EbookMode.FitMode;

public class FxlViewerOnGestureListener extends ViewerOnGestureListener {
    /** ダブルタップによる拡大縮小処理スレッド */
    private MoveThread mMoveThread;

    /** ダブルタップによる拡大率 */
    private static final float ZOOM_SCALE_BY_DOUBLETAP = 2.5f;

    public FxlViewerOnGestureListener(ViewerActivity viewerActivity, EbookView ebookView) {
        super(viewerActivity, ebookView);
        mMoveThread = null;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        super.onDown(e);
        if (shouldTightenPaging()) {
            // 横フィット
            mPageScrollable = false;
        }
        return true;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        if (mMoveThread != null) {
            return true;
        }
        return super.onSingleTapConfirmed(e);
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if (mMoveThread != null) {
            return true;
        }
        return super.onScroll(e1, e2, distanceX, distanceY);
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if (mMoveThread != null) {
            return true;
        }
        return super.onFling(e1, e2, velocityX, velocityY);
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        LogUtil.v("MotionEvent");
        if (isTouchEventDisable()) {
            return true;
        }
        try {
            if (mMoveThread == null) {
                Page currentPage =
                        mEbookView.getRetentionPageHelper().getCurrentPage();
                if (currentPage.hasBitmap() == false) {
                    LogUtil.d("bitmap is null");
                    return true;
                }

                if (currentPage.isZoomed()) {
                    LogUtil.v("zoom out");
                    zoomWithPoint(currentPage,
                            e.getRawX(), e.getRawY(), Page.PAGE_DEFAULT_SCALE);
                } else {
                    LogUtil.v("zoom in");
                    float scale = ZOOM_SCALE_BY_DOUBLETAP;
                    float minimumScale = currentPage.getMinimumScale();

                    // ディスプレイフィット時の拡大率が最大拡大率より大きい場合は拡大処理を出来なくする
                    if (minimumScale >= ViewerOnScaleGestureListener.MAXIMUM_SCALE) {
                        return true;
                    }
                    // 最大拡大率を超えていたら値を調節
                    if (minimumScale * scale > ViewerOnScaleGestureListener.MAXIMUM_SCALE) {
                        scale = ViewerOnScaleGestureListener.MAXIMUM_SCALE / minimumScale;
                    }
                    zoomWithPoint(currentPage, e.getRawX(), e.getRawY(), scale);
                }
            }
        } catch (RuntimeException ex) {
            LogUtil.e("unexpected error", ex);
            ViewerErrorDialog.show(mViewerActivity.getSupportFragmentManager(),
                    ViewerErrorDialog.ID_UNEXPECTED_ERR);
        }
        return true;
    }

    private void zoomWithPoint(Page page, float rawX, float rawY, float toScale) {
        float befScale = page.getScale();
        float toX = (page.getPosition().x - rawX) * (toScale / befScale) + rawX;
        float toY = (page.getPosition().y - rawY) * (toScale / befScale) + rawY;

        page.setScale(toScale);
        toX = page.getScaleXposition(toX);
        toY = page.getScaleYposition(toY);
        page.setScale(befScale);

        mMoveThread = new MoveThread(page, toX, toY, toScale);
        MyViewCompat.postOnAnimation(mEbookView, mMoveThread);
    }

    /**
     * ダブルタップによる拡大縮小アニメーション用スレッド
     */
    private class MoveThread implements Runnable {
        Page mPage;
        PointF mToPoint;
        float mToScale;
        int mSpeed;
        float mRemain;
        float mProgressX;
        float mProgressY;
        float mProgressScale;
        boolean mIsHalt;

        private static final float ANIMATION_FPS = 10;
        private static final float ANIMATION_MILLIS = 300;

        public MoveThread(Page page, float toX, float toY, float toScale) {
            LogUtil.d("start toX=%f, toY=%f, toScale:%f", toX, toY, toScale);
            mIsHalt = false;
            mPage = page;
            mToPoint = new PointF(toX, toY);
            mToScale = toScale;
            mSpeed = (int) (ANIMATION_MILLIS / ANIMATION_FPS);
            mRemain = ANIMATION_MILLIS;
            mProgressX = (mToPoint.x - page.getPosition().x) / ANIMATION_FPS;
            mProgressY = (mToPoint.y - page.getPosition().y) / ANIMATION_FPS;
            mProgressScale = (toScale - page.getScale()) / ANIMATION_FPS;
        }

        public void halt() {
            mIsHalt = true;
            mIsPageMoving = false;
        }

        @Override
        public void run() {
            try {
                mEbookView.setZoomAnimating(mIsHalt == false);
                if (mIsHalt) {
                    return;
                }

                mIsPageMoving = true;
                mPage.setPosition(mPage.getPosition().x + mProgressX,
                        mPage.getPosition().y + mProgressY);

                mPage.setScale(mPage.getScale() + mProgressScale);
                mRemain -= mSpeed;
                // 最終位置を越えたら停止
                if (mRemain <= 0.0f) {
                    LogUtil.d("finish toScale=%f", mToScale);
                    mPage.setPosition(mToPoint.x, mToPoint.y);
                    mPage.setScale(mToScale);
                    mEbookView.getRetentionPageHelper().replaceToHighQuality();
                    halt();
                    mMoveThread = null;
                }
                mEbookView.setZoomAnimating(mIsHalt == false);
                mEbookView.drawPostInvalidate();
                if (mIsHalt == false) {
                    // 最終位置を越えるまでアニメーションを続ける
                    MyViewCompat.postOnAnimation(mEbookView, this);
                }
            } catch (RuntimeException e) {
                LogUtil.e("unexpected error", e);
                mViewerActivity.postShowDialog(ViewerErrorDialog.ID_UNEXPECTED_ERR);
            }
        }
    }

    /**
     * 横拡大モード時、上下スクロールしやすいように少し判定に余裕を持たせる。この適用有無を判定
     * 
     * @return 横フィット、かつ、非ズームの場合はtrue、それ以外ならfalse
     */
    private boolean shouldTightenPaging() {
        return mEbookView.getEbookMode().getFitMode() == FitMode.WIDTH_FIT
                && !mEbookView.getRetentionPageHelper().getCurrentPage().isZoomed();
    }
}
