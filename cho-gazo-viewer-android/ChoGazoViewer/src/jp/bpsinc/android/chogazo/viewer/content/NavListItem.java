
package jp.bpsinc.android.chogazo.viewer.content;

/**
 * 目次の1行分の情報を取得するためのインタフェース
 */
public interface NavListItem {
    /**
     * 目次の各行タイトルを取得する
     * 
     * @return 目次の各行タイトル
     */
    public String getTitle();

    /**
     * 目次の各行に対応する単ページ表示時のページ数を取得する
     * 
     * @return 目次の各行に対応する単ページ表示時のページ数(0始まり)
     */
    public int getSinglePageIndex();
}
