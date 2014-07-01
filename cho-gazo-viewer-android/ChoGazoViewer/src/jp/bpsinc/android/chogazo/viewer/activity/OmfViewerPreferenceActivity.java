
package jp.bpsinc.android.chogazo.viewer.activity;

import jp.bpsinc.android.chogazo.viewer.util.LayoutUtil;
import jp.bpsinc.android.chogazo.viewer.util.LogUtil;
import jp.bpsinc.android.chogazo.viewer.util.OrientationUtil;
import jp.bpsinc.android.chogazo.viewer.preference.AutoPlayIntervalPreference;
import jp.bpsinc.android.chogazo.viewer.R;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;

public class OmfViewerPreferenceActivity extends ViewerPreferenceActivity implements
        Preference.OnPreferenceChangeListener {
    /** 回転設定 */
    private ListPreference mOrientationPreference;
    /** タップ時の動作 */
    private ListPreference mTapActionPreference;
    /** ページめくりアニメーション */
    private ListPreference mAnimationPreference;
    /** 自動再生間隔 */
    private AutoPlayIntervalPreference mAutoPlayIntervalPreference;
    /** ループ再生 */
    private CheckBoxPreference mLoopPlay;
    /** 自動再生方向 */
    private ListPreference mAutoPlayDirection;

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LogUtil.v();
        super.onCreate(savedInstanceState);
        LayoutUtil.setupTitleBar(this, R.string.pref_viewer_title);
        addPreferencesFromResource(R.xml.cho_gazo_viewer_omf_pref);

        mOrientationPreference = (ListPreference) findPreference(
                getString(R.string.pre_key_viewer_orientation));
        mTapActionPreference = (ListPreference) findPreference(
                getString(R.string.pre_key_viewer_tap_action));
        mAnimationPreference = (ListPreference) findPreference(
                getString(R.string.pre_key_viewer_animation));
        mAutoPlayIntervalPreference = (AutoPlayIntervalPreference) findPreference(
                getString(R.string.pre_key_viewer_auto_play_interval));
        mLoopPlay = (CheckBoxPreference) findPreference(
                getString(R.string.pre_key_viewer_loop_play));
        mAutoPlayDirection = (ListPreference) findPreference(
                getString(R.string.pre_key_viewer_auto_play_direction));

        mOrientationPreference.setOnPreferenceChangeListener(this);
        mTapActionPreference.setOnPreferenceChangeListener(this);
        mAnimationPreference.setOnPreferenceChangeListener(this);
        mAutoPlayIntervalPreference.setOnPreferenceChangeListener(this);
        mLoopPlay.setOnPreferenceChangeListener(this);
        mAutoPlayDirection.setOnPreferenceChangeListener(this);

        OrientationUtil.setOrientation(this, mOrientationPreference.getValue());

        // 回転設定変えた場合など、resultCodeが初期化されることがあるのでここで設定
        if (savedInstanceState != null) {
            setResult(RESULT_OK);
        }
        setupFileInformation();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        LogUtil.v();
        boolean retBool = true;
        if (preference instanceof ListPreference) {
            // リストプリファレンスの処理
            ListPreference listPreference = (ListPreference) preference;
            String strValue = (String) newValue;

            // 違う値が設定されたか判定
            if (listPreference.getValue().equals(strValue) == false) {
                if (listPreference == mOrientationPreference) {
                    // 回転設定の場合、このアクティビティにも即座に適用
                    OrientationUtil.setOrientation(this, strValue);
                }
            } else {
                retBool = false;
            }
        }
        if (retBool) {
            setResult(RESULT_OK);
        }
        return retBool;
    }

    @Override
    protected void updateSummary() {
        mOrientationPreference.setSummary(mOrientationPreference.getEntry());
        mTapActionPreference.setSummary(mTapActionPreference.getEntry());
        mAnimationPreference.setSummary(mAnimationPreference.getEntry());
        mAutoPlayIntervalPreference.setSummary(
                String.valueOf(mAutoPlayIntervalPreference.getValue()));
        mAutoPlayDirection.setSummary(mAutoPlayDirection.getEntry());
    }
}
