
package jp.bpsinc.android.chogazo.viewer.listener;

import jp.bpsinc.android.chogazo.viewer.util.LogUtil;
import jp.bpsinc.android.chogazo.viewer.activity.ViewerActivity;
import jp.bpsinc.android.chogazo.viewer.content.Page;
import jp.bpsinc.android.chogazo.viewer.view.EbookView;
import android.view.GestureDetector;
import android.view.MotionEvent;

public abstract class ViewerOnGestureListener extends GestureDetector.SimpleOnGestureListener {
    protected ViewerActivity mViewerActivity;
    protected EbookView mEbookView;
    protected boolean mIsPageMoving;
    protected boolean mPageScrollable;

    /** 横フィットでズームしていない時、ページ遷移するために必要な最低スクロール量(縦スクロールしたい時に誤ってページ遷移してしまわないようにする) */
    private static final int PAGE_JUMP_MIN_SCROLL_AMOUNT = 16;

    public ViewerOnGestureListener(ViewerActivity viewerActivity, EbookView ebookView) {
        mViewerActivity = viewerActivity;
        mEbookView = ebookView;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        LogUtil.v("MotionEvent");
        mIsPageMoving = false;
        mPageScrollable = true;
        return true;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        float tapX = e.getX();
        float tapY = e.getY();
        LogUtil.v("MotionEvent tapX=%f, tapY=%f", tapX, tapY);

        if (isTouchEventDisable()) {
            return true;
        }

        int viewWidth = mEbookView.getWidth();
        if (viewWidth / 3 > tapX) {
            // 画面左をタップ
            onLeftAreaTapped();
        } else if (viewWidth / 3 * 2 < tapX) {
            // 画面右をタップ
            onRightAreaTapped();
        } else {
            // 画面中央をタップ
            onCenterAreaTapped();
        }
        return super.onSingleTapConfirmed(e);
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        LogUtil.v("MotionEvent distanceX=%f, distanceY=%f", distanceX, distanceY);

        if (isTouchEventDisable()) {
            return true;
        }

        mIsPageMoving = true;
        boolean isPageJump = false;

        Page currentPage = mEbookView.getRetentionPageHelper().getCurrentPage();
        if (mPageScrollable == false) {
            float xDiff = Math.abs(e1.getX() - e2.getX());
            float yDiff = Math.abs(e1.getY() - e2.getY());

            if (xDiff > PAGE_JUMP_MIN_SCROLL_AMOUNT && xDiff > yDiff) {
                mPageScrollable = true;
            } else if (yDiff > PAGE_JUMP_MIN_SCROLL_AMOUNT && yDiff > xDiff) {
                // 縦スクロール量の方が多い場合はページ遷移しない
                mPageScrollable = false;
            }
        }
        if (mPageScrollable) {
            int currentPageIndex = mEbookView.getCurrentPageIndex();

            // ページ移動が縦スクロールか横スクロールか判定
            if (mEbookView.getEbookMode().isVerticalScroll()) {
                int viewHeight = mEbookView.getHeight();
                float viewBottomPosition = mEbookView.getPagePositionY(currentPageIndex)
                        + mEbookView.getPosition().y;
                float viewTopPosition = viewBottomPosition + viewHeight;

                LogUtil.d("pageY=%f, distanceY=%f", currentPage.getPosition().y, distanceY);
                if (currentPage.canScrollToY(currentPage.getPosition().y, distanceY) == false) {
                    // ページ内移動がY方向にできない
                    isPageJump = true;
                } else if (distanceY > 0) {
                    // ページ内移動が可能な状態で下から上にスクロールした時
                    if (viewTopPosition > viewHeight) {
                        isPageJump = true;
                        if (viewTopPosition - viewHeight < distanceY) {
                            distanceY = viewTopPosition - viewHeight;
                        }
                    }
                } else {
                    // ページ内移動が可能な状態で上から下にスクロールした時
                    if (viewBottomPosition < 0) {
                        isPageJump = true;
                        if (viewBottomPosition - distanceY > 0) {
                            distanceY = viewBottomPosition;
                        }
                    }
                }
            } else {
                int viewWidth = mEbookView.getWidth();
                float viewLeftPosition = mEbookView.
                        getPagePositionX(mEbookView.convertedPage(currentPageIndex))
                        + mEbookView.getPosition().x;
                float viewRightPosition = viewLeftPosition + viewWidth;

                LogUtil.d("pageX=%f, distanceX=%f", currentPage.getPosition().x, distanceX);
                if (currentPage.canScrollToX(currentPage.getPosition().x, distanceX) == false) {
                    // ページ内移動がX方向にできない
                    isPageJump = true;
                } else if (distanceX > 0) {
                    // ページ内移動が可能な状態で右から左にスクロールした時
                    if (viewRightPosition > viewWidth) {// 画面右端
                        isPageJump = true;
                        if (viewRightPosition - viewWidth < distanceX) {
                            distanceX = viewRightPosition - viewWidth;
                        }
                    }
                } else {
                    // ページ内移動が可能な状態で左から右にスクロールした時
                    if (viewLeftPosition < 0) {// 画面左端
                        isPageJump = true;
                        if (viewLeftPosition - distanceX > 0) {
                            distanceX = viewLeftPosition;
                        }
                    }
                }
            }
        }
        // ページ内移動がXまたはY方向にできなければページを移動する
        if (isPageJump) {
            float toX = mEbookView.getPosition().x;
            float toY = mEbookView.getPosition().y;
            if (mEbookView.getEbookMode().isVerticalScroll()) {
                toY -= distanceY;
                distanceY = 0;
            } else {
                toX -= distanceX;
                distanceX = 0;
            }
            mEbookView.viewMoveTo(toX, toY);
        }
        currentPage.pageScrollTo(distanceX, distanceY);
        mEbookView.drawInvalidate();

        return true;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        LogUtil.v("MotionEvent velocityX=%f, velocityY=%f", velocityX, velocityY);

        Page currentPage = mEbookView.getRetentionPageHelper().getCurrentPage();

        // スクロール中(ページ移動アニメーションも含む)か判定、カレントページがビットマップ持ってない場合もminXやmaxXなどの値がおかしくなるので判定
        if (isTouchEventDisable() || currentPage.hasBitmap() == false) {
            return true;
        }
        mIsPageMoving = true;

        // ページフィットの箇所までFling
        int vx = (int) (velocityX);
        int vy = (int) (velocityY);

        int minX = (int) currentPage.getCanScaleMinX();
        int maxX = (int) currentPage.getCanScaleMaxX();

        int minY = (int) currentPage.getCanScaleMinY();
        int maxY = (int) currentPage.getCanScaleMaxY();

        int startX = (int) currentPage.getPosition().x;
        int startY = (int) currentPage.getPosition().y;

        LogUtil.v("posX=%d, posY=%d, minX=%d, minY=%d, maxX=%d, maxY=%d, vx=%d, vy=%d",
                startX, startY, minX, minY, maxX, maxY, vx, vy);
        mEbookView.getScroller().setScrollingTargetIsPage(true);
        mEbookView.getScroller().fling(startX, startY, vx, vy, minX, maxX, minY, maxY);
        mEbookView.drawInvalidate();

        return true;
    }

