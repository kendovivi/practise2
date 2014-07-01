
package jp.bpsinc.android.chogazo.viewer.content.epub;

import android.content.Context;

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
import jp.bpsinc.android.chogazo.viewer.util.StringUtil;
import jp.bpsinc.android.util.zip.ZipEntry;
import jp.bpsinc.android.util.zip.ZipFile;

public class EpubReader implements ContentsReader {
    /** コンテツのパス */
    protected String mPath;
    /** ZIPファイル(RandomAccessFile対応版)*/
    protected ZipFile mZipFile;
    /** キー：エントリ名、値：エントリ名に対応するZipEntry */
    protected HashMap<String, ZipEntry> mZipEntries = new HashMap<String, ZipEntry>();

    @Override
    public void open(Context context, String path, Serializable option) throws
            FileNotFoundException, DrmException, ContentsUnzipException,
            ContentsOtherException, UnexpectedException {
        File file = new File(path);
        if (!file.canRead()) {
            throw new FileNotFoundException(file.getPath() + " is not found");
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
    public byte[] getFileContents(String entryName) throws ContentsOtherException {
        LogUtil.v("entryName = %s", entryName);
        if (isClosed()) {
            throw new ContentsOtherException("ZipFile is closed");
        }
        entryName = StringUtil.trimHeadSlash(entryName);

        BufferedInputStream is = null;
        try {
            if (mZipEntries.containsKey(entryName)) {
                ZipEntry entry = mZipEntries.get(entryName);
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
    public long getFileSize(String entryName) {
        LogUtil.v("entryName = %s", entryName);
        entryName = StringUtil.trimHeadSlash(entryName);

        if (mZipEntries.containsKey(entryName)) {
            return mZipEntries.get(entryName).getSize();
        }
        return -1l;
    }

    @Override
    public InputStream getInputStream(String entryName) throws ContentsOtherException {
        LogUtil.v("entryName = %s", entryName);
        if (mZipFile == null) {
            throw new ContentsOtherException("ZipFile is closed");
        }
        entryName = StringUtil.trimHeadSlash(entryName);

        try {
            if (mZipEntries.containsKey(entryName)) {
                return mZipFile.getInputStream(mZipEntries.get(entryName));
            }
        } catch (IOException e) {
            LogUtil.e(e);
        }
        return null;
    }

    @Override
    public boolean hasFile(String entryName) {
        LogUtil.v("entryName = %s", entryName);
        return mZipEntries.containsKey(StringUtil.trimHeadSlash(entryName));
    }

    protected void setZipEntries() {
        ZipEntry zipEntry = null;
        Enumeration<? extends ZipEntry> entries = mZipFile.entries();
        while (entries.hasMoreElements()) {
            zipEntry = entries.nextElement();

            // ディレクトリは無視
            if (zipEntry.isDirectory() == false) {
                mZipEntries.put(zipEntry.getName(), zipEntry);
            }
        }
    }
}
