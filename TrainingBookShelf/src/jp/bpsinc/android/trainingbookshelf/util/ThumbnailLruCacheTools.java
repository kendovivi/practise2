
package jp.bpsinc.android.trainingbookshelf.util;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

public class ThumbnailLruCacheTools {

    private static final int PERCENT_OF_MEMORY_TO_USE_FOR_CACHE = 25;

    private static final int REQ_WIDTH_FOR_THUMBNAIL = 150;

    private static final int REQ_HEIGHT_FOR_THUMBNAIL = 200;

    private static LruCache<String, Bitmap> sMemoryCache;

    // getInstance()でしかオブジェクトを生成することができない
    private ThumbnailLruCacheTools() {
    }

    // getInstance()にしか呼ばれない
    private static class ThumbnailLruCacheHolder {
        // LruCacheToolsクラスのインスタンスが１つしかないことを保証する
        private static final ThumbnailLruCacheTools INSTANCE = new ThumbnailLruCacheTools();
    }

    /**
     * @return
     */
    public static ThumbnailLruCacheTools getInstance() {

        if (sMemoryCache == null) {
            sMemoryCache = new LruCache<String, Bitmap>(getCacheSize()) {
                @Override
                protected int sizeOf(String key, Bitmap bitmap) {
                    return bitmap.getRowBytes() * bitmap.getHeight();
                }
            };
        }
        return ThumbnailLruCacheHolder.INSTANCE;
    }

    /**
     * 全メモリの一部をキャッシュとして使う
     * 
     * @return キャッシュのサイズ 単位： bytes
     */
    private static int getCacheSize() {
        int MaxMemory = (int) Runtime.getRuntime().maxMemory();
        int cacheSize = (int) (MaxMemory * (PERCENT_OF_MEMORY_TO_USE_FOR_CACHE / 100.0));
        return cacheSize;
    }

    /**
     * キャッシュに画像を保存する
     * 
     * @param key
     * @param bitmap
     */
    private void addBitmap(String key, Bitmap bitmap) {
        if (getBitmap(key) == null && bitmap != null) {
            sMemoryCache.put(key, bitmap);
        }
    }

    /**
     * キャッシュに保存されている画像返す
     * 
     * @param key
     * @return
     */
    public Bitmap getBitmap(String key) {
        Bitmap bitmap = sMemoryCache.get(key);
        return bitmap;
    }

    /**
     * 指定しているサムネイルの幅と高さにより、Bitmap画像をdecodeし、メモリに追加する
     * 
     * @param Epubファイルパス文字列、保存キー文字列
     * @return サムネイル用Bitmap
     */
    public Bitmap decodeAndSaveBitmap(String epubPath, String key) {
        // EPUBファイルカバーを解析する
        Bitmap bitmap = BitMapTools.decodeBitmap(
                epubPath,
                REQ_WIDTH_FOR_THUMBNAIL,
                REQ_HEIGHT_FOR_THUMBNAIL
                );
        addBitmap(key, bitmap);

        return bitmap;
    }

    public void clear() {
        if (sMemoryCache != null) {
            sMemoryCache.evictAll();
        }
    }
}
