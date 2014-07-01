
package jp.bpsinc.android.chogazo.viewer.util;

import java.io.File;
import java.util.Calendar;
import java.util.Locale;

public class StringUtil {
    /**
     * 引数の文字列が空か調べる 引数のStringが null、または、0文字のいずれかの場合は空とみなす。
     * 
     * @param value 対象文字列
     * @return 空の場合true、そうでなければfalse
     */
    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    /**
     * Stringクラスのtrim機能に全角スペース除去追加
     * 
     * @param str 文字列
     * @return 文字列の前後にある空白類を削除した文字列を返す、一切削除しなかった場合は引数と同じオブジェクトを返す
     */
    public static String trim(String str) {
        int len = str.length();
        int st = 0;

        while ((st < len) && ((str.charAt(st) <= ' ') || (str.charAt(st) == '　'))) {
            st++;
        }
        while ((st < len) && ((str.charAt(len - 1) <= ' ') || (str.charAt(len - 1) == '　'))) {
            len--;
        }
        return ((st > 0) || (len < str.length())) ? str.substring(st, len) : str;
    }

    /**
     * ファイルパスの先頭のスラッシュを取り除く
     * 
     * @param filePath ファイルパス
     * @return 
     *         ファイルパスの先頭にあるスラッシュを全て削除した文字列、ファイルパスの先頭にスラッシュが存在しない場合は引数と同じオブジェクトを返す
     */
    public static String trimHeadSlash(String filePath) {
        while (filePath.startsWith("/")) {
            filePath = filePath.substring(1);
        }
        return filePath;
    }

    /**
     * BOMを取り除く
     * 
     * @param str 文字列
     * @return BOMを削除した文字列、BOMが存在しない場合は引数と同じオブジェクトを返す
     */
    public static String trimBOM(String str) {
        if (str != null && str.length() > 0 && str.charAt(0) == 0xFEFF) {
            str = str.substring(1);
        }
        return str;
    }

    /**
     * 文字列から?を探して?以降を削除し、続けて#を探して#以降を削除し、<br>
     * 先頭にスラッシュががない場合スラッシュを付けた文字列を返す
     * 
     * @param uri
     * @return ?以降を削除して#以降を削除して先頭にスラッシュが無かったらスラッシュを付けた文字列、引数がnullの場合は空文字を返す
     */
    public static String trimUri(String uri) {
        if (uri == null) {
            uri = "";
        }
        if (uri.contains("?")) {
            uri = uri.substring(0, uri.indexOf("?"));
        }
        if (uri.contains("#")) {
            uri = uri.substring(0, uri.indexOf("#"));
        }
        if (!uri.startsWith("/")) {
            uri = "/" + uri;
        }
        return uri;
    }

    /**
     * ファイルパス文字列からディレクトリ部分(文字列の最後の'/'以前)を削除する
     * 
     * @param filePath ファイルパス
     * @return ファイルパスからディレクトリ部分を削除した文字列、ディレクトリ部分が存在しない場合は引数と同じオブジェクトを返す
     */
    public static String getFileName(String filePath) {
        int index = filePath.lastIndexOf('/');
        if (index == -1) {
            return filePath;
        }
        return filePath.substring(index + 1);
    }

    /**
     * ファイル名文字列から拡張子(文字列の最後の'.'以降)を削除する
     * 
     * @param fileName ファイル名
     * @return ファイル名から拡張子を削除した文字列、拡張子が存在しない場合は引数と同じオブジェクトを返す
     */
    public static String getFileNameWithoutExtension(String fileName) {
        int index = fileName.lastIndexOf('.');
        if (index == -1) {
            return fileName;
        }
        return fileName.substring(0, index);
    }

    /**
     * 文字列が、指定された接尾辞で終るかどうかを判定する(大文字小文字を区別しない)
     * 
     * @param str 文字列
     * @param suffix 接尾辞
     * @return
     */
    public static boolean endsWithIgnoreCase(String str, String suffix) {
        if (str == null && suffix == null) {
            return true;
        }
        if (str == null || suffix == null) {
            return false;
        }
        if (str.length() < suffix.length()) {
            return false;
        }
        String sub = str.substring(str.length() - suffix.length());
        return sub.equalsIgnoreCase(suffix);
    }

    /**
     * ファイルパス文字列から親ディレクトリパス文字列を取得する
     * 
     * @param filePath ファイルパス
     * @return
     */
    public static String getParentPath(String filePath) {
        int index = filePath.lastIndexOf(File.separator);
        if (index == -1) {
            return "";
        }

        return filePath.substring(0, index);
    }

    /**
     * 現在時刻をyyyy/mm/dd hh:mi:ssの形式で文字列で取得する
     * 
     * @return 現在時刻の文字列
     */
    public static String getCurrentTime() {
        Calendar calendar = Calendar.getInstance();
        return String.format(Locale.getDefault(),
                "%4d/%02d/%02d %02d:%02d:%02d",
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DATE),
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                calendar.get(Calendar.SECOND));
    }
}
