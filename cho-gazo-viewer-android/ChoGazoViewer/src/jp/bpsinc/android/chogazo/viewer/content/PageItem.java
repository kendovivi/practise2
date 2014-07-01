
package jp.bpsinc.android.chogazo.viewer.content;

import jp.bpsinc.android.chogazo.viewer.exception.ContentsOtherException;
import jp.bpsinc.android.chogazo.viewer.exception.ContentsParseException;

public interface PageItem {
    /**
     * ページを参照するためのキーを取得する
     * 
     * @return ページを参照するためのキー
     * @throws ContentsParseException 解析に失敗
     * @throws ContentsOtherException その他のエラー
     */
    public String getKey() throws ContentsParseException, ContentsOtherException;
}
