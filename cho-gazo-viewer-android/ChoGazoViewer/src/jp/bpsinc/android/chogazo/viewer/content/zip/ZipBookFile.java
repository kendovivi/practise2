
package jp.bpsinc.android.chogazo.viewer.content.zip;

import jp.bpsinc.android.chogazo.viewer.content.Book;
import jp.bpsinc.android.chogazo.viewer.content.ContentsReader;
import jp.bpsinc.android.chogazo.viewer.exception.ContentsOtherException;
import jp.bpsinc.android.chogazo.viewer.exception.ContentsParseException;
import jp.bpsinc.android.chogazo.viewer.exception.UnexpectedException;

public class ZipBookFile extends Book {

    protected ContentsReader mReader;
    
    @Override
    public void parse(ContentsReader reader) throws ContentsParseException, ContentsOtherException,
            UnexpectedException {
        mReader = reader;
        setSinglePageInfo();
        setSpreadPageInfo();
    }

    @Override
    protected void bibliographicInit() {
        // 何もしない
    }

    private void setSinglePageInfo() {
        int currentPage = 0;
        while (mReader.hasFile(String.valueOf(currentPage))) {
            ZipItem zipItem = new ZipItem(String.valueOf(currentPage));
            currentPage = addSinglePageInfo(zipItem, currentPage);
        }
    }

    private void setSpreadPageInfo() {
        ZipItem tmpLeft = null;
        ZipItem tmpRight = null;
        int currentPage = 0;
        
        //setSinglePageInfo()执行后可用，注意调用顺序
        int size = getPageCount();

        while (currentPage < size) {
            // TODO while内不加判断，减少浪费
            if (isRtl()) {
                tmpRight = new ZipItem(String.valueOf(currentPage));
                if (currentPage + 1 < size) {
                    tmpLeft = new ZipItem(String.valueOf(currentPage));
                }
                currentPage = addSpreadPageInfo(tmpLeft, tmpRight, currentPage);
            }
        }
    }
}
