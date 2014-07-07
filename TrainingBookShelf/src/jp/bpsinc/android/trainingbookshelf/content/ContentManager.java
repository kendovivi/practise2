
package jp.bpsinc.android.trainingbookshelf.content;

import java.io.File;
import java.io.FilenameFilter;

import android.app.Activity;
import android.os.Environment;
import android.support.v4.app.Fragment;

public class ContentManager {
    /** 外部ストレージにあるEPUBファイル名リスト */
    private String[] mEpubFileNameList;

    /** 外部ストレージパス */
    public static final File EXTERNAL_STORAGE_DIRECTORY = Environment.getExternalStorageDirectory();
    /** EPUB拡張子 */
    public static String EXTENSION_EPUB = ".epub";
    /** ZIP拡張子 */
    public static String EXTENSION_ZIP = ".zip";
    /** 対応するコンテンツの拡張子配列 */
    public static String[] EXTENSION_ACCEPTABLE = {
            EXTENSION_EPUB, EXTENSION_ZIP
    };

    /**
     * バックグランドコンテンツ読み込みスレッドを起動する
     */
    public static void loadEpubFile(Fragment fragment, int shelfType, Activity activity) {
        new CallEpubLoad(fragment, shelfType, activity);
    }

    /**
     * 指定パスから、ファイルを探す
     * 
     * @param fileDir 探すパス
     * @param ext 対応するコンテンツの拡張子
     * @return　ファイル名リスト
     */
    public static String[] getFileName(File fileDir, final String ext) {
        String[] fileNameList = fileDir.list(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String filename) {
                return (filename != null && filename.endsWith(ext));
            }
        });
        return fileNameList;
    }

    /**
     * 指定パスから、ファイル総数を取得
     * 
     * @param fileDir 探すパス
     * @return　ファイル総数
     */
    public static int getTotalFileCount(File fileDir) {
        String[] fileNameList = fileDir.list(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String filename) {
                return (filename != null && isExtAcceptable(filename, EXTENSION_ACCEPTABLE));
            }
        });
        return fileNameList.length;
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
        int currentEpubFileCount = getFileName(EXTERNAL_STORAGE_DIRECTORY, this.EXTENSION_EPUB).length;
        return epubFileCount != currentEpubFileCount;
    }

    private static boolean isExtAcceptable(String str, String[] exts) {
        for (String ext : exts) {
            if (str != null && str.endsWith(ext)) {
                return true;
            }
        }
        return false;
    }

}
