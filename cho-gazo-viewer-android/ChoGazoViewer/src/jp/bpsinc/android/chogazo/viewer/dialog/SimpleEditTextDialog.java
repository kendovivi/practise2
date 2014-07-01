/**
 * Copyright DMM.com Labo Co.,Ltd.
 */

package jp.bpsinc.android.chogazo.viewer.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnShowListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.text.InputFilter;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

public class SimpleEditTextDialog extends DialogFragment {
    public interface OnButtonClickListener {
        public void onPositiveClick(int id, String text, Bundle args);
        public void onNegativeClick(int id, Bundle args);
    }
    /** アラートダイアログ：タグ */
    private static final String DLG_TAG = "edit";
    /** バンドルキー：ID */
    private static final String BUNDLE_TITLE = "title";
    /** バンドルキー：メッセージ */
    private static final String BUNDLE_MESSAGE = "message";
    /** バンドルキー：OKボタンテキスト */
    private static final String BUNDLE_BTN_OK = "btn_ok";
    /** バンドルキー：Cancelボタンテキスト */
    private static final String BUNDLE_BTN_CANCEL = "btn_cancel";
    /** バンドルキー：EditTextのフォーカス */
    private static final String BUNDLE_EDIT_FOCUS = "edit_focus";
    /** 呼び出し識別用IDのkey */
    private static final String BUNDLE_KEY_ID = "id";
    /** バンドルキー：入力ボックス初期値 */
    private static final String BUNDLE_EDIT_INPUT_TEXT = "edit_input_text";

    public static void show(int id, FragmentManager manager, String title, String message,
            String editText, String btnOk, String btnCancel, boolean editFocus, Bundle args,
            OnButtonClickListener onButtonClickListener) {
        if (!(onButtonClickListener instanceof FragmentActivity)) {
            throw new IllegalArgumentException();
        }
        args.putString(BUNDLE_TITLE, title);
        args.putString(BUNDLE_MESSAGE, message);
        args.putString(BUNDLE_BTN_OK, btnOk);
        args.putString(BUNDLE_BTN_CANCEL, btnCancel);
        args.putInt(BUNDLE_KEY_ID, id);
        args.putString(BUNDLE_EDIT_INPUT_TEXT, editText);
        args.putBoolean(BUNDLE_EDIT_FOCUS, editFocus);

        SimpleEditTextDialog dialog = new SimpleEditTextDialog();
        dialog.setArguments(args);
        dialog.show(manager, DLG_TAG);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        String title = args.getString(BUNDLE_TITLE);
        String message = args.getString(BUNDLE_MESSAGE);
        String btnOk = args.getString(BUNDLE_BTN_OK);
        String btnCancel = args.getString(BUNDLE_BTN_CANCEL);
        String editInputText = args.getString(BUNDLE_EDIT_INPUT_TEXT);
        final boolean editFocus = args.getBoolean(BUNDLE_EDIT_FOCUS);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        if (title != null) {
            builder.setTitle(title);
        }
        if (message != null) {
            builder.setMessage(message);
        }
        final EditText editText = new EditText(getActivity());
        // テキスト入力ボックスの文字数制限を128にしておく
        InputFilter[] inputFilter = new InputFilter[1];
        inputFilter[0] = new InputFilter.LengthFilter(128);
        editText.setFilters(inputFilter);
        if (editInputText != null) {
            editText.setText(editInputText);
        }
        builder.setView(editText);
        if (btnOk != null) {
            builder.setPositiveButton(btnOk,  new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    OnButtonClickListener onButtonClickListener =
                            (OnButtonClickListener) getActivity();
                    if (onButtonClickListener != null) {
                        String inputText = editText.getText().toString();
                        onButtonClickListener.onPositiveClick(
                                getArguments().getInt(BUNDLE_KEY_ID), inputText, getArguments());
                    }
                }
            });
        }
        if (btnCancel != null) {
            builder.setNegativeButton(btnCancel, new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    OnButtonClickListener onButtonClickListener =
                            (OnButtonClickListener) getActivity();
                    if (onButtonClickListener != null) {
                        onButtonClickListener.onNegativeClick(
                                getArguments().getInt(BUNDLE_KEY_ID), getArguments());
                    }
                }
            });
        }
        AlertDialog dialog = builder.create();
        if (editFocus) {
            // フォーカスフラグがtrueならダイアログ表示時に自動でソフトウェアキーボードを出すようにする
            dialog.setOnShowListener(new OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    editText.setSelection(editText.length());
                    InputMethodManager inputMethodManager = (InputMethodManager) getActivity().
                            getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.showSoftInput(editText, 0);
                }
            });
        }
        return dialog;
    }
}
