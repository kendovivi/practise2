
package jp.bpsinc.android.chogazo.viewer.content.zip;

import java.util.Enumeration;

import jp.bpsinc.android.chogazo.viewer.content.AbstractZipReader;
import jp.bpsinc.android.util.zip.ZipEntry;

public class ZipReader extends AbstractZipReader {

    @Override
    protected void setZipEntries() {
        ZipEntry zipEntry = null;
        Enumeration<? extends ZipEntry> entries = mZipFile.entries();
        int key = 0;
        while (entries.hasMoreElements()) {
            zipEntry = entries.nextElement();
            mZipEntries.put(String.valueOf(key), zipEntry);
            key++;
        }
    }
}
