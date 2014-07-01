
package jp.bpsinc.android.chogazo.viewer.adapter;

import java.util.List;

import jp.bpsinc.android.chogazo.viewer.activity.BookmarkActivity;
import jp.bpsinc.android.chogazo.viewer.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

public class BookmarkListAdapter extends ArrayAdapter<BookmarkListData> {
    public interface OnListButtonClickListener {
        public void onListButtonClick(View view, int position);
    }

    private final BookmarkActivity mBookmarkActivity;
    private final LayoutInflater mInflater;

    public BookmarkListAdapter(BookmarkActivity bookmarkActivity, List<BookmarkListData> objects) {
        super(bookmarkActivity, 0, objects);
        mBookmarkActivity = bookmarkActivity;
        mInflater = (LayoutInflater) bookmarkActivity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        final View row;
        TextView label = null;
        TextView bookmarkPage = null;
        Button editButton = null;
        BookmarkListData listData = getItem(position);

        if (convertView == null) {
            // 1行のレイアウト生成
            row = mInflater.inflate(R.layout.cho_gazo_viewer_bookmark_row, null);

            // 効率化(再利用)のためにビューを保存
            label = (TextView) row.findViewById(R.id.bookmark_row_label);
            bookmarkPage = (TextView) row.findViewById(R.id.bookmark_row_page);
            editButton = (Button) row.findViewById(R.id.bookmark_row_edit_button);
            row.setTag(new BookmarkListViewHolder(bookmarkPage, label, editButton));
        } else {
            row = convertView;
            BookmarkListViewHolder holder = (BookmarkListViewHolder) row.getTag();
            label = holder.getLabel();
            bookmarkPage = holder.getBookmarkPage();
            editButton = holder.getEditButton();
        }
        // 毎回セットし直さないとポジションおかしくなるので気をつけること
        label.setText(listData.getLabel());
        bookmarkPage.setText(String.valueOf(listData.getBookmarkPage() + 1) + "ページ");
        editButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mBookmarkActivity.onListButtonClick(row, position);
            }
        });

        return row;
    }
}
