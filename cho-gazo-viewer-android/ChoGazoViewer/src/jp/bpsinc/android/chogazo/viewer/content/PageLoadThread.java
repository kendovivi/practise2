
package jp.bpsinc.android.chogazo.viewer.content;

import java.io.IOException;

import jp.bpsinc.android.chogazo.viewer.content.Page.OnBeforeReplaceHandler;
import jp.bpsinc.android.chogazo.viewer.dialog.ViewerErrorDialog;
import jp.bpsinc.android.chogazo.viewer.exception.ContentsOtherException;
import jp.bpsinc.android.chogazo.viewer.exception.ContentsParseException;
import jp.bpsinc.android.chogazo.viewer.exception.LoadImageException;
import jp.bpsinc.android.chogazo.viewer.util.LogUtil;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;

public class PageLoadThread implements Runnable {
    public interface PageLoadCompleteListener {
        public void onLoadComplete(Page page);
    }

    private FragmentActivity mActivity;

    private Page mPage;

    private PageLoadCompleteListener mCompleteListener;

    private Page.LoadQuality mLoadQuality;

    private Handler mHandler;

    private boolean mIsHalt;

    public PageLoadThread(FragmentActivity activity,
            Page page, PageLoadCompleteListener callback, Page.LoadQuality loadQuality) {
        mActivity = activity;
        mPage = page;
        mCompleteListener = callback;
        mLoadQuality = loadQuality;
        mHandler = new Handler();
        mIsHalt = false;
    }

    public boolean shouldAbort() {
        return mIsHalt || mPage == null;
    }

    @Override
    public void run() {
        LogUtil.d("ImageLoadThread run loadType=%s pageInfo=%s",
                mLoadQuality.name(), mPage.getPageInfo().toString());
        if (shouldAbort()) {
            return;
        }

        OnBeforeReplaceHandler beforeUpdate = new OnBeforeReplaceHandler() {
            @Override
            public boolean onBeforeReplace() {
                if (shouldAbort()) {
                    return false;
                }
                if (mPage.canReplaceToQuality(mLoadQuality) == false) {
                    // すでに同高解像度なので、読み込む必要なし
                    LogUtil.i("This page is already high quality. skip.");
                    return false;
                }
                return true;
            }
        };

        try {
            if (mPage.canReplaceToQuality(mLoadQuality) == false) {
                // すでに同高解像度なので、読み込む必要なし
                LogUtil.i("This page is already high quality. skip.");
                return;
            }
            if (mPage.isPageSpread() == false) {
                mPage.detectBitmapSize();
                if (shouldAbort()) {
                    return;
                }
                if (mLoadQuality == Page.LoadQuality.HIGH) {
                    mPage.replaceToOriginalSize(beforeUpdate);
                } else {
                    mPage.replaceToDisplayFitSize(beforeUpdate);
                }
            } else {
                mPage.detectSpreadBitmapSize();
                if (shouldAbort()) {
                    return;
                }
                if (mLoadQuality == Page.LoadQuality.HIGH) {
                    mPage.replaceToOriginalSpreadSize(beforeUpdate);
                } else {
                    mPage.replaceToDisplayFitSpreadSize(beforeUpdate);
                }
            }
            if (shouldAbort()) {
                return;
            }
            mPage.onChangeBitmap();
            if (mCompleteListener != null) {
                mCompleteListener.onLoadComplete(mPage);
            }
        } catch (RuntimeException e) {
            LogUtil.e("unexpected error", e);
            postShowDialog(ViewerErrorDialog.ID_UNEXPECTED_ERR);
        } catch (ContentsParseException e) {
            LogUtil.e("contents parse error", e);
            postShowDialog(ViewerErrorDialog.ID_CONTENTS_PARSE_ERR);
        } catch (ContentsOtherException e) {
            LogUtil.e("contents file error", e);
            postShowDialog(ViewerErrorDialog.ID_CONTENTS_OTHER_ERR);
        } catch (LoadImageException e) {
            // byteデータがnullだったりデコードに失敗した場合
            LogUtil.e("Failed to load image", e);
            postShowDialog(ViewerErrorDialog.ID_LOAD_IMAGE_ERR);
        } catch (IOException e) {
            LogUtil.e("contents i/o error", e);
            postShowDialog(ViewerErrorDialog.ID_CONTENTS_OTHER_ERR);
        } catch (OutOfMemoryError e) {
            LogUtil.e("out of memory in the read method of the bitmap", e);
            postShowDialog(ViewerErrorDialog.ID_OUT_OF_MEMORY_ERR);
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        LogUtil.d("thread is finalize");
    }

    public void halt() {
        mIsHalt = true;
        LogUtil.d("thread halt call pageInfo=%s quality=%s",
                mPage.getPageInfo().toString(), mLoadQuality.name());
    }

    private void postShowDialog(final int id) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                ViewerErrorDialog.show(mActivity.getSupportFragmentManager(), id);
            }
        });
    }
}
