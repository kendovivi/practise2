
package jp.bpsinc.android.trainingbookshelf.async;

import android.os.AsyncTask;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import jp.bpsinc.android.chogazo.viewer.content.epub.EpubReader;
import jp.bpsinc.android.chogazo.viewer.exception.ContentsOtherException;
import jp.bpsinc.android.chogazo.viewer.exception.ContentsParseException;
import jp.bpsinc.android.chogazo.viewer.exception.ContentsUnzipException;
import jp.bpsinc.android.chogazo.viewer.exception.DrmException;
import jp.bpsinc.android.chogazo.viewer.exception.UnexpectedException;
import jp.bpsinc.android.trainingbookshelf.content.BookshelfEpubFile;
import jp.bpsinc.android.trainingbookshelf.content.ContentInfo;
import jp.bpsinc.android.trainingbookshelf.content.ContentManager;

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
        String[] epubFileNames = ContentManager
                .getEpubFileName(ContentManager.EXTERNAL_STORAGE_DIRECTORY);
        // 指定パスに、EPUBファイルの数
        int epubFileCount = ContentManager
                .getEpubFileCount(ContentManager.EXTERNAL_STORAGE_DIRECTORY);
        // 指定パスから、ファイルの末が.epubのファイル有無チェック
        this.mContentLoaderTaskListener.onCheckExistence(epubFileCount);

        EpubReader reader = new EpubReader();
        BookshelfEpubFile epubFile = new BookshelfEpubFile();

        for (int n_i = 0; n_i < epubFileCount; n_i++) {
            File epubPath = new File(
                    ContentManager.EXTERNAL_STORAGE_DIRECTORY,
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

            ContentInfo content = new ContentInfo(String.valueOf(n_i), epubFile.getTitle(),
                    epubFile.getAuthor(), epubPath.getAbsolutePath());
            mContentList.add(content);
            publishProgress((int) (((float) n_i / epubFileCount) * 100));
        }
        return 0;
    };

    @Override
    protected void onPostExecute(Integer result) {
        super.onPostExecute(result);
        this.mContentLoaderTaskListener.onTaskFinish(mContentList);
    }

}
