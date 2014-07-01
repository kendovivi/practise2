
package jp.bpsinc.android.chogazo.viewer.dialog;

import jp.bpsinc.android.chogazo.viewer.Config;
import jp.bpsinc.android.chogazo.viewer.R;
import jp.bpsinc.android.chogazo.viewer.util.LogUtil;
import jp.bpsinc.android.chogazo.viewer.util.WindowUtil;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;

public class BrightnessDialog extends DialogFragment implements
        SeekBar.OnSeekBarChangeListener, CompoundButton.OnCheckedChangeListener {
    /** アラートダイアログ：タグ */
    private static final String DLG_TAG = "brightness";

    private FragmentActivity mActivity;
    private SharedPreferences mPref;
    private SeekBar mSeekBar;
    private CheckBox mCheckBox;
    /**
     * 内部で一時的に保持する設定値、ダイアログを閉じるときにOKを押したらプリファレンスに反映<br>
     * プリファレンスに保存される値は1～100(チェックボックスがONの場合は-100～-1、マイナス値の場合は一律で「端末の設定に従う」になる)、<br>
     * シークバーのprogress値は0～99なので気をつけること
     */
    private int mBrightness;

    public static void show(FragmentManager manager) {
        BrightnessDialog dialog = new BrightnessDialog();
        dialog.show(manager, DLG_TAG);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mActivity = getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        LayoutInflater inflater = (LayoutInflater) mActivity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.cho_gazo_viewer_brightness_dialog, null);

        mCheckBox = (CheckBox) view.findViewById(R.id.brightness_checkbox);
        mSeekBar = (SeekBar) view.findViewById(R.id.brightness_seekbar);
        mCheckBox.setOnCheckedChangeListener(this);
        mSeekBar.setOnSeekBarChangeListener(this);

        mPref = mActivity.getSharedPreferences(
                Config.VIEWER_SETTING_PREF_NAME, Context.MODE_PRIVATE);

        mBrightness = mPref.getInt(getString(R.string.pre_key_viewer_brightness),
                getResources().getInteger(R.integer.pre_default_viewer_brightness));

        // 保存されている値が正か負かでチェックボックスのON/OFFを切り替える
        if (mBrightness < 0) {
            mCheckBox.setChecked(true);
            mSeekBar.setProgress(-(mBrightness));
        } else {
            mCheckBox.setChecked(false);
            mSeekBar.setProgress(mBrightness - 1);
        }

        builder.setTitle(R.string.viewer_brightness_dialog_title);
        builder.setView(view);
        builder.setPositiveButton(R.string.viewer_dlg_btn_positive, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 画面の明るさ設定を保存
                SharedPreferences.Editor editor = mPref.edit();
                editor.putInt(getString(R.string.pre_key_viewer_brightness), mBrightness);
                editor.commit();
            }
        });
        builder.setNegativeButton(R.string.viewer_dlg_btn_negative, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 画面の明るさ設定を元に戻す
                WindowUtil.setViewerBtightness(mActivity);
            }
        });
        return builder.create();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        LogUtil.v("isChecked=%b, brightness=%d", isChecked, mBrightness);
        if (isChecked) {
            if (mBrightness > 0) {
                mBrightness = -mBrightness;
            }
            mSeekBar.setEnabled(false);
        } else {
            if (mBrightness < 0) {
                mBrightness = -mBrightness;
            }
            mSeekBar.setEnabled(true);
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        LogUtil.v("progress=%d, fromUser=%b", progress, fromUser);
        if (fromUser) {
            // 不正値とかないし、ここではまだプリファレンスに保存はされないのでリスナーの戻り値は無視
            mBrightness = progress + 1;
            WindowUtil.setBrightness(mActivity, mBrightness);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // 何もしない
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        // 何もしない
    }
}
