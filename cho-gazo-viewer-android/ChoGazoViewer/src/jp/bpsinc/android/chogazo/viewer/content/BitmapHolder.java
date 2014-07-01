
package jp.bpsinc.android.chogazo.viewer.content;

import java.io.InputStream;

import jp.bpsinc.android.chogazo.viewer.content.Page.Size;
import jp.bpsinc.android.chogazo.viewer.exception.LoadImageException;
import jp.bpsinc.android.chogazo.viewer.util.LogUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;

public class BitmapHolder {
    public static final float HALF = 0.5f;

    /** Bitmap */
    private Bitmap mBitmap;
    /** 
     * 半分のサイズのBitmap、通常サイズのBitmapが描画時のサイズの2倍以上の場合にモアレやシャギを低減させるために使用する
     * 描画時にリサイズ＆recycleすると、HoneycombやICSにて謎の画像が混ざる現象が発生するため常に保持しておく
     */
    private Bitmap mHalfBitmap;
    /**
     * 読み込んだBitmapのディスプレイフィットスケール、縦フィット、横フィット、見開きの設定などに応じて値は変化、
     * 見開きの場合、左右で画像サイズが違う場合は大きい方の画像のディスプレイフィットスケールを格納する、
     * この場合小さい方の画像は縦横どちらもディスプレイにフィットしない
     */
    private float mFitScale;
    /** ビットマップを原寸デコードした場合のサイズ */
    private Size mOriginalSize;

    /** BITMAP拡大率の初期値 */
    public static final float DISPLAY_FIT_DEFAULT_SCALE = 1.0f;

    public BitmapHolder() {
        LogUtil.v();
        mBitmap = null;
        mHalfBitmap = null;
        mFitScale = DISPLAY_FIT_DEFAULT_SCALE;
        mOriginalSize = null;
    }

    public synchronized void cleanupBitmap() {
        LogUtil.v();
        if (mBitmap != null && mBitmap.isRecycled() == false) {
            mBitmap.recycle();
        }
        if (mHalfBitmap != null && mHalfBitmap.isRecycled() == false) {
            mHalfBitmap.recycle();
        }
        mBitmap = null;
        mHalfBitmap = null;
        mFitScale = DISPLAY_FIT_DEFAULT_SCALE;
    }

    public synchronized void setBitmap(Bitmap bitmap) {
        mBitmap = bitmap;
    }

    public synchronized Bitmap getBitmap() {
        return mBitmap;
    }

    /**
     * 通常のBitmapが1/2以下のサイズで描画される可能性がある場合、半分のサイズにリサイズして別領域に保持する
     */
    public synchronized void setHalfBitmap() {
        if (hasBitmap() && getOriginalHeight() * HALF > getFitScaleHeight()
                && getOriginalWidth() * HALF > getFitScaleWidth()) {
            mHalfBitmap = Bitmap.createScaledBitmap(mBitmap, (int) (mBitmap.getWidth() * HALF),
                    (int) (mBitmap.getHeight() * HALF), true);
        }
    }

    public synchronized Bitmap getHalfBitmap() {
        return mHalfBitmap;
    }

    public synchronized void setFitScale(float fitScale) {
        mFitScale = fitScale;
    }

    public synchronized float getFitScale() {
        return mFitScale;
    }

    public synchronized boolean hasBitmap() {
        return mBitmap != null && mBitmap.isRecycled() == false;
    }

    /**
     * ストリームから画像のステータス情報のみ取得し、取得したサイズを設定する
     * 
     * @param is 画像ストリーム
     * @throws LoadImageException デコード失敗
     */
    public void setOriginalSize(InputStream is) throws LoadImageException {
        // オプションでステータス情報のみ取得に設定
        Options options = new Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(is, null, options);

        // ファイルのデコードに失敗した(対応しないMIMEタイプだった)ら例外をthrow
        if (options.outMimeType == null) {
            throw new LoadImageException("failed to decode bitmap.");
        }
        mOriginalSize = new Size(options.outWidth, options.outHeight);
    }

    public Size getOriginalSize() {
        return mOriginalSize;
    }

    public int getOriginalWidth() {
        int width = 0;
        if (mOriginalSize != null) {
            width = mOriginalSize.width;
        }
        return width;
    }

    public int getOriginalHeight() {
        int height = 0;
        if (mOriginalSize != null) {
            height = mOriginalSize.height;
        }
        return height;
    }

    public boolean isOriginalSize() {
        return mBitmap.getWidth() == mOriginalSize.width
                && mBitmap.getHeight() == mOriginalSize.height;
    }

    public float getFitScaleWidth() {
        return mBitmap.getWidth() * mFitScale;
    }

    public float getFitScaleHeight() {
        return mBitmap.getHeight() * mFitScale;
    }
}
