
package jp.bpsinc.android.trainingbookshelf.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import com.bps.trainingbookshelf.R;

public class LoadEpubDialog extends Dialog {

    public LoadEpubDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.loading_epub);
        setContentView(R.layout.dialog_loag_epub);
    }
}
