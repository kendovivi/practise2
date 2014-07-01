
package jp.bpsinc.android.trainingbookshelf.async;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.widget.ImageView;
import java.lang.ref.WeakReference;
import jp.bpsinc.android.trainingbookshelf.adapter.ThumbTypeRowAdapter;
import jp.bpsinc.android.trainingbookshelf.util.ThumbnailLruCacheTools;

public class ImageLoader extends AsyncTask<String, Integer, Bitmap> {

    private Context mContext;
    /** コンテンツid */
    private String mUid;
    /** コンテンツthumb nail読み込み中の画像 */
    private Bitmap mDefaultCover;
    /** コンテンツサムネイルのimageView参照 */
    private final WeakReference<ImageView> imageViewReference;

    public ImageLoader(ImageView imageView, Context context, Bitmap defaultCover) {
        mContext = context;
        imageViewReference = new WeakReference<ImageView>(imageView);
        mDefaultCover = defaultCover;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Bitmap doInBackground(String... params) {
        mUid = params[0];
        String epubPath = params[1];
        // EPUBカバー読み込み、メモリにあるなら、直接にそのまま使う。なければ、EPUBカバーstreamを解析する、解析した画像をメモリに追加する
        Bitmap bitmap = ThumbnailLruCacheTools.getInstance().decodeAndSaveBitmap(epubPath,
                mUid);
        return bitmap;
    };

    protected void onPostExecute(Bitmap bitmap) {
        if (isCancelled()) {
            bitmap = null;
        }
        if (imageViewReference != null && bitmap != null) {
            final ImageView imageView = (ImageView) imageViewReference.get();
            final ImageLoader task = getImageLoaderTask(imageView);
            if (this == task && imageView != null) {
                ThumbTypeRowAdapter.changeThumbToReqSize(imageView, bitmap, mContext);
                imageView.setImageBitmap(bitmap);
            }
        }
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
    }

    static class AsyncDrawalbe extends BitmapDrawable {
        private final WeakReference<ImageLoader> imageLoaderTaskReference;

        public AsyncDrawalbe(Resources res, Bitmap bitmap, ImageLoader imageLoaderTask) {
            super(res, bitmap);
            imageLoaderTaskReference = new WeakReference<ImageLoader>(imageLoaderTask);
        }

        public ImageLoader getImageLoaderTask() {
            return imageLoaderTaskReference.get();
        }
    }

    public static boolean cancelCurrentTask(String uid, ImageView imageView) {
        final ImageLoader imageLoaderTask = getImageLoaderTask(imageView);

        if (imageLoaderTask != null) {
            // TODO: 名称
            String bitmapData = imageLoaderTask.mUid;
            if (bitmapData != null && !bitmapData.equals(uid)) {
                imageLoaderTask.cancel(true);
            } else {
                return false;
            }
        }
        return true;
    }

    public static ImageLoader getImageLoaderTask(ImageView imageView) {
        if (imageView != null) {
            final Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawalbe) {
                final AsyncDrawalbe asyncDrawable = (AsyncDrawalbe) drawable;
                return asyncDrawable.getImageLoaderTask();
            }
        }
        return null;
    }

    @SuppressLint("NewApi")
    public void loadBitmap(String uid, String epubPath, ImageView imageView) {
        if (cancelCurrentTask(uid, imageView)) {
            final ImageLoader imageLoaderTask = new ImageLoader(imageView, mContext, mDefaultCover);
            final AsyncDrawalbe asyncDrawable = new AsyncDrawalbe(mContext.getResources(),
                    mDefaultCover, imageLoaderTask);
            imageView.setImageDrawable(asyncDrawable);
            // バックグランドにサムネイルを読み込む処理を複数スレッドで同時に走らせたいが、executeは端末ごとに動作が違うため、ここで端末バージョンを分けて処理する。
            // 端末バージョン3.0以降は executeOnExecutorメソッドを呼ぶ
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                imageLoaderTask.executeOnExecutor(THREAD_POOL_EXECUTOR, uid, epubPath);
            } else {
                // executeメソッドを呼ぶ, バージョン1.6以下はシングルスレッド、2.3から3.0まで(3.0含まない)複数スレッドをバックグランドで起動する
                imageLoaderTask.execute(uid, epubPath);
            }
        }
    }

}
