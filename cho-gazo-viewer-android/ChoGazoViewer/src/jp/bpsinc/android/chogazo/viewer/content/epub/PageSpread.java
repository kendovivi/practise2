
package jp.bpsinc.android.chogazo.viewer.content.epub;

/**
 * opfファイルSpine itemrefプロパティ
 */
public enum PageSpread {
    /** 見開きで左 */
    LEFT("page-spread-left"),
    /** 見開きで右 */
    RIGHT("page-spread-right"),
    /** 見開きで真ん中 */
    CENTER("rendition:page-spread-center"),
    /** 指定なし、不明 */
    NONE(null);

    private String mValue;

    private PageSpread(String value) {
        mValue = value;
    }

    public String getValue() {
        return mValue;
    }
}
