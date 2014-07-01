package jp.bpsinc.android.chogazo.viewer.adapter;

public class NavInfo {
    private String mTitle;
    private int mSinglePageIndex;

    public NavInfo(String title, int singlePageIndex) {
        mTitle = title;
        mSinglePageIndex = singlePageIndex;
    }

    public String getTitle() {
        return mTitle;
    }

    public int getSinglePageIndex() {
        return mSinglePageIndex;
    }
}
