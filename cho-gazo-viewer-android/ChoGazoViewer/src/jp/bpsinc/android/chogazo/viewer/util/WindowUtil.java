
package jp.bpsinc.android.chogazo.viewer.util;

import jp.bpsinc.android.chogazo.viewer.Config;
import jp.bpsinc.android.chogazo.viewer.R;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.WindowManager;

public class WindowUtil {
    /**
     * 画面の明るさを設定、0未満の値はデフォルト(システム設定)、1～100(実際には100fで割って0～1の値を設定)はユーザの好みの明るさに調節可能
     * (0にすると画面が真っ暗になって操作不可能になる？)
     * 
     * @param activity 画面の明るさを変更するアクティビティ
     * @param brightness 画面の明るさ(暗1～100明)
     */
    public static void setBrightness(Activity activity, int brightness) {
        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        lp.screenBrightness = brightness / 100f;
        activity.getWindow().setAttributes(lp);
    }

    /**
     * ビューアの画面の明るさ設定値を読み込んで設定する
     * 
     * @param activity 画面の明るさを変更するビューアのアクティビティ
     */
    public static void setViewerBtightness(Activity activity) {
        SharedPreferences pref = activity.getSharedPreferences(
                Config.VIEWER_SETTING_PREF_NAME, Context.MODE_PRIVATE);
        WindowUtil.setBrightness(activity, pref.getInt(
                activity.getString(R.string.pre_key_viewer_brightness),
                activity.getResources().getInteger(R.integer.pre_default_viewer_brightness)));
    }
}
