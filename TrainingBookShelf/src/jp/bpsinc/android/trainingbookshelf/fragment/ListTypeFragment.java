
package jp.bpsinc.android.trainingbookshelf.fragment;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ListView;
import com.bps.trainingbookshelf.R;
import java.util.ArrayList;
import jp.bpsinc.android.trainingbookshelf.activity.BookShelfMainActivity;
import jp.bpsinc.android.trainingbookshelf.adapter.ListTypeRowAdapter;
import jp.bpsinc.android.trainingbookshelf.content.ContentInfo;
import jp.bpsinc.android.trainingbookshelf.preferences.ShelfPreferences;

public class ListTypeFragment extends Fragment {
    private BookShelfMainActivity mActivity;
    /** 本棚メインレイアウト */
    private View mBookShelfView;
    /** 本棚コンテンツリストビュー */
    private ListView mContentListView;
    /** 本棚コンテンツリストビュー各行adapter　 */
    private ListTypeRowAdapter mListTypeRowAdapter;

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
        mBookShelfView = inflater.inflate(R.layout.bookshelf_list_type_layout, container, false);
        return mBookShelfView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mContentListView = (ListView) mBookShelfView.findViewById(R.id.bookshelf_listview);

        mListTypeRowAdapter = new ListTypeRowAdapter(mActivity,
                R.layout.bookshelf_row,
                mContentList, this);
        mContentListView.setAdapter(mListTypeRowAdapter);

    }

    // TODO 回転の対応
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    // TODO サムネイルイベント
    public OnClickListener createListContentClickListener(final int position) {
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
        return ShelfPreferences.SHELF_TYPE_LIST;
    }

    public ListTypeRowAdapter getAdapter() {
        return mListTypeRowAdapter;
    }

}
