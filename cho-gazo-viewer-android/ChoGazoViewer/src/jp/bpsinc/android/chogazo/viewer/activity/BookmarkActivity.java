
package jp.bpsinc.android.chogazo.viewer.activity;

import java.util.ArrayList;
import java.util.List;

import jp.bpsinc.android.chogazo.viewer.util.LayoutUtil;
import jp.bpsinc.android.chogazo.viewer.util.LogUtil;
import jp.bpsinc.android.chogazo.viewer.adapter.BookmarkInfo;
import jp.bpsinc.android.chogazo.viewer.adapter.BookmarkListAdapter;
import jp.bpsinc.android.chogazo.viewer.adapter.BookmarkListAdapter.OnListButtonClickListener;
import jp.bpsinc.android.chogazo.viewer.adapter.BookmarkListData;
import jp.bpsinc.android.chogazo.viewer.db.AbstractRow;
import jp.bpsinc.android.chogazo.viewer.db.BookmarkTable;
import jp.bpsinc.android.chogazo.viewer.dialog.SimpleEditTextDialog;
import jp.bpsinc.android.chogazo.viewer.dialog.SimpleEditTextDialog.OnButtonClickListener;
import jp.bpsinc.android.chogazo.viewer.R;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.widget.ListView;

public class BookmarkActivity extends FragmentActivity implements OnItemClickListener,
        OnListButtonClickListener, OnButtonClickListener {

    /** 入力値：ビューアから渡されるブックマーク情報 */
    public static final String INTENT_KEY_BOOKMARK_INFO = "INTENT_KEY_BOOKMARK_INFO";
    /** 戻り値：ビューアに返すしおりページ */
    public static final String INTENT_DATA_KEY_BOOKMARK_PAGE = "INTENT_DATA_KEY_BOOKMARK_PAGE";
    /** リクエストID:編集ボタン */
    private static final int REQUEST_ID_EDIT_BUTTON = 1;
    /** ブックマーク情報を表示するリストビュー */
    private ListView mListView;
    /** アダプター生成に使うブックマーク情報リスト */
    private List<BookmarkListData> mList;
    /** リストビューにセットするアダプター */
    private BookmarkListAdapter mAdapter;
    /** しおり情報操作用DBクラス */
    private BookmarkTable mBookmarkTable;
    /** しおり付けるのに必要なビューアから渡される情報 */
    private BookmarkInfo mBookmarkInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LogUtil.v();
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.cho_gazo_viewer_bookmark);
        LayoutUtil.setupTitleBar(this, R.string.viewer_bookmark_head_text);

        Intent intent = getIntent();
        mBookmarkInfo = (BookmarkInfo) intent.getSerializableExtra(INTENT_KEY_BOOKMARK_INFO);
        mBookmarkTable = new BookmarkTable(getApplicationContext());

        // しおり表示に使用するアダプターを生成してリストをセット
        mList = new ArrayList<BookmarkListData>();
        mAdapter = new BookmarkListAdapter(this, mList);
        mListView = (ListView) findViewById(R.id.bookmark_list);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
        listInit(mBookmarkInfo.getContentsKey());
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        LogUtil.v("position=%d, id=%d", position, id);
        BookmarkListData bookmarkListData =
                (BookmarkListData) mListView.getItemAtPosition(position);
        Intent intent = new Intent();
        intent.putExtra(INTENT_DATA_KEY_BOOKMARK_PAGE, bookmarkListData.getBookmarkPage());
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onListButtonClick(View view, int position) {
        BookmarkListData listData = (BookmarkListData) mListView.getItemAtPosition(position);
        LogUtil.v("bookmarkPage=%d, label=%s", listData.getBookmarkPage(), listData.getLabel());

        Bundle args = new Bundle();
        args.putInt(ViewerActivity.BUNDLE_KEY_INT_1, position);
        SimpleEditTextDialog.show(REQUEST_ID_EDIT_BUTTON, getSupportFragmentManager(),
                getString(R.string.viewer_bookmark_edit_dialog_title),
                null,
                listData.getLabel(),
                getString(R.string.viewer_bookmark_edit_dialog_ok),
                getString(R.string.viewer_bookmark_edit_dialog_delete),
                true, args, this);
    }

    private void listInit(String contentsKey) {
        mList.clear();
        List<AbstractRow> bookmarkList = mBookmarkTable.getBookmark(contentsKey);
        for (AbstractRow row : bookmarkList) {
            BookmarkTable.Row bookmarkRow = (BookmarkTable.Row) row;
            mList.add(new BookmarkListData(bookmarkRow.getBookmarkPage(), bookmarkRow.getLabel()));
        }
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPositiveClick(int id, String text, Bundle args) {
        switch (id) {
            case REQUEST_ID_EDIT_BUTTON:
                // 編集ダイアログでOKボタン押した時の処理(ラベルのアップデート処理)
                BookmarkListData listData = (BookmarkListData) mListView.getItemAtPosition(
                        args.getInt(ViewerActivity.BUNDLE_KEY_INT_1));
                if (text.length() > 0 && text.equals(listData.getLabel()) == false) {
                    mBookmarkTable.updateBookmarkLabel(mBookmarkInfo.getContentsKey(),
                            listData.getBookmarkPage(), text);
                    listData.setLabel(text);
                    mAdapter.notifyDataSetChanged();
                }
                break;
        }
    }

    @Override
    public void onNegativeClick(int id, Bundle args) {
        switch (id) {
            case REQUEST_ID_EDIT_BUTTON:
                // 編集ダイアログで削除ボタン押した時の処理
                BookmarkListData listData = (BookmarkListData) mListView.getItemAtPosition(
                        args.getInt(ViewerActivity.BUNDLE_KEY_INT_1));
                mBookmarkTable.deleteBookmark(
                        mBookmarkInfo.getContentsKey(), listData.getBookmarkPage());
                mList.remove(mList.indexOf(listData));
                mAdapter.notifyDataSetChanged();
                break;
        }
    }
}
