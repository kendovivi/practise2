
package jp.bpsinc.android.trainingbookshelf.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.RelativeLayout;
import com.bps.trainingbookshelf.R;
import java.util.ArrayList;
import jp.bpsinc.android.trainingbookshelf.activity.BookShelfMainActivity;
import jp.bpsinc.android.trainingbookshelf.adapter.ThumbTypeRowAdapter;
import jp.bpsinc.android.trainingbookshelf.content.ContentInfo;
import jp.bpsinc.android.trainingbookshelf.preferences.ShelfPreferences;

public class ThumbTypeFragment extends Fragment {
    private BookShelfMainActivity mActivity;
    /** 本棚メインレイアウト */
    private View mBookShelfView;
    /** 本棚コンテンツリストビュー */
    private GridView mContentGridView;
    /** 本棚コンテンツリストビュー各行adapter　 */
    private ThumbTypeRowAdapter mThumbTypeRowAdapter;

    /** テストデータ関連 */
    private static ArrayList<ContentInfo> mContentList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (BookShelfMainActivity) getActivity();
        if (mActivity.getContentList() != null) {
            mContentList = mActivity.getContentList();
        } else {
            mContentList = new ArrayList<ContentInfo>();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBookShelfView = (RelativeLayout) inflater.inflate(R.layout.bookshelf_thumb_type_layout,
                container, false);

        return mBookShelfView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mContentGridView = (GridView) mBookShelfView.findViewById(R.id.bookshelf_gridview);
        mThumbTypeRowAdapter = new ThumbTypeRowAdapter(mActivity,
                R.layout.bookshelf_row,
                mContentList, this);
        mContentGridView.setAdapter(mThumbTypeRowAdapter);
    }

    // TODO サムネイルイベント
    public OnClickListener createThumbContentClickListener(final int position) {
        OnClickListener listener = new OnClickListener() {

            @Override
            public void onClick(View v) {
                // Toast.makeText(mActivity, "-->> position = " + position,
                // Toast.LENGTH_SHORT).show();
            }
        };
        return listener;
    }

    public int getShelfType() {
        return ShelfPreferences.SHELF_TYPE_THUMBNAIL;
    }

    public ThumbTypeRowAdapter getAdapter() {
        return mThumbTypeRowAdapter;
    }

}
