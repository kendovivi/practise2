
package jp.bpsinc.android.chogazo.viewer.content;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import jp.bpsinc.android.chogazo.viewer.content.Page.LoadQuality;
import jp.bpsinc.android.chogazo.viewer.content.PageLoadThread.PageLoadCompleteListener;
import jp.bpsinc.android.chogazo.viewer.util.LogUtil;
import android.support.v4.app.FragmentActivity;

public class PageLoadManager {
    private FragmentActivity mActivity;

    private PageLoadCompleteListener mCompleteListener;

    private Map<Page, PageLoadThread> mPageLoadThreadMap;

    private ThreadPoolExecutor mExecutor;

    public PageLoadManager(FragmentActivity activity, PageLoadCompleteListener listener) {
        mActivity = activity;
        mCompleteListener = listener;
        mPageLoadThreadMap = new HashMap<Page, PageLoadThread>();
        mExecutor = newThreadPoolExecutor();
    }

    private ThreadPoolExecutor newThreadPoolExecutor() {
        return new ThreadPoolExecutor(1, 1, 3000L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>());
    }

    public void addPageLoadThread(Page page, LoadQuality loadQuality) {
        LogUtil.v();
        stopPageLoadThread(page);
        PageLoadThread thread = new PageLoadThread(mActivity, page, mCompleteListener, loadQuality);
        mExecutor.execute(thread);
        mPageLoadThreadMap.put(page, thread);
    }

    public void stopPageLoadThread(Page page) {
        LogUtil.v();
        PageLoadThread pageLoadThread = mPageLoadThreadMap.get(page);
        if (pageLoadThread != null) {
            mExecutor.remove(pageLoadThread);
            pageLoadThread.halt();
            mPageLoadThreadMap.remove(page);
        }
    }

    public void stopAllTasks() {
        LogUtil.v();
        try {
            for (PageLoadThread th : mPageLoadThreadMap.values()) {
                th.halt();
            }
            mExecutor.shutdown();
            if (mExecutor.awaitTermination(10, TimeUnit.SECONDS) == false) {
                mExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            LogUtil.w("PageImageLoadManager shutdownNow!!", e);
            mExecutor.shutdownNow();
        }
        mExecutor = newThreadPoolExecutor();
        mPageLoadThreadMap.clear();
    }
}
