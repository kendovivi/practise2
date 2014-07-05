
package jp.bpsinc.android.trainingbookshelf.async;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import jp.bpsinc.android.chogazo.viewer.content.epub.EpubReader;
import jp.bpsinc.android.chogazo.viewer.content.zip.ZipBookFile;
import jp.bpsinc.android.chogazo.viewer.content.zip.ZipReader;
import jp.bpsinc.android.chogazo.viewer.exception.ContentsOtherException;
import jp.bpsinc.android.chogazo.viewer.exception.ContentsParseException;
import jp.bpsinc.android.chogazo.viewer.exception.ContentsUnzipException;
import jp.bpsinc.android.chogazo.viewer.exception.DrmException;
import jp.bpsinc.android.chogazo.viewer.exception.UnexpectedException;
import jp.bpsinc.android.trainingbookshelf.content.BookshelfEpubFile;
import jp.bpsinc.android.trainingbookshelf.content.ContentInfo;
import jp.bpsinc.android.trainingbookshelf.content.ContentManager;
import android.os.AsyncTask;

public class ContentLoaderTask extends AsyncTask<Integer, Integer, Integer> {

    /** 外部ストレージから読み込んだEPUBコンテンツリスト */
    private ArrayList<ContentInfo> mContentList;
    /** タスクListenerインタフェイス */
    private ContentLoaderTaskListener mContentLoaderTaskListener;

    public ContentLoaderTask(ContentLoaderTaskListener contentLoaderTaskListener) {
        this.mContentLoaderTaskListener = contentLoaderTaskListener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mContentList = new ArrayList<ContentInfo>();
        this.mContentLoaderTaskListener.onTaskStart();
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        this.mContentLoaderTaskListener.onLoading(values[0]);
    }

    @Override
    protected Integer doInBackground(Integer... params) {
        File dir = ContentManager.EXTERNAL_STORAGE_DIRECTORY;

        // EPUB
        String[] epubFileNames = ContentManager
                .getEpubFileName(dir);
        // 指定パスに、EPUBファイルの数
        int epubFileCount = ContentManager
                .getEpubFileCount(dir);
        // 指定パスから、ファイルの末が.epubのファイル有無チェック
        this.mContentLoaderTaskListener.onCheckExistence(epubFileCount);

        EpubReader reader = new EpubReader();
        BookshelfEpubFile epubFile = new BookshelfEpubFile();

        for (int n_i = 0; n_i < epubFileCount; n_i++) {
            File epubPath = new File(
                    dir,
                    epubFileNames[n_i]
                    );

            try {
                reader.open(null, epubPath.getAbsolutePath(), null);
                epubFile.parse(reader);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (DrmException e) {
                e.printStackTrace();
            } catch (ContentsUnzipException e) {
                e.printStackTrace();
            } catch (ContentsOtherException e) {
                e.printStackTrace();
            } catch (UnexpectedException e) {
                e.printStackTrace();
            } catch (ContentsParseException e) {
                e.printStackTrace();
            } finally {
                reader.close();
            }

            ContentInfo content = new ContentInfo(
                    String.valueOf(n_i),
                    epubFile.getTitle(),
                    epubFile.getAuthor(),
                    epubPath.getAbsolutePath(),
                    ContentInfo.CONTENT_TYPE_EPUB
                    );
            mContentList.add(content);
            publishProgress((int) (((float) n_i / epubFileCount) * 100));
        }

        // ZIP
        String[] zipFileNames = ContentManager.getZipFileName(dir);
        int zipFileCount = ContentManager.getZipFileCount(dir);
        ZipReader zipReader = new ZipReader();
        ZipBookFile zipFile = new ZipBookFile();

        for (int n_i = 0; n_i < zipFileCount; n_i++) {
            File zipPath = new File(
                    dir,
                    zipFileNames[n_i]
                    );
            try {
                zipReader.open(null, zipPath.getAbsolutePath(), null);
                zipFile.parse(zipReader);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (DrmException e) {
                e.printStackTrace();
            } catch (ContentsUnzipException e) {
                e.printStackTrace();
            } catch (ContentsOtherException e) {
                e.printStackTrace();
            } catch (UnexpectedException e) {
                e.printStackTrace();
            } catch (ContentsParseException e) {
                e.printStackTrace();
            } finally {
                reader.close();
            }
            ContentInfo content = new ContentInfo(String.valueOf(n_i),
                    zipFile.getTitle(),
                    zipFile.getAuthor(),
                    zipPath.getAbsolutePath(),
                    ContentInfo.CONTENT_TYPE_ZIP
                    );
            mContentList.add(content);
        }
        return 0;
    };

    @Override
    protected void onPostExecute(Integer result) {
        super.onPostExecute(result);
        this.mContentLoaderTaskListener.onTaskFinish(mContentList);
    }

}
