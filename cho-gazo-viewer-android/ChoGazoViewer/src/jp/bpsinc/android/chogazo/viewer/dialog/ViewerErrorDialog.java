package jp.bpsinc.android.chogazo.viewer.dialog;

import jp.bpsinc.android.chogazo.viewer.R;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnKeyListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.KeyEvent;

public class ViewerErrorDialog extends DialogFragment {
    public static final int ID_OK = 0;
    /** 引数エラー、本棚(呼び出し元)から渡された情報に誤りがある */
    public static final int ID_ILLEGAL_ARG_ERR = -11;
    /** 引数エラー、本棚(呼び出し元)から渡されたパス情報に誤りがある */
    public static final int ID_PATH_NOTFOUND_ERR = -12;
    /** 引数エラー、本棚(呼び出し元)から渡された設定情報に誤りがある */
    public static final int ID_SETTING_ERR = -13;
    /** メモリ不足エラー、画像読み込み時にメモリ不足が発生 */
    public static final int ID_OUT_OF_MEMORY_ERR = -20;
    /** 画像読み込みエラー、画像読み込み(解析)に失敗 */
    public static final int ID_LOAD_IMAGE_ERR = -21;
    /** DRM解除エラー */
    public static final int ID_DRM_RELEASE_ERR = -30;
    /** コンテンツ解凍エラー */
    public static final int ID_CONTENTS_UNZIP_ERR = -31;
    /** コンテンツ解析エラー */
    public static final int ID_CONTENTS_PARSE_ERR = -32;
    /** コンテンツ関連のその他のエラー */
    public static final int ID_CONTENTS_OTHER_ERR = -39;
    /** その他のエラー */
    public static final int ID_UNEXPECTED_ERR = -1024;

    /** アラートダイアログ：タグ **/
    private static final String DLG_TAG = "login";
    /** バンドルキー：ID */
    private static final String BUNDLE_ID = "id";

    public static void show(FragmentManager manager, int id) {
        ViewerErrorDialog dialog = new ViewerErrorDialog();
        Bundle bundle = new Bundle();
        bundle.putInt(BUNDLE_ID, id);
        dialog.setArguments(bundle);
        dialog.show(manager, DLG_TAG);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        int id = getArguments().getInt(BUNDLE_ID);
        String title = getString(R.string.viewer_dlg_err_title);
        String message = null;
        switch (id) {
            case ID_OK:
                break;
            case ID_OUT_OF_MEMORY_ERR:
                message = String.format(getString(R.string.viewer_dlg_err_mes_memory), id);
                break;
            case ID_DRM_RELEASE_ERR:
            case ID_CONTENTS_UNZIP_ERR:
            case ID_CONTENTS_PARSE_ERR:
            case ID_CONTENTS_OTHER_ERR:
                message = String.format(getString(R.string.viewer_dlg_err_mes_contents), id);
                break;
            case ID_ILLEGAL_ARG_ERR:
            case ID_PATH_NOTFOUND_ERR:
            case ID_SETTING_ERR:
            case ID_LOAD_IMAGE_ERR:
            case ID_UNEXPECTED_ERR:
                message = String.format(getString(R.string.viewer_dlg_err_mes_unexpected), id);
                break;
            default:
                break;
        }

        if (title != null) {
            builder.setTitle(title);
        }
        if (message != null) {
            builder.setMessage(message);
        }
        builder.setPositiveButton(getString(R.string.viewer_dlg_btn_positive),
                new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getActivity().finish();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (KeyEvent.KEYCODE_SEARCH == keyCode || KeyEvent.KEYCODE_BACK == keyCode) {
                    // キャンセル不可
                    return true;
                }
                return false;
            }
        });
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }
}
