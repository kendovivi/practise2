
package jp.bpsinc.android.trainingbookshelf.content;

import android.app.Activity;
import android.os.Environment;
import android.support.v4.app.Fragment;
import java.io.File;
import java.io.FilenameFilter;

public class ContentManager {

    /** 外部ストレージパス */
    public static final File EXTERNAL_STORAGE_DIRECTORY = Environment.getExternalStorageDirectory();

    /** 外部ストレージにあるEPUBファイル名リスト */
    private String[] mEpubFileNameList;

    /**
     * バックグランドEPUB読み込みスレッドを起動する
     */
    public static void loadEpubFile(Fragment fragment, int shelfType, Activity activity) {
        new CallEpubLoad(fragment, shelfType, activity);
    }

    /**
     * 指定パスから、EPUBファイルを探す
     * 
     * @param path 探すパス
     * @return　EPUBファイル名リスト
     */
    public static String[] getEpubFileName(File fileDir) {
        String[] epubFileNameList = fileDir.list(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String filename) {
                return (filename != null && filename.endsWith(".epub"));
            }
        });
        return epubFileNameList;
    }

    /**
     * 指定パスにEPUBファイルの個数
     * 
     * @return
     */
    public static int getEpubFileCount(File fileDir) {
        String[] epubFileNameList = getEpubFileName(fileDir);
        if (epubFileNameList == null) {
            return 0;
        } else {
            return epubFileNameList.length;
        }
    }

    /**
     * 外部ストレージ内のEPUBファイルの数が変わっているかをチェック TODO: ファイルの数ではなく、中身もチェックする必要、ファイルパスとか
     * 
     * @return
     */
    private boolean checkIsEpubFilesChanged() {
        if (mEpubFileNameList == null) {
            return false;
        }
        int epubFileCount = mEpubFileNameList.length;
        int currentEpubFileCount = getEpubFileName(EXTERNAL_STORAGE_DIRECTORY).length;
        return epubFileCount != currentEpubFileCount;
    }

}