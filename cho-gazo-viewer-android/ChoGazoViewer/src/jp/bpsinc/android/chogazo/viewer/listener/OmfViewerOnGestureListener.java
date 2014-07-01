
package jp.bpsinc.android.chogazo.viewer.listener;

import android.view.MotionEvent;
import jp.bpsinc.android.chogazo.viewer.util.LogUtil;
import jp.bpsinc.android.chogazo.viewer.activity.ViewerActivity;
import jp.bpsinc.android.chogazo.viewer.view.EbookView;

public class OmfViewerOnGestureListener extends ViewerOnGestureListener {
    public OmfViewerOnGestureListener(ViewerActivity viewerActivity, EbookView ebookView) {
        super(viewerActivity, ebookView);
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        LogUtil.v("MotionEvent");
        if (isTouchEventDisable()) {
            return true;
        }
        mEbookView.spreadChange(true);
        mEbookView.pageListInit();
        return true;
    }
}
