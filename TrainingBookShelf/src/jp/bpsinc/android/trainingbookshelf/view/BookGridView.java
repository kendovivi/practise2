
package jp.bpsinc.android.trainingbookshelf.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.GridView;
import com.bps.trainingbookshelf.R;
import jp.bpsinc.android.trainingbookshelf.util.DisplayUtil;

public class BookGridView extends GridView {
    /** サムネイルの幅　dp */
    public static final int THUMBNAIL_WIDTH_IN_DPI = 85;
    /** サムネイルの高さ dp */
    public static final int THUMBNAIL_HEIGHT_IN_DPI = 105;
    /** 背景高さがコンテンツ高さ(マージン含む)の倍数 */
    private static float SHELF_HEIGHT_EXTEND_RATE_OVER_CONTENT = 1.5f;

    /** サムネイル表示用、コンテンツサムネイルの高さによりextendedされた行背景 */
    private Bitmap mExtendedThumbnailBackground;

    public BookGridView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // サムネイル表示用、行背景
        Bitmap thumbnailBackground = BitmapFactory.decodeResource(getResources(),
                R.drawable.thumbnail_type_background);
        // コンテンツの高さにより、修正した背景
        mExtendedThumbnailBackground = extendBackgroundBitmap(thumbnailBackground, context);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        int top = 0;
        if (getChildAt(0) != null) {
            top = getChildAt(0).getTop();
        }
        int backgroundImgHeight = mExtendedThumbnailBackground.getHeight();
        int backgroundImgWidth = mExtendedThumbnailBackground.getWidth();
        int mScreenHeight = getHeight();
        int mScreenWidth = getWidth();

        for (int y = top; y < mScreenHeight; y += backgroundImgHeight) {
            for (int x = 0; x < mScreenWidth; x += backgroundImgWidth) {
                canvas.drawBitmap(mExtendedThumbnailBackground, x, y, null);
            }
        }
        super.dispatchDraw(canvas);
    }

    /**
     * コンテンツの幅と高さにより、適切な本棚背景を作る
     * 
     * @param bitmap
     * @param context
     * @return
     */
    private Bitmap extendBackgroundBitmap(Bitmap bitmap, Context context) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int reqHeight = (int) (THUMBNAIL_HEIGHT_IN_DPI * DisplayUtil.getScreenDensity(context) * SHELF_HEIGHT_EXTEND_RATE_OVER_CONTENT);
        int reqWidth = (reqHeight / height) * width;
        return Bitmap.createScaledBitmap(bitmap, reqWidth, reqHeight, true);
    }

}
