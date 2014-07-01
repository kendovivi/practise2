
package jp.bpsinc.android.chogazo.viewer.content;

public class PageInfo {
    /** 単ページで使うページ情報 */
    private PageItem mCenterPageItem;

    /** 見開き左ページで使うページ情報 */
    private PageItem mLeftPageItem;

    /** 見開き右ページで使うページ情報 */
    private PageItem mRightPageItem;

    /** ページ数(本全体の何ページ目か) */
    private int mThisPage;

    /**
     * @param centerPageItem 単ページ(見開きの設定の場合画面中央に表示される)情報
     * @param thisPage 本全体に対するこのページのページ数(0始まり)
     */
    public PageInfo(PageItem centerPageItem, int thisPage) {
        if (centerPageItem == null) {
            throw new IllegalArgumentException("Single page item must not be null");
        }
        mCenterPageItem = centerPageItem;
        mThisPage = thisPage;
    }

    /**
     * @param leftPageItem 見開きの左ページ情報
     * @param rightPageItem 見開きの右ページ情報
     * @param thisPage 本全体に対するこのページのページ数(0始まり)、2ページともある場合は手前のページ数
     */
    public PageInfo(PageItem leftPageItem, PageItem rightPageItem, int thisPage) {
        if (leftPageItem == null && rightPageItem == null) {
            throw new IllegalArgumentException("Both left and right is null");
        }
        mLeftPageItem = leftPageItem;
        mRightPageItem = rightPageItem;
        mThisPage = thisPage;
    }

    public PageItem getCenterPageItem() {
        return mCenterPageItem;
    }

    public PageItem getLeftPageItem() {
        return mLeftPageItem;
    }

    public PageItem getRightPageItem() {
        return mRightPageItem;
    }

    /**
     * 本全体に対するこのページのページ数を取得する<br>
     * 全100ページの場合、オブジェクトごとに0～99までの値を保持する<br>
     * 見開きページの場合は手前のページ数を取得する(3～4ページを保持している場合は2(3ページ目)を取得する)
     * 
     * @return ページ数(本全体の何ページ目か)
     */
    public int getThisPage() {
        return mThisPage;
    }
}
