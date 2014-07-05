
package jp.bpsinc.android.trainingbookshelf.fragment;

import java.io.Serializable;
import java.util.ArrayList;

import jp.bpsinc.android.chogazo.viewer.activity.ViewerActivity;
import jp.bpsinc.android.chogazo.viewer.content.ViewerContents;
import jp.bpsinc.android.chogazo.viewer.content.epub.EpubFile;
import jp.bpsinc.android.chogazo.viewer.content.epub.EpubReader;
import jp.bpsinc.android.chogazo.viewer.content.zip.ZipBookFile;
import jp.bpsinc.android.chogazo.viewer.content.zip.ZipReader;
import jp.bpsinc.android.trainingbookshelf.activity.BookShelfMainActivity;
import jp.bpsinc.android.trainingbookshelf.adapter.ThumbTypeRowAdapter;
import jp.bpsinc.android.trainingbookshelf.content.ContentInfo;
import jp.bpsinc.android.trainingbookshelf.preferences.ShelfPreferences;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.RelativeLayout;

import com.bps.trainingbookshelf.R;

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
                Intent intent = new Intent(mActivity,
                        jp.bpsinc.android.chogazo.viewer.activity.FxlViewerActivity.class);

                ViewerContents viewerContents = new ViewerContents();
                viewerContents.setContentsKey(String.valueOf(position));
                viewerContents.setPath(mContentList.get(position).getEpubPath());

                String readerClassName = null;
                String bookClassName = null;
                switch (mContentList.get(position).getType()) {
                    case ContentInfo.CONTENT_TYPE_EPUB:
                        readerClassName = EpubReader.class.getName();
                        bookClassName = EpubFile.class.getName();
                        break;
                    case ContentInfo.CONTENT_TYPE_ZIP:
                        readerClassName = ZipReader.class.getName();
                        bookClassName = ZipBookFile.class.getName();
                        break;
                }
                viewerContents.setReaderClassName(readerClassName);
                viewerContents.setBookClassName(bookClassName);

                intent.putExtra(ViewerActivity.INTENT_KEY_CONTENTS, (Serializable) viewerContents);
                mActivity.startActivity(intent);
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
