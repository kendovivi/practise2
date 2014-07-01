
package jp.bpsinc.android.chogazo.viewer.menu;

import java.io.Serializable;

public class ViewerMenuResources implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 画面の回転 */
    private boolean mOrientationEnable;
    private int mOrientationTitleResId;
    private int mOrientationSensorTitleResId;
    private int mOrientationSensorSummaryResId;
    private int mOrientationPortraitTitleResId;
    private int mOrientationPortraitSummaryResId;
    private int mOrientationLandscapeTitleResId;
    private int mOrientationLandscapeSummaryResId;
    /** 横画面の見開き表示 */
    private boolean mSpreadEnable;
    private int mSpreadTitleResId;
    private int mSpreadOnSummaryResId;
    private int mSpreadOffSummaryResId;
    /** 左右エリアのタップ */
    private boolean mTapActionEnable;
    private int mTapActionTitleResId;
    private int mTapActionNormalTitleResId;
    private int mTapActionNormalSummaryResId;
    private int mTapActionNextTitleResId;
    private int mTapActionNextSummaryResId;
    private int mTapActionNoneTitleResId;
    private int mTapActionNoneSummaryResId;
    /** ページめくりアニメーション */
    private boolean mAnimationEnable;
    private int mAnimationSlideTitleResId;
    private int mAnimationSlideSummaryResId;
    private int mAnimationNoneTitleResId;
    private int mAnimationNoneSummaryResId;
    /** 自動再生 */
    private boolean mAutoPlayEnableResId;
    private int mAutoPlayCategoryTitleResId;
    /** 自動再生 - 間隔 */
    private int mAutoPlayIntervalTitleResId;
    /** 自動再生 - ループ */
    private int mLoopPlayTitleResId;
    private int mLoopPlayOnSummaryResId;
    private int mLoopPlayOffSummaryResId;
    /** 自動再生 - 方向 */
    private int mAutoPlayDirectionTitleResId;
    private int mAutoPlayDirectionForwardTitleResId;
    private int mAutoPlayDirectionForwardSummaryResId;
    private int mAutoPlayDirectionReverseTitleResId;
    private int mAutoPlayDirectionReverseSummaryResId;
    private int mAutoPlayDirectionRandomTitleResId;
    private int mAutoPlayDirectionRandomSummaryResId;
    /** ファイル情報 */
    private int mFileInfoCategoryTitleResId;
    private int mFileInfoTitleTitleResId;
    private int mFileInfoTitleKanaTitleResId;
    private int mFileInfoAuthorTitleResId;
    private int mFileInfoAuthorKanaTitleResId;
    private int mFileInfoFileNameTitleResId;

    public boolean isOrientationEnable() {
        return mOrientationEnable;
    }

    public void setOrientationEnable(boolean orientationEnable) {
        this.mOrientationEnable = orientationEnable;
    }

    public int getOrientationTitleResId() {
        return mOrientationTitleResId;
    }

    public void setOrientationTitleResId(int orientationTitleResId) {
        this.mOrientationTitleResId = orientationTitleResId;
    }

    public int getOrientationSensorTitleResId() {
        return mOrientationSensorTitleResId;
    }

    public void setOrientationSensorTitleResId(int orientationSensorTitleResId) {
        this.mOrientationSensorTitleResId = orientationSensorTitleResId;
    }

    public int getOrientationSensorSummaryResId() {
        return mOrientationSensorSummaryResId;
    }

    public void setOrientationSensorSummaryResId(int orientationSensorSummaryResId) {
        this.mOrientationSensorSummaryResId = orientationSensorSummaryResId;
    }

    public int getOrientationPortraitTitleResId() {
        return mOrientationPortraitTitleResId;
    }

    public void setOrientationPortraitTitleResId(int orientationPortraitTitleResId) {
        this.mOrientationPortraitTitleResId = orientationPortraitTitleResId;
    }

    public int getOrientationPortraitSummaryResId() {
        return mOrientationPortraitSummaryResId;
    }

    public void setOrientationPortraitSummaryResId(int orientationPortraitSummaryResId) {
        this.mOrientationPortraitSummaryResId = orientationPortraitSummaryResId;
    }

    public int getOrientationLandscapeTitleResId() {
        return mOrientationLandscapeTitleResId;
    }

    public void setOrientationLandscapeTitleResId(int orientationLandscapeTitleResId) {
        this.mOrientationLandscapeTitleResId = orientationLandscapeTitleResId;
    }

    public int getOrientationLandscapeSummaryResId() {
        return mOrientationLandscapeSummaryResId;
    }

    public void setOrientationLandscapeSummaryResId(int orientationLandscapeSummaryResId) {
        this.mOrientationLandscapeSummaryResId = orientationLandscapeSummaryResId;
    }

    public boolean ismSpreadEnable() {
        return mSpreadEnable;
    }

    public void setSpreadEnable(boolean spreadEnable) {
        this.mSpreadEnable = spreadEnable;
    }

    public int getSpreadTitleResId() {
        return mSpreadTitleResId;
    }

    public void setSpreadTitleResId(int spreadTitleResId) {
        this.mSpreadTitleResId = spreadTitleResId;
    }

    public int getSpreadOnSummaryResId() {
        return mSpreadOnSummaryResId;
    }

    public void setSpreadOnSummaryResId(int spreadOnSummaryResId) {
        this.mSpreadOnSummaryResId = spreadOnSummaryResId;
    }

    public int getSpreadOffSummaryResId() {
        return mSpreadOffSummaryResId;
    }

    public void setSpreadOffSummaryResId(int spreadOffSummaryResId) {
        this.mSpreadOffSummaryResId = spreadOffSummaryResId;
    }

    public boolean ismTapActionEnable() {
        return mTapActionEnable;
    }

    public void setTapActionEnable(boolean tapActionEnable) {
        this.mTapActionEnable = tapActionEnable;
    }

    public int getTapActionTitleResId() {
        return mTapActionTitleResId;
    }

    public void setTapActionTitleResId(int tapActionTitleResId) {
        this.mTapActionTitleResId = tapActionTitleResId;
    }

    public int getTapActionNormalTitleResId() {
        return mTapActionNormalTitleResId;
    }

    public void setTapActionNormalTitleResId(int tapActionNormalTitleResId) {
        this.mTapActionNormalTitleResId = tapActionNormalTitleResId;
    }

    public int getTapActionNormalSummaryResId() {
        return mTapActionNormalSummaryResId;
    }

    public void setTapActionNormalSummaryResId(int tapActionNormalSummaryResId) {
        this.mTapActionNormalSummaryResId = tapActionNormalSummaryResId;
    }

    public int getTapActionNextTitleResId() {
        return mTapActionNextTitleResId;
    }

    public void setTapActionNextTitleResId(int tapActionNextTitleResId) {
        this.mTapActionNextTitleResId = tapActionNextTitleResId;
    }

    public int getTapActionNextSummaryResId() {
        return mTapActionNextSummaryResId;
    }

    public void setTapActionNextSummaryResId(int tapActionNextSummaryResId) {
        this.mTapActionNextSummaryResId = tapActionNextSummaryResId;
    }

    public int getTapActionNoneTitleResId() {
        return mTapActionNoneTitleResId;
    }

    public void setTapActionNoneTitleResId(int tapActionNoneTitleResId) {
        this.mTapActionNoneTitleResId = tapActionNoneTitleResId;
    }

    public int getTapActionNoneSummaryResId() {
        return mTapActionNoneSummaryResId;
    }

    public void setTapActionNoneSummaryResId(int tapActionNoneSummaryResId) {
        this.mTapActionNoneSummaryResId = tapActionNoneSummaryResId;
    }

    public boolean ismAnimationEnable() {
        return mAnimationEnable;
    }

    public void setAnimationEnable(boolean animationEnable) {
        this.mAnimationEnable = animationEnable;
    }

    public int getAnimationSlideTitleResId() {
        return mAnimationSlideTitleResId;
    }

    public void setAnimationSlideTitleResId(int animationSlideTitleResId) {
        this.mAnimationSlideTitleResId = animationSlideTitleResId;
    }

    public int getAnimationSlideSummaryResId() {
        return mAnimationSlideSummaryResId;
    }

    public void setAnimationSlideSummaryResId(int animationSlideSummaryResId) {
        this.mAnimationSlideSummaryResId = animationSlideSummaryResId;
    }

    public int getAnimationNoneTitleResId() {
        return mAnimationNoneTitleResId;
    }

    public void setAnimationNoneTitleResId(int animationNoneTitleResId) {
        this.mAnimationNoneTitleResId = animationNoneTitleResId;
    }

    public int getAnimationNoneSummaryResId() {
        return mAnimationNoneSummaryResId;
    }

    public void setAnimationNoneSummaryResId(int animationNoneSummaryResId) {
        this.mAnimationNoneSummaryResId = animationNoneSummaryResId;
    }

    public boolean ismAutoPlayEnableResId() {
        return mAutoPlayEnableResId;
    }

    public void setAutoPlayEnableResId(boolean mAutoPlayEnableResId) {
        this.mAutoPlayEnableResId = mAutoPlayEnableResId;
    }

    public int getAutoPlayCategoryTitleResId() {
        return mAutoPlayCategoryTitleResId;
    }

    public void setAutoPlayCategoryTitleResId(int autoPlayCategoryTitleResId) {
        this.mAutoPlayCategoryTitleResId = autoPlayCategoryTitleResId;
    }

    public int getAutoPlayIntervalTitleResId() {
        return mAutoPlayIntervalTitleResId;
    }

    public void setAutoPlayIntervalTitleResId(int mAutoPlayIntervalTitleResId) {
        this.mAutoPlayIntervalTitleResId = mAutoPlayIntervalTitleResId;
    }

    public int getLoopPlayTitleResId() {
        return mLoopPlayTitleResId;
    }

    public void setLoopPlayTitleResId(int mLoopPlayTitleResId) {
        this.mLoopPlayTitleResId = mLoopPlayTitleResId;
    }

    public int getLoopPlayOnSummaryResId() {
        return mLoopPlayOnSummaryResId;
    }

    public void setLoopPlayOnSummaryResId(int mLoopPlayOnSummaryResId) {
        this.mLoopPlayOnSummaryResId = mLoopPlayOnSummaryResId;
    }

    public int getLoopPlayOffSummaryResId() {
        return mLoopPlayOffSummaryResId;
    }

    public void setLoopPlayOffSummaryResId(int mLoopPlayOffSummaryResId) {
        this.mLoopPlayOffSummaryResId = mLoopPlayOffSummaryResId;
    }

    public int getAutoPlayDirectionTitleResId() {
        return mAutoPlayDirectionTitleResId;
    }

    public void setAutoPlayDirectionTitleResId(int mAutoPlayDirectionTitleResId) {
        this.mAutoPlayDirectionTitleResId = mAutoPlayDirectionTitleResId;
    }

    public int getAutoPlayDirectionForwardTitleResId() {
        return mAutoPlayDirectionForwardTitleResId;
    }

    public void setAutoPlayDirectionForwardTitleResId(int mAutoPlayDirectionForwardTitleResId) {
        this.mAutoPlayDirectionForwardTitleResId = mAutoPlayDirectionForwardTitleResId;
    }

    public int getAutoPlayDirectionForwardSummaryResId() {
        return mAutoPlayDirectionForwardSummaryResId;
    }

    public void setAutoPlayDirectionForwardSummaryResId(int mAutoPlayDirectionForwardSummaryResId) {
        this.mAutoPlayDirectionForwardSummaryResId = mAutoPlayDirectionForwardSummaryResId;
    }

    public int getAutoPlayDirectionReverseTitleResId() {
        return mAutoPlayDirectionReverseTitleResId;
    }

    public void setAutoPlayDirectionReverseTitleResId(int mAutoPlayDirectionReverseTitleResId) {
        this.mAutoPlayDirectionReverseTitleResId = mAutoPlayDirectionReverseTitleResId;
    }

    public int getAutoPlayDirectionReverseSummaryResId() {
        return mAutoPlayDirectionReverseSummaryResId;
    }

    public void setAutoPlayDirectionReverseSummaryResId(int mAutoPlayDirectionReverseSummaryResId) {
        this.mAutoPlayDirectionReverseSummaryResId = mAutoPlayDirectionReverseSummaryResId;
    }

    public int getAutoPlayDirectionRandomTitleResId() {
        return mAutoPlayDirectionRandomTitleResId;
    }

    public void setAutoPlayDirectionRandomTitleResId(int mAutoPlayDirectionRandomTitleResId) {
        this.mAutoPlayDirectionRandomTitleResId = mAutoPlayDirectionRandomTitleResId;
    }

    public int getAutoPlayDirectionRandomSummaryResId() {
        return mAutoPlayDirectionRandomSummaryResId;
    }

    public void setAutoPlayDirectionRandomSummaryResId(int mAutoPlayDirectionRandomSummaryResId) {
        this.mAutoPlayDirectionRandomSummaryResId = mAutoPlayDirectionRandomSummaryResId;
    }

    public int getFileInfoCategoryTitleResId() {
        return mFileInfoCategoryTitleResId;
    }

    public void setFileInfoCategoryTitleResId(int fileInfoCategoryTitleResId) {
        this.mFileInfoCategoryTitleResId = fileInfoCategoryTitleResId;
    }

    public int getFileInfoTitleTitleResId() {
        return mFileInfoTitleTitleResId;
    }

    public void setFileInfoTitleTitleResId(int fileInfoTitleTitleResId) {
        this.mFileInfoTitleTitleResId = fileInfoTitleTitleResId;
    }

    public int getFileInfoTitleKanaTitleResId() {
        return mFileInfoTitleKanaTitleResId;
    }

    public void setFileInfoTitleKanaTitleResId(int fileInfoTitleKanaTitleResId) {
        this.mFileInfoTitleKanaTitleResId = fileInfoTitleKanaTitleResId;
    }

    public int getFileInfoAuthorTitleResId() {
        return mFileInfoAuthorTitleResId;
    }

    public void setFileInfoAuthorTitleResId(int fileInfoAuthorTitleResId) {
        this.mFileInfoAuthorTitleResId = fileInfoAuthorTitleResId;
    }

    public int getFileInfoAuthorKanaTitleResId() {
        return mFileInfoAuthorKanaTitleResId;
    }

    public void setFileInfoAuthorKanaTitleResId(int fileInfoAuthorKanaTitleResId) {
        this.mFileInfoAuthorKanaTitleResId = fileInfoAuthorKanaTitleResId;
    }

    public int getFileInfoFileNameTitleResId() {
        return mFileInfoFileNameTitleResId;
    }

    public void setFileInfoFileNameTitleResId(int fileInfoFileNameTitleResId) {
        this.mFileInfoFileNameTitleResId = fileInfoFileNameTitleResId;
    }
}
