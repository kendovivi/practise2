
package jp.bpsinc.android.chogazo.viewer.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

public class SimpleYesNoDialog extends DialogFragment {
    public static interface YesNoListener {
        void onDialogPositiveClick(int id, Bundle args);

        void onDialogNegativeClick(int id, Bundle args);
    }

    /** アラートダイアログ：タグ */
    private static final String DLG_TAG = "yes_no";
    /** バンドルキー：ID */
    private static final String BUNDLE_ID = "id";
    /** バンドルキー：タイトル */
    private static final String BUNDLE_TITLE = "title";
    /** バンドルキー：メッセージ */
    private static final String BUNDLE_MESSAGE = "message";
    /** バンドルキー：YESボタンテキスト */
    private static final String BUNDLE_POSITIVE_TEXT = "positive_text";
    /** バンドルキー：NOボタンテキスト */
    private static final String BUNDLE_NEGATIVE_TEXT = "negative_text";

    public SimpleYesNoDialog() {
        // do nothing
    }

    public static void show(int id, FragmentManager manager, String title, String message,
            String positiveText, String negativeText, Bundle args, YesNoListener listener) {
        if (!(listener instanceof FragmentActivity)) {
            throw new IllegalArgumentException();
        }

        args.putInt(BUNDLE_ID, id);
        args.putString(BUNDLE_TITLE, title);
        args.putString(BUNDLE_MESSAGE, message);
        args.putString(BUNDLE_POSITIVE_TEXT, positiveText);
        args.putString(BUNDLE_NEGATIVE_TEXT, negativeText);

        SimpleYesNoDialog dialog = new SimpleYesNoDialog();
        dialog.setArguments(args);
        dialog.show(manager, DLG_TAG);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String title = getArguments().getString(BUNDLE_TITLE);
        String message = getArguments().getString(BUNDLE_MESSAGE);
        String positiveText = getArguments().getString(BUNDLE_POSITIVE_TEXT);
        String negativeText = getArguments().getString(BUNDLE_NEGATIVE_TEXT);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        if (title != null) {
            builder.setTitle(title);
        }
        if (message != null) {
            builder.setMessage(message);
        }
        if (positiveText != null) {
            builder.setPositiveButton(positiveText, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    YesNoListener listener = (YesNoListener) getActivity();
                    if (listener != null) {
                        Bundle args = getArguments();
                        listener.onDialogPositiveClick(args.getInt(BUNDLE_ID), args);
                    }
                }
            });
        }
        if (negativeText != null) {
            builder.setNegativeButton(negativeText, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    YesNoListener listener = (YesNoListener) getActivity();
                    if (listener != null) {
                        Bundle args = getArguments();
                        listener.onDialogNegativeClick(args.getInt(BUNDLE_ID), args);
                    }
                }
            });
        }
        return builder.create();
    }
}
