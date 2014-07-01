
package jp.bpsinc.android.trainingbookshelf.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.bps.trainingbookshelf.R;
import java.util.List;
import jp.bpsinc.android.trainingbookshelf.async.ImageLoader;
import jp.bpsinc.android.trainingbookshelf.content.ContentInfo;
import jp.bpsinc.android.trainingbookshelf.fragment.ListTypeFragment;
import jp.bpsinc.android.trainingbookshelf.util.ThumbnailLruCacheTools;

public class ListTypeRowAdapter extends ArrayAdapter<ContentInfo> {

    private Activity mActivity;
    private ListTypeFragment mListFragment;
    private LayoutInflater mInflater;

    /** getViewを行っている行 */
    private ContentInfo mContent;
    /** getViewを行っている行のview */
    private LinearLayout mRowView;
    /** getViewを行っている行にあるる各コンテンツview */
    private RelativeLayout mItemView;
    private Bitmap mDefaultCover;

    public ListTypeRowAdapter(Context context, int resource, List<ContentInfo> contentList,
            ListTypeFragment fragment) {
        super(context, resource, contentList);
        mActivity = (Activity) context;
        mListFragment = fragment;
        mInflater = mActivity.getLayoutInflater();
        mDefaultCover = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.default_book_cover);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        mRowView = (LinearLayout) convertView;
        mContent = (ContentInfo) getItem(position);

        // 初めて入る時
        if (convertView == null) {
            viewHolder = new ViewHolder();
            setNewView(viewHolder);
        } else {
            viewHolder = (ViewHolder) mRowView.getTag();
        }

        // 行にある各コンテンツの配置
        Bitmap coverBitmap = ThumbnailLruCacheTools.getInstance().getBitmap(mContent.getUid());
        // メモリキャッシュにカバー画像の有無をチェック、メモリキャッシュに存在すればそれを使う、なければ画像読み込みスレッドを走らせる
        if (coverBitmap != null) {
            ThumbTypeRowAdapter.changeThumbToReqSize(viewHolder.contentImageView, coverBitmap, mActivity);
            viewHolder.contentImageView.setImageBitmap(coverBitmap);
        } else {
            ThumbTypeRowAdapter.changeThumbToReqSize(viewHolder.contentImageView, mDefaultCover, mActivity);
            viewHolder.contentImageView.setImageBitmap(mDefaultCover);
            startLoadingImage(viewHolder.contentImageView, mContent);
        }

        viewHolder.contentTitle.setText(mContent.getContentTitle());
        viewHolder.contentAuthor.setText(mContent.getContentAuthor());
        // サムネイルの代わりに、そのレイアウトをlistener設定する
        viewHolder.clickLayout.setOnClickListener(mListFragment
                .createListContentClickListener(position));

        // XMLでclickable=falseは無効でした。　2系の端末が行クリック効果をなしにするため、ここに設定している。　4系の場合、既にlistSelectorはtransparentで、行のクリック効果をなしにしている。
        mRowView.setOnClickListener(null);
        return mRowView;
    }

    static class ViewHolder {
        ImageView contentImageView;
        RelativeLayout clickLayout;
        TextView contentTitle;
        TextView contentAuthor;
        CheckBox bookCheckBox;
    }

    private void setNewView(ViewHolder viewHolder) {
        mRowView = (LinearLayout) mInflater.inflate(R.layout.bookshelf_row,
                null);
        mItemView = (RelativeLayout) mInflater.inflate(R.layout.content_list_item_list_type,
                mRowView, false);
        viewHolder.clickLayout = (RelativeLayout) mItemView.findViewById(R.id.list_item_click_layout);
        viewHolder.contentTitle = (TextView)
                mItemView.findViewById(R.id.content_title);
        viewHolder.contentAuthor = (TextView)
                mItemView.findViewById(R.id.content_author);
        viewHolder.contentImageView = (ImageView)
                mItemView.findViewById(R.id.content_image);
        mRowView.addView(mItemView);
        mRowView.setTag(viewHolder);
    }

    /**
     * サムネイル画像を読み込むと設定
     * 
     * @param imageView　
     * @param book　コンテンツ
     */
    private void startLoadingImage(ImageView imageView, ContentInfo content) {
        ImageLoader loader = new ImageLoader(imageView, mActivity, mDefaultCover);
        loader.loadBitmap(content.getUid(), content.getEpubPath(), imageView);
    }

}
