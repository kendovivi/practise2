
package jp.bpsinc.android.chogazo.viewer.content.zip;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.zip.ZipException;

import jp.bpsinc.android.chogazo.viewer.content.ContentsReader;
import jp.bpsinc.android.chogazo.viewer.exception.ContentsOtherException;
import jp.bpsinc.android.chogazo.viewer.exception.ContentsUnzipException;
import jp.bpsinc.android.chogazo.viewer.exception.DrmException;
import jp.bpsinc.android.chogazo.viewer.exception.UnexpectedException;
import jp.bpsinc.android.chogazo.viewer.util.LogUtil;
import jp.bpsinc.android.util.zip.ZipEntry;
import jp.bpsinc.android.util.zip.ZipFile;
import android.content.Context;

public class ZipReader implements ContentsReader {

    /** コンテンツパス */
    protected String mPath;
    protected ZipFile mZipFile;
    protected HashMap<String, ZipEntry> mZipEntries = new HashMap<String, ZipEntry>();

    @Override
    public void open(Context context, String path, Serializable option)
            throws FileNotFoundException, DrmException, ContentsUnzipException,
            ContentsOtherException, UnexpectedException {
        File file = new File(path);
        if (!file.canRead()) {
            throw new FileNotFoundException(file.getPath() + "is not found");
        }
        mPath = path;
        try {
            mZipFile = new ZipFile(path);
        } catch (ZipException e) {
            throw new ContentsUnzipException("unzip error", e);
        } catch (IOException e) {
            throw new ContentsOtherException("zip file i/o error", e);
        }
        setZipEntries();
    }

    @Override
    public void close() {
        if (mZipFile != null) {
            try {
                mZipFile.close();
            } catch (IOException e) {
                LogUtil.e(e);
            }
        }
        mPath = null;
        mZipFile = null;
        mZipEntries.clear();
    }

    @Override
    public boolean isClosed() {
        return mZipFile == null;
    }

    @Override
    public String getPath() {
        return mPath;
    }

    @Override
    public byte[] getFileContents(String key) throws ContentsOtherException {
        if (isClosed()) {
            throw new ContentsOtherException("ZipFile is closed");
        }

        BufferedInputStream is = null;
        try {
            if (mZipEntries.containsKey(key)) {
                ZipEntry entry = mZipEntries.get(key);
                is = new BufferedInputStream(mZipFile.getInputStream(entry));

                int entrySize = (int) entry.getSize();
                byte[] buf = new byte[entrySize];

                int c;
                int total = 0;
                while ((c = is.read(buf, total, entrySize - total)) != -1) {
                    total += c;
                    if (total >= entrySize) {
                        break;
                    }
                }
                return buf;
            }
        } catch (IOException e) {
            LogUtil.e(e);
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (Exception e) {
                LogUtil.e(e);
            }
        }
        return null;
    }

    @Override
    public long getFileSize(String key) {
        if (mZipEntries.containsKey(key)) {
            return mZipEntries.get(key).getSize();
        }
        return -1l;
    }

    @Override
    public InputStream getInputStream(String key) throws ContentsOtherException {
        try {
            if (mZipEntries.containsKey(key)) {

                return mZipFile.getInputStream(mZipEntries.get(key));
            }
        } catch (IOException e) {
            LogUtil.e(e);
        }
        return null;
    }

    @Override
    public boolean hasFile(String key) {
        return mZipEntries.containsKey(key);
    }
    
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
