
package jp.bpsinc.android.trainingbookshelf.preferences;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class ShelfPreferences {

    /** 本棚表示タイプ */
    public static final String SHELF_TYPE = "SHELF_TYPE";
    /** 本棚表示タイプ　サムネイルタイプ */
    public static final int SHELF_TYPE_THUMBNAIL = 1;
    /** 本棚表示タイプ　リストタイプ */
    public static final int SHELF_TYPE_LIST = 2;
    /** 本棚デフォルトタイプ */
    public static final int SHELF_TYPE_DEFAULT = SHELF_TYPE_THUMBNAIL;

    private Activity mActivity;
    private SharedPreferences mPref;

    public ShelfPreferences(Activity activity) {
        mActivity = activity;
        mPref = PreferenceManager.getDefaultSharedPreferences(mActivity);
    }

    /**
     * 本棚タイプを取得。プリファレンスファイルに入っていない場合、デフォルトタイプを使う。
     * 
     * @return　int 本棚タイプ
     */
    public int getShelfType() {
        return mPref.getInt(SHELF_TYPE, SHELF_TYPE_DEFAULT);
    }

    /**
     * 現在の本棚タイプをプリファレンスファイルに上書
     * 
     * @param type
     */
    public void saveShelfType(int type) {
        Editor editor = mPref.edit();
        editor.putInt(SHELF_TYPE, type);
        editor.commit();
    }
}
