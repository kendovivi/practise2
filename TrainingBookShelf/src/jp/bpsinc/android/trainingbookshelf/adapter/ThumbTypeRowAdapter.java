
package jp.bpsinc.android.trainingbookshelf.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.bps.trainingbookshelf.R;
import java.util.ArrayList;
import jp.bpsinc.android.trainingbookshelf.async.ImageLoader;
import jp.bpsinc.android.trainingbookshelf.content.ContentInfo;
import jp.bpsinc.android.trainingbookshelf.fragment.ThumbTypeFragment;
import jp.bpsinc.android.trainingbookshelf.util.DisplayUtil;
import jp.bpsinc.android.trainingbookshelf.util.ThumbnailLruCacheTools;
import jp.bpsinc.android.trainingbookshelf.view.BookGridView;

public class ThumbTypeRowAdapter extends ArrayAdapter<ContentInfo> {

    private Activity mActivity;
    private ThumbTypeFragment mBookShelfFragment;
    private LayoutInflater mInflater;

    private ContentInfo mCurrentContent;
    /** getViewを行っている行のview */
    private RelativeLayout mView;
    private Bitmap mDefaultCover;

    public ThumbTypeRowAdapter(Context context, int resource, ArrayList<ContentInfo> contentList,
            ThumbTypeFragment fragment) {
        super(context, resource, contentList);
        mActivity = (Activity) context;
        mBookShelfFragment = fragment;
        mInflater = mActivity.getLayoutInflater();
        mDefaultCover = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.default_book_cover);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        mView = (RelativeLayout) convertView;
        mCurrentContent = (ContentInfo) getItem(position);

        // 初めて入る時
        if (convertView == null) {
            viewHolder = new ViewHolder();
            setNewView(viewHolder);
        } else {
            viewHolder = (ViewHolder) mView.getTag();
        }

        // 行にある各コンテンツの配置
        Bitmap coverBitmap = ThumbnailLruCacheTools.getInstance().getBitmap(
                mCurrentContent.getUid());
        // メモリキャッシュにカバー画像の有無をチェック、メモリキャッシュに存在すればそれを使う、なければ画像読み込みスレッドを走らせる
        if (coverBitmap != null) {
            changeThumbToReqSize(viewHolder.contentImageView, coverBitmap, mActivity);
            viewHolder.contentImageView.setImageBitmap(coverBitmap);

        } else {
            changeThumbToReqSize(viewHolder.contentImageView, mDefaultCover, mActivity);
            viewHolder.contentImageView.setImageBitmap(mDefaultCover);
            startLoadingImage(viewHolder.contentImageView, mCurrentContent);
        }

        // サムネイルの代わりに、そのレイアウトをlistener設定する
        viewHolder.clickLayout.setOnClickListener(mBookShelfFragment
                .createThumbContentClickListener(position));

        // XMLでclickable=falseは無効でした。　2系の端末が行クリック効果をなしにするため、ここに設定している。　4系の場合、既にlistSelectorはtransparentで、行のクリック効果をなしにしている。
        mView.setOnClickListener(null);
        return mView;
    }

    static class ViewHolder {
        ImageView contentImageView;
        RelativeLayout clickLayout;
    }

    private void setNewView(ViewHolder viewHolder) {
        mView = (RelativeLayout) mInflater.inflate(
                R.layout.content_list_item_thumbnail_type, null);
        viewHolder.clickLayout = (RelativeLayout) mView.findViewById(R.id.tp_background);
        viewHolder.contentImageView = (ImageView)
                mView.findViewById(R.id.content_image);
        mView.setTag(viewHolder);
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

    /**
     * サムネイルImageViewを指定サイズに変更する
     * 
     * @param thumbnailView サムネイルImageView
     * @param epubCoverBitmap 元EPUBファイルカバー
     */
    public static void changeThumbToReqSize(ImageView thumbnailView, Bitmap epubCoverBitmap, Context context) {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) thumbnailView
                .getLayoutParams();
        int sizeArray[] = calcReqSizeForThumb(
                epubCoverBitmap,
                (int) (BookGridView.THUMBNAIL_WIDTH_IN_DPI * DisplayUtil.getScreenDensity(context)),
                (int) (BookGridView.THUMBNAIL_HEIGHT_IN_DPI * DisplayUtil.getScreenDensity(context)));
        params.width = sizeArray[0];
        params.height = sizeArray[1];
    }

    /**
     * 画像ファイルの縦横比率をkeepするため、ImageViewデフォルトの幅と高さにより、新しいImageViewの幅と高さを再計算する。
     * 現在、計算は3つの場合で分けられている: 1. 画像サイズがデフォルトImageViewのサイズより小さい 2. 縦フィット 3.　横フィット
     * 
     * @param defWidth
     * @param defHeight
     * @param epubCoverBitmap
     * @return　画像サイズArray, {幅、高さ}
     */
    public static int[] calcReqSizeForThumb(Bitmap epubCoverBitmap, int defWidth, int defHeight) {
        int rtnArray[] = {0, 0};
        int width = epubCoverBitmap.getWidth();
        int height = epubCoverBitmap.getHeight();

        // 画像サイズの幅と高さ両方がデフォルトより小さいである場合、そのまま使う
        if (width <= defWidth && height <= defHeight) {
            rtnArray[0] = width;
            rtnArray[1] = height;
        } else {
            // サムネイル縦フィット
            if ((float) defHeight / height < (float) defWidth / width) {
                float rate = (float) defHeight / height;
                rtnArray[0] = (int) (width * rate);
                rtnArray[1] = defHeight;
            // サムネイル横フィット
            } else {
                float rate = (float) defWidth / width;
                rtnArray[0] = defWidth;
                rtnArray[1] = (int) (height * rate);
            }
        }
        return rtnArray;
    }
}
