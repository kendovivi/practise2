package jp.bpsinc.android.trainingbookshelf.async;

import java.util.ArrayList;
import jp.bpsinc.android.trainingbookshelf.content.ContentInfo;

public interface ContentLoaderTaskListener {
    void onTaskStart();
    void onCheckExistence(int count);
    void onLoading(int Progress);
    void onTaskFinish(ArrayList<ContentInfo> result);
}