    protected boolean isTouchEventDisable() {
        return mEbookView.getScroller().isFinishedAndFinishEventProcessed() == false;
    }

    public boolean isPageMoving() {
        return mIsPageMoving;
    }

    public void setIsPageMoving(boolean isPageMoving) {
        mIsPageMoving = isPageMoving;
    }

    private void onCenterAreaTapped() {
        mViewerActivity.openMenu();
    }

    private void onLeftAreaTapped() {
        if (mEbookView.getRetentionPageHelper().getCurrentPage().isZoomed()) {
            mViewerActivity.openMenu();
        } else {
            switch (mEbookView.getEbookMode().getTapAction()) {
                case NORMAL:
                    if (mEbookView.getPageAccess().isRtl()) {
                        mEbookView.pageScrollOrNextPage();
                    } else {
                        mEbookView.pageScrollOrPrevPage();
                    }
                    break;
                case NEXT:
                    mEbookView.pageScrollOrNextPage();
                    break;
                case NONE:
                    // 何もしない
                    break;
            }
        }
    }

    private void onRightAreaTapped() {
        if (mEbookView.getRetentionPageHelper().getCurrentPage().isZoomed()) {
            mViewerActivity.openMenu();
        } else {
            switch (mEbookView.getEbookMode().getTapAction()) {
                case NORMAL:
                    if (mEbookView.getPageAccess().isRtl()) {
                        mEbookView.pageScrollOrPrevPage();
                    } else {
                        mEbookView.pageScrollOrNextPage();
                    }
                    break;
                case NEXT:
                    mEbookView.pageScrollOrNextPage();
                    break;
                case NONE:
                    // 何もしない
                    break;
            }
        }
    }
}
