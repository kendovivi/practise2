
package jp.bpsinc.android.trainingbookshelf.async;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import jp.bpsinc.android.chogazo.viewer.content.Book;
import jp.bpsinc.android.chogazo.viewer.content.ContentsReader;
import jp.bpsinc.android.chogazo.viewer.content.epub.EpubFile;
import jp.bpsinc.android.chogazo.viewer.content.epub.EpubReader;
import jp.bpsinc.android.chogazo.viewer.content.zip.ZipBookFile;
import jp.bpsinc.android.chogazo.viewer.content.zip.ZipReader;
import jp.bpsinc.android.chogazo.viewer.exception.ContentsOtherException;
import jp.bpsinc.android.chogazo.viewer.exception.ContentsParseException;
import jp.bpsinc.android.chogazo.viewer.exception.ContentsUnzipException;
import jp.bpsinc.android.chogazo.viewer.exception.DrmException;
import jp.bpsinc.android.chogazo.viewer.exception.UnexpectedException;
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

        String[] fileNames = null;
        // 各対応するコンテンツタイプのファイル数
        int fileCount = 0;
        // 読み込んだファイル数
        int subTotal = 0;
        // 対応するコンテンツタイプのファイル総数
        int total = ContentManager.getTotalFileCount(dir);
        ContentsReader reader = null;
        Book bookFile = null;
        //各コンテンツタイプにより、ファイル名リストを取得し、readerやbookFileクラスを指定する
        for (int type : ContentInfo.CONTENT_TYPES) {
            switch (type) {
                case ContentInfo.CONTENT_TYPE_EPUB:
                    // EPUB
                    fileNames = ContentManager
                            .getFileName(dir, ContentManager.EXTENSION_EPUB);
                    reader = new EpubReader();
                    bookFile = new EpubFile();
                    break;
                case ContentInfo.CONTENT_TYPE_ZIP:
                    // ZIP
                    fileNames = ContentManager
                            .getFileName(dir, ContentManager.EXTENSION_ZIP);
                    reader = new ZipReader();
                    bookFile = new ZipBookFile();
                    break;
            }
            // this.mContentLoaderTaskListener.onCheckExistence(fileCount);
            fileCount = fileNames.length;
            for (int n_i = 0 + subTotal; n_i < fileCount; n_i++) {
                File path = new File(
                        dir,
                        fileNames[n_i]
                        );

                try {
                    reader.open(null, path.getAbsolutePath(), null);
                    bookFile.parse(reader);
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
                        bookFile.getTitle(),
                        bookFile.getAuthor(),
                        path.getAbsolutePath(),
                        type
                        );
                mContentList.add(content);
                publishProgress((int) (((float) n_i / total) * 100));
            }
            subTotal += fileCount;
        }
        return 0;
    };

    @Override
    protected void onPostExecute(Integer result) {
        super.onPostExecute(result);
        this.mContentLoaderTaskListener.onTaskFinish(mContentList);
    }

}
