
package jp.bpsinc.android.chogazo.viewer.content.zip;

import jp.bpsinc.android.chogazo.viewer.content.PageItem;
import jp.bpsinc.android.chogazo.viewer.exception.ContentsOtherException;
import jp.bpsinc.android.chogazo.viewer.exception.ContentsParseException;

public class ZipItem implements PageItem {

    private String mKey;

    public ZipItem(String key) {
        if (key != null) {
            mKey = key;
        }
    }

    @Override
    public String getKey() throws ContentsParseException, ContentsOtherException {
        return mKey;
    }

}
