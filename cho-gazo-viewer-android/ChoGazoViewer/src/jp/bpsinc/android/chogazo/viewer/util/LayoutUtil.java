package jp.bpsinc.android.chogazo.viewer.util;

import jp.bpsinc.android.chogazo.viewer.R;
import android.app.Activity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class LayoutUtil {
    /**
     * タイトルテキストと戻るボタンの設定を行う、戻るボタンはActivity.onBackPressed()を実行する<br>
     * cho_gazo_viewer_pref_header.xmlをincludeしているレイアウトを適用している画面のみ使用可能
     * 
     * @param activity cho_gazo_viewer_pref_header.xmlを含んだレイアウトをセット済みのアクティビティ
     * @param titleResource タイトルとして表示するリソースのID
     */
    public static void setupTitleBar(final Activity activity, int titleResource) {
        ((TextView) activity.findViewById(R.id.viewer_pref_title)).setText(titleResource);
        ImageView backButton = (ImageView) activity.findViewById(R.id.viewer_pref_back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.onBackPressed();
            }
        });
    }
}
