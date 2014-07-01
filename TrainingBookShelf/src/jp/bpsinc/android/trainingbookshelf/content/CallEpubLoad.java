
package jp.bpsinc.android.trainingbookshelf.content;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.bps.trainingbookshelf.R;
import java.util.ArrayList;
import jp.bpsinc.android.trainingbookshelf.activity.BookShelfMainActivity;
import jp.bpsinc.android.trainingbookshelf.async.ContentLoaderTask;
import jp.bpsinc.android.trainingbookshelf.async.ContentLoaderTaskListener;
import jp.bpsinc.android.trainingbookshelf.dialog.LoadEpubDialog;
import jp.bpsinc.android.trainingbookshelf.fragment.ListTypeFragment;
import jp.bpsinc.android.trainingbookshelf.fragment.ThumbTypeFragment;
import jp.bpsinc.android.trainingbookshelf.preferences.ShelfPreferences;

public class CallEpubLoad {

    private BookShelfMainActivity mActivity;

    private int mShelfType;

    private LoadEpubDialog mLoadEpubDialog;

    private ArrayList<ContentInfo> mContentList;

    private Fragment mFragment;

    public CallEpubLoad(Fragment fragment, int shelfType, Activity activity) {
        mFragment = fragment;
        mShelfType = shelfType;
        mActivity = (BookShelfMainActivity) activity;
        mLoadEpubDialog = new LoadEpubDialog(activity);

        ContentLoaderTask task = new ContentLoaderTask(new ContentLoaderTaskListener() {

            @Override
            public void onTaskStart() {
                // EPUB読み込みタスク開始、ダイアログを表示
                mLoadEpubDialog.show();
            }

            @Override
            public void onCheckExistence(int count) {
                if (count == 0) {
                    // コンテンツがない提示
                }
            }

            @Override
            public void onLoading(int progress) {
                // EPUB読み込み途中、ダイアログプログレス更新
                TextView loadingProgress = (TextView) mLoadEpubDialog
                        .findViewById(R.id.epub_loading_text);
                loadingProgress.setText("Now Loading .... " + progress + " %");
            }

            @Override
            public void onTaskFinish(ArrayList<ContentInfo> result) {
                // 指定パスから、EPUBの読み込みタスク終了
                if (result != null) {
                    mContentList = result;
                    mActivity.setContentList(mContentList);
                    ArrayAdapter<ContentInfo> adapter =
                            mShelfType == ShelfPreferences.SHELF_TYPE_THUMBNAIL ?
                                    ((ThumbTypeFragment) mFragment).getAdapter() :
                                    ((ListTypeFragment) mFragment).getAdapter();

                    // adapterのリストを更新して、adapterに伝える
                    addAll(adapter, mContentList);
                }
                // EPUB読み込みタスク終了、ダイアログを消す
                mLoadEpubDialog.dismiss();
            }
        });
        task.execute(1);
    }

    private void addAll(ArrayAdapter<ContentInfo> adapter, ArrayList<ContentInfo> contentList) {
        for (ContentInfo content : contentList) {
            adapter.add(content);
        }
    }
}
