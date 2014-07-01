
package jp.bpsinc.android.chogazo.viewer.listener;

import jp.bpsinc.android.chogazo.viewer.util.LogUtil;
import jp.bpsinc.android.chogazo.viewer.content.Page;
import jp.bpsinc.android.chogazo.viewer.view.EbookView;
import android.graphics.PointF;

public class ViewerOnScaleGestureListener extends
        ScaleGestureDetector.SimpleOnScaleGestureListener {
    protected EbookView mEbookView;

    private float mBeginScale;
    private float mBeginFocusX;
    private float mBeginFocusY;
    private PointF mBeginPoint;
    private boolean mIsScalling;

    /** 画像の原寸サイズに対する最大拡大率(拡大処理でこのサイズを超えることはないが、フィットデコード時に超える可能性はある) */
    public static final float MAXIMUM_SCALE = 5.0f;

    public ViewerOnScaleGestureListener(EbookView ebookView) {
        mEbookView = ebookView;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        Page currentPage = mEbookView.getRetentionPageHelper().getCurrentPage();
        if (currentPage.hasBitmap() == false) {
            return false;
        }
        mIsScalling = true;
        mEbookView.setZoomAnimating(true);
        mBeginScale = currentPage.getScale();
        mBeginFocusX = detector.getFocusX();
        mBeginFocusY = detector.getFocusY();
        mBeginPoint = new PointF(currentPage.getPosition().x, currentPage.getPosition().y);
        LogUtil.v("MotionEvent beginScale=%f, beginFocusX=%f, beginFocusY=%f, beginPoint.x=%f, beginPoint.y=%f",
                mBeginScale, mBeginFocusX, mBeginFocusY, mBeginPoint.x, mBeginPoint.y);
        return true;
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        LogUtil.v("MotionEvent");
        Page currentPage = mEbookView.getRetentionPageHelper().getCurrentPage();
        float minimumScale = currentPage.getMinimumScale();

        // ディスプレイフィット時の拡大率が最大拡大率より大きい場合は拡大処理を出来なくする
        if (minimumScale >= MAXIMUM_SCALE) {
            return true;
        }
        float scale = currentPage.getScale() * detector.getScaleFactor();

        // 変更後のスケールが範囲外の場合は境界値とする、既に境界値の場合は何もせず処理を終了する
        if (scale < Page.PAGE_DEFAULT_SCALE) {
            if (currentPage.getScale() == Page.PAGE_DEFAULT_SCALE) {
                return true;
            }
            scale = Page.PAGE_DEFAULT_SCALE;
        } else if (minimumScale * scale > MAXIMUM_SCALE) {
            if (minimumScale * currentPage.getScale() == MAXIMUM_SCALE) {
                return true;
            }
            scale = MAXIMUM_SCALE / minimumScale;
        }
        currentPage.setScale(scale);
        float scaleDif = scale / mBeginScale;

        float x = (mBeginPoint.x - mBeginFocusX) * scaleDif + detector.getFocusX();
        float y = (mBeginPoint.y - mBeginFocusY) * scaleDif + detector.getFocusY();
        currentPage.setPosition(currentPage.getScaleXposition(x), currentPage.getScaleYposition(y));
        mEbookView.drawInvalidate();
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
        LogUtil.v("MotionEvent");
        mIsScalling = false;
        mEbookView.getRetentionPageHelper().replaceToHighQuality();
        mEbookView.setZoomAnimating(false);
        mEbookView.drawInvalidate();
    }

    public boolean isScalling() {
        return mIsScalling;
    }
}
