
package jp.bpsinc.android.trainingbookshelf.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

public class DisplayUtil {

    private static final int CURRENT_SDK = Build.VERSION.SDK_INT;

    @SuppressLint("NewApi")
    public static int getScreenWidthDp(Context context) {
        int width = 0;
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        if (CURRENT_SDK >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            display.getMetrics(displayMetrics);
            width = displayMetrics.widthPixels;
            // dpに変更　(dp = pixel / density)
            width = (int) (width / displayMetrics.density);
        } else {
            float density = context.getResources().getDisplayMetrics().density;
            int widthPixels = display.getWidth();
            // dpに変更
            width = (int) (widthPixels / density);
        }
        return width;
    }

    /**
     * マルチディスプレイに画像を正確に表示するため、レイアウトパラメタなどの計算変換率
     * 
     * @param screenDpi ディスプレイのデフォルトDpi
     * @return
     */
    public static float getScreenDensity(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        float rate = 0;
        if (CURRENT_SDK >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            display.getMetrics(displayMetrics);
            rate = displayMetrics.density;
        } else {
            float density = context.getResources().getDisplayMetrics().density;
            rate = density;
        }
        return rate;
    }
}
