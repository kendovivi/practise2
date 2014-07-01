package jp.bpsinc.android.chogazo.viewer.activity;

import java.io.File;

import jp.bpsinc.android.chogazo.viewer.Config;
import jp.bpsinc.android.chogazo.viewer.R;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.view.Window;

public abstract class ViewerPreferenceActivity extends PreferenceActivity implements
        OnSharedPreferenceChangeListener {
    public static final String EXTRA_CONTENT_TITLE = "EXTRA_CONTENT_TITLE";
    public static final String EXTRA_CONTENT_TITLE_KANA = "EXTRA_CONTENT_TITLE_KANA";
    public static final String EXTRA_CONTENT_AUTHOR = "EXTRA_CONTENT_AUTHOR";
    public static final String EXTRA_CONTENT_AUTHOR_KANA = "EXTRA_CONTENT_AUTHOR_KANA";
    public static final String EXTRA_CONTENT_FILE_PATH = "EXTRA_CONTENT_FILE_PATH";

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName(Config.VIEWER_SETTING_PREF_NAME);
        setContentView(R.layout.cho_gazo_viewer_pref_list);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().
                registerOnSharedPreferenceChangeListener(this);
        updateSummary();
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().
                unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        updateSummary();
    }

    /**
     * ファイル情報の部分に値をセットする。
     */
    @SuppressWarnings("deprecation")
    protected void setupFileInformation() {
        Intent intent = getIntent();
        String title = intent.getStringExtra(EXTRA_CONTENT_TITLE);
        String author = intent.getStringExtra(EXTRA_CONTENT_AUTHOR);
        String titleKana = intent.getStringExtra(EXTRA_CONTENT_TITLE_KANA);
        String authorKana = intent.getStringExtra(EXTRA_CONTENT_AUTHOR_KANA);
        String filename = intent.getStringExtra(EXTRA_CONTENT_FILE_PATH);
        if (title != null || author != null || filename != null || titleKana != null
                || authorKana != null) {
            filename = new File(filename).getName();
            findPreference(getString(R.string.pref_file_info_title)).setSummary(title);
            findPreference(getString(R.string.pref_file_info_title_kana)).setSummary(titleKana);
            findPreference(getString(R.string.pref_file_info_author)).setSummary(author);
            findPreference(getString(R.string.pref_file_info_author_kana)).setSummary(authorKana);
            findPreference(getString(R.string.pref_file_info_filename)).setSummary(filename);
        } else {
            Preference fileInfo = findPreference(getString(R.string.pref_subtitle_file_info));
            getPreferenceScreen().removePreference(fileInfo);
        }
    }

    /**
     * 適宜summaryを更新する
     */
    protected abstract void updateSummary();
}
