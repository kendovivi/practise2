
package jp.bpsinc.android.trainingbookshelf.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import jp.bpsinc.android.chogazo.viewer.content.Book;
import jp.bpsinc.android.chogazo.viewer.content.PageAccess;
import jp.bpsinc.android.chogazo.viewer.content.PageInfo;
import jp.bpsinc.android.chogazo.viewer.content.epub.EpubReader;
import jp.bpsinc.android.chogazo.viewer.exception.ContentsOtherException;
import jp.bpsinc.android.chogazo.viewer.exception.ContentsParseException;
import jp.bpsinc.android.chogazo.viewer.exception.ContentsUnzipException;
import jp.bpsinc.android.chogazo.viewer.exception.DrmException;
import jp.bpsinc.android.chogazo.viewer.exception.UnexpectedException;
import jp.bpsinc.android.trainingbookshelf.content.BookshelfEpubFile;

public class BitMapTools {

    public BitMapTools() {
    }

    /**
     * EPUBファイルカバーstreamをdecodeする
     * 
     * @param epubPath　EPUBファイルパス
     * @param reqWidth　指定幅
     * @param reqHeight　指定高さ
     * @return
     */
    public static Bitmap decodeBitmap(String epubPath, int reqWidth, int reqHeight) {
        Bitmap bitmap = null;
        EpubReader reader = new EpubReader();
        BookshelfEpubFile epubFile = new BookshelfEpubFile();
        InputStream inputStream = null;
        InputStream copyInputStream = null;

        try {
            reader.open(null, epubPath, null);
            epubFile.parse(reader);
            PageAccess pageAccess = new PageAccess(reader, (Book) epubFile) {
                @Override
                public ArrayList<PageInfo> getSpreadPageInfoList() {
                    return null;
                }

                @Override
                public ArrayList<PageInfo> getSinglePageInfoList() {
                    return null;
                }
            };
            inputStream = pageAccess.getPageStream(epubFile.getCoverItem());
            copyInputStream = pageAccess.getPageStream(epubFile.getCoverItem());
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            bitmap = BitmapFactory.decodeStream(inputStream, null, opts);
            opts.inSampleSize = calculateInSampleSize(opts, reqWidth, reqHeight);
            opts.inJustDecodeBounds = false;
            // inputstreamは一回しか使えない
            bitmap = BitmapFactory.decodeStream(copyInputStream, null, opts);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (DrmException e) {
            e.printStackTrace();
        } catch (ContentsUnzipException e) {
            e.printStackTrace();
        } catch (ContentsOtherException e) {
            e.printStackTrace();
        } catch (ContentsParseException e) {
            e.printStackTrace();
        } catch (UnexpectedException e) {
            e.printStackTrace();
        } finally {
            try {
                reader.close();
                if (inputStream != null) {
                    inputStream.close();
                }
                if (copyInputStream != null) {
                    copyInputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return bitmap;
    }

    /**
     * 元bitmap幅、高さと指定された幅と高さを比較する結果により、圧縮比率を決める
     * 
     * @param opts
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    public static int calculateInSampleSize(BitmapFactory.Options opts,
            int reqWidth, int reqHeight) {
        int imageHeight = opts.outHeight;
        int imageWidth = opts.outWidth;
        // 压缩比例
        int inSampleSize = 1;
        if (imageHeight > reqHeight || imageWidth > reqWidth) {
            final int heightRatio = Math.round((float) imageHeight
                    / (float) reqHeight);
            final int widthRatio = Math.round((float) imageWidth
                    / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return inSampleSize;
    }

}
