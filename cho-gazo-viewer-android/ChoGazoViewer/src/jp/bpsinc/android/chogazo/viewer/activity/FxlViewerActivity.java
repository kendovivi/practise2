
package jp.bpsinc.android.chogazo.viewer.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import jp.bpsinc.android.chogazo.viewer.util.LogUtil;
import jp.bpsinc.android.chogazo.viewer.util.OrientationUtil;
import jp.bpsinc.android.chogazo.viewer.view.EbookMode;
import jp.bpsinc.android.chogazo.viewer.view.FxlMode;
import jp.bpsinc.android.chogazo.viewer.Config;
import jp.bpsinc.android.chogazo.viewer.R;

public class FxlViewerActivity extends ViewerActivity {
    /**
     * 縦表示見開き設定の状態で設定画面を開き、単ページ設定に変更＋端末回転をして設定画面を閉じた時に
     * onActivityResultより先にonConfigurationChangedが発生する事により、
     * 横表示(アスペクトフィット)見開き設定に切り替わった後単ページに設定し直されてしまう、
     * そのため横単ページになった時に常に横見開き時の手前のページが表示されてしまう、このフラグはその現象を防ぐためだけに使用する
     */
    private boolean mInvalidSettingFlag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LogUtil.v();
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // 設定変更前の状態を保持しておく
        SharedPreferences pref = getSharedPreferences(
                Config.VIEWER_SETTING_PREF_NAME, Context.MODE_PRIVATE);
        boolean isSpread = pref.getBoolean(getString(R.string.pre_key_viewer_spread),
                getResources().getBoolean(R.bool.pre_default_viewer_spread));
        boolean isSpreadView = mEbookMode.isSpreadView();

        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_CODE_SETTING:
                // 設定画面から戻ってきた時
                if (mInvalidSettingFlag) {
                    // フラグが立ってる場合は何もしない
                } else if (isSpreadView != mEbookMode.isSpreadView()) {
                    // 見開きページのポジションを取るためにspreadChange前にModeを元の状態にしておく必要がある、そのため見開き設定を元に戻す
                    mEbookMode.setIsSpread(!isSpread);
                    // 設定適用前と後で使用すべきページリストが変わっていた場合、現在のページインデックスとページリストを切り替える
                    mEbookView.spreadChange(true);
                    mEbookView.pageListInit();
                }
                mInvalidSettingFlag = false;
                break;
            default:
                break;
        }
    }

    @Override
    protected EbookMode createEbookMode() {
        // ここの見開き設定は適当にfalse入れとく、直後にapplySettingで更新される
        return new FxlMode(OrientationUtil.isLandscape(this), false);
    }

    @Override
    protected void applySetting() {
        SharedPreferences pref = getSharedPreferences(
                Config.VIEWER_SETTING_PREF_NAME, Context.MODE_PRIVATE);

        // 回転設定
        applySettingOrientation(pref);
        // 見開き表示設定
        applySettingSpread(pref);
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
        SharedPreferences pref = getSharedPreferences(
                Config.VIEWER_SETTING_PREF_NAME, Context.MODE_PRIVATE);
        boolean isSpread = pref.getBoolean(getString(R.string.pre_key_viewer_spread),
                getResources().getBoolean(R.bool.pre_default_viewer_spread));
        if (mEbookMode.isSpread()) {
            if (isSpread || mEbookMode.isLandscape()) {
                // FXLで見開き設定の場合、縦=単ページ、横=見開きとなるため、現在のページインデックスを切り替える
                mEbookView.spreadChange(false);
            } else {
                // 縦見開き設定の状態で設定画面を開き、単ページに変更設定＋端末回転をして設定画面を閉じた時だけここに来るはず
                // onActivityResultより先にonConfigurationChangedが発生するためここでフラグを立て、
                // applySetting(onConfigurationChanged)でこのフラグが立ってる場合は見開き切り替えをしない
                mInvalidSettingFlag = true;
            }
        }
    }
}
