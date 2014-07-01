
package jp.bpsinc.android.chogazo.viewer.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import jp.bpsinc.android.chogazo.viewer.Config;
import jp.bpsinc.android.chogazo.viewer.util.LogUtil;
import jp.bpsinc.android.chogazo.viewer.util.OrientationUtil;
import jp.bpsinc.android.chogazo.viewer.view.EbookMode;
import jp.bpsinc.android.chogazo.viewer.view.OmfMode;

public class OmfViewerActivity extends ViewerActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LogUtil.v();
        super.onCreate(savedInstanceState);
    }

    @Override
    protected EbookMode createEbookMode() {
        boolean isLandscape = OrientationUtil.isLandscape(this);
        boolean isSpread;

        // OMFの初期表示は端末の向きごとに固定されてる
        if (isLandscape) {
            isSpread = true;
        } else {
            isSpread = false;
        }

        return new OmfMode(isLandscape, isSpread);
    }

    @Override
    protected void applySetting() {
        SharedPreferences pref = getSharedPreferences(
                Config.VIEWER_SETTING_PREF_NAME, Context.MODE_PRIVATE);

        // 回転設定
        applySettingOrientation(pref);
        // タップ動作設定
        applySettingTapAction(pref);
        // ページめくりアニメーション設定
        applySettingPageAnimation(pref);
        // 自動再生間隔設定
        applySettingAutoPlayInterval(pref);
        // ループ再生設定
        applySettingLoopPlay(pref);
        // 自動再生方向設定
        applySettingPlayDirection(pref);
    }

    @Override
    protected void orientationChange() {
        mEbookView.spreadChange(true);
    }
}
