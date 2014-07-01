package jp.bpsinc.android.chogazo.viewer.preference;

import jp.bpsinc.android.chogazo.viewer.R;
import jp.bpsinc.android.chogazo.viewer.util.LogUtil;
import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

public class AutoPlayIntervalPreference extends DialogPreference implements
        SeekBar.OnSeekBarChangeListener {
    /** 自動再生間隔の最小値 */
    private static final int MIN_INTERVAL = 2;

    /** 自動再生間隔のデフォルト値 */
    private final int mDefaultAutoPlayInterval;
    private SeekBar mSeekBar;
    private TextView mSeekText;
    /**
     * 内部で一時的に保持する設定値、ダイアログを閉じるときにOKを押したらプリファレンスに反映<br>
     * プリファレンスに保存される値は2～60、シークバーのprogress値は0～58なので気をつけること
     */
    private int mAutoPlayInterval;

    public AutoPlayIntervalPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        // 自動再生間隔設定用のダイアログレイアウトを設定
        setDialogLayoutResource(R.layout.cho_gazo_viewer_auto_play_interval_dialog);
        // デフォルト値はリソースから取得
        mDefaultAutoPlayInterval = context.getResources().getInteger(
                R.integer.pre_default_viewer_auto_play_interval);
    }

    @Override
    protected void onBindDialogView(View view) {
        LogUtil.v();
        super.onBindDialogView(view);
        // ここの処理、ダイアログ開く度に取得し直さないとダメ
        mSeekBar = (SeekBar) view.findViewById(R.id.cho_gazo_viewer_auto_play_interval_seek_bar);
        mSeekBar.setOnSeekBarChangeListener(this);
        mSeekText = (TextView) view.findViewById(R.id.cho_gazo_viewer_auto_play_interval_text);
        mAutoPlayInterval = getPersistedInt(mDefaultAutoPlayInterval);

        mSeekBar.setProgress(mAutoPlayInterval - MIN_INTERVAL);
        setSeekText(mAutoPlayInterval);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        LogUtil.v("positiveResult=%b", positiveResult);
        if (positiveResult) {
            // OK押したらプリファレンスに反映、リスナーは値を変える度に呼び出してるのでここでは必要なし
            persistInt(mAutoPlayInterval);
        } else {
            // キャンセルした場合は明るさなどを戻す必要があるので、ダイアログ開いた時に保存されていた値でリスナー呼び出す
            callChangeListener(getPersistedInt(mDefaultAutoPlayInterval));
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        LogUtil.v("progress=%d, fromUser=%b", progress, fromUser);
        if (fromUser) {
            mAutoPlayInterval = progress + MIN_INTERVAL;
            setSeekText(mAutoPlayInterval);
            callChangeListener(mAutoPlayInterval);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // 特に何もしない
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        // 特に何もしない
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        int value = mDefaultAutoPlayInterval;
        if (restoreValue) {
            value = getPersistedInt(value);
        } else {
            if (defaultValue != null) {
                value = Integer.valueOf((Integer) defaultValue);
            }
        }
        persistInt(value);
    }

    @Override
    public void setSummary(CharSequence summary) {
        super.setSummary(summary + "秒");
    }

    public int getValue() {
        return getPersistedInt(mDefaultAutoPlayInterval);
    }

    private void setSeekText(int interval) {
        mSeekText.setText(interval + "秒");
    }
}
